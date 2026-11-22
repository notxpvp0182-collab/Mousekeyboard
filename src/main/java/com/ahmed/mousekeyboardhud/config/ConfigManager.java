package com.ahmed.mousekeyboardhud.config;

import com.ahmed.mousekeyboardhud.MouseKeyboardHudClient;
import com.ahmed.mousekeyboardhud.util.MathUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Loads, sanitizes and atomically saves {@link HudConfig} as JSON in the Fabric config directory. */
public final class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("mousekeyboardhud.json");

    private static HudConfig config = new HudConfig();

    private ConfigManager() {}

    public static HudConfig get() {
        return config;
    }

    public static void load() {
        if (!Files.isRegularFile(FILE)) {
            config = new HudConfig();
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            HudConfig loaded = GSON.fromJson(reader, HudConfig.class);
            config = loaded != null ? loaded : new HudConfig();
        } catch (IOException | JsonParseException | IllegalStateException e) {
            MouseKeyboardHudClient.LOGGER.error("Config unreadable, backing it up and using defaults", e);
            backupBroken();
            config = new HudConfig();
        }
        sanitize(config);
        save();
    }

    public static void save() {
        sanitize(config);
        Path tmp = FILE.resolveSibling(FILE.getFileName() + ".tmp");
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                GSON.toJson(config, writer);
            }
            try {
                Files.move(tmp, FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicFailed) {
                Files.move(tmp, FILE, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            MouseKeyboardHudClient.LOGGER.error("Could not save config", e);
        }
    }

    /** Serializes the current config; used by the HUD editor to support Cancel. */
    public static String snapshot() {
        return GSON.toJson(config);
    }

    /** Restores a snapshot created by {@link #snapshot()} without writing to disk. */
    public static void restore(String json) {
        try {
            HudConfig restored = GSON.fromJson(json, HudConfig.class);
            if (restored != null) {
                sanitize(restored);
                config = restored;
            }
        } catch (JsonParseException ignored) {
            // keep current config
        }
    }

    public static void resetAll() {
        config = new HudConfig();
        save();
    }

    private static void backupBroken() {
        try {
            Files.move(FILE, FILE.resolveSibling("mousekeyboardhud.json.broken"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
            // nothing else we can do
        }
    }

    /** Fills missing sections (older/hand-edited files) and clamps every numeric range. */
    public static void sanitize(HudConfig c) {
        if (c.general == null) c.general = new HudConfig.General();
        if (c.keyboard == null) c.keyboard = new HudConfig.Keyboard();
        if (c.mouse == null) c.mouse = new HudConfig.Mouse();
        if (c.animation == null) c.animation = new HudConfig.Animation();

        HudConfig.Keyboard k = c.keyboard;
        if (k.fullPos == null) k.fullPos = new HudConfig.Pos(0.04f, 0.70f);
        if (k.leftPos == null) k.leftPos = new HudConfig.Pos(0.04f, 0.70f);
        if (k.rightPos == null) k.rightPos = new HudConfig.Pos(0.30f, 0.70f);
        clampPos(k.fullPos);
        clampPos(k.leftPos);
        clampPos(k.rightPos);
        k.scale = MathUtil.clamp(k.scale, 0.3f, 4f);
        k.halvesScale = MathUtil.clamp(k.halvesScale, 0.3f, 4f);
        k.keySize = MathUtil.clamp(k.keySize, 10f, 32f);
        k.keySpacing = MathUtil.clamp(k.keySpacing, 0f, 8f);
        k.cornerRadius = MathUtil.clamp(k.cornerRadius, 0f, 12f);
        k.opacity = MathUtil.clamp(k.opacity, 0.05f, 1f);
        k.outlineWidth = MathUtil.clamp(k.outlineWidth, 0.5f, 3f);
        k.glowStrength = MathUtil.clamp(k.glowStrength, 0f, 3f);

        HudConfig.Mouse m = c.mouse;
        if (m.pos == null) m.pos = new HudConfig.Pos(0.52f, 0.70f);
        clampPos(m.pos);
        m.scale = MathUtil.clamp(m.scale, 0.3f, 4f);
        m.cornerRadius = MathUtil.clamp(m.cornerRadius, 0f, 1f);
        m.opacity = MathUtil.clamp(m.opacity, 0.05f, 1f);
        m.outlineWidth = MathUtil.clamp(m.outlineWidth, 0.5f, 3f);
        m.glowStrength = MathUtil.clamp(m.glowStrength, 0f, 3f);

        HudConfig.Animation a = c.animation;
        a.speed = MathUtil.clamp(a.speed, 0.25f, 3f);
        a.pressDepth = MathUtil.clamp(a.pressDepth, 0f, 4f);
        a.glowIntensity = MathUtil.clamp(a.glowIntensity, 0f, 2f);
        if (a.easing == null) a.easing = com.ahmed.mousekeyboardhud.animation.Easing.EASE_OUT;

        c.general.guiTheme = MathUtil.clamp(c.general.guiTheme, 0, 2);
    }

    private static void clampPos(HudConfig.Pos p) {
        if (Float.isNaN(p.x) || Float.isNaN(p.y)) {
            p.x = 0.05f;
            p.y = 0.7f;
        }
        p.x = MathUtil.clamp(p.x, -0.5f, 1.5f);
        p.y = MathUtil.clamp(p.y, -0.5f, 1.5f);
    }
}
