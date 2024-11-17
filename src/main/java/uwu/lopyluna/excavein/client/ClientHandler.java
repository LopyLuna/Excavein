package uwu.lopyluna.excavein.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import uwu.lopyluna.excavein.Excavein;
import uwu.lopyluna.excavein.entries.ExcaveinEntries;
import uwu.lopyluna.excavein.entries.ShapeEntry;
import uwu.lopyluna.excavein.entries.ShapeModifierEntry;
import uwu.lopyluna.excavein.packets.ExcaveinPacket;
import uwu.lopyluna.excavein.packets.KeybindPacket;
import uwu.lopyluna.excavein.shape_modifiers.ShapeModifier;
import uwu.lopyluna.excavein.shapes.Shape;

import java.util.*;

import static uwu.lopyluna.excavein.config.ClientConfig.DISABLE_SCROLL;
import static uwu.lopyluna.excavein.config.ClientConfig.TOGGLEABLE_KEY;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = Excavein.MOD_ID, value = Dist.CLIENT)
public class ClientHandler {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final int TICK_INTERVAL = 1;
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
    public static KeyMapping NEXT_MODIFIER;
    public static KeyMapping PREV_MODIFIER;
    public static KeyMapping MODIFIER_SCROLL;
    public static List<KeyMapping> KEYBINDS = new ArrayList<>();
    protected static final Map<ShapeEntry<? extends Shape>, KeyMapping> shapeKeys = new HashMap<>();
    protected static final Map<ShapeModifierEntry<? extends ShapeModifier>, KeyMapping> modifierKeys = new HashMap<>();
    public static boolean keyActivated = false;
    public static boolean keyPressed = false;
    static UUID uuid;
    private static int tickCounter = 0;

    public static void register(RegisterKeyMappingsEvent event) {
        SELECTION_ACTIVATION = create("selection_activation", GLFW.GLFW_KEY_GRAVE_ACCENT);

        NEXT_MODE = create("next_shape", GLFW.GLFW_KEY_UP);
        PREV_MODE = create("prev_shape", GLFW.GLFW_KEY_DOWN);

        NEXT_MODIFIER = create("next_modifier", GLFW.GLFW_KEY_UP, KeyModifier.ALT);
        PREV_MODIFIER = create("prev_modifier", GLFW.GLFW_KEY_DOWN, KeyModifier.ALT);

        MODIFIER_SCROLL = create("modifier_scroll", GLFW.GLFW_MOD_ALT);

        ExcaveinEntries.getShapeEntries().forEach((integer, shapeEntry) -> {
            if (shapeEntry.hasKeybind()) {
                KeyMapping keyMapping = ClientHandler.create(shapeEntry.getLang(), GLFW.GLFW_KEY_UNKNOWN);
                shapeKeys.put(shapeEntry, keyMapping);
                ClientHandler.KEYBINDS.add(keyMapping);
            }
        });
        ExcaveinEntries.getShapeModifierEntries().forEach((integer, shapeModifierEntry) -> {
            if (shapeModifierEntry.hasKeybind()) {
                KeyMapping keyMapping = ClientHandler.create(shapeModifierEntry.getLang(), GLFW.GLFW_KEY_UNKNOWN);
                modifierKeys.put(shapeModifierEntry, keyMapping);
                ClientHandler.KEYBINDS.add(keyMapping);
            }
        });

        event.register(SELECTION_ACTIVATION);
        event.register(NEXT_MODE);
        event.register(PREV_MODE);
        event.register(NEXT_MODIFIER);
        event.register(PREV_MODIFIER);
        event.register(MODIFIER_SCROLL);

        KEYBINDS.forEach(event::register);
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

    public static KeyMapping create(String id, int key, KeyModifier keyModifier) {
        return new KeyMapping(
                "key.excavein." + id,
                KeyConflictContext.IN_GAME,
                keyModifier,
                InputConstants.Type.KEYSYM,
                key,
                "key.categories.excavein");
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (mc.getConnection() != null && !Minecraft.getInstance().isPaused() && mc.level != null) {
            mc.level.players().forEach(player -> {
                if (player != null) {
                    tickCounter++;
                    if (tickCounter >= TICK_INTERVAL) {
                        tickCounter = 0;
                        uuid = player.getUUID();
                        keyPressed = (!TOGGLEABLE_KEY.get() && SELECTION_ACTIVATION != null && SELECTION_ACTIVATION.isDown()) || (TOGGLEABLE_KEY.get() && keyActivated);

                        int switchMode = 0;
                        if (NEXT_MODIFIER.consumeClick()) switchMode = 3;
                        else if (NEXT_MODIFIER.consumeClick()) switchMode = 4;
                        else if (NEXT_MODE.consumeClick()) switchMode = 1;
                        else if (PREV_MODE.consumeClick()) switchMode = 2;

                        ExcaveinEntries.getShapeEntries().forEach((integer, shapeEntry) ->
                                PacketDistributor.sendToServer(new KeybindPacket(uuid, shapeKeys.get(shapeEntry).consumeClick(), integer, "shape")));
                        ExcaveinEntries.getShapeModifierEntries().forEach((integer, modifierEntry) ->
                                PacketDistributor.sendToServer(new KeybindPacket(uuid, modifierKeys.get(modifierEntry).consumeClick(), integer, "modifier")));

                        PacketDistributor.sendToServer(new ExcaveinPacket(uuid, keyPressed, switchMode));
                    }
                    if (TOGGLEABLE_KEY.get() && SELECTION_ACTIVATION.consumeClick()) {
                        keyActivated = !keyActivated;
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (uuid != null) {
            if (keyPressed && !DISABLE_SCROLL.get()) {
                int switchMode = 0;
                if ((MODIFIER_SCROLL.isDown() && event.getScrollDeltaY() > 0) || NEXT_MODIFIER.consumeClick())
                    switchMode = 3;
                else if ((MODIFIER_SCROLL.isDown() && event.getScrollDeltaY() < 0) || NEXT_MODIFIER.consumeClick())
                    switchMode = 4;
                else if ((event.getScrollDeltaY() > 0) || NEXT_MODE.consumeClick()) switchMode = 1;
                else if ((event.getScrollDeltaY() < 0) || PREV_MODE.consumeClick()) switchMode = 2;

                PacketDistributor.sendToServer(new ExcaveinPacket(uuid, true, switchMode));
                event.setCanceled(true);
            }
        }
    }


}
