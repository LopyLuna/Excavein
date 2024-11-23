package uwu.lopyluna.excavein.data;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import uwu.lopyluna.excavein.config.ServerConfig;
import uwu.lopyluna.excavein.entries.ExcaveinEntries;
import uwu.lopyluna.excavein.entries.ShapeEntry;
import uwu.lopyluna.excavein.entries.ShapeModifierEntry;
import uwu.lopyluna.excavein.packets.ClientHelperBoolsPacket;
import uwu.lopyluna.excavein.packets.ClientHelperModesPacket;
import uwu.lopyluna.excavein.packets.CooldownPacket;
import uwu.lopyluna.excavein.packets.SelectedBlocksPacket;
import uwu.lopyluna.excavein.shape_modifiers.ShapeModifier;
import uwu.lopyluna.excavein.shapes.Shape;
import uwu.lopyluna.excavein.utils.BreakingUtils;
import uwu.lopyluna.excavein.utils.Interact;
import uwu.lopyluna.excavein.utils.InteractionUtils;
import uwu.lopyluna.excavein.utils.Utils;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static uwu.lopyluna.excavein.config.ServerConfig.*;
import static uwu.lopyluna.excavein.utils.Utils.findInInventory;

@SuppressWarnings("unused")
public class SelectionPlayerData {

    private final BreakingUtils breakingUtils;
    private final InteractionUtils interactionUtils;
    private final CooldownData cooldownData;
    private final ServerLevel level;
    private final ServerPlayer player;
    private final UUID playerUUID;
    private int shapeMode;
    private int modifierMode;
    private boolean keyPressed;
    private boolean displayChat;

    public SelectionPlayerData(ServerLevel pLevel, UUID uuid) {
        shapeMode = 0;
        modifierMode = 0;
        level = pLevel;
        playerUUID = uuid;
        player = (ServerPlayer) pLevel.getPlayerByUUID(uuid);
        breakingUtils = new BreakingUtils(this);
        interactionUtils = new InteractionUtils(this);
        cooldownData = new CooldownData(this);
    }

    public void updateKey(boolean keyPressed, boolean displayChat) {
        this.keyPressed = keyPressed;
        this.displayChat = displayChat;
    }

    //SHAPE MODE

    public ShapeEntry<? extends Shape> getShape() {
        return ExcaveinEntries.getShapeEntries().get(getShapeMode());
    }

    public ShapeEntry<? extends Shape> getNextShape() {
        return ExcaveinEntries.getShapeEntries().get((getShapeMode() + 1) % ExcaveinEntries.sizeShape);
    }

    public ShapeEntry<? extends Shape> getPrevShape() {
        return ExcaveinEntries.getShapeEntries().get((getShapeMode() - 1 + ExcaveinEntries.sizeShape) % ExcaveinEntries.sizeShape);
    }

    public int getShapeMode() {
        if (shapeMode > ExcaveinEntries.sizeShape)
            shapeMode = 0;
        if (shapeMode < 0)
            shapeMode = ExcaveinEntries.sizeShape;
        return shapeMode;
    }

    public void setShapeMode(int mode) {
        int i = mode > ExcaveinEntries.sizeShape ? ExcaveinEntries.sizeShape : Math.max(mode, 0);
        if (getShapeMode() != i) {
            shapeMode = i;
            getShapeMode();
            displayShapeMode();
        }
    }

    public void nextShapeMode() {
        shapeMode = (getShapeMode() + 1) % ExcaveinEntries.sizeShape;
        getShapeMode();
        displayShapeMode();
    }

    public void previousShapeMode() {
        shapeMode = (getShapeMode() - 1 + ExcaveinEntries.sizeShape) % ExcaveinEntries.sizeShape;
        getShapeMode();
        displayShapeMode();
    }

    //MODIFIER MODE

    public ShapeModifierEntry<? extends ShapeModifier> getModifier() {
        return ExcaveinEntries.getShapeModifierEntries().get(getModifierMode());
    }

    public ShapeModifierEntry<? extends ShapeModifier> getNextModifier() {
        return ExcaveinEntries.getShapeModifierEntries().get((getModifierMode() + 1) % ExcaveinEntries.sizeModifier);
    }

    public ShapeModifierEntry<? extends ShapeModifier> getPrevModifier() {
        return ExcaveinEntries.getShapeModifierEntries().get((getModifierMode() - 1 + ExcaveinEntries.sizeModifier) % ExcaveinEntries.sizeModifier);
    }

    public int getModifierMode() {
        if (modifierMode > ExcaveinEntries.sizeModifier)
            modifierMode = 0;
        if (modifierMode < 0)
            modifierMode = ExcaveinEntries.sizeModifier;
        return modifierMode;
    }

    public void setModifierMode(int mode) {
        int i = mode > ExcaveinEntries.sizeModifier ? ExcaveinEntries.sizeModifier : Math.max(mode, 0);
        if (getModifierMode() != i) {
            modifierMode = i;
            getModifierMode();
            displayShapeMode();
        }
    }

    public void nextModifierMode() {
        modifierMode = (getModifierMode() + 1) % ExcaveinEntries.sizeModifier;
        getModifierMode();
        displayShapeMode();
    }

    public void previousModifierMode() {
        modifierMode = (getModifierMode() - 1 + ExcaveinEntries.sizeModifier) % ExcaveinEntries.sizeModifier;
        getModifierMode();
        displayShapeMode();
    }


    //OVERALL CLASS

