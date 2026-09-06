package com.abhikaran.floatingkeyboard.screen;

import com.abhikaran.floatingkeyboard.client.FloatingKeyboardClient;
import com.abhikaran.floatingkeyboard.config.KeyboardConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class KeyboardSettingsScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget xField;
    private TextFieldWidget yField;
    private TextFieldWidget scaleField;

    public KeyboardSettingsScreen(Screen parent) {
        super(Text.translatable("floating_keyboard.settings.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        KeyboardConfig cfg = FloatingKeyboardClient.CONFIG;
        int center = width / 2;
        xField = new TextFieldWidget(textRenderer, center - 110, 65, 220, 20, Text.translatable("floating_keyboard.settings.x"));
        yField = new TextFieldWidget(textRenderer, center - 110, 105, 220, 20, Text.translatable("floating_keyboard.settings.y"));
        scaleField = new TextFieldWidget(textRenderer, center - 110, 145, 220, 20, Text.translatable("floating_keyboard.settings.scale"));
        xField.setText(Integer.toString(cfg.x));
        yField.setText(Integer.toString(cfg.y));
        scaleField.setText(String.format(java.util.Locale.ROOT, "%.2f", cfg.scale));
        addDrawableChild(xField);
        addDrawableChild(yField);
        addDrawableChild(scaleField);

        addDrawableChild(ButtonWidget.builder(Text.translatable("floating_keyboard.settings.save"), b -> saveAndClose())
                .dimensions(center - 110, 185, 105, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("floating_keyboard.settings.reset"), b -> {
            cfg.reset();
            xField.setText(Integer.toString(cfg.x));
            yField.setText(Integer.toString(cfg.y));
            scaleField.setText("1.00");
        }).dimensions(center + 5, 185, 115, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"), b -> close())
                .dimensions(center - 55, 215, 110, 20).build());
    }

    private void saveAndClose() {
        KeyboardConfig cfg = FloatingKeyboardClient.CONFIG;
        try { cfg.x = Integer.parseInt(xField.getText().trim()); } catch (Exception ignored) {}
        try { cfg.y = Integer.parseInt(yField.getText().trim()); } catch (Exception ignored) {}
        try { cfg.scale = Double.parseDouble(scaleField.getText().trim()); } catch (Exception ignored) {}
        cfg.save();
        close();
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        renderBackground(context, mouseX, mouseY, deltaTicks);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 35, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer, Text.translatable("floating_keyboard.settings.x"), width / 2 - 110, 55, 0xFFD7D9E3);
        context.drawTextWithShadow(textRenderer, Text.translatable("floating_keyboard.settings.y"), width / 2 - 110, 95, 0xFFD7D9E3);
        context.drawTextWithShadow(textRenderer, Text.translatable("floating_keyboard.settings.scale"), width / 2 - 110, 135, 0xFFD7D9E3);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("floating_keyboard.settings.hint"), width / 2, 250, 0xFF9EA4B8);
        super.render(context, mouseX, mouseY, deltaTicks);
    }
}
