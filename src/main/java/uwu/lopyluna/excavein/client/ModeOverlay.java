package uwu.lopyluna.excavein.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import uwu.lopyluna.excavein.Excavein;
import uwu.lopyluna.excavein.config.ClientConfig;

import static uwu.lopyluna.excavein.Utils.ticksToTime;
import static uwu.lopyluna.excavein.Utils.OffsetTime.*;
import static uwu.lopyluna.excavein.client.BlockOutlineRenderer.outlineBlocks;
import static uwu.lopyluna.excavein.config.ClientConfig.SELECTION_OFFSET_X;
import static uwu.lopyluna.excavein.config.ClientConfig.SELECTION_OFFSET_Y;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = Excavein.MOD_ID, value = Dist.CLIENT)
public class ModeOverlay {

    private static final Minecraft mc = Minecraft.getInstance();

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent event) {
        PoseStack poseStack = event.getPoseStack();
        if (mc.options.hideGui || mc.noRender || mc.options.reducedDebugInfo().get() || mc.options.renderDebug || mc.options.renderFpsChart || mc.options.renderDebugCharts || !KeybindHandler.SELECTION_ACTIVATION.isDown())
            return;

        poseStack.pushPose();

        Font fontRenderer = mc.font;

        int screenWidth = event.getWindow().getGuiScaledWidth();
        int xPos = 10 + SELECTION_OFFSET_X.get();
        int yPos = 10 + SELECTION_OFFSET_Y.get();

        SelectionMode currentMode = SelectionMode.getCurrentMode();
        SelectionMode previousMode = SelectionMode.getPreviousMode();
        SelectionMode nextMode = SelectionMode.getNextMode();

        int r = ClientConfig.OUTLINE_COLOR_R.get();
        int g = ClientConfig.OUTLINE_COLOR_G.get();
        int b = ClientConfig.OUTLINE_COLOR_B.get();
        int alpha = (int) ((ClientConfig.OUTLINE_ALPHA.get() / 100.0f) * 255);
        int alphaH = (int) (((ClientConfig.OUTLINE_ALPHA.get() * 0.5) / 100.0f) * 255);

        int color = (alpha << 24) | (r << 16) | (g << 8) | b;
        int colorH = (alphaH << 24) | (r << 16) | (g << 8) | b;


        if (currentMode != null) {
            fontRenderer.drawShadow(poseStack, translateText("mode") + currentMode.getName(), xPos, yPos, color, true);
        }
        int blockCount = outlineBlocks.isEmpty() ? 0 : outlineBlocks.size();
        if (blockCount > 0 && !ClientCooldownHandler.isCooldownActive()) {
            fontRenderer.drawShadow(poseStack, translateText("selecting") + blockCount + translateText("blocks"), xPos, yPos + 15, color, true);
        }

        if (ClientCooldownHandler.isCooldownActive()) {
            fontRenderer.drawShadow(poseStack, translateText("cooldown") + ticksToTime(ClientCooldownHandler.getRemainingCooldown(), SECONDS), xPos, yPos + 15, color, true);
        }
        poseStack.popPose();
        PoseStack poseStackScale = event.getPoseStack();
        poseStackScale.pushPose();
        float scale = 0.75f;
        poseStackScale.scale(scale, scale, scale);
        if (previousMode != null) {
            fontRenderer.drawShadow(poseStackScale, translateText("scroll_up") + previousMode.getName(), xPos + 8, yPos - 5, colorH, true);
        }
        if (nextMode != null) {
            fontRenderer.drawShadow(poseStackScale, translateText("scroll_down") + nextMode.getName(), xPos + 8, yPos + 15, colorH, true);
        }
        poseStackScale.popPose();
    }

    public static String translateText(String translate) {
        return Component.translatable("excavein.overlay." + translate).getString().replaceAll("_", " ");
    }
}
