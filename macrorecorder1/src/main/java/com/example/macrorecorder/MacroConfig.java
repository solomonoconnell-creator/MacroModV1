package com.example.macrorecorder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MacroConfig {
    public enum HealthAction { STOP_ONLY, RUN_COMMAND, DISCONNECT, COMMAND_THEN_DISCONNECT }

    // playback
    public String selectedRecording = "";
    public boolean loop = false;
    public int loopCount = 0;          // 0 = forever
    public int loopDelayTicks = 20;

    // health rule
    public boolean healthRuleEnabled = true;
    public boolean triggerWhenBelow = true;   // false = trigger when ABOVE the threshold
    public int healthPercent = 30;
    public HealthAction healthAction = HealthAction.DISCONNECT;
    public String command = "/hub";
    public boolean monitorWhenNotPlaying = false;

    // ---------------------------------------------------------------
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static MacroConfig I = new MacroConfig();

    static Path dir() { return FabricLoader.getInstance().getConfigDir().resolve("macrorecorder"); }
    static Path file() { return dir().resolve("config.json"); }

    public static void load() {
        try {
            if (Files.exists(file())) {
                MacroConfig c = GSON.fromJson(Files.readString(file()), MacroConfig.class);
                if (c != null) I = c;
            }
        } catch (Exception e) {
            System.err.println("[MacroRecorder] Could not read config: " + e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(dir());
            Files.writeString(file(), GSON.toJson(I));
        } catch (IOException e) {
            System.err.println("[MacroRecorder] Could not save config: " + e);
        }
    }
}
