package com.ahmed.mousekeyboardhud.animation;

/**
 * Frame clock shared by HUD and GUI code. tick() is idempotent within the same frame:
 * several callers in one frame all see the same delta.
 */
public final class Clock {
    private static long last = System.nanoTime();
    private static float dt = 0.016f;

    private Clock() {}

    public static float tick() {
        long now = System.nanoTime();
        long diff = now - last;
        if (diff > 500_000L) { // > 0.5 ms means a new frame
            dt = Math.min(diff / 1_000_000_000f, 0.1f);
            last = now;
        }
        return dt;
    }

    public static float dt() {
        return dt;
    }
}
