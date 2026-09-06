package com.abhikaran.floatingkeyboard.mixin;

import com.abhikaran.floatingkeyboard.client.KeyboardOverlay;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void floatingKeyboard$mouseButton(long window, MouseInput input, int action, CallbackInfo ci) {
        if (!KeyboardOverlay.isVisible()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        double x = client.mouse.getX();
        double y = client.mouse.getY();
        if (action == GLFW.GLFW_PRESS) {
            if (KeyboardOverlay.contains(x, y)) {
                KeyboardOverlay.click(x, y);
                ci.cancel();
            }
        } else if (action == GLFW.GLFW_RELEASE && KeyboardOverlay.isDragging()) {
            KeyboardOverlay.endDrag();
            ci.cancel();
        }
    }

    @Inject(method = "onCursorPos", at = @At("HEAD"))
    private void floatingKeyboard$cursor(long window, double x, double y, CallbackInfo ci) {
        if (KeyboardOverlay.isDragging()) KeyboardOverlay.dragTo(x, y);
    }
}
