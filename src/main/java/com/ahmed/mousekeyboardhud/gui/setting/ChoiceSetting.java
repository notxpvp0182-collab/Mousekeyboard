package com.ahmed.mousekeyboardhud.gui.setting;

import com.ahmed.mousekeyboardhud.gui.Theme;
import com.ahmed.mousekeyboardhud.gui.Ui;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** Dropdown: the row shows the current option, clicking expands the list inline. */
public final class ChoiceSetting extends Setting {

    private static final int OPTION_H = 18;

    private final String[] options;
    private final IntSupplier getter;
    private final IntConsumer setter;
    private boolean open;

    public ChoiceSetting(String name, String[] options, IntSupplier getter, IntConsumer setter) {
        super(name);
        this.options = options;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public int height() {
        return open ? ROW_H + options.length * OPTION_H + 4 : ROW_H;
    }

    @Override
    public void render(GuiGraphics g, Font font, Theme t, int x, int y, int w, int mx, int my) {
        if (Ui.hit(mx, my, x + 2, y, w - 4, ROW_H)) Ui.round(g, x + 2, y + 1, w - 4, ROW_H - 2, 4, t.hover());
        Ui.text(g, font, name, x + 10, Ui.textY(y, ROW_H), t.text());

        int current = Math.max(0, Math.min(options.length - 1, getter.getAsInt()));
        int pw = Math.max(70, font.width(options[current]) + 22);
        int px = x + w - 10 - pw;
        Ui.round(g, px, y + 3, pw, ROW_H - 6, 4, t.hover());
        Ui.border(g, px, y + 3, pw, ROW_H - 6, 4, open ? t.accent() : t.border());
        Ui.text(g, font, options[current], px + 7, Ui.textY(y, ROW_H), t.text());
        // chevron
        int cx = px + pw - 10;
        int cy = y + ROW_H / 2 - 1;
        for (int i = 0; i < 3; i++) {
            Ui.rect(g, cx + (open ? i : 2 - i), cy + (open ? 2 - i : i) - 0, 1, 1, t.textDim());
            Ui.rect(g, cx + 4 - (open ? i : 2 - i), cy + (open ? 2 - i : i), 1, 1, t.textDim());
        }

        if (open) {
            int oy = y + ROW_H;
            Ui.round(g, px, oy, pw, options.length * OPTION_H + 2, 4, t.panelHigh());
            Ui.border(g, px, oy, pw, options.length * OPTION_H + 2, 4, t.border());
            for (int i = 0; i < options.length; i++) {
                int ry = oy + 1 + i * OPTION_H;
                boolean hover = Ui.hit(mx, my, px, ry, pw, OPTION_H);
                if (hover || i == current) Ui.round(g, px + 2, ry, pw - 4, OPTION_H, 3, i == current ? t.accentSoft() : t.hover());
                Ui.text(g, font, options[i], px + 7, Ui.textY(ry, OPTION_H), i == current ? t.text() : t.textDim());
            }
        }
    }

    @Override
    public boolean mouseClicked(int x, int y, int w, double mx, double my, int button) {
        if (button != 0) return false;
        if (Ui.hit(mx, my, x + 2, y, w - 4, ROW_H)) {
            open = !open;
            return true;
        }
        if (open) {
            int current = Math.max(0, Math.min(options.length - 1, getter.getAsInt()));
            // option list geometry mirrors render()
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            int pw = Math.max(70, mc.font.width(options[current]) + 22);
            int px = x + w - 10 - pw;
            int oy = y + ROW_H;
            for (int i = 0; i < options.length; i++) {
                if (Ui.hit(mx, my, px, oy + 1 + i * OPTION_H, pw, OPTION_H)) {
                    setter.accept(i);
                    open = false;
                    return true;
                }
            }
            open = false;
        }
        return false;
    }

    @Override
    public void unfocus() {
        open = false;
    }
}
