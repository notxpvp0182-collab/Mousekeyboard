package com.ahmed.mousekeyboardhud.hud;

/** The independently positionable pieces of the HUD. */
public enum HudElementId {
    KEYBOARD_FULL("Keyboard"),
    KEYBOARD_LEFT("Left Half"),
    KEYBOARD_RIGHT("Right Half"),
    MOUSE("Mouse");

    private final String label;

    HudElementId(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public boolean isKeyboard() {
        return this != MOUSE;
    }
}
