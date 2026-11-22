package com.ahmed.mousekeyboardhud.gui.setting;

import com.ahmed.mousekeyboardhud.gui.Theme;
import com.ahmed.mousekeyboardhud.gui.Ui;
import com.ahmed.mousekeyboardhud.rendering.Draw;
import com.ahmed.mousekeyboardhud.util.ColorUtil;
import com.ahmed.mousekeyboardhud.util.MathUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * ARGB color row with an inline picker: saturation/value square, hue bar, alpha bar,
 * R/G/B sliders and a HEX (RRGGBBAA) field.
 */
public final class ColorSetting extends Setting {

    private enum Drag { NONE, SV, HUE, ALPHA, R, G, B }

    private static final int SV_H = 52;
    private static final int BAR_H = 8;
    private static final int PICKER_H = 4 + SV_H + 5 + BAR_H + 5 + BAR_H + 5 + 3 * (BAR_H + 3) + 4 + 18 + 6;

    private final IntSupplier getter;
    private final IntConsumer setter;

    private boolean open;
    private Drag drag = Drag.NONE;
    private final float[] hsv = new float[3];
    private boolean editingHex;
    private final StringBuilder hexBuffer = new StringBuilder();

    public ColorSetting(String name, IntSupplier getter, IntConsumer setter) {
        super(name);
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public int height() {
        return open ? ROW_H + PICKER_H : ROW_H;
    }

    private void syncHsv() {
        ColorUtil.toHsv(getter.getAsInt(), hsv);
    }

    // ---- geometry (all relative to the picker's top-left) ----------------------------------------
    private int px(int x) { return x + 10; }
    private int pw(int w) { return w - 20; }
    private int svY(int y) { return y + ROW_H + 4; }
    private int hueY(int y) { return svY(y) + SV_H + 5; }
    private int alphaY(int y) { return hueY(y) + BAR_H + 5; }
    private int rgbY(int y, int i) { return alphaY(y) + BAR_H + 5 + i * (BAR_H + 3); }
    private int hexY(int y) { return rgbY(y, 3) + 4; }

    @Override
    public void render(GuiGraphics g, Font font, Theme t, int x, int y, int w, int mx, int my) {
        int color = getter.getAsInt();
        if (Ui.hit(mx, my, x + 2, y, w - 4, ROW_H)) Ui.round(g, x + 2, y + 1, w - 4, ROW_H - 2, 4, t.hover());
        Ui.text(g, font, name, x + 10, Ui.textY(y, ROW_H), t.text());

        int sx = x + w - 10 - 34;
        Ui.round(g, sx, y + 4, 34, ROW_H - 8, 4, 0xFF1A1D26);
        Ui.round(g, sx, y + 4, 34, ROW_H - 8, 4, color);
        Ui.border(g, sx, y + 4, 34, ROW_H - 8, 4, open ? t.accent() : t.border());

        if (!open) return;
        int left = px(x);
        int width = pw(w);
        Ui.round(g, x + 4, y + ROW_H, w - 8, PICKER_H - 2, 5, t.panelHigh());

        // saturation / value square: horizontal columns of vertical gradients
        int sy = svY(y);
        int cols = Math.min(width, 48);
        for (int i = 0; i < cols; i++) {
            int cx0 = left + i * width / cols;
            int cx1 = left + (i + 1) * width / cols;
            int top = ColorUtil.hsv(hsv[0], i / (float) (cols - 1), 1f);
            Ui.gradient(g, cx0, sy, cx1 - cx0, SV_H, top, 0xFF000000);
        }
        int kx = left + Math.round(hsv[1] * (width - 1));
        int ky = sy + Math.round((1f - hsv[2]) * (SV_H - 1));
        Draw.outline(g, kx - 3, ky - 3, 7, 7, 3, 1, Ui.c(0xFFFFFFFF));

        // hue bar
        int hy = hueY(y);
        for (int i = 0; i < 36; i++) {
            int cx0 = left + i * width / 36;
            int cx1 = left + (i + 1) * width / 36;
            Ui.rect(g, cx0, hy, cx1 - cx0, BAR_H, ColorUtil.hsv(i / 36f, 1f, 1f));
        }
        marker(g, left + Math.round(hsv[0] * (width - 1)), hy);

        // alpha bar
        int ay = alphaY(y);
        Ui.rect(g, left, ay, width, BAR_H, 0xFF14161D);
        int opaque = ColorUtil.withAlpha(color, 255);
        for (int i = 0; i < 36; i++) {
            int cx0 = left + i * width / 36;
            int cx1 = left + (i + 1) * width / 36;
            Ui.rect(g, cx0, ay, cx1 - cx0, BAR_H, ColorUtil.mulAlpha(opaque, i / 35f));
        }
        marker(g, left + Math.round(ColorUtil.alpha(color) / 255f * (width - 1)), ay);

        // RGB sliders
        int[] ch = {ColorUtil.red(color), ColorUtil.green(color), ColorUtil.blue(color)};
        String[] labels = {"R", "G", "B"};
        int[] tints = {0xFFE5534B, 0xFF4FBF73, 0xFF4D8DFF};
        for (int i = 0; i < 3; i++) {
            int ry = rgbY(y, i);
            Ui.text(g, font, labels[i], left, ry, t.textDim());
            int tx = left + 12;
            int tw = width - 12 - 28;
            Ui.round(g, tx, ry + 2, tw, 4, 2, t.track());
            Ui.round(g, tx, ry + 2, Math.max(2, Math.round(tw * ch[i] / 255f)), 4, 2, tints[i]);
            Ui.round(g, tx + Math.round((tw - 6) * ch[i] / 255f), ry, 6, 8, 3, 0xFFFFFFFF);
            Ui.textRight(g, font, Integer.toString(ch[i]), left + width, ry, t.textDim());
        }

        // HEX field
        int fy = hexY(y);
        Ui.round(g, left, fy, width, 18, 4, 0x33000000);
        Ui.border(g, left, fy, width, 18, 4, editingHex ? t.accent() : t.border());
        String shown = editingHex ? hexBuffer.toString() : ColorUtil.toHex(color);
        Ui.text(g, font, "#", left + 6, Ui.textY(fy, 18), t.textDim());
        Ui.text(g, font, shown, left + 14, Ui.textY(fy, 18), t.text());
        if (editingHex && (System.currentTimeMillis() / 450) % 2 == 0) {
            Ui.rect(g, left + 14 + font.width(shown), fy + 4, 1, 10, t.text());
        }
        Ui.textRight(g, font, "RRGGBBAA", left + width - 6, Ui.textY(fy, 18), t.textDim());
    }

    private void marker(GuiGraphics g, int mx, int y) {
        Ui.round(g, mx - 2, y - 1, 5, BAR_H + 2, 2, 0xFFFFFFFF);
    }

    private void commit(int argb) {
        setter.accept(argb);
    }

    private void applyDrag(Drag d, int x, int y, int w, double mx, double my) {
        int left = px(x);
        int width = pw(w);
        int color = getter.getAsInt();
        int a = ColorUtil.alpha(color);
        switch (d) {
            case SV -> {
                hsv[1] = MathUtil.clamp((float) ((mx - left) / Math.max(1, width - 1)), 0f, 1f);
                hsv[2] = 1f - MathUtil.clamp((float) ((my - svY(y)) / (SV_H - 1)), 0f, 1f);
                commit(ColorUtil.withAlpha(ColorUtil.hsv(hsv[0], hsv[1], hsv[2]), a));
            }
            case HUE -> {
                hsv[0] = MathUtil.clamp((float) ((mx - left) / Math.max(1, width - 1)), 0f, 0.999f);
                commit(ColorUtil.withAlpha(ColorUtil.hsv(hsv[0], hsv[1], hsv[2]), a));
            }
            case ALPHA -> {
                float f = MathUtil.clamp((float) ((mx - left) / Math.max(1, width - 1)), 0f, 1f);
                commit(ColorUtil.withAlpha(color, Math.round(f * 255f)));
            }
            case R, G, B -> {
                int tx = left + 12;
                int tw = width - 12 - 28;
                int v = Math.round(MathUtil.clamp((float) ((mx - tx - 3) / Math.max(1, tw - 6)), 0f, 1f) * 255f);
                int r = ColorUtil.red(color), gr = ColorUtil.green(color), b = ColorUtil.blue(color);
                if (d == Drag.R) r = v; else if (d == Drag.G) gr = v; else b = v;
                int next = ColorUtil.argb(a, r, gr, b);
                commit(next);
                ColorUtil.toHsv(next, hsv);
            }
            default -> { }
        }
    }

    @Override
    public boolean mouseClicked(int x, int y, int w, double mx, double my, int button) {
        if (button != 0) return false;
        if (Ui.hit(mx, my, x + 2, y, w - 4, ROW_H)) {
            open = !open;
            if (open) syncHsv();
            editingHex = false;
            return true;
        }
        if (!open) return false;
        int left = px(x);
        int width = pw(w);
        editingHex = false;
        if (Ui.hit(mx, my, left, svY(y), width, SV_H)) {
            drag = Drag.SV;
        } else if (Ui.hit(mx, my, left, hueY(y) - 2, width, BAR_H + 4)) {
            drag = Drag.HUE;
        } else if (Ui.hit(mx, my, left, alphaY(y) - 2, width, BAR_H + 4)) {
            drag = Drag.ALPHA;
        } else if (Ui.hit(mx, my, left, rgbY(y, 0) - 1, width, 3 * (BAR_H + 3))) {
            int idx = (int) ((my - rgbY(y, 0)) / (BAR_H + 3));
            drag = idx == 0 ? Drag.R : idx == 1 ? Drag.G : Drag.B;
        } else if (Ui.hit(mx, my, left, hexY(y), width, 18)) {
            editingHex = true;
            hexBuffer.setLength(0);
            hexBuffer.append(ColorUtil.toHex(getter.getAsInt()));
            return true;
        } else {
            return Ui.hit(mx, my, x + 4, y + ROW_H, w - 8, PICKER_H);
        }
        applyDrag(drag, x, y, w, mx, my);
        return true;
    }

    @Override
    public void mouseDragged(int x, int y, int w, double mx, double my) {
        if (drag != Drag.NONE) applyDrag(drag, x, y, w, mx, my);
    }

    @Override
    public void mouseReleased() {
        drag = Drag.NONE;
    }

    @Override
    public boolean keyPressed(int key) {
        if (!editingHex) return false;
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            Integer parsed = ColorUtil.parseHex(hexBuffer.toString(), getter.getAsInt());
            if (parsed != null) {
                commit(parsed);
                syncHsv();
            }
            editingHex = false;
        } else if (key == GLFW.GLFW_KEY_ESCAPE) {
            editingHex = false;
        } else if (key == GLFW.GLFW_KEY_BACKSPACE && hexBuffer.length() > 0) {
            hexBuffer.setLength(hexBuffer.length() - 1);
        }
        return true;
    }

    @Override
    public boolean charTyped(int codepoint) {
        if (!editingHex) return false;
        char ch = (char) codepoint;
        if (Character.digit(ch, 16) >= 0 && hexBuffer.length() < 8) {
            hexBuffer.append(Character.toUpperCase(ch));
        }
        return true;
    }

    @Override
    public boolean capturesKeyboard() {
        return editingHex;
    }

    @Override
    public void unfocus() {
        editingHex = false;
        drag = Drag.NONE;
    }
}
