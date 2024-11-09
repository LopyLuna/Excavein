package uwu.lopyluna.excavein.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import uwu.lopyluna.excavein.network.KeybindPacket;
import uwu.lopyluna.excavein.network.SelectionInspectionPacket;

import static uwu.lopyluna.excavein.client.SelectionMode.setMode;
import static uwu.lopyluna.excavein.config.ClientConfig.*;

@SuppressWarnings("unused")
public class KeybindHandler {

    public static KeyMapping SELECTION_ACTIVATION;
    public static KeyMapping SELECTION;
    public static KeyMapping VEIN;
    public static KeyMapping EXCAVATE;
    public static KeyMapping TUNNEL;
    public static KeyMapping LARGE_TUNNEL;
    public static KeyMapping DIAGONAL_TUNNEL;
    public static KeyMapping SIDE_SELECTION;
    public static KeyMapping SIDE_VEIN;
    public static KeyMapping SIDE_EXCAVATE;
    public static KeyMapping SURFACE;

    public static KeyMapping NEXT_MODE;
    public static KeyMapping PREV_MODE;

    public static void register() {
        SELECTION_ACTIVATION = create("selection_activation", GLFW.GLFW_KEY_GRAVE_ACCENT);

        NEXT_MODE = create("next_mode", GLFW.GLFW_KEY_UP);
        PREV_MODE = create("prev_mode", GLFW.GLFW_KEY_DOWN);

        SELECTION = createModKey(SelectionMode.SELECTION.name().toLowerCase());
        VEIN = createModKey(SelectionMode.VEIN.name().toLowerCase());
        EXCAVATE = createModKey(SelectionMode.EXCAVATE.name().toLowerCase());
        TUNNEL = createModKey(SelectionMode.TUNNEL.name().toLowerCase());
        LARGE_TUNNEL = createModKey(SelectionMode.LARGE_TUNNEL.name().toLowerCase());
        DIAGONAL_TUNNEL = createModKey(SelectionMode.DIAGONAL_TUNNEL.name().toLowerCase());
        SIDE_SELECTION = createModKey(SelectionMode.SIDE_SELECTION.name().toLowerCase());
        SIDE_VEIN = createModKey(SelectionMode.SIDE_VEIN.name().toLowerCase());
        SIDE_EXCAVATE = createModKey(SelectionMode.SIDE_EXCAVATE.name().toLowerCase());
        SURFACE = createModKey(SelectionMode.SURFACE.name().toLowerCase());

        KeyBindingHelper.registerKeyBinding(SELECTION_ACTIVATION);

        KeyBindingHelper.registerKeyBinding(NEXT_MODE);
        KeyBindingHelper.registerKeyBinding(PREV_MODE);

        KeyBindingHelper.registerKeyBinding(SELECTION);
        KeyBindingHelper.registerKeyBinding(VEIN);
        KeyBindingHelper.registerKeyBinding(EXCAVATE);
        KeyBindingHelper.registerKeyBinding(TUNNEL);
        KeyBindingHelper.registerKeyBinding(LARGE_TUNNEL);
        KeyBindingHelper.registerKeyBinding(DIAGONAL_TUNNEL);
        KeyBindingHelper.registerKeyBinding(SIDE_SELECTION);
        KeyBindingHelper.registerKeyBinding(SIDE_VEIN);
        KeyBindingHelper.registerKeyBinding(SIDE_EXCAVATE);
        KeyBindingHelper.registerKeyBinding(SURFACE);

        ClientTickEvents.END_CLIENT_TICK.register(KeybindHandler::onClientTick);
    }

    public static KeyMapping createModKey(String id) {
        return create("active_" + id, GLFW.GLFW_KEY_UNKNOWN);
    }

    public static KeyMapping create(String id, int key) {
        return new KeyMapping(
                "key.excavein." + id,
                InputConstants.Type.KEYSYM,
                key,
                "key.categories.excavein");
    }

