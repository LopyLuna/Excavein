package dev.lopyluna.excavein.client;

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
import dev.lopyluna.excavein.Excavein;
import dev.lopyluna.excavein.entries.ExcaveinEntries;
import dev.lopyluna.excavein.entries.ShapeEntry;
import dev.lopyluna.excavein.entries.ShapeModifierEntry;
import dev.lopyluna.excavein.packets.ExcaveinPacket;
import dev.lopyluna.excavein.packets.KeybindPacket;
import dev.lopyluna.excavein.packets.ModesPacket;
import dev.lopyluna.excavein.shape_modifiers.ShapeModifier;
import dev.lopyluna.excavein.shapes.Shape;

import java.util.*;

import static dev.lopyluna.excavein.config.ClientConfig.*;
import static dev.lopyluna.excavein.utils.Utils.isNotFakePlayer;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = Excavein.MOD_ID, value = Dist.CLIENT)
public class ClientHandler {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final int TICK_INTERVAL = 1;
    public static KeyMapping SELECTION_ACTIVATION;
    public static KeyMapping NEXT_MODE;
    public static KeyMapping PREV_MODE;
    public static KeyMapping NEXT_MODIFIER;
    public static KeyMapping PREV_MODIFIER;
    public static KeyMapping MODIFIER_SCROLL;
    public static List<KeyMapping> KEYBINDS = new ArrayList<>();
    protected static final Map<ShapeEntry<? extends Shape>, KeyMapping> shapeKeys = new HashMap<>();
    protected static final Map<ShapeModifierEntry<? extends ShapeModifier>, KeyMapping> modifierKeys = new HashMap<>();
    private static boolean keyActivated = false;
    public static boolean keyPressed = false;
    static UUID uuid;
    private static int tickCounter = 0;

    public ClientHandler(RegisterKeyMappingsEvent event) {
        SELECTION_ACTIVATION = create("selection_activation", GLFW.GLFW_KEY_GRAVE_ACCENT);

        NEXT_MODE = create("next_shape", GLFW.GLFW_KEY_UP);
        PREV_MODE = create("prev_shape", GLFW.GLFW_KEY_DOWN);

        NEXT_MODIFIER = create("next_modifier", GLFW.GLFW_KEY_UP, KeyModifier.ALT);
        PREV_MODIFIER = create("prev_modifier", GLFW.GLFW_KEY_DOWN, KeyModifier.ALT);

        MODIFIER_SCROLL = create("modifier_scroll", GLFW.GLFW_KEY_LEFT_ALT);

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
        if (mc.getConnection() != null && !Minecraft.getInstance().isPaused() && mc.player != null) {
            uuid = mc.player.getUUID();
            if (isNotFakePlayer(mc.player)) {
                tickCounter++;
                if (tickCounter >= TICK_INTERVAL) {
                    tickCounter = 0;
                    keyPressed = (!TOGGLEABLE_KEY.get() && SELECTION_ACTIVATION != null && SELECTION_ACTIVATION.isDown()) || (TOGGLEABLE_KEY.get() && keyActivated);

                    if (uuid != null) PacketDistributor.sendToServer(new ExcaveinPacket(uuid, keyPressed, DISPLAY_SELECTION_CHAT.get()));
                }
                if (TOGGLEABLE_KEY.get() && SELECTION_ACTIVATION.consumeClick()) keyActivated = !keyActivated;
            }
        }
    }

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        if (mc.getConnection() != null && !Minecraft.getInstance().isPaused() && mc.level != null && uuid != null && isNotFakePlayer(mc.level.getPlayerByUUID(uuid))) {
            int switchMode = 0;
            if (NEXT_MODIFIER.consumeClick()) switchMode = 3;
            else if (NEXT_MODIFIER.consumeClick()) switchMode = 4;
            else if (NEXT_MODE.consumeClick()) switchMode = 1;
            else if (PREV_MODE.consumeClick()) switchMode = 2;
            PacketDistributor.sendToServer(new ModesPacket(uuid, keyPressed, switchMode));

            if (!shapeKeys.isEmpty())
                ExcaveinEntries.getShapeEntries().forEach((integer, shapeEntry) -> {
                    if (shapeEntry.hasKeybind()) PacketDistributor.sendToServer(new KeybindPacket(uuid, shapeKeys.get(shapeEntry).consumeClick(), integer, "shape"));
                });
            if (!modifierKeys.isEmpty())
                ExcaveinEntries.getShapeModifierEntries().forEach((integer, modifierEntry) -> {
                    if (modifierEntry.hasKeybind()) PacketDistributor.sendToServer(new KeybindPacket(uuid, modifierKeys.get(modifierEntry).consumeClick(), integer, "modifier"));
                });
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (mc.getConnection() != null && !Minecraft.getInstance().isPaused() && mc.level != null && uuid != null && isNotFakePlayer(mc.level.getPlayerByUUID(uuid))) {
            if (keyPressed && !DISABLE_SCROLL.get()) {
                int switchMode = 0;
                if (MODIFIER_SCROLL.isDown() && event.getScrollDeltaY() > 0) switchMode = 3;
                else if (MODIFIER_SCROLL.isDown() && event.getScrollDeltaY() < 0) switchMode = 4;
                else if (event.getScrollDeltaY() > 0) switchMode = 1;
                else if (event.getScrollDeltaY() < 0) switchMode = 2;

                PacketDistributor.sendToServer(new ModesPacket(uuid, true, switchMode));
                event.setCanceled(true);
            }
        }
    }
}
