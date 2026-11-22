package com.ahmed.mousekeyboardhud.gui;

import com.ahmed.mousekeyboardhud.rendering.Draw;
import com.ahmed.mousekeyboardhud.util.ColorUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Thin drawing layer for GUI widgets; applies one global fade so open/close animations affect everything. */
public final class Ui {

    /** 0..1 multiplier applied to every color drawn through this class. */
    public static float fade = 1f;

    private Ui() {}

    public static int c(int color) {
        return fade >= 0.999f ? color : ColorUtil.mulAlpha(color, fade);
    }

    public static void rect(GuiGraphics g, int x, int y, int w, int h, int color) {
        Draw.rect(g, x, y, w, h, c(color));
    }

    public static void round(GuiGraphics g, int x, int y, int w, int h, int r, int color) {
        Draw.fill(g, x, y, w, h, r, c(color));
    }

    public static void border(GuiGraphics g, int x, int y, int w, int h, int r, int color) {
        Draw.outline(g, x, y, w, h, r, 1, c(color));
    }

    public static void gradient(GuiGraphics g, int x, int y, int w, int h, int top, int bottom) {
        Draw.gradientV(g, x, y, w, h, c(top), c(bottom));
    }

    public static void text(GuiGraphics g, Font font, String s, int x, int y, int color) {
        int col = c(color);
        if ((col >>> 24) == 0) return;
        g.drawString(font, s, x, y, col, false);
    }

    public static void textCentered(GuiGraphics g, Font font, String s, int cx, int y, int color) {
        text(g, font, s, cx - font.width(s) / 2, y, color);
    }

    public static void textRight(GuiGraphics g, Font font, String s, int right, int y, int color) {
        text(g, font, s, right - font.width(s), y, color);
    }

    /** Vertically centers 8px text inside a box starting at y with height h. */
    public static int textY(int y, int h) {
        return y + (h - 8) / 2;
    }

    /** Glass panel: translucent body, soft top highlight, hairline border. */
    public static void glassPanel(GuiGraphics g, Theme t, int x, int y, int w, int h) {
        round(g, x, y, w, h, t.radius() + 2, t.panel());
        gradient(g, x + 2, y + 1, w - 4, Math.min(h / 3, 40), 0x12FFFFFF, 0x00FFFFFF);
        Draw.outline(g, x, y, w, h, t.radius() + 2, 1, c(t.border()));
    }

    public static boolean hit(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
