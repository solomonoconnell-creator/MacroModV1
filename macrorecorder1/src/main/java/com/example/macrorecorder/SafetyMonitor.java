package com.example.macrorecorder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

final class SafetyMonitor {
    private SafetyMonitor() {}

    private static boolean latched;       // fire once per "crossing"
    private static int disconnectIn = -1; // ticks until disconnect (lets the command go out first)

    static void tick(Minecraft mc) {
        if (disconnectIn >= 0 && --disconnectIn < 0) { Compat.leaveWorld(mc); return; }

        MacroConfig c = MacroConfig.I;
        LocalPlayer p = mc.player;
        if (p == null || !c.healthRuleEnabled) { latched = false; return; }
        if (!Playback.active && !c.monitorWhenNotPlaying) { latched = false; return; }

        float pct = p.getHealth() / p.getMaxHealth() * 100f;
        boolean hit = c.triggerWhenBelow ? pct < c.healthPercent : pct > c.healthPercent;
        if (!hit) { latched = false; return; }
        if (latched) return;
        latched = true;

        Playback.stop(mc, "Health rule triggered (" + Math.round(pct) + "%)");
        switch (c.healthAction) {
            case STOP_ONLY -> {}
            case RUN_COMMAND -> Compat.sendCommandOrChat(mc, c.command);
            case DISCONNECT -> Compat.leaveWorld(mc);
            case COMMAND_THEN_DISCONNECT -> {
                Compat.sendCommandOrChat(mc, c.command);
                disconnectIn = 10; // half a second
            }
        }
    }
}
