package com.abhikaran.floatingkeyboard.client;

import com.abhikaran.floatingkeyboard.config.KeyboardConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class KeyboardOverlay {
    private static boolean visible;
    private static boolean symbolsPage;
    private static boolean capsLock;
    private static boolean dragging;
    private static double dragOffsetX;
    private static double dragOffsetY;
    private static TextFieldWidget lastTarget;

    private KeyboardOverlay() {}

    public static boolean isVisible() { return visible; }
    public static boolean isDragging() { return dragging; }

    public static void toggle() {
        visible = !visible;
        if (visible) captureTarget();
        else dragging = false;
    }

    public static void show() {
        visible = true;
        captureTarget();
    }

    public static void hide() {
        visible = false;
        dragging = false;
    }

    public static void tick() {
        if (visible && MinecraftClient.getInstance().currentScreen != null) {
            TextFieldWidget current = findFocusedTextField(MinecraftClient.getInstance().currentScreen);
            if (current != null && current.isFocused()) lastTarget = current;
        }
    }

    private static void captureTarget() {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen != null) {
            TextFieldWidget field = findFocusedTextField(screen);
            if (field != null) lastTarget = field;
        }
    }

    public static TextFieldWidget getTarget() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen != null) {
            TextFieldWidget field = findFocusedTextField(client.currentScreen);
            if (field != null && field.isFocused()) {
                lastTarget = field;
                return field;
            }
        }
        return lastTarget;
    }

    private static TextFieldWidget findFocusedTextField(Screen screen) {
        Element focused = screen.getFocused();
        if (focused instanceof TextFieldWidget field) return field;
        for (Element child : screen.children()) {
            TextFieldWidget found = findTextField(child);
            if (found != null && found.isFocused()) return found;
        }
        return null;
    }

    private static TextFieldWidget findTextField(Element element) {
        if (element instanceof TextFieldWidget field) return field;
        if (element instanceof ParentElement parent) {
            for (Element child : parent.children()) {
                TextFieldWidget found = findTextField(child);
                if (found != null) return found;
            }
        }
        return null;
    }

    public static int width() { return Math.round(430 * (float) FloatingKeyboardClient.CONFIG.scale); }
    public static int height() { return Math.round(270 * (float) FloatingKeyboardClient.CONFIG.scale); }

    public static int x(int screenWidth) {
        int w = width();
        int x = FloatingKeyboardClient.CONFIG.x;
        if (x < 0) x = (screenWidth - w) / 2;
        return Math.max(2, Math.min(screenWidth - w - 2, x));
    }

    public static int y(int screenHeight) {
        int h = height();
        int y = FloatingKeyboardClient.CONFIG.y;
        if (y < 0) y = screenHeight - h - 18;
        return Math.max(2, Math.min(screenHeight - h - 2, y));
    }

    public static void beginDrag(double mouseX, double mouseY) {
        if (!visible) return;
        int x = x(MinecraftClient.getInstance().getWindow().getScaledWidth());
        int y = y(MinecraftClient.getInstance().getWindow().getScaledHeight());
        if (mouseX >= x && mouseX <= x + width() && mouseY >= y && mouseY <= y + 24 * FloatingKeyboardClient.CONFIG.scale) {
            dragging = true;
            dragOffsetX = mouseX - x;
            dragOffsetY = mouseY - y;
        }
    }

    public static void dragTo(double mouseX, double mouseY) {
        if (!dragging) return;
        int sw = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int sh = MinecraftClient.getInstance().getWindow().getScaledHeight();
        int nx = (int) Math.round(mouseX - dragOffsetX);
        int ny = (int) Math.round(mouseY - dragOffsetY);
        FloatingKeyboardClient.CONFIG.x = Math.max(2, Math.min(sw - width() - 2, nx));
        FloatingKeyboardClient.CONFIG.y = Math.max(2, Math.min(sh - height() - 2, ny));
    }

    public static void endDrag() {
        if (dragging) FloatingKeyboardClient.CONFIG.save();
        dragging = false;
    }

    public static boolean contains(double mouseX, double mouseY) {
        int x = x(MinecraftClient.getInstance().getWindow().getScaledWidth());
        int y = y(MinecraftClient.getInstance().getWindow().getScaledHeight());
        return mouseX >= x && mouseX <= x + width() && mouseY >= y && mouseY <= y + height();
    }

    public static boolean click(double mouseX, double mouseY) {
        if (!visible) return false;
        if (!contains(mouseX, mouseY)) return false;
        if (mouseY <= y(MinecraftClient.getInstance().getWindow().getScaledHeight()) + 24 * FloatingKeyboardClient.CONFIG.scale) {
            beginDrag(mouseX, mouseY);
            return true;
        }

        ButtonHit hit = buttonAt(mouseX, mouseY);
        if (hit == null) return true;
        perform(hit.action);
        return true;
    }

    private static void perform(Action action) {
        switch (action.type) {
            case TEXT -> type(action.value);
            case CAPS -> capsLock = !capsLock;
            case BACKSPACE -> sendKey(GLFW.GLFW_KEY_BACKSPACE);
            case ENTER -> sendKey(GLFW.GLFW_KEY_ENTER);
            case PASTE -> paste();
            case SPACE -> type(" ");
            case NEXT -> symbolsPage = true;
            case PREV -> symbolsPage = false;
        }
    }

    private static void type(String value) {
        TextFieldWidget target = getTarget();
        if (target != null && target.isEditable()) {
            target.charTyped(new CharInput(value.codePointAt(0), 0));
        }
    }

    private static void paste() {
        TextFieldWidget target = getTarget();
        if (target != null && target.isEditable()) target.write(MinecraftClient.getInstance().keyboard.getClipboard());
    }

    private static void sendKey(int key) {
        MinecraftClient client = MinecraftClient.getInstance();
        KeyInput input = new KeyInput(key, 0, 0);
        if (client.currentScreen != null) {
            client.currentScreen.keyPressed(input);
        } else {
            TextFieldWidget target = getTarget();
            if (target != null) target.keyPressed(input);
        }
    }

    public static void render(DrawContext context, int mouseX, int mouseY) {
        if (!visible) return;
        MinecraftClient client = MinecraftClient.getInstance();
        int sw = context.getScaledWindowWidth();
        int sh = client.getWindow().getScaledHeight();
        int px = x(sw);
        int py = y(sh);
        double s = FloatingKeyboardClient.CONFIG.scale;

        int panelW = width();
        int panelH = height();
        context.fill(px, py, px + panelW, py + panelH, 0xE91A1B22);
        context.fill(px, py, px + panelW, py + Math.max(20, (int)(25*s)), 0xFF25283A);
        context.drawTextWithShadow(client.textRenderer, "Floating Keyboard", px + (int)(10*s), py + (int)(7*s), 0xFFE8EAF6);
        context.drawTextWithShadow(client.textRenderer, symbolsPage ? "123" : "ABC", px + panelW - (int)(35*s), py + (int)(7*s), 0xFF8FA8FF);

        List<KeyVisual> keys = buildLayout(px, py, s);
        for (KeyVisual key : keys) {
            boolean hover = mouseX >= key.x && mouseX <= key.x + key.w && mouseY >= key.y && mouseY <= key.y + key.h;
            int bg = hover ? 0xFF4A4E63 : 0xFF30333D;
            if (key.action.type == ActionType.CAPS && capsLock) bg = 0xFF465D92;
            context.fill(key.x, key.y, key.x + key.w, key.y + key.h, bg);
            context.drawCenteredTextWithShadow(client.textRenderer, key.label, key.x + key.w / 2, key.y + Math.max(3, (key.h - 9) / 2), 0xFFF3F4F7);
        }

        TextFieldWidget target = getTarget();
        if (target == null) {
            context.drawTextWithShadow(client.textRenderer, "No focused text field", px + (int)(10*s), py + panelH - (int)(13*s), 0xFFFF9E9E);
        }
    }

    private static List<KeyVisual> buildLayout(int px, int py, double s) {
        List<KeyVisual> out = new ArrayList<>();
        int gap = Math.max(2, (int)(4*s));
        int keyH = Math.max(16, (int)(34*s));
        int small = Math.max(24, (int)(38*s));
        int areaTop = py + (int)(30*s);
        int contentW = width() - (int)(16*s);
        int cols = 10;
        int cellW = (contentW - gap * (cols - 1)) / cols;

        if (!symbolsPage) {
            addRow(out, "QWERTYUIOP", areaTop, px + (int)(8*s), cellW, keyH, gap, s);
            addRow(out, "ASDFGHJKL", areaTop + keyH + gap, px + (int)(30*s), cellW, keyH, gap, s);
            addRow(out, "ZXCVBNM", areaTop + 2*(keyH + gap), px + (int)(55*s), cellW, keyH, gap, s);
            int cy = areaTop + 3*(keyH + gap);
            addControl(out, "CAPS", px + (int)(8*s), cy, (int)(70*s), keyH, Action.caps());
            addControl(out, "⌫", px + (int)(82*s), cy, (int)(48*s), keyH, Action.backspace());
            addControl(out, "PASTE", px + (int)(134*s), cy, (int)(62*s), keyH, Action.paste());
            addControl(out, "SPACE", px + (int)(200*s), cy, (int)(110*s), keyH, Action.space());
            addControl(out, "ENTER", px + (int)(314*s), cy, (int)(62*s), keyH, Action.enter());
            addControl(out, "123", px + (int)(380*s), cy, (int)(42*s), keyH, Action.next());
        } else {
            addRow(out, "1234567890", areaTop, px + (int)(8*s), cellW, keyH, gap, s);
            String symbols = "@#£_&-+()/*\"':;!?~`|•√π÷×§∆€¥$¢^°={}";
            int index = 0;
            int y = areaTop + keyH + gap;
            for (int row = 0; index < symbols.length() && row < 3; row++) {
                String part = symbols.substring(index, Math.min(index + cols, symbols.length()));
                addRow(out, part, y, px + (int)(8*s), cellW, keyH, gap, s);
                index += part.length(); y += keyH + gap;
            }
            String more = "\\%©®™✓[]><.,";
            addRow(out, more, y, px + (int)(8*s), cellW, keyH, gap, s);
            y += keyH + gap;
            addControl(out, "⌫", px + (int)(8*s), y, (int)(48*s), keyH, Action.backspace());
            addControl(out, "ABC", px + (int)(60*s), y, (int)(52*s), keyH, Action.prev());
            addControl(out, "PASTE", px + (int)(116*s), y, (int)(62*s), keyH, Action.paste());
            addControl(out, "SPACE", px + (int)(182*s), y, (int)(112*s), keyH, Action.space());
            addControl(out, "ENTER", px + (int)(298*s), y, (int)(62*s), keyH, Action.enter());
        }
        return out;
    }

    private static void addRow(List<KeyVisual> out, String labels, int y, int startX, int cellW, int h, int gap, double scale) {
        for (int i = 0; i < labels.length(); i++) {
            String label = String.valueOf(labels.charAt(i));
            String value = label;
            if (Character.isLetter(label.charAt(0)) && !capsLock) value = label.toLowerCase();
            out.add(new KeyVisual(startX + i * (cellW + gap), y, cellW, h, value, Action.text(value)));
        }
    }

    private static void addControl(List<KeyVisual> out, String label, int x, int y, int w, int h, Action action) {
        out.add(new KeyVisual(x, y, w, h, label, action));
    }

    private static ButtonHit buttonAt(double mx, double my) {
        for (KeyVisual key : buildLayout(x(MinecraftClient.getInstance().getWindow().getScaledWidth()), y(MinecraftClient.getInstance().getWindow().getScaledHeight()), FloatingKeyboardClient.CONFIG.scale)) {
            if (mx >= key.x && mx <= key.x + key.w && my >= key.y && my <= key.y + key.h) return new ButtonHit(key.action);
        }
        return null;
    }

    private record KeyVisual(int x, int y, int w, int h, String label, Action action) {}
    private record ButtonHit(Action action) {}
    private record Action(ActionType type, String value) {
        static Action text(String s) { return new Action(ActionType.TEXT, s); }
        static Action caps() { return new Action(ActionType.CAPS, ""); }
        static Action backspace() { return new Action(ActionType.BACKSPACE, ""); }
        static Action paste() { return new Action(ActionType.PASTE, ""); }
        static Action enter() { return new Action(ActionType.ENTER, ""); }
        static Action space() { return new Action(ActionType.SPACE, ""); }
        static Action next() { return new Action(ActionType.NEXT, ""); }
        static Action prev() { return new Action(ActionType.PREV, ""); }
    }
    private enum ActionType { TEXT, CAPS, BACKSPACE, PASTE, ENTER, SPACE, NEXT, PREV }
}
