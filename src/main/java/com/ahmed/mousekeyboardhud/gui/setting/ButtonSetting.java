package com.ahmed.mousekeyboardhud.gui.setting;

import com.ahmed.mousekeyboardhud.gui.Theme;
import com.ahmed.mousekeyboardhud.gui.Ui;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Full-width action button. */
public final class ButtonSetting extends Setting {

    private final Runnable action;

    public ButtonSetting(String name, Runnable action) {
        super(name);
        this.action = action;
    }

    @Override
    public int height() {
        return 28;
    }

    @Override
    public void render(GuiGraphics g, Font font, Theme t, int x, int y, int w, int mx, int my) {
        boolean hover = Ui.hit(mx, my, x + 8, y + 3, w - 16, 22);
        Ui.round(g, x + 8, y + 3, w - 16, 22, 5, hover ? t.accentSoft() : t.hover());
        Ui.border(g, x + 8, y + 3, w - 16, 22, 5, hover ? t.accent() : t.border());
        Ui.textCentered(g, font, name, x + w / 2, Ui.textY(y + 3, 22), t.text());
    }

    @Override
    public boolean mouseClicked(int x, int y, int w, double mx, double my, int button) {
        if (button != 0 || !Ui.hit(mx, my, x + 8, y + 3, w - 16, 22)) return false;
        action.run();
        return true;
    }
}
