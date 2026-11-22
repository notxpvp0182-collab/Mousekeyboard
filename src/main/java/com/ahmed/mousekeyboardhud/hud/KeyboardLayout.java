package com.ahmed.mousekeyboardhud.hud;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Static, realistic 15-unit-wide keyboard layout following the reference image.
 * Three views are derived from one key list: the full board and its left/right halves.
 */
public final class KeyboardLayout {

    /** A set of keys with the bounding box (in key units) they occupy. */
    public static final class View {
        public final KeyDef[] keys;
        public final float minX;
        public final float minY;
        public final float maxX;
        public final float maxY;

        View(List<KeyDef> list) {
            this.keys = list.toArray(new KeyDef[0]);
            float x0 = Float.MAX_VALUE, y0 = Float.MAX_VALUE, x1 = 0, y1 = 0;
            for (KeyDef k : keys) {
                x0 = Math.min(x0, k.x);
                y0 = Math.min(y0, k.y);
                x1 = Math.max(x1, k.x + k.w);
                y1 = Math.max(y1, k.y + k.h);
            }
            this.minX = x0;
            this.minY = y0;
            this.maxX = x1;
            this.maxY = y1;
        }

        public float widthUnits() { return maxX - minX; }
        public float heightUnits() { return maxY - minY; }
    }

    public static final View FULL;
    public static final View LEFT;
    public static final View RIGHT;

    private static final float ROW_GAP = 0.2f;

    static {
        List<KeyDef> all = new ArrayList<>();
        buildRows(all);
        List<KeyDef> full = new ArrayList<>();
        List<KeyDef> left = new ArrayList<>();
        List<KeyDef> right = new ArrayList<>();
        for (KeyDef k : all) {
            if (k.mode != KeyDef.Mode.SPLIT_ONLY) full.add(k);
            if (k.mode != KeyDef.Mode.FULL_ONLY) {
                (k.half == KeyDef.Half.LEFT ? left : right).add(k);
            }
        }
        FULL = new View(full);
        LEFT = new View(left);
        RIGHT = new View(right);
    }

    private KeyboardLayout() {}

    public static View view(HudElementId id) {
        return switch (id) {
            case KEYBOARD_LEFT -> LEFT;
            case KEYBOARD_RIGHT -> RIGHT;
            default -> FULL;
        };
    }

    private record Spec(String top, String label, int code, float w) {}

    private static Spec k(String top, String label, int code, float w) {
        return new Spec(top, label, code, w);
    }

    private static Spec k(String label, int code, float w) {
        return new Spec(null, label, code, w);
    }

    private static Spec gap(float w) {
        return new Spec(null, null, -1, w);
    }

    /** Adds a row; keys with index >= rightFrom (spacers not counted) belong to the right half. */
    private static void row(List<KeyDef> out, float y, int rightFrom, Spec... specs) {
        float x = 0;
        int index = 0;
        for (Spec s : specs) {
            if (s.label() == null) {
                x += s.w();
                continue;
            }
            KeyDef.Half half = index >= rightFrom ? KeyDef.Half.RIGHT : KeyDef.Half.LEFT;
            out.add(new KeyDef(s.top(), s.label(), s.code(), x, y, s.w(), 1f, half, KeyDef.Mode.BOTH));
            x += s.w();
            index++;
        }
    }

