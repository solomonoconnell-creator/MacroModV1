package com.example.macrorecorder;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;

/**
 * Every call that touches a Minecraft API likely to shift between versions lives here.
 * If the build fails after a Minecraft update, this is the first file to check.
 */
final class Compat {
    private Compat() {}

    /** Simulates a physical key press so consumeClick() fires (attack, use, drop, swap hands). */
    static void click(KeyMapping m) {
        KeyMapping.click(InputConstants.getKey(m.saveString()));
    }

    static void msg(Minecraft mc, String text) {
        mc.gui.setOverlayMessage(Component.literal("[Macro] " + text), false);
    }

    static void sendCommandOrChat(Minecraft mc, String text) {
        if (mc.player == null || text == null || text.isBlank()) return;
        String t = text.trim();
        if (t.startsWith("/")) mc.player.connection.sendCommand(t.substring(1));
        else mc.player.connection.sendChat(t);
    }

    /** Same thing the "Save and Quit" / "Disconnect" button does. */
    static void leaveWorld(Minecraft mc) {
        if (mc.level == null) return;
        boolean local = mc.isLocalServer();
        mc.level.disconnect(ClientLevel.DEFAULT_QUIT_MESSAGE);
               if (local) mc.disconnect(new GenericMessageScreen(Component.translatable("menu.savingLevel")), false);
        else mc.disconnect(new TitleScreen(), false);
    }

    static int getSlot(Minecraft mc) { return mc.player.getInventory().getSelectedSlot(); }

    static void setSlot(Minecraft mc, int slot) { mc.player.getInventory().setSelectedSlot(slot); }
}
