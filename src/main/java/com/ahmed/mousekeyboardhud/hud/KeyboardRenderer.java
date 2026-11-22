package com.ahmed.mousekeyboardhud.hud;

import com.ahmed.mousekeyboardhud.animation.Easing;
import com.ahmed.mousekeyboardhud.config.HudConfig;
import com.ahmed.mousekeyboardhud.hud.components.Rect;
import com.ahmed.mousekeyboardhud.input.InputTracker;
import com.ahmed.mousekeyboardhud.rendering.Draw;
import com.ahmed.mousekeyboardhud.util.ColorUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Draws one keyboard view (full / left half / right half) in the reference image's dark-glass style. */
public final class KeyboardRenderer {

    private KeyboardRenderer() {}

    public static float unit(HudConfig.Keyboard k, float scale) {
        return (k.keySize + k.keySpacing) * scale;
    }

    /** Computes size in GUI pixels of a view at the given scale (position is left untouched). */
    public static void measure(KeyboardLayout.View view, HudConfig.Keyboard k, float scale, Rect out) {
        float unit = unit(k, scale);
        float gap = k.keySpacing * scale;
        int pad = padding(unit);
        out.w = Math.round(view.widthUnits() * unit - gap) + pad * 2;
        out.h = Math.round(view.heightUnits() * unit - gap) + pad * 2;
    }

    private static int padding(float unit) {
        return Math.max(2, Math.round(unit * 0.28f));
    }

    public static void render(GuiGraphics g, Font font, long window, KeyboardLayout.View view, HudConfig cfg,
                              float scale, Rect r, boolean live, float dt) {
        HudConfig.Keyboard k = cfg.keyboard;
        HudConfig.Animation anim = cfg.animation;
        float op = k.opacity;
        float unit = unit(k, scale);
        float gap = k.keySpacing * scale;
        int pad = padding(unit);
        int radius = Math.max(0, Math.round(k.cornerRadius * scale));
        int thick = Math.max(1, Math.round(k.outlineWidth * scale));
        Easing easing = anim.easing;

        // Chassis (the "keyboard background"): chamfered dark glass plate like the reference.
        if (k.backgroundEnabled) {
            int cut = Math.max(2, Math.round(unit * 0.55f));
            Draw.fill(g, r.x, r.y, r.w, r.h, cut, ColorUtil.mulAlpha(k.backgroundColor, op), true);
            Draw.gradientV(g, r.x + cut, r.y + 1, r.w - cut * 2, Math.max(2, r.h / 4),
                    ColorUtil.mulAlpha(0x1CFFFFFF, op), 0x00FFFFFF);
            if (k.outlineEnabled) {
                Draw.outline(g, r.x, r.y, r.w, r.h, cut, thick, ColorUtil.mulAlpha(k.outlineColor, op * 0.45f), true);
            }
        }

        float pressSeconds = 0.06f / anim.speed;
        float releaseSeconds = 0.15f / anim.speed;
        int spread = Math.max(1, Math.round((2f + 2f * k.glowStrength) * scale));

        for (KeyDef key : view.keys) {
            boolean down = live && InputTracker.isKeyDown(window, key.code);
            if (anim.enabled) {
                key.tween.update(down, dt, down ? pressSeconds : releaseSeconds);
            } else {
                key.tween.set(down ? 1f : 0f);
            }
            float e = key.tween.eased(easing);

            int x0 = r.x + pad + Math.round((key.x - view.minX) * unit);
            int x1 = r.x + pad + Math.round((key.x + key.w - view.minX) * unit - gap);
            int y0 = r.y + pad + Math.round((key.y - view.minY) * unit);
            int y1 = r.y + pad + Math.round((key.y + key.h - view.minY) * unit - gap);
            int w = x1 - x0;
            int h = y1 - y0;
            if (w <= 0 || h <= 0) continue;

            if (anim.enabled && e > 0f) {
                y0 += Math.round(anim.pressDepth * scale * e);
            }

            if (k.glowEnabled && e > 0.02f) {
                int gc = ColorUtil.mulAlpha(k.glowColor, e * anim.glowIntensity * op);
                Draw.glow(g, x0, y0, w, h, radius, spread, gc, false);
            }

            int base = k.keyBackgroundEnabled ? k.keyColor : ColorUtil.withAlpha(k.pressedColor, 0);
            int fill = ColorUtil.lerp(base, k.pressedColor, e);
            Draw.fill(g, x0, y0, w, h, radius, ColorUtil.mulAlpha(fill, op));

            if (k.keyBackgroundEnabled) {
                int gloss = Math.max(1, Math.round(h * 0.42f));
                Draw.gradientV(g, x0 + radius, y0 + thick, w - radius * 2, gloss,
                        ColorUtil.mulAlpha(0x22FFFFFF, op * (1f - e * 0.6f)), 0x00FFFFFF);
            }
            if (k.outlineEnabled) {
                int oc = ColorUtil.lerp(k.outlineColor, ColorUtil.withAlpha(k.pressedColor, 255), e * 0.65f);
                Draw.outline(g, x0, y0, w, h, radius, thick, ColorUtil.mulAlpha(oc, op));
            }
            drawLabel(g, font, key, x0, y0, w, h, ColorUtil.mulAlpha(k.textColor, op));
        }
    }

    private static void drawLabel(GuiGraphics g, Font font, KeyDef key, int x, int y, int w, int h, int color) {
        if ((color >>> 24) == 0) return;
        float cx = x + w * 0.5f;
        boolean dual = key.top != null && key.top.length() == 1 && key.label.length() == 1;
        boolean single = key.label.length() == 1 && key.top == null;
        if (dual) {
            drawText(g, font, key.topText(font), key.topWidth(font), cx, y + h * 0.31f, h * 0.30f, w * 0.8f, color);
            drawText(g, font, key.mainText(font), key.mainWidth(font), cx, y + h * 0.69f, h * 0.40f, w * 0.8f, color);
        } else if (single) {
            drawText(g, font, key.mainText(font), key.mainWidth(font), cx, y + h * 0.5f, h * 0.52f, w * 0.8f, color);
        } else {
            // word labels (ESC, SHIFT, F1 ...): the text lives in key.top for ordinary keys
            String text = key.top != null && key.top.length() > 1 ? key.top : key.label;
            if (text == key.label) {
                drawText(g, font, key.mainText(font), key.mainWidth(font), cx, y + h * 0.5f, h * 0.32f, w * 0.86f, color);
            } else {
                drawText(g, font, key.topText(font), key.topWidth(font), cx, y + h * 0.5f, h * 0.32f, w * 0.86f, color);
            }
        }
    }

    /** Draws text centered on (cx, cy) with the glyph height of {@code targetHeight}, shrunk to fit maxWidth. */
    private static void drawText(GuiGraphics g, Font font, net.minecraft.network.chat.Component text, int textWidth,
                                 float cx, float cy, float targetHeight, float maxWidth, int color) {
        if (textWidth <= 0) return;
        float s = targetHeight / 8f;
        if (textWidth * s > maxWidth) s = maxWidth / textWidth;
        if (s < 0.2f) return;
        var pose = g.pose();
        pose.pushMatrix();
        pose.translate(cx - textWidth * s * 0.5f, cy - 4.5f * s);
        pose.scale(s, s);
        g.drawString(font, text, 0, 0, color, false);
        pose.popMatrix();
    }
}
