package com.ahmed.mousekeyboardhud.gui;

import com.ahmed.mousekeyboardhud.rendering.Draw;
import net.minecraft.client.gui.GuiGraphics;

/** Small 10x10 vector-style icons built from rectangles, all with the same 1px stroke weight. */
public final class Icons {

    public static final int SIZE = 10;

    private Icons() {}

    private static int c(int color) {
        return Ui.c(color);
    }

    public static void draw(GuiGraphics g, Category category, int x, int y, int color) {
        int col = c(color);
        switch (category) {
            case GENERAL -> {
                // three sliders
                for (int i = 0; i < 3; i++) {
                    int ly = y + 1 + i * 4;
                    Draw.rect(g, x, ly, SIZE, 1, col);
                    int kx = x + (i == 1 ? 6 : 2);
                    Draw.fill(g, kx, ly - 1, 3, 3, 1, col);
                }
            }
            case KEYBOARD -> {
                Draw.outline(g, x, y + 1, SIZE, 8, 2, 1, col);
                for (int i = 0; i < 3; i++) {
                    Draw.rect(g, x + 2 + i * 2, y + 3, 1, 1, col);
                    Draw.rect(g, x + 2 + i * 2, y + 5, 1, 1, col);
                }
                Draw.rect(g, x + 3, y + 7, 4, 1, col);
            }
            case MOUSE -> {
                Draw.outline(g, x + 1, y, 8, SIZE, 4, 1, col);
                Draw.rect(g, x + 5, y + 1, 1, 3, col);
                Draw.rect(g, x + 2, y + 4, 6, 1, col);
            }
            case ANIMATION -> {
                Draw.outline(g, x, y, SIZE, SIZE, 5, 1, col);
                // play triangle
                for (int i = 0; i < 5; i++) Draw.rect(g, x + 3 + (i < 3 ? 0 : 0), y + 2 + i, Math.min(i, 4 - i) + 1 + 1, 1, col);
            }
            case COLORS -> {
                Draw.outline(g, x, y, SIZE, SIZE, 5, 1, col);
                Draw.fill(g, x + 3, y + 3, 4, 4, 2, col);
            }
            case HUD -> {
                // corner brackets
                Draw.rect(g, x, y, 4, 1, col); Draw.rect(g, x, y, 1, 4, col);
                Draw.rect(g, x + 6, y, 4, 1, col); Draw.rect(g, x + 9, y, 1, 4, col);
                Draw.rect(g, x, y + 9, 4, 1, col); Draw.rect(g, x, y + 6, 1, 4, col);
                Draw.rect(g, x + 6, y + 9, 4, 1, col); Draw.rect(g, x + 9, y + 6, 1, 4, col);
                Draw.rect(g, x + 4, y + 4, 2, 2, col);
            }
            case KEYBINDS -> {
                Draw.outline(g, x, y + 1, 6, 6, 3, 1, col);
                Draw.rect(g, x + 5, y + 6, 1, 4, col);
                Draw.rect(g, x + 5, y + 8, 3, 1, col);
                Draw.rect(g, x + 5, y + 9, 2, 1, col);
            }
            case ADVANCED -> {
                for (int i = 0; i < 3; i++) Draw.fill(g, x + 1 + i * 3, y + 4, 2, 2, 1, col);
            }
        }
    }
}
