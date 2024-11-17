package uwu.lopyluna.excavein.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.EventHooks;
import uwu.lopyluna.excavein.data.SelectionPlayerData;
import uwu.lopyluna.excavein.mixins.BlockAccessor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

import static uwu.lopyluna.excavein.config.ServerConfig.*;

public class BreakingUtils {
    public static Set<BlockPos> savedBlockPositions = new HashSet<>();
    private final SelectionPlayerData player;
    private int breakDelay = 0;
    private int amount = 0;
    private boolean breaking = false;

    public BreakingUtils(SelectionPlayerData data) {
        player = data;
    }

    public void tick() {
        if (breakDelay > 0)
            breakDelay--;
    }

    public void preformBreak() {
        if (isBreaking()) {
            if (getBlockPositions().isEmpty()) {
                end();
            } else {
                if (DELAY_BETWEEN_BREAK.get() == 0) {
                    getBlockPositions().forEach(this::removeBlockPos);
                } else if (ready()) {
                    for (int i = 0; i < BLOCK_PER_BREAK.get(); i++) removeAnyBlockPos();
                    resetDelay();
                }
            }
        }
    }

    public void resetDelay() {
        breakDelay = DELAY_BETWEEN_BREAK.get();
    }

    public int breakDelay() {
        return breakDelay;
    }

    public boolean ready() {
        return breakDelay() == 0;
    }

    public boolean isBreaking() {
        return breaking;
    }

    public void breakBlocks(BlockPos pos) {
        saveBlockPositions();
        getBlockPositions().remove(pos);
        breaking = true;
    }

    public void end() {
        if (breaking) player.resetCooldown(amount);
        breaking = false;
        amount = 0;
    }

    public Set<BlockPos> getBlockPositions() {
        return savedBlockPositions;
    }

    public void removeBlockPos(BlockPos pos) {
        if (!isBreaking())
            return;
        if (!player.flagMessage()) {
            end();
            return;
        }
        if (pos == null)
            return;
        destroyBlock(pos);
        savedBlockPositions.remove(pos);
        amount++;
    }

    public void removeAnyBlockPos() {
        if (!isBreaking())
            return;
        if (!player.flagMessage()) {
            end();
            return;
        }
        Optional<BlockPos> pos = savedBlockPositions.stream().findAny();
        if (pos.isEmpty())
            return;
        destroyBlock(pos.get());
        savedBlockPositions.remove(pos.get());
        amount++;
    }

    public void saveBlockPositions() {
        savedBlockPositions = randomizePositions(player.getBlocks());
    }

    public Set<BlockPos> randomizePositions(Set<BlockPos> blockPosSet) {
        List<BlockPos> blockPosList = new ArrayList<>(blockPosSet);
        Collections.shuffle(blockPosList, new Random());
        return new LinkedHashSet<>(blockPosList);
    }

    public void destroyBlock(BlockPos pPos) {
        Level level = player.getLevel();
        Player player = this.player.getPlayer();
        BlockState blockstate = level.getBlockState(pPos);
        BlockEntity blockentity = level.getBlockEntity(pPos);
        Block block = blockstate.getBlock();
        if (block instanceof GameMasterBlock && !player.canUseGameMasterBlocks()) {
            level.sendBlockUpdated(pPos, blockstate, blockstate, 3);
        } else {
            if (blockstate.is(BlockTags.GUARDED_BY_PIGLINS)) {
                PiglinAi.angerNearbyPiglins(player, false);
            }
            level.gameEvent(GameEvent.BLOCK_DESTROY, pPos, GameEvent.Context.of(player, blockstate));

            level.removeBlock(pPos, false);
            if (!player.isCreative()) {
                ItemStack itemstack = player.getMainHandItem();

                ItemStack itemstack1 = itemstack.copy();
                Tool tool = itemstack1.get(DataComponents.TOOL);
                if (tool != null && !NO_DURABILITY_LOSS.get() && (!level.isClientSide && blockstate.getDestroySpeed(level, pPos) != 0.0F && tool.damagePerBlock() > 0)) {
                    itemstack.hurtAndBreak(tool.damagePerBlock(), player, EquipmentSlot.MAINHAND);
                    player.awardStat(Stats.ITEM_USED.get(itemstack.getItem()));
                }
                playerDestroy(block, level, player, pPos, blockstate, blockentity, itemstack);
                if (itemstack.isEmpty() && !itemstack1.isEmpty()) {
                    EventHooks.onPlayerDestroyItem(player, itemstack, InteractionHand.MAIN_HAND);
                }
            }
        }
    }

