package uwu.lopyluna.excavein.client;

import com.google.common.base.Strings;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import uwu.lopyluna.excavein.Excavein;
import uwu.lopyluna.excavein.config.ClientConfig;
import uwu.lopyluna.excavein.entries.ExcaveinEntries;
import uwu.lopyluna.excavein.utils.Utils;

import java.awt.*;

import static uwu.lopyluna.excavein.client.BlockOutlineRenderer.amountBreak;
import static uwu.lopyluna.excavein.client.BlockOutlineRenderer.amountInteract;
import static uwu.lopyluna.excavein.client.ClientHandler.SELECTION_ACTIVATION;
import static uwu.lopyluna.excavein.client.ClientHandler.keyPressed;
import static uwu.lopyluna.excavein.config.ClientConfig.*;
import static uwu.lopyluna.excavein.config.ServerConfig.*;
import static uwu.lopyluna.excavein.utils.Utils.OffsetTime.SECONDS;

@SuppressWarnings({"unused", "all"})
@EventBusSubscriber(modid = Excavein.MOD_ID, value = Dist.CLIENT)
public class ModeOverlay {

    private static final Minecraft mc = Minecraft.getInstance();
    static int dots = 0;
    static int i = 0;
    static int tick = 0;

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiEvent.Post event) {
        PoseStack poseStack = event.getGuiGraphics().pose();
        if (mc.getConnection() == null || mc.player == null || mc.options.hideGui || mc.noRender || mc.showOnlyReducedInfo())
            return;

        poseStack.pushPose();

        if (ClientConfig.DEBUG.get() && mc.player.isCreative())
            renderDebug(event, poseStack);

        if (!((!TOGGLEABLE_KEY.get() && SELECTION_ACTIVATION != null && SELECTION_ACTIVATION.isDown()) || (TOGGLEABLE_KEY.get() && keyPressed)))
            return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int xPos = SELECTION_OFFSET_X.get();
        int yPos = SELECTION_OFFSET_Y.get();

        String currentMode = ClientHelper.currentMode;
        String previousMode = ClientHelper.previousMode;
        String nextMode = ClientHelper.nextMode;

        String currentModifier = ClientHelper.currentModifier;
        String previousModifier = ClientHelper.previousModifier;
        String nextModifier = ClientHelper.nextModifier;

        int r = MIXED_SELECTION_COLOR_R.get();
        int g = MIXED_SELECTION_COLOR_G.get();
        int b = MIXED_SELECTION_COLOR_B.get();

        int color = color(r, g, b, 255).getRGB();
        int colorD = color((int) (r * 0.9), (int) (g * 0.9), (int) (b * 0.9), 255).getRGB();
        int colorWarning = color(r, (int) (g * 0.75), (int) (b * 0.75), 255).getRGB();

        boolean dropShadow = TEXT_SHADOW.get();
        boolean background = TEXT_BACKGROUND.get();
        boolean leftSide = TEXT_LEFT_SIDE.get();

        String scrollUp = sideString(translateText("scroll_up"), "", " ", leftSide);
        String scrollDown = sideString(translateText("scroll_down"), "", " ", leftSide);
        String mode = sideString(translateText("mode"), "", ": ", leftSide);
        String modifier = sideString(translateText("modifier"), "", ": ", leftSide);

        int order = 0;

        if (previousMode != null) {
            sideText(previousMode,
                    scrollUp, "", leftSide, mode.length() - 1, order, true, event.getGuiGraphics());
            order++;
        }
        if (currentMode != null) {
            sideText(currentMode,
                    mode, "", leftSide, 0, order, false, event.getGuiGraphics());
            order++;
        }
        if (nextMode != null) {
            sideText(nextMode,
                    scrollDown, "", leftSide, mode.length() - 1, order, true, event.getGuiGraphics());
            order++;
        }
        boolean breaking = (!ClientHelper.currentlyBreaking || DELAY_BETWEEN_BREAK.get() == 0 || !WAIT_TILL_BROKEN.get());

        if (!breaking && !ClientHelper.requiredFlags) {
            String tag = "";
            if (REQUIRES_XP.get() && !mc.player.isCreative() && mc.player.totalExperience == 0)
                tag = "xp";
            else if (REQUIRES_HUNGER.get() && !mc.player.isCreative() && mc.player.getFoodData().getFoodLevel() == 0)
                tag = "hunger";
            else if (REQUIRES_FUEL_ITEM.get() && !mc.player.isCreative() && Utils.findInInventory(mc.player) == 0)
                tag = "fuel";

            renderText(tag.isEmpty() ? "" : translateText("require_" + tag), order, xPos, yPos, event.getGuiGraphics(), leftSide, colorWarning, dropShadow, background);
            order++;
        } else if (breaking && ClientHelper.requiredFlags) {
            int breakCount = amountBreak;
            int interactCount = amountInteract;
            if (ClientCooldownHandler.isCooldownActive()) {
                renderText(translateText("cooldown") + ticksToTime(ClientCooldownHandler.getRemainingCooldown(), SECONDS), order, xPos, yPos, event.getGuiGraphics(), leftSide, color, dropShadow, background);
                order++;
            } else {
                if (breakCount > 0) {
                    renderText(translateText("selecting") + breakCount + translateText("break_blocks"), order, xPos, yPos, event.getGuiGraphics(), leftSide, color, dropShadow, background);
                    order++;
                }
                if (interactCount > 0) {
                    renderText(translateText("selecting") + interactCount + translateText("interact_blocks"), order, xPos, yPos, event.getGuiGraphics(), leftSide, color, dropShadow, background);
                    order++;
                }
            }
        } else if (breaking) {
            renderText(translateText("breaking") + animatedDotsString(), order, xPos, yPos, event.getGuiGraphics(), leftSide, colorD, dropShadow, background);
            order++;
        }

        order++;

        if (previousModifier != null) {
            sideText((previousModifier.isEmpty() ? "None" : previousModifier),
                    scrollUp, "", leftSide, modifier.length() - 1, order, true, event.getGuiGraphics());
            order++;
        }
        if (currentModifier != null) {
            sideText((currentModifier.isEmpty() ? "None" : currentModifier),
                    modifier, "", leftSide, 0, order, false, event.getGuiGraphics());
            order++;
        }
        if (nextModifier != null) {
            sideText((nextModifier.isEmpty() ? "None" : nextModifier),
                    scrollDown, "", leftSide, modifier.length() - 1, order, true, event.getGuiGraphics());
        }

        poseStack.popPose();
    }

    @SuppressWarnings("SameParameterValue")
    private static void sideText(String pText, String pPrefix, String pSuffix, boolean pLeftSide, int pSpaceAmount, int pOffsetOrder, boolean darken, GuiGraphics pGuiGraphics) {
        boolean dropShadow = TEXT_SHADOW.get();
        boolean background = TEXT_BACKGROUND.get();
        int r = MIXED_SELECTION_COLOR_R.get();
        int g = MIXED_SELECTION_COLOR_G.get();
        int b = MIXED_SELECTION_COLOR_B.get();
        int color = color(r, g, b, 255).getRGB();
        int colorD = color((int) (r * 0.9), (int) (g * 0.9), (int) (b * 0.9), 255).getRGB();
        int xPos = SELECTION_OFFSET_X.get();
        int yPos = SELECTION_OFFSET_Y.get();

        if (pLeftSide)
            renderText(" ".repeat(pSpaceAmount) + pPrefix + pText + pSuffix, pOffsetOrder, xPos, yPos, pGuiGraphics, true, darken ? colorD : color, dropShadow, background);
        else
            renderText(pSuffix + pText + pPrefix + " ".repeat(pSpaceAmount), pOffsetOrder, xPos, yPos, pGuiGraphics, false, darken ? colorD : color, dropShadow, background);
    }

    @SuppressWarnings("SameParameterValue")
    private static String sideString(String pText, String pPrefix, String pSuffix, boolean pLeftSide) {
        return pLeftSide ? pPrefix + pText + pSuffix : new StringBuilder(pSuffix).reverse() + pText + new StringBuilder(pPrefix).reverse();
    }

    private static void renderDebug(RenderGuiEvent.Post event, PoseStack poseStack) {
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int xPos = SELECTION_OFFSET_X.get();
        int yPos = SELECTION_OFFSET_Y.get();
        int r = MIXED_SELECTION_COLOR_R.get();
        int g = MIXED_SELECTION_COLOR_G.get();
        int b = MIXED_SELECTION_COLOR_B.get();
        int color = color(r, g, b, 255).getRGB();
        boolean dropShadow = TEXT_SHADOW.get();
        boolean background = TEXT_BACKGROUND.get();
        boolean leftSide = !TEXT_LEFT_SIDE.get();

        renderText("-Shapes-", i, xPos, yPos, event.getGuiGraphics(), leftSide, color, dropShadow, background);
        i++;
        ExcaveinEntries.getShapeEntries().forEach((integer, shapeEntry) -> {
            renderText(shapeEntry.getLang() + " :Entry | " + integer + " :ID", i, xPos, yPos, event.getGuiGraphics(), leftSide, color, dropShadow, background);
            i++;
        });
        renderText("-Modifiers-", i, xPos, yPos, event.getGuiGraphics(), leftSide, color, dropShadow, background);
        i++;
        ExcaveinEntries.getShapeModifierEntries().forEach((integer, shapeEntry) -> {
            renderText(shapeEntry.getLang() + " :Entry | " + integer + " :ID", i, xPos, yPos, event.getGuiGraphics(), leftSide, color, dropShadow, background);
            i++;
        });
        renderText("-EntriesIDs-", i, xPos, yPos, event.getGuiGraphics(), leftSide, color, dropShadow, background);
        i++;
        ExcaveinEntries.getShapeValue().forEach((integer, resourceLocation) -> {
            renderText(resourceLocation.toString() + " :Loc | " + integer + " :ID", i, xPos, yPos, event.getGuiGraphics(), leftSide, color, dropShadow, background);
            i++;
        });
        ExcaveinEntries.getModifierValue().forEach((integer, resourceLocation) -> {
            renderText(resourceLocation.toString() + " :Loc | " + integer + " :ID", i, xPos, yPos, event.getGuiGraphics(), leftSide, color, dropShadow, background);
            i++;
        });
        ExcaveinEntries.getShapes().forEach((resourceLocation, shape) -> {
            renderText(resourceLocation.toString() + " :Loc | " + shape.getName() + " :Shape", i, xPos, yPos, event.getGuiGraphics(), leftSide, color, dropShadow, background);
            i++;
        });
        ExcaveinEntries.getShapeModifiers().forEach((resourceLocation, shapeModifier) -> {
            renderText(resourceLocation.toString() + " :Loc | " + shapeModifier.getName() + " :Modifier", i, xPos, yPos, event.getGuiGraphics(), leftSide, color, dropShadow, background);
            i++;
        });
        renderText("-Others-", i, xPos, yPos, event.getGuiGraphics(), leftSide, color, dropShadow, background);
        i++;
        renderText(ExcaveinEntries.sizeShape + " :Shape Size", i, xPos, yPos, event.getGuiGraphics(), leftSide, color, dropShadow, background);
        i++;
        renderText(ExcaveinEntries.sizeModifier + " :Modifier Size", i, xPos, yPos, event.getGuiGraphics(), leftSide, color, dropShadow, background);
        i = 0;
    }

    public static String animatedDotsString() {
        return ".".repeat(Math.max(0, dots));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (tick > 0) tick--;
        else {
            tick = 3;
            if (dots < 3) dots++;
            else dots = 0;
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
        return new Color(Mth.clamp(r, 0, 255), Mth.clamp(g, 0, 255), Mth.clamp(b, 0, 255), Mth.clamp(a, 0, 255));
    }

    public static String translateText(String translate) {
        return Component.translatable("excavein.overlay." + translate).getString().replaceAll("_", " ");
    }

    @SuppressWarnings("all")
    public static String ticksToTime(int value, Utils.OffsetTime off) {
        boolean bT = off == Utils.OffsetTime.TICKS;
        boolean bS = off == Utils.OffsetTime.SECONDS || bT;
        boolean bM = off == Utils.OffsetTime.MINUTES || bS;
        boolean bH = off == Utils.OffsetTime.HOURS || bM;
        boolean bD = off == Utils.OffsetTime.DAYS || bH;
        boolean bMTH = off == Utils.OffsetTime.MONTHS || bD;
        int t = value;
        int s = t / 20;
        int m = s / 60;
        int h = m / 60;
        int d = h / 24;
        int mth = d / 30;
        int y = mth / 12;
        t %= 20;
        s %= 60;
        m %= 60;
        h %= 24;
        mth %= 30;
        String ticks = bT ? conversion(t, "t", d > 0 || mth > 0 || y > 0 || s > 0 || m > 0 || h > 0, bT) : "";
        String secs = bS ? conversion(s, "s", d > 0 || mth > 0 || y > 0 || m > 0 || h > 0, off == Utils.OffsetTime.SECONDS) : "";
        String mins = bM ? conversion(m, "m", d > 0 || mth > 0 || y > 0 || h > 0, off == Utils.OffsetTime.MINUTES) : "";
        String hours = bH ? conversion(h, "h", d > 0 || mth > 0 || y > 0, off == Utils.OffsetTime.HOURS) : "";
        String days = bD ? conversion(d, "d", mth > 0 || y > 0, off == Utils.OffsetTime.DAYS) : "";
        String months = bMTH ? conversion(mth, "m", y > 0, off == Utils.OffsetTime.MONTHS) : "";
        String years = y > 0 ? y + "y" : "";
        return years + months + days + hours + mins + secs + ticks;
    }

    public static String conversion(int value, String inc, boolean above, boolean isEnding) {
        return value > 0 ? above ? value < 10 ? ":0" + value + inc : ":" + value + inc : value + inc : above ? ":00" + inc : isEnding ? "0" + inc : "";
    }
}
