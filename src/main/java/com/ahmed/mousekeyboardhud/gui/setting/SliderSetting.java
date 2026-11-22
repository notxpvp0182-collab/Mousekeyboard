package com.ahmed.mousekeyboardhud.gui.setting;

import com.ahmed.mousekeyboardhud.gui.Theme;
import com.ahmed.mousekeyboardhud.gui.Ui;
import com.ahmed.mousekeyboardhud.util.MathUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Locale;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

/** Horizontal slider with a value readout. */
public final class SliderSetting extends Setting {

    private final double min;
    private final double max;
    private final double step;
    private final int decimals;
    private final String suffix;
    private final DoubleSupplier getter;
    private final DoubleConsumer setter;

    private double cachedValue = Double.NaN;
    private String cachedText = "";

    public SliderSetting(String name, double min, double max, double step, int decimals, String suffix,
                         DoubleSupplier getter, DoubleConsumer setter) {
        super(name);
        this.min = min;
        this.max = max;
        this.step = step;
        this.decimals = decimals;
        this.suffix = suffix;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public int height() {
        return ROW_H;
    }

    private int trackX(int x, int w) {
        return x + (int) (w * 0.44f);
    }

    private int trackW(int x, int w) {
        return x + w - 52 - trackX(x, w);
    }

    @Override
    public void render(GuiGraphics g, Font font, Theme t, int x, int y, int w, int mx, int my) {
        double v = getter.getAsDouble();
        if (v != cachedValue) {
            cachedValue = v;
            cachedText = String.format(Locale.ROOT, "%." + decimals + "f%s", v, suffix);
        }
        if (Ui.hit(mx, my, x + 2, y, w - 4, ROW_H)) Ui.round(g, x + 2, y + 1, w - 4, ROW_H - 2, 4, t.hover());
        Ui.text(g, font, name, x + 10, Ui.textY(y, ROW_H), t.text());

        int tx = trackX(x, w);
        int tw = trackW(x, w);
        int ty = y + ROW_H / 2 - 2;
        float f = (float) MathUtil.clamp((float) ((v - min) / (max - min)), 0f, 1f);
        Ui.round(g, tx, ty, tw, 4, 2, t.track());
        int fw = Math.max(4, Math.round(tw * f));
        Ui.round(g, tx, ty, fw, 4, 2, t.accent());
        Ui.round(g, tx + Math.round((tw - 8) * f), y + ROW_H / 2 - 4, 8, 8, 4, 0xFFFFFFFF);
        Ui.textRight(g, font, cachedText, x + w - 10, Ui.textY(y, ROW_H), t.textDim());
    }

    private void applyAt(int x, int w, double mx) {
        int tx = trackX(x, w);
        int tw = trackW(x, w);
        double f = MathUtil.clamp((float) ((mx - tx - 4) / (double) Math.max(1, tw - 8)), 0f, 1f);
        double v = min + f * (max - min);
        if (step > 0) v = Math.round(v / step) * step;
        setter.accept(Math.max(min, Math.min(max, v)));
    }

    @Override
    public boolean mouseClicked(int x, int y, int w, double mx, double my, int button) {
        if (button != 0 || !Ui.hit(mx, my, trackX(x, w) - 6, y, trackW(x, w) + 12, ROW_H)) return false;
        applyAt(x, w, mx);
        return true;
    }

    @Override
    public void mouseDragged(int x, int y, int w, double mx, double my) {
        applyAt(x, w, mx);
    }
}
