package uwu.lopyluna.excavein.client;

import com.google.common.base.Strings;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import uwu.lopyluna.excavein.Excavein;
import uwu.lopyluna.excavein.Utils;

import java.awt.*;

import static uwu.lopyluna.excavein.Utils.ticksToTime;
import static uwu.lopyluna.excavein.Utils.OffsetTime.*;
import static uwu.lopyluna.excavein.client.BlockOutlineRenderer.*;
import static uwu.lopyluna.excavein.client.BlockOutlineRenderer.isBreaking;
import static uwu.lopyluna.excavein.client.KeybindHandler.SELECTION_ACTIVATION;
import static uwu.lopyluna.excavein.client.KeybindHandler.keyActivated;
import static uwu.lopyluna.excavein.config.ClientConfig.*;
import static uwu.lopyluna.excavein.config.ServerConfig.*;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = Excavein.MOD_ID, value = Dist.CLIENT)
public class ModeOverlay {

    private static final Minecraft mc = Minecraft.getInstance();
    static int dots = 0;

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiEvent event) {
        PoseStack poseStack = event.getGuiGraphics().pose();
        if (mc.getConnection() == null || mc.player == null || mc.options.hideGui || mc.noRender || mc.options.reducedDebugInfo().get() || mc.options.renderDebug || mc.options.renderFpsChart || mc.options.renderDebugCharts || !((!TOGGLEABLE_KEY.get() && SELECTION_ACTIVATION != null && SELECTION_ACTIVATION.isDown()) || (TOGGLEABLE_KEY.get() && keyActivated)))
            return;

        poseStack.pushPose();

        int screenWidth = event.getWindow().getGuiScaledWidth();
        int xPos = SELECTION_OFFSET_X.get();
        int yPos = SELECTION_OFFSET_Y.get();

        SelectionMode currentMode = SelectionMode.getCurrentMode();
        SelectionMode previousMode = SelectionMode.getPreviousMode();
        SelectionMode nextMode = SelectionMode.getNextMode();

        int r = SELECTION_COLOR_R.get();
        int g = SELECTION_COLOR_G.get();
        int b = SELECTION_COLOR_B.get();

        int color = color(r, g, b, 255).getRGB();
        int colorA = color(r - 32, g - 32, b - 32, 255).getRGB();

        boolean dropShadow = TEXT_SHADOW.get();
        boolean background = TEXT_BACKGROUND.get();
        boolean leftSide = TEXT_LEFT_SIDE.get();

        if (currentMode != null) {
            renderText(translateText("mode") + currentMode.getName(), 1, xPos, yPos, event.getGuiGraphics(), leftSide, color, dropShadow, background);
        }
        if (!(isBreaking && WAIT_TILL_BROKEN.get()) && requiredFlag(mc.player)) {
            String tag = "";
            if (REQUIRES_XP.get() && !mc.player.isCreative() && mc.player.totalExperience == 0)
                tag = "xp"; else
            if (REQUIRES_HUNGER.get() && !mc.player.isCreative() && mc.player.getFoodData().getFoodLevel() == 0)
                tag = "hunger"; else
            if (REQUIRES_FUEL_ITEM.get() && !mc.player.isCreative() && Utils.findInInventory(mc.player) == 0)
                tag = "fuel";
            renderText(tag.isEmpty() ? "" : translateText("require_" + tag), 3, xPos, yPos, event.getGuiGraphics(), leftSide, color, dropShadow, background);
        } else if (!(isBreaking && WAIT_TILL_BROKEN.get()) && !requiredFlag(mc.player)) {
            int blockCount = outlineBlocks.isEmpty() ? 0 : outlineBlocks.size();
            if (blockCount > 0 && !ClientCooldownHandler.isCooldownActive()) {
                renderText(translateText("selecting") + blockCount + translateText("blocks"), 3, xPos, yPos, event.getGuiGraphics(), leftSide, color, dropShadow, background);
            }
            if (ClientCooldownHandler.isCooldownActive()) {
                renderText(translateText("cooldown") + ticksToTime(ClientCooldownHandler.getRemainingCooldown(), SECONDS), 3, xPos, yPos, event.getGuiGraphics(), leftSide, color, dropShadow, background);
            }
        } else if (isBreaking && WAIT_TILL_BROKEN.get()) renderText(translateText("breaking") + animatedDotsString(), 3, xPos, yPos, event.getGuiGraphics(), leftSide, color, dropShadow, background);

        if (previousMode != null) {
            renderText(translateText("scroll_up") + previousMode.getName(), 0, xPos, yPos, event.getGuiGraphics(), leftSide, color, dropShadow, background);
        }
        if (nextMode != null) {
            renderText(translateText("scroll_down") + nextMode.getName(), 2, xPos, yPos, event.getGuiGraphics(), leftSide, color, dropShadow, background);
        }
        poseStack.popPose();
    }

    public static String animatedDotsString() {
        return ".".repeat(Math.max(0, dots));
    }

    static int tick = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (mc.getConnection() != null && event.phase == TickEvent.Phase.END && !Minecraft.getInstance().isPaused()) {
            if (tick > 0) tick--; else { tick = 3;
                if (dots < 3) dots++; else dots = 0;
            }
        }
    }

    private static void renderText(String pText, int pOffsetOrder, int pOffX, int pOffY, GuiGraphics pGuiGraphics, boolean pLeftSide, int pTextColor, boolean pDropShadow, boolean pBackground) {
        int i = 9;
        if (!Strings.isNullOrEmpty(pText) && pBackground) {
            int k = mc.font.width(pText);
            int l = (pLeftSide ? 2 : pGuiGraphics.guiWidth() - 2 - k) + pOffX;
            int i1 = (2 + i * pOffsetOrder) + pOffY;
            pGuiGraphics.fill(l - 1, i1 - 1, l + k + 1, i1 + i - 1, -1873784752);
        }
        if (!Strings.isNullOrEmpty(pText)) {
            int k1 = mc.font.width(pText);
            int l1 = (pLeftSide ? 2 : pGuiGraphics.guiWidth() - 2 - k1) + pOffX;
            int i2 = (2 + i * pOffsetOrder) + pOffY;
            pGuiGraphics.drawString(mc.font, pText, l1, i2, pTextColor, pDropShadow);
        }
    }

    public static Color color(int r, int g, int b, int a) {
        return new Color(Mth.clamp(r,0,255), Mth.clamp(g,0,255), Mth.clamp(b,0,255), Mth.clamp(a,0,255));
    }

    public static String translateText(String translate) {
        return Component.translatable("excavein.overlay." + translate).getString().replaceAll("_", " ");
    }
}
