package com.abhikaran.floatingkeyboard.mixin;

import com.abhikaran.floatingkeyboard.client.KeyboardOverlay;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenRenderMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void floatingKeyboard$render(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (KeyboardOverlay.isVisible()) {
            KeyboardOverlay.render(context, mouseX, mouseY);
        }
    }
}
