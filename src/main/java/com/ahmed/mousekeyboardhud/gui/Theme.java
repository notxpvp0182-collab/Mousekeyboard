package com.ahmed.mousekeyboardhud.gui;

/** Immutable set of GUI colors and metrics. Swap the active one via {@link ThemeManager}. */
public record Theme(String name, int backdrop, int panel, int panelHigh, int border, int text, int textDim,
                    int accent, int accentSoft, int hover, int track, int radius) {
}
