# Mouse & Keyboard HUD

Client-side Fabric mod for **Minecraft Java 1.21.11** by **Ahmed**. A premium dark-glass keyboard + mouse overlay
(modelled on the reference image) with real-time input, split keyboard, HUD editor, ClickGUI and Mod Menu support.

## Versions (from the official FabricMC/fabric-example-mod 1.21.11 branch)
- Minecraft 1.21.11, Java 21, Mojang (official) mappings
- Fabric Loader 0.20.1, Fabric Loom `1.19-SNAPSHOT` (`net.fabricmc.fabric-loom-remap`)
- Fabric API 0.141.6+1.21.11, Mod Menu 17.0.1, Gradle 9.8.2 (wrapper)

## Build
```
./gradlew build          # Windows: gradlew.bat build
```
The mod jar is `build/libs/mousekeyboardhud-1.0.0.jar` (not the `-sources` jar). Test in dev with `./gradlew runClient`.

## Install
Put the jar in `.minecraft/mods` together with Fabric API (Mod Menu optional but recommended).

## Use
- **Right Shift** - ClickGUI, **Right Alt** - HUD Editor (rebind in Options > Controls > Key Binds, or in the ClickGUI > Keybinds).
- **HUD Editor**: drag to move, drag a corner handle to resize (uniform scale), mouse wheel over an element scales it,
  right-click for customization, toolbar for Keyboard / Mouse / Full / Left / Right toggles, resets, ClickGUI, Cancel, Save.
  Esc saves and closes. Cancel restores the state from when the editor was opened.
- **Full keyboard OFF** shows the left and right halves as separate, independently positionable pieces; each half can be hidden.
- **Mod Menu** opens the ClickGUI, which has a HUD Editor button.
- Settings are stored in `config/mousekeyboardhud.json`. A corrupt file is renamed `.broken` and defaults are used.

## Notes
- Fn has no keycode in GLFW, so it is drawn but cannot light up.
- Input is polled from GLFW; the scroll wheel is observed by chaining the existing GLFW scroll callback. Nothing is consumed or blocked.
- Project layout: `config/` persistence, `input/` GLFW polling + scroll, `animation/` easing & tweens, `rendering/` shape primitives,
  `hud/` layout + renderers, `hud/editor/` HUD editor, `gui/` ClickGUI, widgets and theme, `keybind/` key mappings.
