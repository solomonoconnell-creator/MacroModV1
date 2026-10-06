package com.example.macrorecorder;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

final class Recorder {
    private Recorder() {}

    static boolean active;
    private static final List<Integer> keys = new ArrayList<>();
    private static final List<Float> yaw = new ArrayList<>();
    private static final List<Float> pitch = new ArrayList<>();
    private static final List<Integer> slot = new ArrayList<>();

    static void start(Minecraft mc) {
        if (mc.player == null) return;
        keys.clear(); yaw.clear(); pitch.clear(); slot.clear();
        active = true;
        Compat.msg(mc, "Recording... (press N to stop)");
    }

    static void stop(Minecraft mc) {
        if (!active) return;
        active = false;
        if (keys.isEmpty()) { Compat.msg(mc, "Recording was empty, nothing saved"); return; }

        Recording r = new Recording();
        int n = keys.size();
        r.keys = new int[n]; r.yaw = new float[n]; r.pitch = new float[n]; r.slot = new int[n];
        for (int i = 0; i < n; i++) {
            r.keys[i] = keys.get(i); r.yaw[i] = yaw.get(i);
            r.pitch[i] = pitch.get(i); r.slot[i] = slot.get(i);
        }
        String name = "rec-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        try {
            RecordingStore.save(name, r);
            MacroConfig.I.selectedRecording = name;
            MacroConfig.save();
            Compat.msg(mc, "Saved " + name + " (" + n + " ticks, " + (n / 20) + "s)");
        } catch (Exception e) {
            Compat.msg(mc, "Failed to save recording: " + e.getMessage());
        }
    }

    static void tick(Minecraft mc) {
        if (!active) return;
        LocalPlayer p = mc.player;
        if (p == null) { active = false; return; }

        int mask = 0;
        if (mc.screen == null) {
            KeyMapping[] ks = Tracked.keys(mc.options);
            for (int i = 0; i < ks.length; i++) if (ks[i].isDown()) mask |= 1 << i;
        }
        keys.add(mask);
        yaw.add(p.getYRot());
        pitch.add(p.getXRot());
        slot.add(Compat.getSlot(mc));
    }
}
