package com.ahmed.mousekeyboardhud.gui.setting;

import com.ahmed.mousekeyboardhud.gui.Theme;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.BooleanSupplier;

/** One row in a settings list. Rows draw themselves and handle their own input in local coordinates. */
public abstract class Setting {

    public static final int ROW_H = 22;

    public final String name;
    private BooleanSupplier visible = () -> true;

    protected Setting(String name) {
        this.name = name;
    }

    public Setting visibleWhen(BooleanSupplier supplier) {
        this.visible = supplier;
        return this;
    }

    public boolean isVisible() {
        return visible.getAsBoolean();
    }

    public abstract int height();

    public abstract void render(GuiGraphics g, Font font, Theme t, int x, int y, int w, int mx, int my);

    /** @return true if the click was consumed. */
    public boolean mouseClicked(int x, int y, int w, double mx, double my, int button) {
        return false;
    }

    public void mouseDragged(int x, int y, int w, double mx, double my) {}

    public void mouseReleased() {}

    public boolean keyPressed(int key) {
        return false;
    }

    public boolean charTyped(int codepoint) {
        return false;
    }

    /** True while the row wants keyboard focus (hex field, keybind capture). */
    public boolean capturesKeyboard() {
        return false;
    }

    /** Called when another row got clicked. */
    public void unfocus() {}
}
