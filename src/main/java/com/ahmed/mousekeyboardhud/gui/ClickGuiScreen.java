package com.ahmed.mousekeyboardhud.gui;

import com.ahmed.mousekeyboardhud.animation.Clock;
import com.ahmed.mousekeyboardhud.animation.Easing;
import com.ahmed.mousekeyboardhud.animation.Tween;
import com.ahmed.mousekeyboardhud.config.ConfigManager;
import com.ahmed.mousekeyboardhud.hud.HudManager;
import com.ahmed.mousekeyboardhud.hud.editor.HudEditorScreen;
import com.ahmed.mousekeyboardhud.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Premium dark-glass ClickGUI: category sidebar + scrollable settings, with open/close animation. */
public final class ClickGuiScreen extends Screen implements HudManager.HudFriendlyScreen {

    private static final int W = 440;
    private static final int H = 280;
    private static final int SIDEBAR_W = 112;
    private static final int ITEM_H = 22;
    private static final int HEADER_H = 34;

    private static Category lastCategory = Category.GENERAL;

    private final Screen parent;
    private final SettingsPanel panel = new SettingsPanel();
    private final Tween open = new Tween();
    private boolean closing;
    private float pillY = -1f;
    private Category category = lastCategory;

    // layout of the current frame (virtual coordinates)
    private float uiScale = 1f;
    private float originX;
    private float originY;

    public ClickGuiScreen(Screen parent) {
        super(Component.literal("Mouse & Keyboard HUD"));
        this.parent = parent;
        selectCategory(category);
    }

    private void selectCategory(Category c) {
        category = c;
        lastCategory = c;
        panel.setSettings(SettingFactory.forCategory(c, this::openEditor));
    }

    private void openEditor() {
        ConfigManager.save();
        Minecraft.getInstance().setScreen(new HudEditorScreen(parent));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        closing = true;
    }

    @Override
    public void removed() {
        ConfigManager.save();
    }

    private void layout(float eased) {
        float fit = Math.min((width - 12f) / W, (height - 12f) / H);
        uiScale = Math.max(0.35f, Math.min(1f, fit)) * (0.94f + 0.06f * eased);
        originX = (width - W * uiScale) / 2f;
        originY = (height - H * uiScale) / 2f;
    }

    private double vx(double mx) { return (mx - originX) / uiScale; }
    private double vy(double my) { return (my - originY) / uiScale; }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        float dt = Clock.tick();
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        Theme t = ThemeManager.current();

        open.update(!closing, dt, 0.16f);
        float e = open.eased(Easing.EASE_OUT);
        if (closing && open.progress() <= 0f) {
            mc.setScreen(parent);
            return;
        }
        if (mc.player == null) HudManager.renderElements(g, mc, false, false);

        layout(e);
        Ui.fade = e;
        Ui.rect(g, 0, 0, width, height, t.backdrop());

        double mx = vx(mouseX), my = vy(mouseY);
        var pose = g.pose();
        pose.pushMatrix();
        pose.translate(originX, originY);
        pose.scale(uiScale, uiScale);

        Ui.glassPanel(g, t, 0, 0, W, H);

        // sidebar
        Ui.rect(g, SIDEBAR_W, 10, 1, H - 20, t.border());
        Ui.text(g, font, "MOUSE & KEYBOARD", 14, 14, t.text());
        Ui.text(g, font, "HUD  \u00b7  Ahmed", 14, 25, t.textDim());

        Category[] cats = Category.values();
        int listY = 46;
        float targetPill = listY + category.ordinal() * ITEM_H;
        if (pillY < 0) pillY = targetPill;
        pillY += (targetPill - pillY) * (1f - (float) Math.exp(-22f * dt));
        Ui.round(g, 8, Math.round(pillY), SIDEBAR_W - 16, ITEM_H - 2, 5, t.accentSoft());
        Ui.rect(g, 8, Math.round(pillY) + 5, 2, ITEM_H - 12, t.accent());
        for (Category c : cats) {
            int iy = listY + c.ordinal() * ITEM_H;
            boolean selected = c == category;
            boolean hover = Ui.hit(mx, my, 8, iy, SIDEBAR_W - 16, ITEM_H - 2);
            if (hover && !selected) Ui.round(g, 8, iy, SIDEBAR_W - 16, ITEM_H - 2, 5, t.hover());
            int col = selected ? t.text() : t.textDim();
            Icons.draw(g, c, 18, iy + (ITEM_H - 2 - Icons.SIZE) / 2, selected ? t.accent() : col);
            Ui.text(g, font, c.label(), 36, Ui.textY(iy, ITEM_H - 2), col);
        }

        // header
        int cx = SIDEBAR_W + 14;
        Ui.text(g, font, category.label(), cx, 14, t.text());
        int btnW = font.width("HUD Editor") + 18;
        int btnX = W - 40 - btnW;
        boolean hoverEd = Ui.hit(mx, my, btnX, 9, btnW, 18);
        Ui.round(g, btnX, 9, btnW, 18, 5, hoverEd ? t.accentSoft() : t.hover());
        Ui.border(g, btnX, 9, btnW, 18, 5, hoverEd ? t.accent() : t.border());
        Ui.textCentered(g, font, "HUD Editor", btnX + btnW / 2, 14, t.text());
        // close button
        boolean hoverX = Ui.hit(mx, my, W - 30, 9, 18, 18);
        Ui.round(g, W - 30, 9, 18, 18, 5, hoverX ? 0x40FF5A5A : t.hover());
        Ui.textCentered(g, font, "x", W - 21, 14, t.text());
        Ui.rect(g, SIDEBAR_W + 10, HEADER_H, W - SIDEBAR_W - 20, 1, t.border());

        panel.render(g, t, SIDEBAR_W + 4, HEADER_H + 4, W - SIDEBAR_W - 10, H - HEADER_H - 10, (int) mx, (int) my);

        pose.popMatrix();
        Ui.fade = 1f;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = vx(event.x()), my = vy(event.y());
        int button = event.button();
        if (button == 0) {
            if (mx < 0 || my < 0 || mx > W || my > H) {
                closing = true;
                return true;
            }
            if (Ui.hit(mx, my, W - 30, 9, 18, 18)) {
                closing = true;
                return true;
            }
            Font font = Minecraft.getInstance().font;
            int btnW = font.width("HUD Editor") + 18;
            if (Ui.hit(mx, my, W - 40 - btnW, 9, btnW, 18)) {
                openEditor();
                return true;
            }
            for (Category c : Category.values()) {
                if (Ui.hit(mx, my, 8, 46 + c.ordinal() * ITEM_H, SIDEBAR_W - 16, ITEM_H - 2)) {
                    if (c != category) selectCategory(c);
                    return true;
                }
            }
        }
        return panel.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        return panel.mouseDragged(vx(event.x()), vy(event.y()));
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        panel.mouseReleased();
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        return panel.mouseScrolled(vx(mouseX), vy(mouseY), vertical);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        if (panel.capturesKeyboard()) {
            panel.keyPressed(key);
            return true;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            closing = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return panel.charTyped(event.codepoint());
    }
}
