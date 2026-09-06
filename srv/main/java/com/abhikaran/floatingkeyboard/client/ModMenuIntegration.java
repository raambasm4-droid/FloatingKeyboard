package com.abhikaran.floatingkeyboard.client;

import com.abhikaran.floatingkeyboard.screen.KeyboardSettingsScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;

public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return KeyboardSettingsScreen::new;
    }
}

