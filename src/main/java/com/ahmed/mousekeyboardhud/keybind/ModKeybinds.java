package com.ahmed.mousekeyboardhud.keybind;

import com.ahmed.mousekeyboardhud.gui.ClickGuiScreen;
import com.ahmed.mousekeyboardhud.hud.editor.HudEditorScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * Registers the two configurable keybinds. They appear in Options > Controls > Key Binds
 * under the "Mouse &amp; Keyboard HUD" category and use Minecraft's normal binding system.
 */
public final class ModKeybinds {

    public static KeyMapping openClickGui;
    public static KeyMapping openHudEditor;

    private ModKeybinds() {}

    public static void register() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("mousekeyboardhud", "main"));

        // Right Shift and Right Alt are rarely bound by vanilla controls, so they do not conflict.
        openClickGui = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.mousekeyboardhud.open_clickgui", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        openHudEditor = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.mousekeyboardhud.open_hud_editor", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_ALT, category));

        ClientTickEvents.END_CLIENT_TICK.register(KeybindHandler::onTick);
    }

    private static final class KeybindHandler {
        static void onTick(Minecraft mc) {
            while (openClickGui.consumeClick()) {
                if (mc.screen == null) mc.setScreen(new ClickGuiScreen(null));
            }
            while (openHudEditor.consumeClick()) {
                if (mc.screen == null) mc.setScreen(new HudEditorScreen(null));
            }
        }
    }
}
