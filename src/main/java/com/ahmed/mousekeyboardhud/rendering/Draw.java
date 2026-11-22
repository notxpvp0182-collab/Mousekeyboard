package com.ahmed.mousekeyboardhud.rendering;

import com.ahmed.mousekeyboardhud.util.ColorUtil;
import net.minecraft.client.gui.GuiGraphics;

/**
 * 2D shape primitives built on {@link GuiGraphics#fill}. Rounded and chamfered corners are produced from
 * cached per-row inset tables and merged into runs, so a rounded key costs only a handful of quads.
 */
public final class Draw {
    private static final int MAX_R = 48;
    private static final int[][] ROUND = new int[MAX_R + 1][];
    private static final int[][] CHAMFER = new int[MAX_R + 1][];

    private Draw() {}

    private static int[] table(int r, boolean chamfer) {
        int[][] cache = chamfer ? CHAMFER : ROUND;
        int[] t = cache[r];
        if (t == null) {
            t = new int[r];
            for (int i = 0; i < r; i++) {
                if (chamfer) {
                    t[i] = Math.max(0, r - i - 1);
                } else {
                    double dy = r - i - 0.5;
                    double dx = Math.sqrt(Math.max(0.0, (double) r * r - dy * dy));
                    t[i] = Math.max(0, (int) Math.round(r - dx));
                }
            }
            cache[r] = t;
        }
        return t;
    }

    private static boolean invisible(int color) {
        return (color >>> 24) == 0;
    }

    public static void rect(GuiGraphics g, int x, int y, int w, int h, int color) {
        if (invisible(color) || w <= 0 || h <= 0) return;
        g.fill(x, y, x + w, y + h, color);
    }

    public static void fill(GuiGraphics g, int x, int y, int w, int h, int r, int color) {
        fill(g, x, y, w, h, r, color, false);
    }

    /** Filled rounded (or chamfered) rectangle. */
    public static void fill(GuiGraphics g, int x, int y, int w, int h, int r, int color, boolean chamfer) {
        if (invisible(color) || w <= 0 || h <= 0) return;
        r = Math.min(Math.min(r, MAX_R), Math.min(w, h) / 2);
        if (r <= 0) {
            g.fill(x, y, x + w, y + h, color);
            return;
        }
        int[] t = table(r, chamfer);
        int i = 0;
        while (i < r) {
            int j = i;
            while (j + 1 < r && t[j + 1] == t[i]) j++;
            int in = t[i];
            g.fill(x + in, y + i, x + w - in, y + j + 1, color);
            g.fill(x + in, y + h - j - 1, x + w - in, y + h - i, color);
            i = j + 1;
        }
        if (h - 2 * r > 0) g.fill(x, y + r, x + w, y + h - r, color);
    }

    public static void outline(GuiGraphics g, int x, int y, int w, int h, int r, int thickness, int color) {
        outline(g, x, y, w, h, r, thickness, color, false);
    }

    /** Rounded (or chamfered) outline of the given thickness, drawn inside the rectangle. */
    public static void outline(GuiGraphics g, int x, int y, int w, int h, int r, int thickness, int color, boolean chamfer) {
        if (invisible(color) || w <= 0 || h <= 0) return;
        int t = Math.max(1, thickness);
        r = Math.min(Math.min(r, MAX_R), Math.min(w, h) / 2);
        if (r <= 0) {
            g.fill(x, y, x + w, y + t, color);
            g.fill(x, y + h - t, x + w, y + h, color);
            if (h - 2 * t > 0) {
                g.fill(x, y + t, x + t, y + h - t, color);
                g.fill(x + w - t, y + t, x + w, y + h - t, color);
            }
            return;
        }
        g.fill(x + r, y, x + w - r, y + t, color);
        g.fill(x + r, y + h - t, x + w - r, y + h, color);
        if (h - 2 * r > 0) {
            g.fill(x, y + r, x + t, y + h - r, color);
            g.fill(x + w - t, y + r, x + w, y + h - r, color);
        }
        int[] tab = table(r, chamfer);
        for (int i = 0; i < r; i++) {
            int o = tab[i];
            int prev = i == 0 ? r : tab[i - 1];
            int end = Math.max(prev, o + t);
            if (i < t) end = Math.min(end, r);
            if (end <= o) continue;
            g.fill(x + o, y + i, x + end, y + i + 1, color);
            g.fill(x + w - end, y + i, x + w - o, y + i + 1, color);
            g.fill(x + o, y + h - 1 - i, x + end, y + h - i, color);
            g.fill(x + w - end, y + h - 1 - i, x + w - o, y + h - i, color);
        }
    }

    /**
     * Soft glow: several expanding outlines with quadratically fading alpha. {@code spread} in pixels.
     */
    public static void glow(GuiGraphics g, int x, int y, int w, int h, int r, int spread, int color, boolean chamfer) {
        if (invisible(color) || spread <= 0) return;
        for (int k = 1; k <= spread; k++) {
            float f = 1f - (k - 1f) / spread;
            int c = ColorUtil.mulAlpha(color, f * f * 0.55f);
            outline(g, x - k, y - k, w + 2 * k, h + 2 * k, r + k, 1, c, chamfer);
        }
    }

    public static void gradientV(GuiGraphics g, int x, int y, int w, int h, int top, int bottom) {
        if (w <= 0 || h <= 0) return;
        g.fillGradient(x, y, x + w, y + h, top, bottom);
    }
}
