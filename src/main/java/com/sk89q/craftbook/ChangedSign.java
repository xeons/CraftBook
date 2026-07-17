package com.sk89q.craftbook;

import com.sk89q.craftbook.bukkit.BukkitCraftBookPlayer;
import com.sk89q.craftbook.mechanics.variables.VariableCommands;
import com.sk89q.craftbook.mechanics.variables.VariableManager;
import com.sk89q.craftbook.util.ParsingUtil;
import com.sk89q.craftbook.util.RegexUtil;
import io.papermc.lib.PaperLib;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;

import java.util.Arrays;
import java.util.Locale;

public class ChangedSign {

    private Block block;
    private Sign sign;
    private String[] lines;
    private String[] oldLines;
    // Which physical side of the sign these lines belong to. Modern (1.20+) signs have two
    // writable sides; this is resolved in flushLines() so reads and writes hit the same side.
    private Side side = Side.FRONT;

    public ChangedSign(Block block, String[] lines, CraftBookPlayer player) {
        this(block, lines);

        if (player != null) {
            checkPlayerVariablePermissions(player);
        }
    }

    public ChangedSign(Block block, String[] lines) {
        Validate.notNull(block);

        this.block = block;

        if (lines == null) {
            this.flushLines();
        } else {
            this.lines = lines;
            this.oldLines = new String[this.lines.length];
            System.arraycopy(this.lines, 0, this.oldLines, 0, this.lines.length);
        }
    }

    public void checkPlayerVariablePermissions(CraftBookPlayer player) {
        if (this.lines != null && VariableManager.instance != null) {
            for (int i = 0; i < 4; i++) {

                String line = this.lines[i];
                for (String var : ParsingUtil.getPossibleVariables(line)) {

                    String key;

                    if (var.contains("|")) {
                        String[] bits = RegexUtil.PIPE_PATTERN.split(var);
                        key = bits[0];
                    } else
                        key = "global";

                    if (!VariableCommands.hasVariablePermission(((BukkitCraftBookPlayer) player).getPlayer(), key, var,
                            "use"))
                        setLine(i, StringUtils.replace(line, '%' + key + '|' + var + '%', ""));
                }
            }
        }
    }

    public Block getBlock() {
        return block;
    }

    public Sign getSign() {
        if (this.sign == null) {
            this.sign = (Sign) PaperLib.getBlockState(this.block, false).getState();
        }
        return sign;
    }

    public Material getType() {

        return block.getType();
    }

    public byte getLightLevel() {

        return block.getLightLevel();
    }

    public int getX() {

        return block.getX();
    }

    public int getY() {

        return block.getY();
    }

    public int getZ() {

        return block.getZ();
    }

    public String[] getLines() {

        return lines;
    }

    public String getLine(int index) throws IndexOutOfBoundsException {

        return ParsingUtil.parseLine(lines[index], null);
    }

    public String getRawLine(int index) throws IndexOutOfBoundsException {

        return lines[index];
    }

    public void setLine(int index, String line) throws IndexOutOfBoundsException {

        lines[index] = line;
    }

    public void setType(Material type) {

        block.setType(type);
    }

    public boolean update(boolean force) {

        if (!hasChanged() && !force)
            return false;
        SignSide signSide = getSign().getSide(side);
        for (int i = 0; i < 4; i++) {
            signSide.setLine(i, lines[i]);
        }
        System.arraycopy(this.lines, 0, this.oldLines, 0, this.lines.length);

        return getSign().update(force, false);
    }

    public void setLines(String[] lines) {
        this.lines = lines;
    }

    public void setOldLines(String[] oldLines) {
        this.oldLines = oldLines;
    }

    public boolean hasChanged() {
        boolean ret = false;
        try {
            for (int i = 0; i < 4; i++)
                if (!oldLines[i].equals(lines[i])) {
                    ret = true;
                    break;
                }
        } catch (Exception ignored) {
        }
        return ret;
    }

    public void flushLines() {
        this.sign = null;
        Sign state = this.getSign();
        // Prefer the front side, but fall back to the back when the front is blank so that
        // a mechanic written on the back of the sign is still picked up (1.20+ two-sided signs).
        this.side = Side.FRONT;
        if (isBlank(state.getSide(Side.FRONT).getLines()) && !isBlank(state.getSide(Side.BACK).getLines())) {
            this.side = Side.BACK;
        }
        this.lines = state.getSide(this.side).getLines();
        if (this.oldLines == null) {
            this.oldLines = new String[lines.length];
        }
        System.arraycopy(this.lines, 0, this.oldLines, 0, this.lines.length);
    }

    public Side getSide() {
        return side;
    }

    private static boolean isBlank(String[] lines) {
        if (lines == null)
            return true;
        for (String line : lines)
            if (line != null && !line.isEmpty())
                return false;
        return true;
    }

    public boolean updateSign(ChangedSign sign) {
        if (!equals(sign)) {
            flushLines();
            return true;
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ChangedSign) {
            if (((ChangedSign) o).getType() != getType())
                return false;
            for (int i = 0; i < 4; i++)
                if (!((ChangedSign) o).getRawLine(i).equals(getRawLine(i)))
                    return false;
            if (((ChangedSign) o).getX() != getX())
                return false;
            if (((ChangedSign) o).getY() != getY())
                return false;
            if (((ChangedSign) o).getZ() != getZ())
                return false;
            if (!((ChangedSign) o).block.getWorld().getUID().equals(block.getWorld().getUID()))
                return false;
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (getType().hashCode() * 1103515245 + 12345
                ^ Arrays.hashCode(lines) * 1103515245 + 12345
                ^ getX() * 1103515245 + 12345
                ^ getY() * 1103515245 + 12345
                ^ getZ() * 1103515245 + 12345
                ^ block.getWorld().getUID().hashCode() * 1103515245 + 12345);
    }

    @Override
    public String toString() {
        return lines[0] + '|' + lines[1] + '|' + lines[2] + '|' + lines[3];
    }

    public boolean hasVariable(String var) {
        if (VariableManager.instance == null)
            return false;

        var = var.toLowerCase(Locale.ENGLISH);
        return lines[0].toLowerCase(Locale.ENGLISH).contains('%' + var + '%')
                || lines[1].toLowerCase(Locale.ENGLISH).contains('%' + var + '%')
                || lines[2].toLowerCase(Locale.ENGLISH).contains('%' + var + '%')
                || lines[3].toLowerCase(Locale.ENGLISH).contains('%' + var + '%');
    }
}
