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

import static uwu.lopyluna.excavein.config.ClientConfig.DISPLAY_SELECTION_CHAT;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = Excavein.MOD_ID, value = Dist.CLIENT)
public class KeybindHandler {

    public static KeyMapping SELECTION_ACTIVATION;

    public static void register(RegisterKeyMappingsEvent event) {
        SELECTION_ACTIVATION = new KeyMapping(
                "key.excavein.selection_activation",
                KeyConflictContext.IN_GAME,
                KeyModifier.NONE,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_GRAVE_ACCENT,
                "key.categories.excavein"
        );

        event.register(SELECTION_ACTIVATION);
    }

    private static final Minecraft mc = Minecraft.getInstance();
    private static int tickCounter = 0;
    private static final int TICK_INTERVAL = 1;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (mc.getConnection() != null && event.phase == TickEvent.Phase.END && !Minecraft.getInstance().isPaused()) {
            tickCounter++;
            if (tickCounter >= TICK_INTERVAL) {
                tickCounter = 0;
                if (SELECTION_ACTIVATION.isDown()) {
                    Excavein.CHANNEL.sendToServer(SelectionInspectionPacket.INSTANCE);
                }
                Excavein.CHANNEL.sendToServer(new KeybindPacket());
            }
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (SELECTION_ACTIVATION.isDown() && event.getScrollDelta() != 0) {
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
