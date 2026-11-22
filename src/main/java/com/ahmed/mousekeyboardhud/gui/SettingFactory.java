package com.ahmed.mousekeyboardhud.gui;

import com.ahmed.mousekeyboardhud.animation.Easing;
import com.ahmed.mousekeyboardhud.config.ConfigManager;
import com.ahmed.mousekeyboardhud.config.HudConfig;
import com.ahmed.mousekeyboardhud.gui.setting.*;
import com.ahmed.mousekeyboardhud.keybind.ModKeybinds;
import com.ahmed.mousekeyboardhud.util.ColorUtil;

import java.util.ArrayList;
import java.util.List;

/** Builds the settings lists shown in the ClickGUI categories and in the HUD editor customization popups. */
public final class SettingFactory {

    private SettingFactory() {}

    private static HudConfig c() { return ConfigManager.get(); }

    private static SliderSetting slider(String n, double min, double max, double step, int dec, java.util.function.DoubleSupplier g, java.util.function.DoubleConsumer s) {
        return new SliderSetting(n, min, max, step, dec, "", g, s);
    }

    /** Slider editing only the alpha channel of a color. */
    private static SliderSetting alpha(String n, java.util.function.IntSupplier g, java.util.function.IntConsumer s) {
        return new SliderSetting(n, 0, 100, 1, 0, "%", () -> ColorUtil.alpha(g.getAsInt()) / 2.55,
                v -> s.accept(ColorUtil.withAlpha(g.getAsInt(), (int) Math.round(v * 2.55))));
    }

    public static List<Setting> forCategory(Category cat, Runnable openEditor) {
        List<Setting> l = new ArrayList<>();
        switch (cat) {
            case GENERAL -> {
                l.add(new ToggleSetting("HUD", () -> c().general.hudEnabled, v -> c().general.hudEnabled = v));
                l.add(new ToggleSetting("Show in menus", () -> c().general.showInScreens, v -> c().general.showInScreens = v));
                l.add(new ChoiceSetting("GUI theme", ThemeManager.names(), () -> c().general.guiTheme, v -> c().general.guiTheme = v));
                l.add(new ButtonSetting("Open HUD Editor", openEditor));
            }
            case KEYBOARD -> {
                l.addAll(keyboardToggles());
                l.addAll(keyboardSizing());
            }
            case MOUSE -> {
                l.addAll(mouseToggles());
                l.addAll(mouseSizing());
            }
            case ANIMATION -> l.addAll(animation());
            case COLORS -> {
                l.add(new HeaderSetting("Keyboard"));
                l.addAll(keyboardColors());
                l.add(new HeaderSetting("Mouse"));
                l.addAll(mouseColors());
            }
            case HUD -> {
                l.add(new ButtonSetting("Open HUD Editor", openEditor));
                l.add(new ToggleSetting("HUD", () -> c().general.hudEnabled, v -> c().general.hudEnabled = v));
                l.add(new ButtonSetting("Reset positions", SettingFactory::resetPositions));
                l.add(new ButtonSetting("Reset scales", SettingFactory::resetScales));
            }
            case KEYBINDS -> {
                l.add(new KeybindSetting("Open ClickGUI", () -> ModKeybinds.openClickGui));
                l.add(new KeybindSetting("Open HUD Editor", () -> ModKeybinds.openHudEditor));
            }
            case ADVANCED -> {
                l.add(new HeaderSetting("Keyboard"));
                l.addAll(keyboardStyle());
                l.add(new HeaderSetting("Mouse"));
                l.addAll(mouseStyle());
                l.add(new ButtonSetting("Reset all settings", () -> ConfigManager.resetAll()));
            }
        }
        return l;
    }

    /** Full keyboard customization (right-click in the HUD editor). */
    public static List<Setting> keyboardAll() {
        List<Setting> l = new ArrayList<>();
        l.addAll(keyboardToggles());
        l.addAll(keyboardSizing());
        l.add(new HeaderSetting("Colors"));
        l.addAll(keyboardColors());
        l.add(new HeaderSetting("Style"));
        l.addAll(keyboardStyle());
        l.add(new HeaderSetting("Animation"));
        l.add(slider("Animation speed", 0.25, 3, 0.05, 2, () -> c().animation.speed, v -> c().animation.speed = (float) v));
        return l;
    }

    /** Full mouse customization (right-click in the HUD editor). */
    public static List<Setting> mouseAll() {
        List<Setting> l = new ArrayList<>();
        l.addAll(mouseToggles());
        l.addAll(mouseSizing());
        l.add(new HeaderSetting("Colors"));
        l.addAll(mouseColors());
        l.add(new HeaderSetting("Style"));
        l.addAll(mouseStyle());
        l.add(new HeaderSetting("Animation"));
        l.add(slider("Click animation speed", 0.25, 3, 0.05, 2, () -> c().animation.speed, v -> c().animation.speed = (float) v));
        return l;
    }

