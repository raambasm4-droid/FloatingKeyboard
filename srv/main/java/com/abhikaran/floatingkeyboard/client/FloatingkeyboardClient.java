package com.abhikaran.floatingkeyboard.client;

import com.abhikaran.floatingkeyboard.config.KeyboardConfig;
import com.abhikaran.floatingkeyboard.screen.KeyboardSettingsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class FloatingKeyboardClient implements ClientModInitializer {
    public static final String MOD_ID = "floating_keyboard";
    public static final KeyboardConfig CONFIG = KeyboardConfig.load();

    public static KeyBinding toggleKey;
    public static KeyBinding settingsKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.floating_keyboard.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.floating_keyboard"
        ));
        settingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.floating_keyboard.settings",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F7,
                "category.floating_keyboard"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> tick());

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CHAT,
                Identifier.of(MOD_ID, "keyboard"),
                (context, delta) -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.currentScreen == null && KeyboardOverlay.isVisible()) {
                        KeyboardOverlay.render(context, (int) client.mouse.getX(), (int) client.mouse.getY());
                    }
                }
        );
    }

    public static void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (toggleKey != null) {
            while (toggleKey.wasPressed()) {
                KeyboardOverlay.toggle();
            }
        }
        if (settingsKey != null) {
            while (settingsKey.wasPressed()) {
                client.setScreen(new KeyboardSettingsScreen(client.currentScreen));
            }
        }
        KeyboardOverlay.tick();
    }
}