    public void playerDestroy(Block block, Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState, @Nullable BlockEntity pBlockEntity, ItemStack pTool) {
        pPlayer.awardStat(Stats.BLOCK_MINED.get(block));
        if (!pPlayer.isCreative()) {
            if ((savedBlockPositions.size() * FOOD_EXHAUSTION_MULTIPLIER.get()) != 0)
                pPlayer.causeFoodExhaustion((float) (0.005F * (savedBlockPositions.size() * FOOD_EXHAUSTION_MULTIPLIER.get())));
            if (XP_EXHAUSTION_AMOUNT.get() != 0)
                pPlayer.giveExperiencePoints(-XP_EXHAUSTION_AMOUNT.get());
        }
        dropResources(pState, pLevel, pPos, pBlockEntity, pPlayer, pTool, BLOCKS_AT_PLAYER.get());
    }

    public void dropResources(BlockState pState, Level pLevel, BlockPos pPos, @Nullable BlockEntity pBlockEntity, @Nullable Entity pEntity, ItemStack pTool, boolean isPlayerPos) {
        if (pLevel instanceof ServerLevel) {
            beginCapturingDrops();
            Vec3 vec;
            if (isPlayerPos) {
                assert pEntity != null;
                vec = pEntity.position();
            } else {
                vec = Vec3.atCenterOf(pPos);
            }
            getDrops(pState, (ServerLevel) pLevel, pPos, pBlockEntity, pEntity, pTool).forEach(p_49944_ -> popResource(pLevel, vec, p_49944_, isPlayerPos));
            List<ItemEntity> captured = stopCapturingDrops();
            CommonHooks.handleBlockDrops((ServerLevel) pLevel, pPos, pState, pBlockEntity, captured, pEntity, pTool);
        }
    }

    public List<ItemStack> getDrops(BlockState pState, ServerLevel pLevel, BlockPos pPos, @Nullable BlockEntity pBlockEntity, @Nullable Entity pEntity, ItemStack pTool) {
        LootParams.Builder lootcontext$builder = (new LootParams.Builder(pLevel))
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pPos))
                .withParameter(LootContextParams.TOOL, pTool)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, pEntity)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, pBlockEntity);
        return pState.getDrops(lootcontext$builder);
    }

    private void beginCapturingDrops() {
        BlockAccessor.excavein$capturedDrops(new ArrayList<>());
    }

    private List<ItemEntity> stopCapturingDrops() {
        List<ItemEntity> drops = BlockAccessor.excavein$capturedDrops();
        BlockAccessor.excavein$capturedDrops(null);
        return drops;
    }

    public void popResource(Level pLevel, Vec3 pPos, ItemStack pStack, boolean isPlayerPos) {
        double f = (double) EntityType.ITEM.getHeight() / 2.0;
        double d0 = (double) ((float) pPos.x() + (isPlayerPos ? 0 : 0.5F)) + Mth.nextDouble(pLevel.random, -0.25, 0.25);
        double d1 = (double) ((float) pPos.y() + (isPlayerPos ? 0 : 0.5F)) + Mth.nextDouble(pLevel.random, -0.25, 0.25) - f;
        double d2 = (double) ((float) pPos.z() + (isPlayerPos ? 0 : 0.5F)) + Mth.nextDouble(pLevel.random, -0.25, 0.25);
        popResource(pLevel, () -> new ItemEntity(pLevel, d0, d1, d2, pStack), pStack);
    }

    private void popResource(Level pLevel, Supplier<ItemEntity> pItemEntitySupplier, ItemStack pStack) {
        if (!pLevel.isClientSide && !pStack.isEmpty() && pLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && !pLevel.restoringBlockSnapshots) {
            ItemEntity itementity = pItemEntitySupplier.get();
            itementity.setPickUpDelay((player.getPlayer().isCreative() ? 0 : ITEM_PICKUP_DELAY.get()));
            List<ItemEntity> stacks = BlockAccessor.excavein$capturedDrops();
            if (stacks != null) {
                stacks.add(itementity);
                BlockAccessor.excavein$capturedDrops(stacks);
            } else {
                pLevel.addFreshEntity(itementity);
            }
        }
    }
}
