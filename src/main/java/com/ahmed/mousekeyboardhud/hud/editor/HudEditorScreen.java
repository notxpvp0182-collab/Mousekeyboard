package com.ahmed.mousekeyboardhud.hud.editor;

import com.ahmed.mousekeyboardhud.animation.Clock;
import com.ahmed.mousekeyboardhud.config.ConfigManager;
import com.ahmed.mousekeyboardhud.config.HudConfig;
import com.ahmed.mousekeyboardhud.gui.ClickGuiScreen;
import com.ahmed.mousekeyboardhud.gui.SettingFactory;
import com.ahmed.mousekeyboardhud.gui.SettingsPanel;
import com.ahmed.mousekeyboardhud.gui.Theme;
import com.ahmed.mousekeyboardhud.gui.ThemeManager;
import com.ahmed.mousekeyboardhud.gui.Ui;
import com.ahmed.mousekeyboardhud.hud.HudElementId;
import com.ahmed.mousekeyboardhud.hud.HudManager;
import com.ahmed.mousekeyboardhud.hud.components.Rect;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Visual HUD editor: drag elements, resize from corner handles (uniform scale), right-click for
 * customization, plus a toolbar for toggles, resets, Save / Cancel and a link to the ClickGUI.
 * Selection boxes and handles are drawn only here, never during gameplay.
 */
public final class HudEditorScreen extends Screen {

    private static final int HANDLE = 6;
    private static final int BTN_H = 16;
    private static final int POPUP_W = 236;

    private record Button(String label, Runnable action, BooleanSupplier active, BooleanSupplier visible) {}

    private final Screen parent;
    private final String snapshot;
    private final List<Button> buttons = new ArrayList<>();
    private final Rect[] rects = new Rect[HudElementId.values().length];
    private final Rect tmp = new Rect();
    private final SettingsPanel popup = new SettingsPanel();

    private HudElementId selected;
    private HudElementId popupFor;
    private int popupX, popupY, popupH;

    private enum Mode { NONE, MOVE, RESIZE }
    private Mode mode = Mode.NONE;
    private int grabDx, grabDy;
    private int corner;
    private float startScale;
    private int startX, startY, startW, startH;

    // toolbar layout of the last frame
    private final int[] btnX = new int[16];
    private final int[] btnY = new int[16];
    private final int[] btnW = new int[16];

    public HudEditorScreen(Screen parent) {
        super(Component.literal("HUD Editor"));
        this.parent = parent;
        this.snapshot = ConfigManager.snapshot();
        for (int i = 0; i < rects.length; i++) rects[i] = new Rect();

        BooleanSupplier always = () -> true;
        BooleanSupplier split = () -> !ConfigManager.get().keyboard.fullKeyboard;
        buttons.add(new Button("Keyboard", () -> c().keyboard.enabled = !c().keyboard.enabled, () -> c().keyboard.enabled, always));
        buttons.add(new Button("Mouse", () -> c().mouse.enabled = !c().mouse.enabled, () -> c().mouse.enabled, always));
        buttons.add(new Button("Full", () -> c().keyboard.fullKeyboard = !c().keyboard.fullKeyboard, () -> c().keyboard.fullKeyboard, always));
        buttons.add(new Button("Left", () -> c().keyboard.leftHalf = !c().keyboard.leftHalf, () -> c().keyboard.leftHalf, split));
        buttons.add(new Button("Right", () -> c().keyboard.rightHalf = !c().keyboard.rightHalf, () -> c().keyboard.rightHalf, split));
        buttons.add(new Button("Reset pos", SettingFactory::resetPositions, () -> false, always));
        buttons.add(new Button("Reset scale", SettingFactory::resetScales, () -> false, always));
        buttons.add(new Button("ClickGUI", this::openClickGui, () -> false, always));
        buttons.add(new Button("Cancel", this::cancel, () -> false, always));
        buttons.add(new Button("Save", this::saveAndClose, () -> true, always));
    }

    private static HudConfig c() {
        return ConfigManager.get();
    }

    // ------------------------------------------------------------------------------------------
    // actions
    // ------------------------------------------------------------------------------------------

    private void saveAndClose() {
        ConfigManager.save();
        Minecraft.getInstance().setScreen(parent);
    }

    private void cancel() {
        ConfigManager.restore(snapshot);
        Minecraft.getInstance().setScreen(parent);
    }

    private void openClickGui() {
        ConfigManager.save();
        Minecraft.getInstance().setScreen(new ClickGuiScreen(parent));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        saveAndClose();
    }

    // ------------------------------------------------------------------------------------------
    // rendering
    // ------------------------------------------------------------------------------------------

