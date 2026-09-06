# Floating Keyboard

**Minecraft Java Edition 1.21.11 / Fabric**  
Author: **Abhikaran**

Floating Keyboard adds a touch-friendly, floating on-screen keyboard to Minecraft. It is designed for touch controls, on-screen cursors and mouse input, including Mojo Launcher-style pointer control.

## Features

- Two keyboard pages: letters and numbers/symbols.
- QWERTY A-Z layout with Caps Lock.
- Backspace, Paste, Space and Enter.
- Number row and the requested common symbol set.
- Right Shift toggles the keyboard by default.
- F7 opens the built-in settings screen.
- Optional Mod Menu integration exposes the same settings screen from Mod Menu.
- Drag the top bar to move the keyboard.
- Resize from settings with a 0.55x–1.60x scale.
- Uses the currently focused Minecraft `TextFieldWidget` as the typing target, so it works with chat, server fields, inventory search and many other vanilla text boxes.
- Client-side only; the server does not need this mod.

## Important behavior

The on-screen keyboard's letters are **not** Minecraft movement keybinds. Pressing `W`, `A`, `S` or `D` on the on-screen keyboard inserts the corresponding character into the focused text field. Physical keyboard input remains normal Minecraft input.

If you manually open the keyboard in a screen that has no focused text field, the keyboard can still be moved and its page changed, but there is no text destination until a text field is focused.

## Build

Use Java 21 and run:

```bash
./gradlew build
```

The built mod is produced in `build/libs/`.

### Dependencies

- Minecraft 1.21.11
- Fabric Loader 0.19.5+
- Fabric API 0.141.6+1.21.11
- Mod Menu 17.0.0 is optional.
