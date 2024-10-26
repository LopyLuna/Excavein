package uwu.lopyluna.excavein.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import uwu.lopyluna.excavein.Excavein;
import uwu.lopyluna.excavein.config.ClientConfig;

import java.awt.*;

import static uwu.lopyluna.excavein.Utils.ticksToTime;
import static uwu.lopyluna.excavein.Utils.OffsetTime.*;
import static uwu.lopyluna.excavein.client.BlockOutlineRenderer.outlineBlocks;
import static uwu.lopyluna.excavein.client.KeybindHandler.SELECTION_ACTIVATION;
import static uwu.lopyluna.excavein.client.KeybindHandler.keyActivated;
import static uwu.lopyluna.excavein.config.ClientConfig.*;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = Excavein.MOD_ID, value = Dist.CLIENT)
public class ModeOverlay {

    private static final Minecraft mc = Minecraft.getInstance();

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent event) {
        PoseStack poseStack = event.getPoseStack();
        if (mc.getConnection() == null || mc.options.hideGui || mc.noRender || mc.options.reducedDebugInfo().get() || mc.options.renderDebug || mc.options.renderFpsChart || mc.options.renderDebugCharts || !((!TOGGLEABLE_KEY.get() && SELECTION_ACTIVATION.isDown()) || (TOGGLEABLE_KEY.get() && keyActivated)))
            return;

        poseStack.pushPose();

        int screenWidth = event.getWindow().getGuiScaledWidth();
        int xPos = 15 + SELECTION_OFFSET_X.get();
        int yPos = 15 + SELECTION_OFFSET_Y.get();

        SelectionMode currentMode = SelectionMode.getCurrentMode();
        SelectionMode previousMode = SelectionMode.getPreviousMode();
        SelectionMode nextMode = SelectionMode.getNextMode();

        int r = ClientConfig.SELECTION_COLOR_R.get();
        int g = ClientConfig.SELECTION_COLOR_G.get();
        int b = ClientConfig.SELECTION_COLOR_B.get();

        int color = color(r, g, b, 255).getRGB();
        int colorA = color(r - 32, g - 32, b - 32, 255).getRGB();

        boolean dropShadow = TEXT_SHADOW.get();

        if (currentMode != null) {
            text(translateText("mode") + currentMode.getName(), xPos, yPos, color, dropShadow, poseStack);
        }
        int blockCount = outlineBlocks.isEmpty() ? 0 : outlineBlocks.size();
        if (blockCount > 0 && !ClientCooldownHandler.isCooldownActive()) {
            text(translateText("selecting") + blockCount + translateText("blocks"), xPos, yPos + 20, color, dropShadow, poseStack);
        }
        if (ClientCooldownHandler.isCooldownActive()) {
            text(translateText("cooldown") + ticksToTime(ClientCooldownHandler.getRemainingCooldown(), SECONDS), xPos, yPos + 20, color, dropShadow, poseStack);
        }
        if (previousMode != null) {
            text(translateText("scroll_up") + previousMode.getName(), xPos, yPos - 10, color, dropShadow, poseStack);
        }
        if (nextMode != null) {
            text(translateText("scroll_down") + nextMode.getName(), xPos, yPos + 10, color, dropShadow, poseStack);
        }
        poseStack.popPose();
    }



    public static void text(String pText, float pX, float pY, int pColor, boolean pDropShadow, PoseStack poseStack) {
        poseStack.pushPose();
        MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        mc.font.drawInBatch(pText, pX, pY, pColor, pDropShadow, poseStack.last().pose(), multibuffersource$buffersource, false, 0, 15728880);
        multibuffersource$buffersource.endBatch();
        poseStack.popPose();
    }

    public static Color color(int r, int g, int b, int a) {
        return new Color(Mth.clamp(r,0,255), Mth.clamp(g,0,255), Mth.clamp(b,0,255), Mth.clamp(a,0,255));
    }

    public static String translateText(String translate) {
        return Component.translatable("excavein.overlay." + translate).getString().replaceAll("_", " ");
    }
}