    private void measureAll() {
        for (HudElementId id : HudElementId.values()) {
            HudManager.measure(id, width, height, rects[id.ordinal()]);
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        Clock.tick();
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        Theme t = ThemeManager.current();

        Ui.rect(g, 0, 0, width, height, 0x40000000);
        HudManager.renderElements(g, mc, true, false);
        measureAll();

        for (HudElementId id : HudElementId.values()) {
            if (!HudManager.isVisible(id)) continue;
            Rect r = rects[id.ordinal()];
            boolean sel = id == selected;
            boolean hover = mode == Mode.NONE && r.contains(mouseX, mouseY) && hitTest(mouseX, mouseY) == id;
            int col = sel ? t.accent() : hover ? 0xB0FFFFFF : 0x50FFFFFF;
            Ui.border(g, r.x - 2, r.y - 2, r.w + 4, r.h + 4, 2, col);
            if (sel || hover) Ui.text(g, font, id.label(), r.x - 2, r.y - 12, sel ? t.accent() : t.text());
            if (sel) {
                for (int k = 0; k < 4; k++) {
                    int hx = handleX(r, k), hy = handleY(r, k);
                    Ui.rect(g, hx, hy, HANDLE, HANDLE, 0xFFFFFFFF);
                    Ui.border(g, hx - 1, hy - 1, HANDLE + 2, HANDLE + 2, 0, t.accent());
                }
            }
        }

        renderToolbar(g, font, t, mouseX, mouseY);
        if (popupFor != null) renderPopup(g, font, t, mouseX, mouseY);
        if (selected == null && popupFor == null) {
            Ui.textCentered(g, font, "Drag to move  \u00b7  corners to resize  \u00b7  right-click to customize",
                    width / 2, height - 14, t.textDim());
        }
    }

    private int handleX(Rect r, int corner) {
        return ((corner & 1) == 0 ? r.x - 2 - HANDLE / 2 : r.right() + 2 - HANDLE / 2);
    }

    private int handleY(Rect r, int corner) {
        return ((corner & 2) == 0 ? r.y - 2 - HANDLE / 2 : r.bottom() + 2 - HANDLE / 2);
    }

    private void layoutToolbar(Font font) {
        int x = 6, y = 6;
        int maxX = width - 6;
        for (int i = 0; i < buttons.size(); i++) {
            Button b = buttons.get(i);
            int w = font.width(b.label()) + 14;
            if (!b.visible().getAsBoolean()) {
                btnW[i] = 0;
                continue;
            }
            if (x + w > maxX && x > 6) {
                x = 6;
                y += BTN_H + 4;
            }
            btnX[i] = x;
            btnY[i] = y;
            btnW[i] = w;
            x += w + 4;
        }
    }

    private void renderToolbar(GuiGraphics g, Font font, Theme t, int mx, int my) {
        layoutToolbar(font);
        for (int i = 0; i < buttons.size(); i++) {
            if (btnW[i] == 0) continue;
            Button b = buttons.get(i);
            boolean on = b.active().getAsBoolean();
            boolean hover = Ui.hit(mx, my, btnX[i], btnY[i], btnW[i], BTN_H);
            Ui.round(g, btnX[i], btnY[i], btnW[i], BTN_H, 4, on ? t.accentSoft() : t.panel());
            Ui.border(g, btnX[i], btnY[i], btnW[i], BTN_H, 4, hover || on ? t.accent() : t.border());
            Ui.textCentered(g, font, b.label(), btnX[i] + btnW[i] / 2, btnY[i] + 4, on || hover ? t.text() : t.textDim());
        }
    }

    private void renderPopup(GuiGraphics g, Font font, Theme t, int mx, int my) {
        popupH = Math.min(280, height - 16);
        Ui.glassPanel(g, t, popupX, popupY, POPUP_W, popupH);
        String title = (popupFor == HudElementId.MOUSE ? "Mouse" : "Keyboard") + " customization";
        Ui.text(g, font, title, popupX + 12, popupY + 10, t.text());
        boolean hoverX = Ui.hit(mx, my, popupX + POPUP_W - 24, popupY + 6, 16, 16);
        Ui.round(g, popupX + POPUP_W - 24, popupY + 6, 16, 16, 4, hoverX ? 0x40FF5A5A : t.hover());
        Ui.textCentered(g, font, "x", popupX + POPUP_W - 16, popupY + 10, t.text());
        Ui.rect(g, popupX + 8, popupY + 27, POPUP_W - 16, 1, t.border());
        popup.render(g, t, popupX + 2, popupY + 31, POPUP_W - 4, popupH - 36, mx, my);
    }

    // ------------------------------------------------------------------------------------------
    // input
    // ------------------------------------------------------------------------------------------

    private HudElementId hitTest(double mx, double my) {
        HudElementId[] ids = HudElementId.values();
        for (int i = ids.length - 1; i >= 0; i--) {
            HudElementId id = ids[i];
            if (HudManager.isVisible(id) && rects[id.ordinal()].contains(mx, my)) return id;
        }
        return null;
    }

    private int hitHandle(Rect r, double mx, double my) {
        for (int k = 0; k < 4; k++) {
            if (Ui.hit(mx, my, handleX(r, k) - 2, handleY(r, k) - 2, HANDLE + 4, HANDLE + 4)) return k;
        }
        return -1;
    }

    private void openPopup(HudElementId id, double mx, double my) {
        popupFor = id;
        popup.setSettings(id == HudElementId.MOUSE ? SettingFactory.mouseAll() : SettingFactory.keyboardAll());
        int h = Math.min(280, height - 16);
        popupX = (int) MathUtil.clamp((float) mx + 14, 4, Math.max(4, width - POPUP_W - 4));
        popupY = (int) MathUtil.clamp((float) my - 20, 4, Math.max(4, height - h - 4));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x(), my = event.y();
        int button = event.button();
        measureAll();

        // popup first
        if (popupFor != null) {
            if (Ui.hit(mx, my, popupX, popupY, POPUP_W, popupH)) {
                if (Ui.hit(mx, my, popupX + POPUP_W - 24, popupY + 6, 16, 16)) {
                    closePopup();
                } else {
                    popup.mouseClicked(mx, my, button);
                }
                return true;
            }
            closePopup();
        }

        if (button == 0) {
            for (int i = 0; i < buttons.size(); i++) {
                if (btnW[i] > 0 && Ui.hit(mx, my, btnX[i], btnY[i], btnW[i], BTN_H)) {
                    buttons.get(i).action().run();
                    return true;
                }
            }
            if (selected != null && HudManager.isVisible(selected)) {
                Rect r = rects[selected.ordinal()];
                int k = hitHandle(r, mx, my);
                if (k >= 0) {
                    mode = Mode.RESIZE;
                    corner = k;
                    startScale = HudManager.scaleOf(selected);
                    startX = r.x; startY = r.y; startW = r.w; startH = r.h;
                    return true;
                }
            }
        }

        HudElementId hit = hitTest(mx, my);
        if (hit == null) {
            selected = null;
            return true;
        }
        selected = hit;
        Rect r = rects[hit.ordinal()];
        if (button == 1) {
            openPopup(hit, mx, my);
        } else if (button == 0) {
            mode = Mode.MOVE;
            grabDx = (int) mx - r.x;
            grabDy = (int) my - r.y;
        }
        return true;
    }

    private void closePopup() {
        popup.unfocusAll();
        popupFor = null;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        double mx = event.x(), my = event.y();
        if (popupFor != null && popup.mouseDragged(mx, my)) return true;
        if (selected == null) return false;
        if (mode == Mode.MOVE) {
            Rect r = rects[selected.ordinal()];
            int nx = (int) MathUtil.clamp((float) (mx - grabDx), 0, Math.max(0, width - r.w));
            int ny = (int) MathUtil.clamp((float) (my - grabDy), 0, Math.max(0, height - r.h));
            HudManager.setPixelPos(selected, nx, ny, width, height);
            return true;
        }
        if (mode == Mode.RESIZE) {
            boolean anchorLeft = (corner & 1) == 1;   // dragging a right corner keeps the left edge
            boolean anchorTop = (corner & 2) == 2;    // dragging a bottom corner keeps the top edge
            int ax = anchorLeft ? startX : startX + startW;
            int ay = anchorTop ? startY : startY + startH;
            float rw = (float) Math.abs(mx - ax) / Math.max(1, startW);
            float rh = (float) Math.abs(my - ay) / Math.max(1, startH);
            float ratio = Math.max(rw, rh);
            HudManager.setScale(selected, startScale * ratio);
            HudManager.measure(selected, width, height, tmp);
            int nx = anchorLeft ? ax : ax - tmp.w;
            int ny = anchorTop ? ay : ay - tmp.h;
            HudManager.setPixelPos(selected,
                    (int) MathUtil.clamp(nx, 0, Math.max(0, width - tmp.w)),
                    (int) MathUtil.clamp(ny, 0, Math.max(0, height - tmp.h)), width, height);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        mode = Mode.NONE;
        popup.mouseReleased();
        return true;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        if (popupFor != null && popup.mouseScrolled(mx, my, vertical)) return true;
        HudElementId target = hitTest(mx, my);
        if (target != null) {
            // wheel over an element scales it in 5% steps
            HudManager.setScale(target, HudManager.scaleOf(target) * (vertical > 0 ? 1.05f : 0.95f));
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        if (popupFor != null && popup.capturesKeyboard()) {
            popup.keyPressed(key);
            return true;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            if (popupFor != null) closePopup();
            else saveAndClose();
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return popupFor != null && popup.charTyped(event.codepoint());
    }
}
