package com.ahmed.mousekeyboardhud.hud;

import com.ahmed.mousekeyboardhud.animation.Clock;
import com.ahmed.mousekeyboardhud.config.ConfigManager;
import com.ahmed.mousekeyboardhud.config.HudConfig;
import com.ahmed.mousekeyboardhud.hud.components.Rect;
import com.ahmed.mousekeyboardhud.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Orchestrates measuring, positioning and drawing of all HUD elements. */
public final class HudManager {

    /** Screens implementing this marker keep the HUD visible behind them (ClickGUI, HUD editor). */
    public interface HudFriendlyScreen {}

    private static final Rect SCRATCH = new Rect();

    private HudManager() {}

    public static boolean isVisible(HudElementId id) {
        HudConfig c = ConfigManager.get();
        return switch (id) {
            case KEYBOARD_FULL -> c.keyboard.enabled && c.keyboard.fullKeyboard;
            case KEYBOARD_LEFT -> c.keyboard.enabled && !c.keyboard.fullKeyboard && c.keyboard.leftHalf;
            case KEYBOARD_RIGHT -> c.keyboard.enabled && !c.keyboard.fullKeyboard && c.keyboard.rightHalf;
            case MOUSE -> c.mouse.enabled;
        };
    }

    public static float scaleOf(HudElementId id) {
        HudConfig c = ConfigManager.get();
        return switch (id) {
            case KEYBOARD_FULL -> c.keyboard.scale;
            case KEYBOARD_LEFT, KEYBOARD_RIGHT -> c.keyboard.halvesScale;
            case MOUSE -> c.mouse.scale;
        };
    }

    public static void setScale(HudElementId id, float scale) {
        HudConfig c = ConfigManager.get();
        float s = MathUtil.clamp(scale, 0.3f, 4f);
        switch (id) {
            case KEYBOARD_FULL -> c.keyboard.scale = s;
            case KEYBOARD_LEFT, KEYBOARD_RIGHT -> c.keyboard.halvesScale = s;
            case MOUSE -> c.mouse.scale = s;
        }
    }

    public static HudConfig.Pos posOf(HudElementId id) {
        HudConfig c = ConfigManager.get();
        return switch (id) {
            case KEYBOARD_FULL -> c.keyboard.fullPos;
            case KEYBOARD_LEFT -> c.keyboard.leftPos;
            case KEYBOARD_RIGHT -> c.keyboard.rightPos;
            case MOUSE -> c.mouse.pos;
        };
    }

    /** Fills {@code out} with the on-screen bounds (GUI pixels) of an element, clamped inside the screen. */
    public static Rect measure(HudElementId id, int guiW, int guiH, Rect out) {
        HudConfig c = ConfigManager.get();
        float scale = scaleOf(id);
        if (id == HudElementId.MOUSE) {
            MouseRenderer.measure(c.mouse, scale, out);
        } else {
            KeyboardRenderer.measure(KeyboardLayout.view(id), c.keyboard, scale, out);
        }
        HudConfig.Pos p = posOf(id);
        out.x = clampAxis(Math.round(p.x * guiW), out.w, guiW);
        out.y = clampAxis(Math.round(p.y * guiH), out.h, guiH);
        return out;
    }

    private static int clampAxis(int v, int size, int screen) {
        return Math.max(0, Math.min(v, Math.max(0, screen - size)));
    }

    /** Stores a new top-left pixel position as screen fractions. */
    public static void setPixelPos(HudElementId id, int px, int py, int guiW, int guiH) {
        HudConfig.Pos p = posOf(id);
        p.x = guiW <= 0 ? p.x : px / (float) guiW;
        p.y = guiH <= 0 ? p.y : py / (float) guiH;
    }

    public static boolean shouldRender(Minecraft mc) {
        HudConfig c = ConfigManager.get();
        if (!c.general.hudEnabled || mc.options.hideGui || mc.player == null) return false;
        if (mc.screen instanceof com.ahmed.mousekeyboardhud.hud.editor.HudEditorScreen) return false; // draws itself
        return mc.screen == null || mc.screen instanceof HudFriendlyScreen || c.general.showInScreens;
    }

    /** Called every frame by the Fabric HUD registry (and by editor screens in preview mode). */
    public static void render(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        renderElements(g, mc, true, true);
    }

    /**
     * @param live       poll real keyboard/mouse state (false = frozen idle preview)
     * @param respectCfg when false, the master HUD switch is ignored (used by the HUD editor)
     */
    public static void renderElements(GuiGraphics g, Minecraft mc, boolean live, boolean respectCfg) {
        if (respectCfg && !shouldRender(mc)) return;
        float dt = Clock.tick();
        HudConfig cfg = ConfigManager.get();
        long window = mc.getWindow().handle();
        int guiW = g.guiWidth();
        int guiH = g.guiHeight();
        boolean pollInput = live && (mc.screen == null || mc.screen instanceof HudFriendlyScreen
                || mc.screen instanceof com.ahmed.mousekeyboardhud.hud.editor.HudEditorScreen);

        for (HudElementId id : HudElementId.values()) {
            if (!isVisible(id)) continue;
            Rect r = measure(id, guiW, guiH, SCRATCH);
            if (id == HudElementId.MOUSE) {
                MouseRenderer.render(g, window, cfg, scaleOf(id), r, pollInput, dt);
            } else {
                KeyboardRenderer.render(g, mc.font, window, KeyboardLayout.view(id), cfg, scaleOf(id), r, pollInput, dt);
            }
        }
    }
}
