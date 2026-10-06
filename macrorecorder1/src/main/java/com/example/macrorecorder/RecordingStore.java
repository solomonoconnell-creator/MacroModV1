package com.example.macrorecorder;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class RecordingStore {
    private RecordingStore() {}
    private static final Gson GSON = new Gson();

    static Path dir() { return MacroConfig.dir().resolve("recordings"); }

    public static List<String> list() {
        List<String> out = new ArrayList<>();
        try {
            Files.createDirectories(dir());
            try (Stream<Path> s = Files.list(dir())) {
                s.map(p -> p.getFileName().toString())
                 .filter(n -> n.endsWith(".json"))
                 .map(n -> n.substring(0, n.length() - 5))
                 .sorted()
                 .forEach(out::add);
            }
        } catch (IOException ignored) {}
        return out;
    }

    public static void save(String name, Recording r) throws IOException {
        Files.createDirectories(dir());
        Files.writeString(dir().resolve(name + ".json"), GSON.toJson(r));
    }

    public static Recording load(String name) {
        try {
            return GSON.fromJson(Files.readString(dir().resolve(name + ".json")), Recording.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static void delete(String name) {
        try { Files.deleteIfExists(dir().resolve(name + ".json")); } catch (IOException ignored) {}
    }
}
