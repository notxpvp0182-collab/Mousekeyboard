package com.ahmed.mousekeyboardhud.gui.setting;

import com.ahmed.mousekeyboardhud.animation.Clock;
import com.ahmed.mousekeyboardhud.gui.Theme;
import com.ahmed.mousekeyboardhud.gui.Ui;
import com.ahmed.mousekeyboardhud.util.ColorUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** On/off switch with an animated knob. */
public final class ToggleSetting extends Setting {

    private final BooleanSupplier getter;
    private final Consumer<Boolean> setter;
    private float anim = -1f;

    public ToggleSetting(String name, BooleanSupplier getter, Consumer<Boolean> setter) {
        super(name);
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public int height() {
        return ROW_H;
    }

    @Override
    public void render(GuiGraphics g, Font font, Theme t, int x, int y, int w, int mx, int my) {
        boolean on = getter.getAsBoolean();
        float target = on ? 1f : 0f;
        if (anim < 0f) anim = target;
        anim += (target - anim) * (1f - (float) Math.exp(-20f * Clock.dt()));
        if (Math.abs(target - anim) < 0.003f) anim = target;

        if (Ui.hit(mx, my, x + 2, y, w - 4, ROW_H)) Ui.round(g, x + 2, y + 1, w - 4, ROW_H - 2, 4, t.hover());
        Ui.text(g, font, name, x + 10, Ui.textY(y, ROW_H), t.text());

        int sw = 24, sh = 12;
        int sx = x + w - 10 - sw;
        int sy = y + (ROW_H - sh) / 2;
        int track = ColorUtil.lerp(t.track(), t.accent(), anim);
        Ui.round(g, sx, sy, sw, sh, 6, track);
        int kx = sx + 2 + Math.round(anim * (sw - sh));
        Ui.round(g, kx, sy + 2, sh - 4, sh - 4, 4, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(int x, int y, int w, double mx, double my, int button) {
        if (button != 0 || !Ui.hit(mx, my, x + 2, y, w - 4, ROW_H)) return false;
        setter.accept(!getter.getAsBoolean());
        return true;
    }
}
