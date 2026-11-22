package com.ahmed.mousekeyboardhud.hud.components;

/** Mutable integer rectangle in GUI-scaled pixels (reused to avoid per-frame allocation). */
public final class Rect {
    public int x;
    public int y;
    public int w;
    public int h;

    public Rect set(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        return this;
    }

    public boolean contains(double px, double py) {
        return px >= x && px < x + w && py >= y && py < y + h;
    }

    public int right() { return x + w; }
    public int bottom() { return y + h; }
}