    public void tick() {
        getBreakingUtils().preformBreak();
        getBreakingUtils().tick();
    }

    public boolean blockBreak(GameType gameModeForPlayer, BlockPos pos) {
        if (isKeyPressed())
            return getBreakingUtils().breakBlocks(gameModeForPlayer, pos);
        return false;
    }

    public List<InteractionResult> blockInteract(GameType gameModeForPlayer, Interact interact) {
        if (isKeyPressed())
            return getInteractionUtils().interactBlocks(gameModeForPlayer, interact);
        return List.of();
    }

    public Set<BlockPos> getBlocks(boolean isBreaking) {
        if (level == null || playerUUID == null || player == null || (!isBreaking && !BLOCK_PLACING.get() && !HAND_INTERACTION.get() && !ITEM_INTERACTION.get()))
            return Set.of();
        BlockHitResult rayTrace = getPlayerRayTraceToBlock(player);
        AttributeInstance attribute = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        int playerBlockRange = attribute == null ? 0 : (int) attribute.getValue();
        Vec3 eye = player.getEyePosition();
        return rayTrace != null ? Utils.constructSelection(isBreaking,
                this,
                rayTrace,
                new BlockPos(new Vec3i((int) eye.x, (int) eye.y, (int) eye.z)),
                ServerConfig.SELECTION_MAX_BLOCK.get(),
                playerBlockRange + ServerConfig.SELECTION_ADD_RANGE.get(),
                getShape().getShape(),
                getModifier().getShapeModifier()) : Set.of();
    }

    public void updateCheck() {
        PacketDistributor.sendToPlayer(player, new SelectedBlocksPacket(getBlocks(true), getBlocks(false)));
        PacketDistributor.sendToPlayer(player, new CooldownPacket(getRemainingCooldown()));
        PacketDistributor.sendToPlayer(player, new ClientHelperBoolsPacket(getBreakingUtils().isBreaking(), requiredFlags(), flag()));
        PacketDistributor.sendToPlayer(player, new ClientHelperModesPacket(
                getShape().getShape().getName(),
                getPrevShape().getShape().getName(),
                getNextShape().getShape().getName(),
                getModifier().getShapeModifier().getName(),
                getPrevModifier().getShapeModifier().getName(),
                getNextModifier().getShapeModifier().getName()
        ));
    }

    public BlockHitResult getPlayerRayTraceToBlock(Player player) {
        AttributeInstance attribute = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        double reachDistance = attribute == null ? 0 : (int) attribute.getValue();
        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 lookVector = player.getLookAngle().scale(reachDistance);
        Vec3 reachPosition = eyePosition.add(lookVector);

        BlockHitResult hitResult = player.level().clip(new ClipContext(
                eyePosition, reachPosition,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));

        if (hitResult.getType() == HitResult.Type.BLOCK)
            return hitResult;
        return null;
    }

    public boolean flag() {
        return requiredFlags() && !isCooldownActive() && isKeyPressed();
    }

    public boolean requiredFlags() {
        boolean hasXP = !REQUIRES_XP.get() || player.totalExperience != 0 || player.isCreative();
        boolean hasFood = !REQUIRES_HUNGER.get() || player.getFoodData().getFoodLevel() != 0 || player.isCreative();
        boolean hasFuel = !REQUIRES_FUEL_ITEM.get() || findInInventory(player) != 0 || player.isCreative();

        return hasXP && hasFood && hasFuel;
    }

    @SuppressWarnings("all")
    public boolean flagMessage() {
        boolean hasXP = !REQUIRES_XP.get() || player.totalExperience != 0 || player.isCreative();
        boolean hasFood = !REQUIRES_HUNGER.get() || player.getFoodData().getFoodLevel() != 0 || player.isCreative();
        boolean hasFuel = !REQUIRES_FUEL_ITEM.get() || findInInventory(player) != 0 || player.isCreative();

        if (!hasXP)
            player.sendSystemMessage(Component.translatable("excavein.warning.require_xp").withStyle(ChatFormatting.RED), true);
        else if (!hasFood)
            player.sendSystemMessage(Component.translatable("excavein.warning.require_hunger").withStyle(ChatFormatting.RED), true);
        else if (!hasFuel)
            player.sendSystemMessage(Component.translatable("excavein.warning.require_fuel").withStyle(ChatFormatting.RED), true);

        return requiredFlags() && !isCooldownActive();
    }

    public void resetCooldown(int amountOfBlocks) {
        cooldownData.resetCooldown(amountOfBlocks);
    }

    public int getRemainingCooldown() {
        return cooldownData.getRemainingCooldown();
    }

    @SuppressWarnings("all")
    public boolean isCooldownActive() {
        return !cooldownData.isCooldownNotActive();
    }

    public void displayShapeMode() {
        if (getShape() == null || getShape().getShape() == null || getModifier() == null || getModifier().getShapeModifier() == null)
            return;
        String string = getModifier().getShapeModifier().getName();
        Component text = Component.literal(Component.translatable("excavein.overlay.current_mode").getString().replaceAll("_", " ") +
                (!string.isEmpty() ? string + " " : "") + getShape().getShape().getName());

        player.sendSystemMessage(text, !displayChat);
    }

    // GET VARIABLES


    public CooldownData getCooldownData() {
        return cooldownData;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public BreakingUtils getBreakingUtils() {
        return breakingUtils;
    }

    public InteractionUtils getInteractionUtils() {
        return interactionUtils;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public boolean isKeyPressed() {
        return keyPressed;
    }
}