    private static final Minecraft mc = Minecraft.getInstance();
    private static int tickCounter = 0;
    private static final int TICK_INTERVAL = 1;
    private static boolean displayText;
    public static boolean keyActivated = false;

    public static void onClientTick(Minecraft client) {
        if (mc.getConnection() != null && !Minecraft.getInstance().isPaused()) {
            tickCounter++;
            if (tickCounter >= TICK_INTERVAL) {
                tickCounter = 0;
                if ((!TOGGLEABLE_KEY.get() && SELECTION_ACTIVATION.isDown()) || (TOGGLEABLE_KEY.get() && keyActivated)) {
                    ClientPlayNetworking.send(new SelectionInspectionPacket(SelectionMode.getCurrentMode().ordinal()));
                }
                ClientPlayNetworking.send(new KeybindPacket((!TOGGLEABLE_KEY.get() && SELECTION_ACTIVATION.isDown()) || (TOGGLEABLE_KEY.get() && keyActivated)));
            }

            if (TOGGLEABLE_KEY.get() && SELECTION_ACTIVATION.consumeClick()) { keyActivated = !keyActivated; }

            if (NEXT_MODE.consumeClick()) { SelectionMode.nextMode(); displayText = true; }
            if (PREV_MODE.consumeClick()) { SelectionMode.previousMode(); displayText = true; }

            if (SELECTION.consumeClick()) { setMode(SelectionMode.SELECTION.ordinal()); displayText = true; }
            if (VEIN.consumeClick()) { setMode(SelectionMode.VEIN.ordinal()); displayText = true; }
            if (EXCAVATE.consumeClick()) { setMode(SelectionMode.EXCAVATE.ordinal()); displayText = true; }
            if (TUNNEL.consumeClick()) { setMode(SelectionMode.TUNNEL.ordinal()); displayText = true; }
            if (LARGE_TUNNEL.consumeClick()) { setMode(SelectionMode.LARGE_TUNNEL.ordinal()); displayText = true; }
            if (DIAGONAL_TUNNEL.consumeClick()) { setMode(SelectionMode.DIAGONAL_TUNNEL.ordinal()); displayText = true; }
            if (SIDE_SELECTION.consumeClick()) { setMode(SelectionMode.SIDE_SELECTION.ordinal()); displayText = true; }
            if (SIDE_VEIN.consumeClick()) { setMode(SelectionMode.SIDE_VEIN.ordinal()); displayText = true; }
            if (SIDE_EXCAVATE.consumeClick()) { setMode(SelectionMode.SIDE_EXCAVATE.ordinal()); displayText = true; }
            if (SURFACE.consumeClick()) { setMode(SelectionMode.SURFACE.ordinal()); displayText = true; }
            if (displayText) {
                SelectionMode currentMode = SelectionMode.getCurrentMode();
                assert Minecraft.getInstance().player != null;
                Minecraft.getInstance().player.displayClientMessage(
                        Component.literal(Component.translatable("excavein.overlay.current_mode").getString().replaceAll("_", " ") + currentMode.getName()), !DISPLAY_SELECTION_CHAT.get());
                displayText = false;
            }
        }
    }

    public static boolean onMouseScroll(double scrollDeltaX, double scrollDeltaY) {
        if (!DISABLE_SCROLL.get() && (((!TOGGLEABLE_KEY.get() && SELECTION_ACTIVATION.isDown()) || (TOGGLEABLE_KEY.get() && keyActivated)) && scrollDeltaY != 0)) {
            if (scrollDeltaY > 0) {
                SelectionMode.nextMode();
            } else {
                SelectionMode.previousMode();
            }

            SelectionMode currentMode = SelectionMode.getCurrentMode();

            assert Minecraft.getInstance().player != null;
            Minecraft.getInstance().player.displayClientMessage(
                    Component.literal(Component.translatable("excavein.overlay.current_mode").getString().replaceAll("_", " ") + currentMode.getName()), !DISPLAY_SELECTION_CHAT.get());

            return true;
        }
        return false;
    }
}
