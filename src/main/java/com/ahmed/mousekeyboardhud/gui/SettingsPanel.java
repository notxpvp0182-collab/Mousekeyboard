package com.ahmed.mousekeyboardhud.gui;

import com.ahmed.mousekeyboardhud.animation.Clock;
import com.ahmed.mousekeyboardhud.gui.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

/** Scrollable list of {@link Setting} rows with clipping, smooth scrolling and input routing. */
public final class SettingsPanel {

    private List<Setting> settings = List.of();
    private float scroll;
    private float scrollTarget;
    private int contentHeight;
    private int viewH;

    // viewport (set during render, reused by input handlers)
    private int vx, vy, vw;

    private Setting active; // row receiving drag events

    public void setSettings(List<Setting> list) {
        for (Setting s : settings) s.unfocus();
        settings = list;
        scroll = 0;
        scrollTarget = 0;
        active = null;
    }

    public void render(GuiGraphics g, Theme t, int x, int y, int w, int h, int mx, int my) {
        Font font = Minecraft.getInstance().font;
        vx = x;
        vy = y;
        vw = w;
        viewH = h;

        int total = 0;
        for (Setting s : settings) if (s.isVisible()) total += s.height();
        contentHeight = total + 4;
        scrollTarget = Math.max(0, Math.min(scrollTarget, Math.max(0, contentHeight - h)));
        scroll += (scrollTarget - scroll) * (1f - (float) Math.exp(-18f * Clock.dt()));
        if (Math.abs(scrollTarget - scroll) < 0.05f) scroll = scrollTarget;

        g.enableScissor(x, y, x + w, y + h);
        int cy = y - Math.round(scroll);
        for (Setting s : settings) {
            if (!s.isVisible()) continue;
            int sh = s.height();
            if (cy + sh > y - 2 && cy < y + h + 2) {
                boolean inView = my >= y && my < y + h;
                s.render(g, font, t, x, cy, w, mx, inView ? my : -10000);
            }
            cy += sh;
        }
        g.disableScissor();

        // scrollbar
        if (contentHeight > h) {
            int barH = Math.max(14, h * h / contentHeight);
            int barY = y + Math.round((h - barH) * (scroll / (contentHeight - h)));
            Ui.round(g, x + w - 3, barY, 2, barH, 1, 0x40FFFFFF);
        }
    }

    public boolean mouseClicked(double mx, double my, int button) {
        if (my < vy || my >= vy + viewH || mx < vx || mx >= vx + vw) return false;
        int cy = vy - Math.round(scroll);
        Setting hit = null;
        // find which row was hit first so other rows can lose focus
        int y = cy;
        for (Setting s : settings) {
            if (!s.isVisible()) continue;
            int sh = s.height();
            if (my >= y && my < y + sh) { hit = s; break; }
            y += sh;
        }
        for (Setting s : settings) if (s != hit) s.unfocus();
        if (hit == null) return true;
        if (hit.mouseClicked(vx, y, vw, mx, my, button)) {
            active = hit;
            activeY = y;
        }
        return true;
    }

    private int activeY;

    public boolean mouseDragged(double mx, double my) {
        if (active == null) return false;
        // keep the row anchored to its position even if its height changed
        int y = vy - Math.round(scroll);
        for (Setting s : settings) {
            if (!s.isVisible()) continue;
            if (s == active) break;
            y += s.height();
        }
        active.mouseDragged(vx, y, vw, mx, my);
        return true;
    }

    public void mouseReleased() {
        if (active != null) active.mouseReleased();
        active = null;
    }

    public boolean mouseScrolled(double mx, double my, double vertical) {
        if (mx < vx || mx >= vx + vw || my < vy || my >= vy + viewH) return false;
        scrollTarget -= (float) (vertical * 28.0);
        scrollTarget = Math.max(0, Math.min(scrollTarget, Math.max(0, contentHeight - viewH)));
        return true;
    }

    /** True if some row currently owns the keyboard (hex field, keybind capture). */
    public boolean capturesKeyboard() {
        for (Setting s : settings) if (s.isVisible() && s.capturesKeyboard()) return true;
        return false;
    }

    public boolean keyPressed(int key) {
        for (Setting s : settings) {
            if (s.isVisible() && s.capturesKeyboard() && s.keyPressed(key)) return true;
        }
        return false;
    }

    public boolean charTyped(int codepoint) {
        for (Setting s : settings) {
            if (s.isVisible() && s.capturesKeyboard() && s.charTyped(codepoint)) return true;
        }
        return false;
    }

    public void unfocusAll() {
        for (Setting s : settings) s.unfocus();
    }
}
