package com.example.macrorecorder;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

final class Playback {
    private Playback() {}

    static boolean active;
    private static Recording rec;
    private static int idx, delay, loopsDone, prevMask;

    static boolean start(Minecraft mc) {
        if (mc.player == null) return false;
        String name = MacroConfig.I.selectedRecording;
        Recording r = name.isEmpty() ? null : RecordingStore.load(name);
        if (r == null || r.length() == 0) {
            Compat.msg(mc, "No recording selected - record one (N) or pick one in the menu (I)");
            return false;
        }
        rec = r; idx = 0; delay = 0; loopsDone = 0; prevMask = 0;
        active = true;
        Compat.msg(mc, "Playing " + name + " (press U to stop)");
        return true;
    }

    static void stop(Minecraft mc, String reason) {
        if (!active) return;
        active = false;
        release(mc);
        Compat.msg(mc, reason);
    }

    private static void release(Minecraft mc) {
        for (KeyMapping k : Tracked.keys(mc.options)) k.setDown(false);
        prevMask = 0;
    }

    private static void apply(Minecraft mc, int mask) {
        KeyMapping[] ks = Tracked.keys(mc.options);
        for (int i = 0; i < ks.length; i++) {
            boolean down = (mask >> i & 1) != 0;
            boolean was = (prevMask >> i & 1) != 0;
            ks[i].setDown(down);
            if (down && !was && Tracked.NEEDS_CLICK[i]) Compat.click(ks[i]);
        }
        prevMask = mask;
    }

    static void tick(Minecraft mc) {
        if (!active) return;
        LocalPlayer p = mc.player;
        if (p == null) { active = false; return; }
        if (p.isDeadOrDying()) { stop(mc, "Playback stopped: you died"); return; }

        // a screen is open (menu, inventory, chat...): hold still and pause
        if (mc.screen != null) { apply(mc, 0); return; }

        if (delay > 0) { delay--; apply(mc, 0); return; }

        if (idx >= rec.length()) {
            loopsDone++;
            MacroConfig c = MacroConfig.I;
            if (c.loop && (c.loopCount == 0 || loopsDone < c.loopCount)) {
                idx = 0; delay = c.loopDelayTicks; apply(mc, 0);
                return;
            }
            stop(mc, "Playback finished");
            return;
        }

        p.setYRot(rec.yaw[idx]); p.setXRot(rec.pitch[idx]);
        p.yRotO = rec.yaw[idx];  p.xRotO = rec.pitch[idx];
        if (Compat.getSlot(mc) != rec.slot[idx]) Compat.setSlot(mc, rec.slot[idx]);
        apply(mc, rec.keys[idx]);
        idx++;
    }
}
