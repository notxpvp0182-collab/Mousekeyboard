package com.ahmed.mousekeyboardhud.config;

import com.ahmed.mousekeyboardhud.animation.Easing;

/**
 * Plain data model of every persisted setting. Colors are ARGB ints.
 * Positions are stored as fractions (0..1) of the GUI-scaled screen so they survive resolution changes.
 */
public final class HudConfig {

    public int configVersion = 1;
    public General general = new General();
    public Keyboard keyboard = new Keyboard();
    public Mouse mouse = new Mouse();
    public Animation animation = new Animation();

    /** Top-left anchor of a HUD element as fractions of the screen. */
    public static final class Pos {
        public float x;
        public float y;

        public Pos() {}

        public Pos(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public static final class General {
        public boolean hudEnabled = true;
        /** Draw the HUD over inventory / chat / pause screens too. */
        public boolean showInScreens = false;
        /** GUI accent theme index, see ThemeManager. */
        public int guiTheme = 0;
    }

    public static final class Keyboard {
        public boolean enabled = true;
        public boolean fullKeyboard = true;
        public boolean leftHalf = true;
        public boolean rightHalf = true;

        public Pos fullPos = new Pos(0.04f, 0.70f);
        public Pos leftPos = new Pos(0.04f, 0.70f);
        public Pos rightPos = new Pos(0.30f, 0.70f);

        public float scale = 0.85f;
        public float halvesScale = 0.85f;
        /** Key edge length in design pixels at scale 1. */
        public float keySize = 18f;
        /** Gap between keys in design pixels at scale 1. */
        public float keySpacing = 2.5f;
        public float cornerRadius = 4f;
        public float opacity = 1f;

        public boolean backgroundEnabled = true;
        public int backgroundColor = 0xB0080A10;

        public boolean keyBackgroundEnabled = true;
        public int keyColor = 0xC0101218;
        public int pressedColor = 0xE04D9DFF;
        public int textColor = 0xFFF2F4F8;

        public boolean outlineEnabled = true;
        public int outlineColor = 0xB8E6EAF2;
        public float outlineWidth = 1f;

        public boolean glowEnabled = true;
        public int glowColor = 0xFF6FB4FF;
        public float glowStrength = 1f;
    }

    public static final class Mouse {
        public boolean enabled = true;
        public Pos pos = new Pos(0.52f, 0.70f);
        public float scale = 0.85f;
        public float cornerRadius = 0.75f;
        public float opacity = 1f;

        public boolean backgroundEnabled = true;
        public int backgroundColor = 0xE00A0C12;

        public boolean outlineEnabled = true;
        public int outlineColor = 0xB8E6EAF2;
        public float outlineWidth = 1f;

        public int leftColor = 0xE04D9DFF;
        public int rightColor = 0xE04D9DFF;
        public int middleColor = 0xE04D9DFF;
        public int scrollColor = 0xFFBFDFFF;
        public boolean showScrollIndicator = true;

        public boolean glowEnabled = true;
        public int glowColor = 0xFF6FB4FF;
        public float glowStrength = 1f;
    }

    public static final class Animation {
        public boolean enabled = true;
        /** 0.25 .. 3 multiplier. */
        public float speed = 1f;
        public Easing easing = Easing.EASE_OUT;
        /** Key travel when pressed, in design pixels. */
        public float pressDepth = 1.2f;
        public float glowIntensity = 1f;
    }
}
