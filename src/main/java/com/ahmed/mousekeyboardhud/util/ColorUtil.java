package com.ahmed.mousekeyboardhud.util;

/** Allocation-free ARGB helpers. */
public final class ColorUtil {
    private ColorUtil() {}

    public static int alpha(int c) { return c >>> 24; }
    public static int red(int c) { return (c >> 16) & 0xFF; }
    public static int green(int c) { return (c >> 8) & 0xFF; }
    public static int blue(int c) { return c & 0xFF; }

    public static int argb(int a, int r, int g, int b) {
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    public static int withAlpha(int c, int a) {
        return (a & 0xFF) << 24 | (c & 0x00FFFFFF);
    }

    /** Multiplies the alpha channel by f (0..1). */
    public static int mulAlpha(int c, float f) {
        int a = Math.round(alpha(c) * MathUtil.clamp(f, 0f, 1f));
        return withAlpha(c, a);
    }

    public static int lerp(int a, int b, float t) {
        if (t <= 0f) return a;
        if (t >= 1f) return b;
        int ar = alpha(a), br = alpha(b);
        return argb(
                Math.round(ar + (br - ar) * t),
                Math.round(red(a) + (red(b) - red(a)) * t),
                Math.round(green(a) + (green(b) - green(a)) * t),
                Math.round(blue(a) + (blue(b) - blue(a)) * t));
    }

    /** h, s, v in 0..1; returns opaque RGB. */
    public static int hsv(float h, float s, float v) {
        h = h - (float) Math.floor(h);
        float c = v * s;
        float hh = h * 6f;
        float x = c * (1f - Math.abs(hh % 2f - 1f));
        float m = v - c;
        float r, g, b;
        if (hh < 1f) { r = c; g = x; b = 0; }
        else if (hh < 2f) { r = x; g = c; b = 0; }
        else if (hh < 3f) { r = 0; g = c; b = x; }
        else if (hh < 4f) { r = 0; g = x; b = c; }
        else if (hh < 5f) { r = x; g = 0; b = c; }
        else { r = c; g = 0; b = x; }
        return argb(255, Math.round((r + m) * 255f), Math.round((g + m) * 255f), Math.round((b + m) * 255f));
    }

    /** Writes h,s,v (0..1) of the color into out[0..2]. */
    public static void toHsv(int c, float[] out) {
        float r = red(c) / 255f, g = green(c) / 255f, b = blue(c) / 255f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float d = max - min;
        float h;
        if (d == 0f) h = 0f;
        else if (max == r) h = ((g - b) / d % 6f) / 6f;
        else if (max == g) h = ((b - r) / d + 2f) / 6f;
        else h = ((r - g) / d + 4f) / 6f;
        if (h < 0f) h += 1f;
        out[0] = h;
        out[1] = max == 0f ? 0f : d / max;
        out[2] = max;
    }

    /** Formats as RRGGBBAA (upper case, no '#'). */
    public static String toHex(int argb) {
        return String.format("%02X%02X%02X%02X", red(argb), green(argb), blue(argb), alpha(argb));
    }

    /** Parses RRGGBB or RRGGBBAA (optional '#'); returns defaultValue-alpha-preserving result or null if invalid. */
    public static Integer parseHex(String text, int currentAlphaSource) {
        String s = text.startsWith("#") ? text.substring(1) : text;
        try {
            if (s.length() == 6) {
                int rgb = Integer.parseInt(s, 16);
                return withAlpha(rgb, alpha(currentAlphaSource));
            } else if (s.length() == 8) {
                long v = Long.parseLong(s, 16);
                int r = (int) (v >> 24) & 0xFF, g = (int) (v >> 16) & 0xFF, b = (int) (v >> 8) & 0xFF, a = (int) v & 0xFF;
                return argb(a, r, g, b);
            }
        } catch (NumberFormatException ignored) {
            // fall through
        }
        return null;
    }
}
