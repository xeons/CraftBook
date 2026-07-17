package com.sk89q.craftbook.mechanics.variables;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.ParsingUtil;

class VariablePacketModifier {

    VariablePacketModifier() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new VariablePacketAdapter());
    }

    private static class VariablePacketAdapter extends PacketAdapter {
        VariablePacketAdapter() {
            // The clientbound CHAT packet was removed in MC 1.19; server messages now travel
            // as SYSTEM_CHAT (plus PLAYER_CHAT for signed player chat, which we leave alone).
            super(CraftBookPlugin.inst(), PacketType.Play.Server.SYSTEM_CHAT);
        }

        @Override
        public void onPacketSending(PacketEvent event) {
            try {
                // Newer builds carry the message as a chat component (NBT); older ones as a JSON string.
                StructureModifier<WrappedChatComponent> components = event.getPacket().getChatComponents();
                if (components.size() > 0 && components.read(0) != null) {
                    WrappedChatComponent chat = components.read(0);
                    chat.setJson(ParsingUtil.parseVariables(chat.getJson(), event.getPlayer()));
                    components.write(0, chat);
                    return;
                }

                StructureModifier<String> strings = event.getPacket().getStrings();
                if (strings.size() > 0 && strings.read(0) != null) {
                    strings.write(0, ParsingUtil.parseVariables(strings.read(0), event.getPlayer()));
                }
            } catch (RuntimeException e) {
                // Never break the outbound packet pipeline over a variable substitution failure.
                CraftBookPlugin.inst().getLogger()
                        .warning("Failed to substitute variables in a system chat packet: " + e.getMessage());
            }
        }
    }
}