    private static void buildRows(List<KeyDef> out) {
        float fw = 0.9f;
        row(out, 0f, 7,
                k("ESC", GLFW.GLFW_KEY_ESCAPE, 1f), gap(0.5f),
                k("F1", GLFW.GLFW_KEY_F1, fw), k("F2", GLFW.GLFW_KEY_F2, fw), k("F3", GLFW.GLFW_KEY_F3, fw),
                k("F4", GLFW.GLFW_KEY_F4, fw), k("F5", GLFW.GLFW_KEY_F5, fw), k("F6", GLFW.GLFW_KEY_F6, fw),
                k("F7", GLFW.GLFW_KEY_F7, fw), k("F8", GLFW.GLFW_KEY_F8, fw), k("F9", GLFW.GLFW_KEY_F9, fw),
                k("F10", GLFW.GLFW_KEY_F10, fw), k("F11", GLFW.GLFW_KEY_F11, fw), k("F12", GLFW.GLFW_KEY_F12, fw),
                gap(0.5f),
                k("PRT SCR", GLFW.GLFW_KEY_PRINT_SCREEN, 1.1f), k("PAU", GLFW.GLFW_KEY_PAUSE, 1.1f));

        float y1 = 1f + ROW_GAP;
        row(out, y1, 6,
                k("~", "`", GLFW.GLFW_KEY_GRAVE_ACCENT, 1f),
                k("!", "1", GLFW.GLFW_KEY_1, 1f), k("@", "2", GLFW.GLFW_KEY_2, 1f), k("#", "3", GLFW.GLFW_KEY_3, 1f),
                k("$", "4", GLFW.GLFW_KEY_4, 1f), k("%", "5", GLFW.GLFW_KEY_5, 1f), k("^", "6", GLFW.GLFW_KEY_6, 1f),
                k("&", "7", GLFW.GLFW_KEY_7, 1f), k("*", "8", GLFW.GLFW_KEY_8, 1f), k("(", "9", GLFW.GLFW_KEY_9, 1f),
                k(")", "0", GLFW.GLFW_KEY_0, 1f), k("_", "-", GLFW.GLFW_KEY_MINUS, 1f), k("+", "=", GLFW.GLFW_KEY_EQUAL, 1f),
                k("BACKSPACE", GLFW.GLFW_KEY_BACKSPACE, 2f));

        row(out, y1 + 1f, 6,
                k("TAB", GLFW.GLFW_KEY_TAB, 1.5f),
                k("Q", GLFW.GLFW_KEY_Q, 1f), k("W", GLFW.GLFW_KEY_W, 1f), k("E", GLFW.GLFW_KEY_E, 1f),
                k("R", GLFW.GLFW_KEY_R, 1f), k("T", GLFW.GLFW_KEY_T, 1f), k("Y", GLFW.GLFW_KEY_Y, 1f),
                k("U", GLFW.GLFW_KEY_U, 1f), k("I", GLFW.GLFW_KEY_I, 1f), k("O", GLFW.GLFW_KEY_O, 1f),
                k("P", GLFW.GLFW_KEY_P, 1f),
                k("{", "[", GLFW.GLFW_KEY_LEFT_BRACKET, 1f), k("}", "]", GLFW.GLFW_KEY_RIGHT_BRACKET, 1f),
                k("|", "\\", GLFW.GLFW_KEY_BACKSLASH, 1.5f));

        row(out, y1 + 2f, 6,
                k("CAPS", GLFW.GLFW_KEY_CAPS_LOCK, 1.75f),
                k("A", GLFW.GLFW_KEY_A, 1f), k("S", GLFW.GLFW_KEY_S, 1f), k("D", GLFW.GLFW_KEY_D, 1f),
                k("F", GLFW.GLFW_KEY_F, 1f), k("G", GLFW.GLFW_KEY_G, 1f), k("H", GLFW.GLFW_KEY_H, 1f),
                k("J", GLFW.GLFW_KEY_J, 1f), k("K", GLFW.GLFW_KEY_K, 1f), k("L", GLFW.GLFW_KEY_L, 1f),
                k(":", ";", GLFW.GLFW_KEY_SEMICOLON, 1f), k("\"", "'", GLFW.GLFW_KEY_APOSTROPHE, 1f),
                k("ENTER", GLFW.GLFW_KEY_ENTER, 2.25f));

        row(out, y1 + 3f, 6,
                k("SHIFT", GLFW.GLFW_KEY_LEFT_SHIFT, 2.25f),
                k("Z", GLFW.GLFW_KEY_Z, 1f), k("X", GLFW.GLFW_KEY_X, 1f), k("C", GLFW.GLFW_KEY_C, 1f),
                k("V", GLFW.GLFW_KEY_V, 1f), k("B", GLFW.GLFW_KEY_B, 1f), k("N", GLFW.GLFW_KEY_N, 1f),
                k("M", GLFW.GLFW_KEY_M, 1f),
                k("<", ",", GLFW.GLFW_KEY_COMMA, 1f), k(">", ".", GLFW.GLFW_KEY_PERIOD, 1f),
                k("?", "/", GLFW.GLFW_KEY_SLASH, 1f),
                k("SHIFT", GLFW.GLFW_KEY_RIGHT_SHIFT, 2.75f));

        // Bottom row is written by hand: the space bar exists once in the full layout and as two pieces when split.
        float y5 = y1 + 4f;
        float cw = 1.25f;
        KeyDef.Half l = KeyDef.Half.LEFT;
        KeyDef.Half r = KeyDef.Half.RIGHT;
        KeyDef.Mode both = KeyDef.Mode.BOTH;
        out.add(new KeyDef(null, "CTRL", GLFW.GLFW_KEY_LEFT_CONTROL, 0f, y5, cw, 1f, l, both));
        out.add(new KeyDef(null, "WIN", GLFW.GLFW_KEY_LEFT_SUPER, cw, y5, cw, 1f, l, both));
        out.add(new KeyDef(null, "ALT", GLFW.GLFW_KEY_LEFT_ALT, cw * 2, y5, cw, 1f, l, both));
        out.add(new KeyDef(null, "\u2014", GLFW.GLFW_KEY_SPACE, cw * 3, y5, 6.25f, 1f, l, KeyDef.Mode.FULL_ONLY));
        out.add(new KeyDef(null, "\u2014", GLFW.GLFW_KEY_SPACE, cw * 3, y5, 3.75f, 1f, l, KeyDef.Mode.SPLIT_ONLY));
        out.add(new KeyDef(null, "\u2014", GLFW.GLFW_KEY_SPACE, 7.5f, y5, 2.5f, 1f, r, KeyDef.Mode.SPLIT_ONLY));
        out.add(new KeyDef(null, "ALT", GLFW.GLFW_KEY_RIGHT_ALT, 10f, y5, cw, 1f, r, both));
        // Fn is handled in keyboard firmware and has no GLFW key code; it is drawn but never lights up.
        out.add(new KeyDef(null, "FN", -1, 10f + cw, y5, cw, 1f, r, both));
        out.add(new KeyDef(null, "MENU", GLFW.GLFW_KEY_MENU, 10f + cw * 2, y5, cw, 1f, r, both));
        out.add(new KeyDef(null, "CTRL", GLFW.GLFW_KEY_RIGHT_CONTROL, 10f + cw * 3, y5, cw, 1f, r, both));
    }
}
