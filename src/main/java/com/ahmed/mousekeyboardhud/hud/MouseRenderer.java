package com.ahmed.mousekeyboardhud.hud;

import com.ahmed.mousekeyboardhud.animation.Easing;
import com.ahmed.mousekeyboardhud.animation.Tween;
import com.ahmed.mousekeyboardhud.config.HudConfig;
import com.ahmed.mousekeyboardhud.hud.components.Rect;
import com.ahmed.mousekeyboardhud.input.InputTracker;
import com.ahmed.mousekeyboardhud.rendering.Draw;
import com.ahmed.mousekeyboardhud.rendering.MouseShape;
import com.ahmed.mousekeyboardhud.util.ColorUtil;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

/**
 * Draws the glossy dark mouse from the reference: oval body, split buttons, center channel,
 * scroll wheel with moving ticks and direction arrows. Reacts to real button state and real scroll input.
 */
public final class MouseRenderer {

    /** Design size of the mouse at scale 1 (width : height follows the reference image). */
    private static final float DESIGN_W = 56f;
    private static final float DESIGN_H = 86f;
    private static final float BUTTON_SPLIT = 0.40f;

    private static final Tween LEFT = new Tween();
    private static final Tween RIGHT = new Tween();
    private static final Tween MIDDLE = new Tween();
    private static final MouseShape BODY = new MouseShape();
    private static final MouseShape HALO = new MouseShape();

    private MouseRenderer() {}

    public static void measure(HudConfig.Mouse m, float scale, Rect out) {
        out.w = Math.max(8, Math.round(DESIGN_W * scale));
        out.h = Math.max(12, Math.round(DESIGN_H * scale));
    }