    private static List<Setting> keyboardToggles() {
        List<Setting> l = new ArrayList<>();
        l.add(new ToggleSetting("Keyboard", () -> c().keyboard.enabled, v -> c().keyboard.enabled = v));
        l.add(new ToggleSetting("Full keyboard", () -> c().keyboard.fullKeyboard, v -> c().keyboard.fullKeyboard = v));
        l.add(new ToggleSetting("Left half", () -> c().keyboard.leftHalf, v -> c().keyboard.leftHalf = v)
                .visibleWhen(() -> !c().keyboard.fullKeyboard));
        l.add(new ToggleSetting("Right half", () -> c().keyboard.rightHalf, v -> c().keyboard.rightHalf = v)
                .visibleWhen(() -> !c().keyboard.fullKeyboard));
        return l;
    }

    private static List<Setting> keyboardSizing() {
        List<Setting> l = new ArrayList<>();
        l.add(slider("Scale", 0.3, 4, 0.05, 2, () -> c().keyboard.scale, v -> c().keyboard.scale = (float) v)
                .visibleWhen(() -> c().keyboard.fullKeyboard));
        l.add(slider("Halves scale", 0.3, 4, 0.05, 2, () -> c().keyboard.halvesScale, v -> c().keyboard.halvesScale = (float) v)
                .visibleWhen(() -> !c().keyboard.fullKeyboard));
        l.add(slider("Key size", 10, 32, 0.5, 1, () -> c().keyboard.keySize, v -> c().keyboard.keySize = (float) v));
        l.add(slider("Key spacing", 0, 8, 0.5, 1, () -> c().keyboard.keySpacing, v -> c().keyboard.keySpacing = (float) v));
        l.add(slider("Corner radius", 0, 12, 0.5, 1, () -> c().keyboard.cornerRadius, v -> c().keyboard.cornerRadius = (float) v));
        l.add(slider("Overall opacity", 5, 100, 1, 0, () -> c().keyboard.opacity * 100, v -> c().keyboard.opacity = (float) (v / 100)));
        return l;
    }

    private static List<Setting> keyboardColors() {
        HudConfig.Keyboard k = c().keyboard;
        List<Setting> l = new ArrayList<>();
        l.add(new ColorSetting("Key color", () -> c().keyboard.keyColor, v -> c().keyboard.keyColor = v));
        l.add(new ColorSetting("Pressed color", () -> c().keyboard.pressedColor, v -> c().keyboard.pressedColor = v));
        l.add(new ColorSetting("Text color", () -> c().keyboard.textColor, v -> c().keyboard.textColor = v));
        l.add(new ColorSetting("Outline color", () -> c().keyboard.outlineColor, v -> c().keyboard.outlineColor = v));
        l.add(new ColorSetting("Background color", () -> c().keyboard.backgroundColor, v -> c().keyboard.backgroundColor = v));
        l.add(new ColorSetting("Glow color", () -> c().keyboard.glowColor, v -> c().keyboard.glowColor = v));
        return l;
    }

    private static List<Setting> keyboardStyle() {
        List<Setting> l = new ArrayList<>();
        l.add(new ToggleSetting("Background", () -> c().keyboard.backgroundEnabled, v -> c().keyboard.backgroundEnabled = v));
        l.add(alpha("Background opacity", () -> c().keyboard.backgroundColor, v -> c().keyboard.backgroundColor = v)
                .visibleWhen(() -> c().keyboard.backgroundEnabled));
        l.add(new ToggleSetting("Key background", () -> c().keyboard.keyBackgroundEnabled, v -> c().keyboard.keyBackgroundEnabled = v));
        l.add(new ToggleSetting("Outline", () -> c().keyboard.outlineEnabled, v -> c().keyboard.outlineEnabled = v));
        l.add(alpha("Outline opacity", () -> c().keyboard.outlineColor, v -> c().keyboard.outlineColor = v)
                .visibleWhen(() -> c().keyboard.outlineEnabled));
        l.add(slider("Outline width", 0.5, 3, 0.5, 1, () -> c().keyboard.outlineWidth, v -> c().keyboard.outlineWidth = (float) v)
                .visibleWhen(() -> c().keyboard.outlineEnabled));
        l.add(new ToggleSetting("Glow", () -> c().keyboard.glowEnabled, v -> c().keyboard.glowEnabled = v));
        l.add(slider("Glow strength", 0, 3, 0.1, 1, () -> c().keyboard.glowStrength, v -> c().keyboard.glowStrength = (float) v)
                .visibleWhen(() -> c().keyboard.glowEnabled));
        return l;
    }

