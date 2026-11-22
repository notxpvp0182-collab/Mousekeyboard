package com.ahmed.mousekeyboardhud.hud;

import com.ahmed.mousekeyboardhud.animation.Tween;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

/** One key of the keyboard layout: geometry (in key units), GLFW code, labels and animation state. */
public final class KeyDef {

    public enum Half { LEFT, RIGHT }

    /** Which layout variants a key appears in. */
    public enum Mode { BOTH, FULL_ONLY, SPLIT_ONLY }

    public final String top;
    public final String label;
    public final int code;
    public final float x;
    public final float y;
    public final float w;
    public final float h;
    public final Half half;
    public final Mode mode;

    /** Animation progress 0 (idle) .. 1 (pressed). */
    public final Tween tween = new Tween();

    private Component mainText;
    private Component topText;
    private int mainWidth = -1;
    private int topWidth;

    public KeyDef(String top, String label, int code, float x, float y, float w, float h, Half half, Mode mode) {
        this.top = top;
        this.label = label;
        this.code = code;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.half = half;
        this.mode = mode;
    }

    private void ensureText(Font font) {
        if (mainWidth >= 0) return;
        mainText = Component.literal(label).withStyle(ChatFormatting.BOLD);
        mainWidth = font.width(mainText);
        if (top != null) {
            topText = Component.literal(top).withStyle(ChatFormatting.BOLD);
            topWidth = font.width(topText);
        }
    }

    public Component mainText(Font font) {
        ensureText(font);
        return mainText;
    }

    public int mainWidth(Font font) {
        ensureText(font);
        return mainWidth;
    }

    public Component topText(Font font) {
        ensureText(font);
        return topText;
    }

    public int topWidth(Font font) {
        ensureText(font);
        return topWidth;
    }
}
