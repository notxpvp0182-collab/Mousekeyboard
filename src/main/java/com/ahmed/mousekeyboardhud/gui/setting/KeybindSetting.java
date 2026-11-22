package com.ahmed.mousekeyboardhud.gui.setting;

import com.ahmed.mousekeyboardhud.gui.Theme;
import com.ahmed.mousekeyboardhud.gui.Ui;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import java.util.function.Supplier;

/**
 * Rebinds a real Minecraft {@link KeyMapping}; the change is also visible in Options > Controls.
 * Click, press a key to bind, Esc cancels, Backspace/Delete unbinds.
 */
public final class KeybindSetting extends Setting {

    private final Supplier<KeyMapping> mapping;
    private boolean listening;

    public KeybindSetting(String name, Supplier<KeyMapping> mapping) {
        super(name);
        this.mapping = mapping;
    }

    @Override
    public int height() {
        return ROW_H;
    }

    private String keyText() {
        KeyMapping m = mapping.get();
        return m == null ? "-" : m.getTranslatedKeyMessage().getString();
    }

    @Override
    public void render(GuiGraphics g, Font font, Theme t, int x, int y, int w, int mx, int my) {
        if (Ui.hit(mx, my, x + 2, y, w - 4, ROW_H)) Ui.round(g, x + 2, y + 1, w - 4, ROW_H - 2, 4, t.hover());
        Ui.text(g, font, name, x + 10, Ui.textY(y, ROW_H), t.text());
        String label = listening ? "Press a key..." : keyText();
        int pw = Math.max(80, font.width(label) + 16);
        int px = x + w - 10 - pw;
        Ui.round(g, px, y + 3, pw, ROW_H - 6, 4, listening ? t.accentSoft() : t.hover());
        Ui.border(g, px, y + 3, pw, ROW_H - 6, 4, listening ? t.accent() : t.border());
        Ui.textCentered(g, font, label, px + pw / 2, Ui.textY(y, ROW_H), t.text());
    }

    @Override
    public boolean mouseClicked(int x, int y, int w, double mx, double my, int button) {
        if (button != 0 || !Ui.hit(mx, my, x + 2, y, w - 4, ROW_H)) return false;
        listening = !listening;
        return true;
    }

    @Override
    public boolean keyPressed(int key) {
        if (!listening) return false;
        listening = false;
        if (key == GLFW.GLFW_KEY_ESCAPE) return true;
        KeyMapping m = mapping.get();
        if (m == null) return true;
        Minecraft mc = Minecraft.getInstance();
        InputConstants.Key bound = (key == GLFW.GLFW_KEY_BACKSPACE || key == GLFW.GLFW_KEY_DELETE)
                ? InputConstants.UNKNOWN
                : InputConstants.Type.KEYSYM.getOrCreate(key);
        mc.options.setKey(m, bound);
        KeyMapping.resetMapping();
        mc.options.save();
        return true;
    }

    @Override
    public boolean capturesKeyboard() {
        return listening;
    }

    @Override
    public void unfocus() {
        listening = false;
    }
}