    private static List<Setting> mouseToggles() {
        List<Setting> l = new ArrayList<>();
        l.add(new ToggleSetting("Mouse", () -> c().mouse.enabled, v -> c().mouse.enabled = v));
        l.add(new ToggleSetting("Scroll indicator", () -> c().mouse.showScrollIndicator, v -> c().mouse.showScrollIndicator = v));
        return l;
    }

    private static List<Setting> mouseSizing() {
        List<Setting> l = new ArrayList<>();
        l.add(slider("Scale", 0.3, 4, 0.05, 2, () -> c().mouse.scale, v -> c().mouse.scale = (float) v));
        l.add(slider("Roundness", 0, 1, 0.05, 2, () -> c().mouse.cornerRadius, v -> c().mouse.cornerRadius = (float) v));
        l.add(slider("Overall opacity", 5, 100, 1, 0, () -> c().mouse.opacity * 100, v -> c().mouse.opacity = (float) (v / 100)));
        return l;
    }

    private static List<Setting> mouseColors() {
        List<Setting> l = new ArrayList<>();
        l.add(new ColorSetting("Left click", () -> c().mouse.leftColor, v -> c().mouse.leftColor = v));
        l.add(new ColorSetting("Right click", () -> c().mouse.rightColor, v -> c().mouse.rightColor = v));
        l.add(new ColorSetting("Middle click", () -> c().mouse.middleColor, v -> c().mouse.middleColor = v));
        l.add(new ColorSetting("Scroll", () -> c().mouse.scrollColor, v -> c().mouse.scrollColor = v));
        l.add(new ColorSetting("Outline color", () -> c().mouse.outlineColor, v -> c().mouse.outlineColor = v));
        l.add(new ColorSetting("Background color", () -> c().mouse.backgroundColor, v -> c().mouse.backgroundColor = v));
        l.add(new ColorSetting("Glow color", () -> c().mouse.glowColor, v -> c().mouse.glowColor = v));
        return l;
    }

    private static List<Setting> mouseStyle() {
        List<Setting> l = new ArrayList<>();
        l.add(new ToggleSetting("Background", () -> c().mouse.backgroundEnabled, v -> c().mouse.backgroundEnabled = v));
        l.add(alpha("Background opacity", () -> c().mouse.backgroundColor, v -> c().mouse.backgroundColor = v)
                .visibleWhen(() -> c().mouse.backgroundEnabled));
        l.add(new ToggleSetting("Outline", () -> c().mouse.outlineEnabled, v -> c().mouse.outlineEnabled = v));
        l.add(alpha("Outline opacity", () -> c().mouse.outlineColor, v -> c().mouse.outlineColor = v)
                .visibleWhen(() -> c().mouse.outlineEnabled));
        l.add(slider("Outline width", 0.5, 3, 0.5, 1, () -> c().mouse.outlineWidth, v -> c().mouse.outlineWidth = (float) v)
                .visibleWhen(() -> c().mouse.outlineEnabled));
        l.add(new ToggleSetting("Glow", () -> c().mouse.glowEnabled, v -> c().mouse.glowEnabled = v));
        l.add(slider("Glow strength", 0, 3, 0.1, 1, () -> c().mouse.glowStrength, v -> c().mouse.glowStrength = (float) v)
                .visibleWhen(() -> c().mouse.glowEnabled));
        return l;
    }

    private static List<Setting> animation() {
        List<Setting> l = new ArrayList<>();
        l.add(new ToggleSetting("Animations", () -> c().animation.enabled, v -> c().animation.enabled = v));
        l.add(slider("Speed", 0.25, 3, 0.05, 2, () -> c().animation.speed, v -> c().animation.speed = (float) v));
        String[] names = new String[Easing.values().length];
        for (int i = 0; i < names.length; i++) names[i] = Easing.values()[i].label();
        l.add(new ChoiceSetting("Easing", names, () -> c().animation.easing.ordinal(), v -> c().animation.easing = Easing.values()[v]));
        l.add(slider("Press depth", 0, 4, 0.1, 1, () -> c().animation.pressDepth, v -> c().animation.pressDepth = (float) v));
        l.add(slider("Glow intensity", 0, 2, 0.05, 2, () -> c().animation.glowIntensity, v -> c().animation.glowIntensity = (float) v));
        return l;
    }

    public static void resetPositions() {
        HudConfig d = new HudConfig();
        HudConfig c = c();
        c.keyboard.fullPos = d.keyboard.fullPos;
        c.keyboard.leftPos = d.keyboard.leftPos;
        c.keyboard.rightPos = d.keyboard.rightPos;
        c.mouse.pos = d.mouse.pos;
    }

    public static void resetScales() {
        HudConfig d = new HudConfig();
        c().keyboard.scale = d.keyboard.scale;
        c().keyboard.halvesScale = d.keyboard.halvesScale;
        c().mouse.scale = d.mouse.scale;
    }
}
