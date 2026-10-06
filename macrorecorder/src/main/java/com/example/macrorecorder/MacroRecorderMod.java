package com.example.macrorecorder;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class MacroRecorderMod implements ClientModInitializer {
    public static final String ID = "macrorecorder";

    private static KeyMapping recordKey, playKey, menuKey;

    @Override
    public void onInitializeClient() {
        MacroConfig.load();

        KeyMapping.Category cat = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(ID, "main"));
        recordKey = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.macrorecorder.record", InputConstants.Type.KEYSYM, InputConstants.KEY_N, cat));
        playKey = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.macrorecorder.play", InputConstants.Type.KEYSYM, InputConstants.KEY_U, cat));
        menuKey = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.macrorecorder.menu", InputConstants.Type.KEYSYM, InputConstants.KEY_I, cat));

        ClientTickEvents.START_CLIENT_TICK.register(MacroRecorderMod::tick);
    }

    private static void tick(Minecraft mc) {
        while (menuKey.consumeClick()) {
            if (mc.screen == null) mc.setScreen(new MacroScreen(null));
        }
        while (recordKey.consumeClick()) {
            if (mc.player == null) continue;
            if (Playback.active) Compat.msg(mc, "Stop playback first (U)");
            else if (Recorder.active) Recorder.stop(mc);
            else Recorder.start(mc);
        }
        while (playKey.consumeClick()) {
            if (mc.player == null) continue;
            if (Recorder.active) Compat.msg(mc, "Stop recording first (N)");
            else if (Playback.active) Playback.stop(mc, "Playback stopped");
            else Playback.start(mc);
        }

        Recorder.tick(mc);
        Playback.tick(mc);
        SafetyMonitor.tick(mc);
    }
}
