package com.ahmed.mousekeyboardhud.util;

public final class MathUtil {
    private MathUtil() {}

    public static float clamp(float v, float min, float max) {
        return v < min ? min : Math.min(v, max);
    }

    public static int clamp(int v, int min, int max) {
        return v < min ? min : Math.min(v, max);
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static boolean inside(double px, double py, int x, int y, int w, int h) {
        return px >= x && px < x + w && py >= y && py < y + h;
    }
}
