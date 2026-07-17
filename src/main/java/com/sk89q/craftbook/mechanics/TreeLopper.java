package com.sk89q.craftbook.mechanics;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TreeLopper extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {

        if(event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if(!Blocks.containsFuzzy(enabledBlocks, BukkitAdapter.adapt(event.getBlock().getBlockData()))) return;
        if(!enabledItems.contains(player.getItemInHand(HandSide.MAIN_HAND).getType())) return;
        if(!player.hasPermission("craftbook.mech.treelopper.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }

        if(!EventUtil.passesFilter(event))
            return;

        Set<Location> visitedLocations = new HashSet<>();
        visitedLocations.add(event.getBlock().getLocation());
        int broken = 1;

        final Block usedBlock = event.getBlock();

        BlockStateHolder originalBlock = BukkitAdapter.adapt(usedBlock.getBlockData());
        int planted = 0;

        if(!player.hasPermission("craftbook.mech.treelopper.sapling"))
            planted = 100;

        Material sapling = null;
        if(placeSaplings && isPlantableSoil(usedBlock.getRelative(0, -1, 0).getType()))
            sapling = getSaplingFor(usedBlock.getType());

        if(sapling != null && planted < maxSaplings(sapling)) {
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SaplingPlanter(usedBlock, sapling), 2);
            planted ++;
        }

        for(Block block : allowDiagonals ? BlockUtil.getIndirectlyTouchingBlocks(usedBlock) : BlockUtil.getTouchingBlocks(usedBlock)) {
            if(block == null) continue; //Top of map, etc.
            if(visitedLocations.contains(block.getLocation())) continue;
            Material blockMaterial = block.getType();
            if(canBreakBlock(event.getPlayer(), originalBlock, block))
                if(searchBlock(event, block, player, originalBlock, visitedLocations, broken, planted)) {
                    if (!singleDamageAxe && (leavesDamageAxe || !Tag.LEAVES.isTagged(blockMaterial))) {
                        ItemUtil.damageHeldItem(event.getPlayer());
                    }
                }
        }
    }

    private static int maxSaplings(Material sapling) {
        // 2x2 trees (dark oak, jungle) have up to four trunk blocks touching the ground.
        if (sapling == Material.DARK_OAK_SAPLING || sapling == Material.JUNGLE_SAPLING)
            return 4;
        else
            return 1;
    }

    private static boolean isPlantableSoil(Material material) {
        return material == Material.DIRT || material == Material.PODZOL
                || material == Material.GRASS_BLOCK || material == Material.MYCELIUM;
    }

    /**
     * Maps a log/wood/leaf {@link Material} to the sapling that regrows it. Replaces the
     * pre-1.13 {@code MaterialData}/{@code TreeSpecies} lookup, which returns nothing for
     * wood types added since the flattening (e.g. mangrove, cherry, pale oak).
     *
     * @return the sapling material, or {@code null} if the block is not a recognised tree block
     */
    private static Material getSaplingFor(Material treeBlock) {
        switch (treeBlock) {
            case OAK_LOG: case OAK_WOOD: case STRIPPED_OAK_LOG: case STRIPPED_OAK_WOOD: case OAK_LEAVES:
                return Material.OAK_SAPLING;
            case SPRUCE_LOG: case SPRUCE_WOOD: case STRIPPED_SPRUCE_LOG: case STRIPPED_SPRUCE_WOOD: case SPRUCE_LEAVES:
                return Material.SPRUCE_SAPLING;
            case BIRCH_LOG: case BIRCH_WOOD: case STRIPPED_BIRCH_LOG: case STRIPPED_BIRCH_WOOD: case BIRCH_LEAVES:
                return Material.BIRCH_SAPLING;
            case JUNGLE_LOG: case JUNGLE_WOOD: case STRIPPED_JUNGLE_LOG: case STRIPPED_JUNGLE_WOOD: case JUNGLE_LEAVES:
                return Material.JUNGLE_SAPLING;
            case ACACIA_LOG: case ACACIA_WOOD: case STRIPPED_ACACIA_LOG: case STRIPPED_ACACIA_WOOD: case ACACIA_LEAVES:
                return Material.ACACIA_SAPLING;
            case DARK_OAK_LOG: case DARK_OAK_WOOD: case STRIPPED_DARK_OAK_LOG: case STRIPPED_DARK_OAK_WOOD: case DARK_OAK_LEAVES:
                return Material.DARK_OAK_SAPLING;
            case MANGROVE_LOG: case MANGROVE_WOOD: case STRIPPED_MANGROVE_LOG: case STRIPPED_MANGROVE_WOOD: case MANGROVE_LEAVES:
                return Material.MANGROVE_PROPAGULE;
            case CHERRY_LOG: case CHERRY_WOOD: case STRIPPED_CHERRY_LOG: case STRIPPED_CHERRY_WOOD: case CHERRY_LEAVES:
                return Material.CHERRY_SAPLING;
            case PALE_OAK_LOG: case PALE_OAK_WOOD: case STRIPPED_PALE_OAK_LOG: case STRIPPED_PALE_OAK_WOOD: case PALE_OAK_LEAVES:
                return Material.PALE_OAK_SAPLING;
            default:
                return null;
        }
    }

    private boolean canBreakBlock(Player player, BlockStateHolder originalBlock, Block toBreak) {

        if(BlockCategories.LOGS.contains(originalBlock) && Tag.LEAVES.isTagged(toBreak.getType()) && breakLeaves) {
//           TODO MaterialData nw = toBreak.getState().getData();
//            Tree old = new Tree(originalBlock.getType(), (byte) originalBlock.getData());
//            if(!(nw instanceof Leaves)) return false;
//            if(((Leaves) nw).getSpecies() != old.getSpecies()) return false;
        } else {
            if(!originalBlock.equalsFuzzy(BukkitAdapter.adapt(toBreak.getBlockData()))) return false;
        }

        if(!ProtectionUtil.canBuild(player, toBreak, false)) {
            CraftBookPlugin.inst().wrapPlayer(player).printError("area.break-permissions");
            return false;
        }

        return true;
    }

    private boolean searchBlock(BlockBreakEvent event, Block block, CraftBookPlayer player, BlockStateHolder originalBlock, Set<Location> visitedLocations, int broken, int planted) {

        if(visitedLocations.contains(block.getLocation()))
            return false;
        if(broken > maxSearchSize)
            return false;
        if(!enabledItems.contains(player.getItemInHand(HandSide.MAIN_HAND).getType()))
            return false;
        Material sapling = null;
        if(placeSaplings && isPlantableSoil(block.getRelative(0, -1, 0).getType()))
            sapling = getSaplingFor(block.getType());
        block.breakNaturally(event.getPlayer().getInventory().getItemInMainHand());
        if(sapling != null && planted < maxSaplings(sapling)) {
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SaplingPlanter(block, sapling), 2);
            planted ++;
        }
        visitedLocations.add(block.getLocation());
        broken += 1;
        for(BlockFace face : allowDiagonals ? LocationUtil.getIndirectFaces() : LocationUtil.getDirectFaces()) {
            Block relativeBlock = block.getRelative(face);
            Material relativeMaterial = relativeBlock.getType();
            if(visitedLocations.contains(relativeBlock.getLocation())) continue;
            if(canBreakBlock(event.getPlayer(), originalBlock, relativeBlock))
                if(searchBlock(event, relativeBlock, player, originalBlock, visitedLocations, broken, planted)) {
                    if (!singleDamageAxe && (leavesDamageAxe || !Tag.LEAVES.isTagged(relativeMaterial))) {
                        ItemUtil.damageHeldItem(event.getPlayer());
                    }
                }
        }

        return true;
    }

    List<BaseBlock> enabledBlocks;
    List<ItemType> enabledItems;
    private int maxSearchSize;
    private boolean allowDiagonals;
    private boolean placeSaplings;
    private boolean breakLeaves;
    private boolean singleDamageAxe;
    private boolean leavesDamageAxe;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "block-list", "A list of log blocks. This can be modified to include more logs. (for mod support etc)");
        enabledBlocks = BlockSyntax.getBlocks(config.getStringList(path + "block-list", BlockCategories.LOGS.getAll().stream().map(BlockType::id).sorted(String::compareToIgnoreCase).collect(Collectors.toList())), true);

        config.setComment(path + "tool-list", "A list of tools that can trigger the TreeLopper mechanic.");
        enabledItems = config.getStringList(path + "tool-list", Arrays.asList(ItemTypes.IRON_AXE.id(), ItemTypes.WOODEN_AXE.id(),
                ItemTypes.STONE_AXE.id(), ItemTypes.DIAMOND_AXE.id(), ItemTypes.GOLDEN_AXE.id()))
                .stream().map(ItemSyntax::getItem).map(ItemStack::getType).map(BukkitAdapter::asItemType).collect(Collectors.toList());

        config.setComment(path + "max-size", "The maximum amount of blocks the TreeLopper can break.");
        maxSearchSize = config.getInt(path + "max-size", 30);

        config.setComment(path + "allow-diagonals", "Allow the TreeLopper to break blocks that are diagonal from each other.");
        allowDiagonals = config.getBoolean(path + "allow-diagonals", false);

        config.setComment(path + "place-saplings", "If enabled, TreeLopper will plant a sapling automatically when a tree is broken.");
        placeSaplings = config.getBoolean(path + "place-saplings", false);

        config.setComment(path + "break-leaves", "If enabled, TreeLopper will break leaves connected to the tree. (If enforce-data is enabled, will only break leaves of same type)");
        breakLeaves = config.getBoolean(path + "break-leaves", false);

        config.setComment(path + "single-damage-axe", "Only remove one damage from the axe, regardless of the amount of logs removed.");
        singleDamageAxe = config.getBoolean(path + "single-damage-axe", false);

        config.setComment(path + "leaves-damage-axe", "Whether the leaves will also damage the axe when single-damage-axe is false and break-leaves is true.");
        leavesDamageAxe = config.getBoolean(path + "leaves-damage-axe", true);
    }

    private static class SaplingPlanter implements Runnable {
        private final Block usedBlock;
        private final Material saplingMaterial;

        SaplingPlanter(Block usedBlock, Material saplingMaterial) {
            this.usedBlock = usedBlock;
            this.saplingMaterial = saplingMaterial;
        }

        @Override
        public void run () {
            // Only plant if the spot is still empty; the log may have been replaced by something else.
            if (usedBlock.getType() == Material.AIR)
                usedBlock.setType(saplingMaterial);
        }

    }
}
