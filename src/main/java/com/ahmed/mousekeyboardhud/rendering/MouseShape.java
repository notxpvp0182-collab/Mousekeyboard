package com.ahmed.mousekeyboardhud.rendering;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Superellipse silhouette used for the mouse body. Per-row insets are cached and only rebuilt when
 * size or roundness changes, so rendering never allocates.
 */
public final class MouseShape {
    private int w = -1;
    private int h = -1;
    private float exponent = -1f;
    private int[] inset = new int[0];

    public void update(int width, int height, float exp) {
        if (width == w && height == h && exp == exponent) return;
        w = width;
        h = height;
        exponent = exp;
        if (inset.length != height) inset = new int[height];
        double half = width / 2.0;
        for (int i = 0; i < height; i++) {
            double ny = Math.abs((i + 0.5 - height / 2.0) / (height / 2.0));
            double v = 1.0 - Math.pow(ny, exp);
            double hw = half * Math.pow(Math.max(0.0, v), 1.0 / exp);
            inset[i] = Math.min(width / 2, (int) Math.round(half - hw));
        }
    }

    public int width() { return w; }
    public int height() { return h; }

    public int inset(int row) {
        return inset[Math.max(0, Math.min(h - 1, row))];
    }

    /** Fills the silhouette between rows [r0, r1) merging identical consecutive rows. */
    public void fillRows(GuiGraphics g, int x, int y, int r0, int r1, int color) {
        if ((color >>> 24) == 0) return;
        r0 = Math.max(0, r0);
        r1 = Math.min(h, r1);
        int i = r0;
        while (i < r1) {
            int j = i;
            while (j + 1 < r1 && inset[j + 1] == inset[i]) j++;
            g.fill(x + inset[i], y + i, x + w - inset[i], y + j + 1, color);
            i = j + 1;
        }
    }

    /** Fills a horizontal band [xFrom, xTo) (relative to x) of rows [r0, r1), clipped to the silhouette. */
    public void fillBand(GuiGraphics g, int x, int y, int r0, int r1, int xFrom, int xTo, int color) {
        if ((color >>> 24) == 0) return;
        r0 = Math.max(0, r0);
        r1 = Math.min(h, r1);
        int i = r0;
        while (i < r1) {
            int j = i;
            while (j + 1 < r1 && inset[j + 1] == inset[i]) j++;
            int a = Math.max(xFrom, inset[i]);
            int b = Math.min(xTo, w - inset[i]);
            if (b > a) g.fill(x + a, y + i, x + b, y + j + 1, color);
            i = j + 1;
        }
    }

    /** Outline of the silhouette, thickness t, drawn inside the bounds. */
    public void outline(GuiGraphics g, int x, int y, int t, int color) {
        if ((color >>> 24) == 0 || h <= 0) return;
        int mid = h / 2;
        int i = 0;
        while (i < h) {
            // pole rows (near top/bottom) are drawn as full spans
            int rowFromPole = i < mid ? i : h - 1 - i;
            int neighbor = i < mid ? i - 1 : i + 1;
            if (rowFromPole < t) {
                int j = i;
                while (j + 1 < h && ((j + 1 < mid ? j + 1 : h - 2 - j) < t) && inset[j + 1] == inset[i]) j++;
                g.fill(x + inset[i], y + i, x + w - inset[i], y + j + 1, color);
                i = j + 1;
                continue;
            }
            int o = inset[i];
            int end = Math.max(o + t, neighbor < 0 || neighbor >= h ? w / 2 : inset[neighbor]);
            end = Math.min(end, w / 2);
            // merge runs of rows with identical spans
            int j = i;
            while (j + 1 < h && spanEqual(j + 1, i, mid, t, end)) j++;
            g.fill(x + o, y + i, x + end, y + j + 1, color);
            g.fill(x + w - end, y + i, x + w - o, y + j + 1, color);
            i = j + 1;
        }
    }

    private boolean spanEqual(int row, int ref, int mid, int t, int refEnd) {
        int rowFromPole = row < mid ? row : h - 1 - row;
        if (rowFromPole < t) return false;
        if (inset[row] != inset[ref]) return false;
        int neighbor = row < mid ? row - 1 : row + 1;
        int end = Math.max(inset[row] + t, neighbor < 0 || neighbor >= h ? w / 2 : inset[neighbor]);
        end = Math.min(end, w / 2);
        return end == refEnd && (row < mid) == (ref < mid);
    }
}
