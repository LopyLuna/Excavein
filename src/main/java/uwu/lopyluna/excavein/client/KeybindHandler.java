package uwu.lopyluna.excavein.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import uwu.lopyluna.excavein.Excavein;
import uwu.lopyluna.excavein.network.KeybindPacket;
import uwu.lopyluna.excavein.network.SelectionInspectionPacket;

import static uwu.lopyluna.excavein.client.SelectionMode.setMode;
import static uwu.lopyluna.excavein.config.ClientConfig.*;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = Excavein.MOD_ID, value = Dist.CLIENT)
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

    public static KeyMapping NEXT_MODE;
    public static KeyMapping PREV_MODE;

    public static void register(RegisterKeyMappingsEvent event) {
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

        event.register(SELECTION_ACTIVATION);

        event.register(NEXT_MODE);
        event.register(PREV_MODE);

        event.register(SELECTION);
        event.register(VEIN);
        event.register(EXCAVATE);
        event.register(TUNNEL);
        event.register(LARGE_TUNNEL);
        event.register(DIAGONAL_TUNNEL);
        event.register(SIDE_SELECTION);
        event.register(SIDE_VEIN);
        event.register(SIDE_EXCAVATE);
    }

    public static KeyMapping createModKey(String id) {
        return create("active_" + id, GLFW.GLFW_KEY_UNKNOWN);
    }

    public static KeyMapping create(String id, int key) {
        return new KeyMapping(
                "key.excavein." + id,
                KeyConflictContext.IN_GAME,
                KeyModifier.NONE,
                InputConstants.Type.KEYSYM,
                key,
                "key.categories.excavein");
    }

    private static final Minecraft mc = Minecraft.getInstance();
    private static int tickCounter = 0;
    private static final int TICK_INTERVAL = 1;
    private static boolean displayText;
    public static boolean keyActivated = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (mc.getConnection() != null && event.phase == TickEvent.Phase.END && !Minecraft.getInstance().isPaused()) {
            tickCounter++;
            if (tickCounter >= TICK_INTERVAL) {
                tickCounter = 0;
                if ((!TOGGLEABLE_KEY.get() && SELECTION_ACTIVATION.isDown()) || (TOGGLEABLE_KEY.get() && keyActivated)) {
                    Excavein.CHANNEL.sendToServer(SelectionInspectionPacket.INSTANCE);
                }
                Excavein.CHANNEL.sendToServer(new KeybindPacket());
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
            if (displayText) {
                SelectionMode currentMode = SelectionMode.getCurrentMode();
                assert Minecraft.getInstance().player != null;
                Minecraft.getInstance().player.displayClientMessage(
                        Component.literal(Component.translatable("excavein.overlay.current_mode").getString().replaceAll("_", " ") + currentMode.getName()), !DISPLAY_SELECTION_CHAT.get());
                displayText = false;
            }
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (!DISABLE_SCROLL.get() && (((!TOGGLEABLE_KEY.get() && SELECTION_ACTIVATION.isDown()) || (TOGGLEABLE_KEY.get() && keyActivated)) && event.getScrollDelta() != 0)) {
            if (event.getScrollDelta() > 0) {
                SelectionMode.nextMode();
            } else {
                SelectionMode.previousMode();
            }

            SelectionMode currentMode = SelectionMode.getCurrentMode();

            assert Minecraft.getInstance().player != null;
            Minecraft.getInstance().player.displayClientMessage(
                    Component.literal(Component.translatable("excavein.overlay.current_mode").getString().replaceAll("_", " ") + currentMode.getName()), !DISPLAY_SELECTION_CHAT.get());

            event.setCanceled(true);
        }
    }
}
