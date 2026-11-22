package com.ahmed.mousekeyboardhud;

import com.ahmed.mousekeyboardhud.gui.ClickGuiScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Makes the Mod Menu "configure" button open the ClickGUI (which links to the HUD editor). */
public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ClickGuiScreen::new;
    }
}
