package com.ahmed.mousekeyboardhud.gui;

import com.ahmed.mousekeyboardhud.config.ConfigManager;
import com.ahmed.mousekeyboardhud.util.ColorUtil;

/** Holds the dark-glass theme presets; the active index is persisted in the config. */
public final class ThemeManager {

    private static final Theme[] PRESETS = {
            preset("Ice", 0xFF4D9DFF),
            preset("Violet", 0xFF8F7BFF),
            preset("Mint", 0xFF3FD6A6)
    };

    private ThemeManager() {}

    private static Theme preset(String name, int accent) {
        return new Theme(name,
                0x66000000,       // backdrop dim
                0xE60B0D13,       // glass panel
                0xF2141824,       // raised surface
                0x2EFFFFFF,       // subtle border
                0xFFEDEFF5,       // text
                0xFF8B93A5,       // dim text
                accent,
                ColorUtil.withAlpha(accent, 0x38),
                0x14FFFFFF,       // hover
                0x26FFFFFF,       // slider track
                5);
    }

    public static Theme current() {
        int i = ConfigManager.get().general.guiTheme;
        return PRESETS[Math.max(0, Math.min(PRESETS.length - 1, i))];
    }

    public static String[] names() {
        String[] out = new String[PRESETS.length];
        for (int i = 0; i < PRESETS.length; i++) out[i] = PRESETS[i].name();
        return out;
    }
}