    public static void render(GuiGraphics g, long window, HudConfig cfg, float scale, Rect r, boolean live, float dt) {
        HudConfig.Mouse m = cfg.mouse;
        HudConfig.Animation anim = cfg.animation;
        float op = m.opacity;
        Easing easing = anim.easing;
        int w = r.w;
        int h = r.h;

        // --- input + animation state -------------------------------------------------------------
        boolean left = live && InputTracker.isMouseDown(window, GLFW.GLFW_MOUSE_BUTTON_LEFT);
        boolean right = live && InputTracker.isMouseDown(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        boolean middle = live && InputTracker.isMouseDown(window, GLFW.GLFW_MOUSE_BUTTON_MIDDLE);
        float pressSeconds = 0.06f / anim.speed;
        float releaseSeconds = 0.15f / anim.speed;
        if (anim.enabled) {
            LEFT.update(left, dt, left ? pressSeconds : releaseSeconds);
            RIGHT.update(right, dt, right ? pressSeconds : releaseSeconds);
            MIDDLE.update(middle, dt, middle ? pressSeconds : releaseSeconds);
        } else {
            LEFT.set(left ? 1f : 0f);
            RIGHT.set(right ? 1f : 0f);
            MIDDLE.set(middle ? 1f : 0f);
        }
        InputTracker.updateScroll(dt, anim.enabled);
        float eL = LEFT.eased(easing);
        float eR = RIGHT.eased(easing);
        float eM = MIDDLE.eased(easing);

        // body roundness: superellipse exponent 4 (boxy) .. 2 (ellipse)
        float exp = 4f - 2f * m.cornerRadius;
        BODY.update(w, h, exp);
        int thick = Math.max(1, Math.round(m.outlineWidth * scale));
        int splitRow = Math.round(h * BUTTON_SPLIT);
        int cx = w / 2;
        int channel = Math.max(1, Math.round(1.6f * scale));
        int chL = cx - channel / 2;
        int chR = chL + channel;

        // --- glow halo ---------------------------------------------------------------------------
        float strongest = Math.max(eL, Math.max(eR, eM));
        if (m.glowEnabled && strongest > 0.02f) {
            int glowColor = eL >= eR && eL >= eM ? m.leftColor : (eR >= eM ? m.rightColor : m.middleColor);
            glowColor = ColorUtil.withAlpha(m.glowColor == 0 ? glowColor : m.glowColor, 255);
            int spread = Math.max(1, Math.round((2f + 2f * m.glowStrength) * scale));
            for (int k = spread; k >= 1; k--) {
                float f = 1f - (k - 1f) / spread;
                int c = ColorUtil.mulAlpha(glowColor, f * f * 0.5f * strongest * anim.glowIntensity * op);
                HALO.update(w + 2 * k, h + 2 * k, exp);
                HALO.outline(g, r.x - k, r.y - k, 1, c);
            }
        }

        // --- body --------------------------------------------------------------------------------
        if (m.backgroundEnabled) {
            BODY.fillRows(g, r.x, r.y, 0, h, ColorUtil.mulAlpha(m.backgroundColor, op));
            // glossy highlight on the upper-left, like the reference reflection
            int gloss = ColorUtil.mulAlpha(0x1AFFFFFF, op);
            BODY.fillBand(g, r.x, r.y, Math.round(h * 0.05f), Math.round(h * 0.36f),
                    Math.round(w * 0.12f), chL - Math.max(1, Math.round(2 * scale)), gloss);
            BODY.fillBand(g, r.x, r.y, Math.round(h * 0.45f), Math.round(h * 0.85f),
                    Math.round(w * 0.55f), Math.round(w * 0.70f), ColorUtil.mulAlpha(0x10FFFFFF, op));
        }

        // --- pressed buttons ---------------------------------------------------------------------
        if (eL > 0f) {
            BODY.fillBand(g, r.x, r.y, 0, splitRow, 0, chL, ColorUtil.mulAlpha(m.leftColor, eL * op));
        }
        if (eR > 0f) {
            BODY.fillBand(g, r.x, r.y, 0, splitRow, chR, w, ColorUtil.mulAlpha(m.rightColor, eR * op));
        }

        // --- structure lines ---------------------------------------------------------------------
        int line = ColorUtil.mulAlpha(m.outlineColor, op * 0.75f);
        if (m.outlineEnabled) {
            BODY.fillBand(g, r.x, r.y, splitRow, splitRow + thick, 0, w, line);
            BODY.fillBand(g, r.x, r.y, thick, splitRow, chL, chR, line);
        }

        // --- scroll wheel ------------------------------------------------------------------------
        int ww = Math.max(4, Math.round(w * 0.16f));
        int wh = Math.max(6, Math.round(h * 0.22f));
        int wx = r.x + cx - ww / 2;
        int wy = r.y + Math.round(h * 0.13f);
        int wr = Math.max(1, ww / 2 - 1);
        int wheelFill = ColorUtil.lerp(ColorUtil.mulAlpha(m.backgroundEnabled ? m.backgroundColor : 0x80000000, 1f),
                m.middleColor, eM);
        Draw.fill(g, wx, wy, ww, wh, wr, ColorUtil.mulAlpha(ColorUtil.withAlpha(wheelFill, Math.max(200, ColorUtil.alpha(wheelFill))), op));
        drawWheelTicks(g, m, wx, wy, ww, wh, op, scale);
        if (m.outlineEnabled) {
            int oc = ColorUtil.lerp(m.outlineColor, ColorUtil.withAlpha(m.middleColor, 255), eM * 0.7f);
            Draw.outline(g, wx, wy, ww, wh, wr, thick, ColorUtil.mulAlpha(oc, op));
        }

        // --- scroll direction arrows -------------------------------------------------------------
        if (m.showScrollIndicator) {
            float up = InputTracker.upIndicator();
            float down = InputTracker.downIndicator();
            int size = Math.max(2, Math.round(h * 0.035f));
            if (up > 0.01f) {
                drawArrow(g, r.x + cx, wy - size - Math.max(1, Math.round(2 * scale)), size, true,
                        ColorUtil.mulAlpha(m.scrollColor, up * op));
            }
            if (down > 0.01f) {
                drawArrow(g, r.x + cx, wy + wh + Math.max(1, Math.round(2 * scale)), size, false,
                        ColorUtil.mulAlpha(m.scrollColor, down * op));
            }
        }

        // --- outline -----------------------------------------------------------------------------
        if (m.outlineEnabled) {
            BODY.outline(g, r.x, r.y, thick, ColorUtil.mulAlpha(m.outlineColor, op));
        }
    }

    /** Horizontal ticks inside the wheel that travel with the real scroll direction. */
    private static void drawWheelTicks(GuiGraphics g, HudConfig.Mouse m, int wx, int wy, int ww, int wh, float op, float scale) {
        int inner = Math.max(1, Math.round(scale));
        int top = wy + inner + 1;
        int bottom = wy + wh - inner - 1;
        int span = bottom - top;
        if (span < 4) return;
        float spacing = span / 3f;
        float phase = InputTracker.wheelPosition();
        phase -= (float) Math.floor(phase);
        float motion = Math.min(1f, Math.abs(InputTracker.upIndicator() - InputTracker.downIndicator()) + 0.35f);
        for (int i = -1; i <= 3; i++) {
            // scrolling up moves the ticks up (negative y)
            float ty = top + (i - phase) * spacing;
            int iy = Math.round(ty);
            if (iy < top || iy > bottom - 1) continue;
            float edge = Math.min(iy - top, bottom - iy) / (spacing * 0.5f);
            float fade = Math.min(1f, Math.max(0.15f, edge));
            int c = ColorUtil.mulAlpha(m.scrollColor, op * 0.55f * fade * motion);
            g.fill(wx + inner + 1, iy, wx + ww - inner - 1, iy + 1, c);
        }
    }

    private static void drawArrow(GuiGraphics g, int cx, int y, int size, boolean up, int color) {
        if ((color >>> 24) == 0) return;
        for (int i = 0; i < size; i++) {
            int half = up ? i : size - 1 - i;
            g.fill(cx - half, y + i, cx + half + 1, y + i + 1, color);
        }
    }
}
