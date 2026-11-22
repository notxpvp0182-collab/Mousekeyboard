package com.ahmed.mousekeyboardhud.gui.setting;

import com.ahmed.mousekeyboardhud.gui.Theme;
import com.ahmed.mousekeyboardhud.gui.Ui;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Small dim section label with a hairline. */
public final class HeaderSetting extends Setting {

    public HeaderSetting(String name) {
        super(name);
    }

    @Override
    public int height() {
        return 20;
    }

    @Override
    public void render(GuiGraphics g, Font font, Theme t, int x, int y, int w, int mx, int my) {
        String label = name.toUpperCase(java.util.Locale.ROOT);
        Ui.text(g, font, label, x + 8, y + 9, t.textDim());
        Ui.rect(g, x + 8 + font.width(label) + 6, y + 13, w - 16 - font.width(label) - 6, 1, t.border());
    }
}
