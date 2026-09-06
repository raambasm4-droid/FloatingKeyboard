package com.abhikaran.floatingkeyboard.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class KeyboardConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("floating_keyboard.json");

    public int x = -1;
    public int y = -1;
    public double scale = 1.0;

    public static KeyboardConfig load() {
        try {
            if (Files.exists(FILE)) {
                try (Reader reader = Files.newBufferedReader(FILE)) {
                    KeyboardConfig cfg = GSON.fromJson(reader, KeyboardConfig.class);
                    if (cfg != null) {
                        cfg.clamp();
                        return cfg;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        KeyboardConfig cfg = new KeyboardConfig();
        cfg.save();
        return cfg;
    }

    public void save() {
        try {
            clamp();
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception ignored) {
        }
    }

    public void reset() {
        x = -1;
        y = -1;
        scale = 1.0;
        save();
    }

    public void clamp() {
        scale = Math.max(0.55, Math.min(1.60, scale));
    }
}

