package com.ahmed.mousekeyboardhud.input;

import com.ahmed.mousekeyboardhud.MouseKeyboardHudClient;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWScrollCallback;

/**
 * Reads raw keyboard/mouse state straight from GLFW (polling) and observes the real scroll wheel by chaining
 * the existing GLFW scroll callback. Nothing is consumed, so gameplay input is never blocked or delayed.
 */
public final class InputTracker {

    private static boolean installed;

    /** Accumulated wheel position in notches (positive = scrolled up). */
    private static float wheelTarget;
    private static float wheelDisplay;
    /** Fading direction indicators, 1 = just scrolled. */
    private static float upIndicator;
    private static float downIndicator;

    private InputTracker() {}

    /** Hooks the scroll callback. Must run after the client window exists. */
    public static void install(Minecraft mc) {
        if (installed) return;
        long handle = mc.getWindow().handle();
        GLFWScrollCallback[] previous = new GLFWScrollCallback[1];
        previous[0] = GLFW.glfwSetScrollCallback(handle, new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xOffset, double yOffset) {
                try {
                    onScroll(yOffset);
                } catch (RuntimeException e) {
                    MouseKeyboardHudClient.LOGGER.warn("Scroll tracking failed", e);
                }
                if (previous[0] != null) {
                    previous[0].invoke(window, xOffset, yOffset);
                }
            }
        });
        installed = true;
    }

    private static void onScroll(double yOffset) {
        if (yOffset == 0.0) return;
        float dir = yOffset > 0 ? 1f : -1f;
        wheelTarget += dir;
        if (dir > 0) {
            upIndicator = 1f;
        } else {
            downIndicator = 1f;
        }
    }

    public static boolean isKeyDown(long handle, int code) {
        return code >= 0 && GLFW.glfwGetKey(handle, code) == GLFW.GLFW_PRESS;
    }

    public static boolean isMouseDown(long handle, int button) {
        return GLFW.glfwGetMouseButton(handle, button) == GLFW.GLFW_PRESS;
    }

    /** Advances the smoothed wheel + indicator state. Call once per rendered frame. */
    public static void updateScroll(float dt, boolean animated) {
        if (animated) {
            float k = 1f - (float) Math.exp(-22f * dt);
            wheelDisplay += (wheelTarget - wheelDisplay) * k;
            float decay = dt * 3.2f;
            upIndicator = Math.max(0f, upIndicator - decay);
            downIndicator = Math.max(0f, downIndicator - decay);
        } else {
            wheelDisplay = wheelTarget;
            upIndicator = Math.max(0f, upIndicator - dt * 6f);
            downIndicator = Math.max(0f, downIndicator - dt * 6f);
        }
        // keep floats small; only the fractional phase matters visually
        if (Math.abs(wheelTarget) > 1000f) {
            float shift = (float) Math.floor(wheelTarget);
            wheelTarget -= shift;
            wheelDisplay -= shift;
        }
    }

    /** Smoothed wheel rotation in notches. */
    public static float wheelPosition() {
        return wheelDisplay;
    }

    public static float upIndicator() {
        return upIndicator;
    }

    public static float downIndicator() {
        return downIndicator;
    }
}
