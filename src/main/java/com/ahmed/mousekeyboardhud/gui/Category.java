package com.ahmed.mousekeyboardhud.gui;

/** ClickGUI sidebar categories. */
public enum Category {
    GENERAL("General"),
    KEYBOARD("Keyboard"),
    MOUSE("Mouse"),
    ANIMATION("Animation"),
    COLORS("Colors"),
    HUD("HUD"),
    KEYBINDS("Keybinds"),
    ADVANCED("Advanced");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
