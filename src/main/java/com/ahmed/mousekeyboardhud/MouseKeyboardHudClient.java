package com.ahmed.mousekeyboardhud;

import com.ahmed.mousekeyboardhud.config.ConfigManager;
import com.ahmed.mousekeyboardhud.hud.HudManager;
import com.ahmed.mousekeyboardhud.input.InputTracker;
import com.ahmed.mousekeyboardhud.keybind.ModKeybinds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Client entrypoint of the Mouse &amp; Keyboard HUD mod (client-side only). */
public final class MouseKeyboardHudClient implements ClientModInitializer {

    public static final String MOD_ID = "mousekeyboardhud";
    public static final Logger LOGGER = LoggerFactory.getLogger("Mouse & Keyboard HUD");

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        ModKeybinds.register();

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                Identifier.fromNamespaceAndPath(MOD_ID, "hud"),
                (graphics, deltaTracker) -> HudManager.render(graphics));

        // The window exists once the client has started; only then can the scroll callback be chained.
        ClientLifecycleEvents.CLIENT_STARTED.register(InputTracker::install);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ConfigManager.save());
    }
}
