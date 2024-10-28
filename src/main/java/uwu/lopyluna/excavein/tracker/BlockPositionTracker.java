package uwu.lopyluna.excavein.tracker;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import uwu.lopyluna.excavein.mixins.BlockAccessor;
import uwu.lopyluna.excavein.network.CooldownPacket;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static uwu.lopyluna.excavein.Utils.*;
import static uwu.lopyluna.excavein.config.ServerConfig.*;
import static uwu.lopyluna.excavein.tracker.CooldownTracker.getCoolDownCheck;
import static uwu.lopyluna.excavein.tracker.CooldownTracker.getRemainingCooldown;

@SuppressWarnings("unused")
@EventBusSubscriber
public class BlockPositionTracker {
    public static Set<BlockPos> currentBlocksPositions = new HashSet<>();
    public static Set<BlockPos> savedBlockPositions = new HashSet<>();
    public static BlockPos savedStartPos;

    private static final int MAX_TICK_DELAY = 1;
    private static int currentTickDelay = 0;

    public static ServerPlayer player;
    public static BlockHitResult cursorRayTrace;
    public static boolean keyIsDown = false;

    public static void setSavedBlocks(Set<BlockPos> blocks) {
        savedBlockPositions = blocks;
        savedStartPos = cursorRayTrace.getBlockPos();
    }

    public static void update(ServerPlayer p, BlockHitResult rayTrace, Set<BlockPos> blocks) {
        player = p;
        cursorRayTrace = rayTrace;
        currentBlocksPositions = blocks;
    }

    public static void update(boolean SelectionKeyIsDown) {
        keyIsDown = SelectionKeyIsDown;
    }

    public static void resetTick() {
        currentTickDelay = MAX_TICK_DELAY;
    }

    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Post event) {
        if (player != null && cursorRayTrace != null) {
            BlockPos cursorBlockPos = cursorRayTrace.getBlockPos();

            boolean isAir = player.serverLevel().isEmptyBlock(cursorBlockPos);

            if (isAir) {
                if (currentTickDelay != MAX_TICK_DELAY) {
                    resetTick();
                }
            } else {
                if (currentTickDelay == 0) {
                    if (!currentBlocksPositions.equals(savedBlockPositions)) {
                        setSavedBlocks(new HashSet<>(currentBlocksPositions));
                    }
                }
            }
            getCoolDownCheck(player);
            int cooldownTicks = getRemainingCooldown(player);
            PacketDistributor.sendToPlayer(player, new CooldownPacket(cooldownTicks));

            if (currentTickDelay > 0) {
                currentTickDelay--;
            }
        }
    }

    static int i = 0;

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        performBlockBreak();
        event.setCanceled(true);
    }

    public static void performBlockBreak() {
        if (!(player instanceof FakePlayer) && keyIsDown) {
            if (CooldownTracker.isCooldownNotActive(player)) {
                ServerLevel level = player.serverLevel();

                savedBlockPositions.forEach(pos -> {
                    boolean valid = (!REQUIRES_HUNGER.get() || player.isCreative() || player.getFoodData().getFoodLevel() != 0) &&
                            (!player.getMainHandItem().isDamageableItem() || (player.getMainHandItem().getMaxDamage() - player.getMainHandItem().getDamageValue()) != 1) &&
                            (!REQUIRES_TOOLS.get() || !player.getMainHandItem().isEmpty() || player.isCreative()) && (!isValidForPlacing(player.serverLevel(), player, pos));

                    if (valid) {
                        destroyBlock(level, player, pos);
                        i++;
                    }
                });
                CooldownTracker.resetCooldown(player, player.isCreative() ? 0 : i);
                resetTick();
                savedBlockPositions.clear();
            }
        }

    }

    public static void destroyBlock(ServerLevel level, ServerPlayer player, BlockPos pPos) {
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

            blockstate.getBlock().destroy(level, pPos, blockstate);
            level.removeBlock(pPos, false);
            if (!player.isCreative()) {
                ItemStack itemstack = player.getMainHandItem();

                ItemStack itemstack1 = itemstack.copy();
                Tool tool = itemstack1.get(DataComponents.TOOL);
                if (tool != null && (!level.isClientSide && blockstate.getDestroySpeed(level, pPos) != 0.0F && tool.damagePerBlock() > 0)) {
                    itemstack1.hurtAndBreak(tool.damagePerBlock(), player, EquipmentSlot.MAINHAND);
                    player.awardStat(Stats.ITEM_USED.get(itemstack1.getItem()));
                }

                playerDestroy(block, level, player, pPos, blockstate, blockentity, itemstack1);
                if (itemstack.isEmpty() && !itemstack1.isEmpty()) {
                    EventHooks.onPlayerDestroyItem(player, itemstack1, InteractionHand.MAIN_HAND);
                }
            }
        }
    }

    public static void playerDestroy(Block block, Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState, @Nullable BlockEntity pBlockEntity, ItemStack pTool) {
        pPlayer.awardStat(Stats.BLOCK_MINED.get(block));
        pPlayer.causeFoodExhaustion((float) (0.005F * (savedBlockPositions.size() * FOOD_EXHAUSTION_MULTIPLIER.get())));
        dropResources(pState, pLevel, pPos, pBlockEntity, pPlayer, pTool, BLOCKS_AT_PLAYER.get());
    }

    public static void dropResources(BlockState pState, Level pLevel, BlockPos pPos, @Nullable BlockEntity pBlockEntity, @Nullable Entity pEntity, ItemStack pTool, boolean isPlayerPos) {
        if (pLevel instanceof ServerLevel) {
            ((BlockAccessor) pState.getBlock()).excavein$beginCapturingDrops();
            Vec3 vec;
            if (isPlayerPos) {
                assert pEntity != null;
                vec = pEntity.position();
            } else {
                vec = Vec3.atCenterOf(pPos);
            }
            getDrops(pState, (ServerLevel)pLevel, pPos, pBlockEntity, pEntity, pTool).forEach(p_49944_ -> popResource(pLevel, vec, p_49944_, isPlayerPos, pState.getBlock()));
            List<ItemEntity> captured = ((BlockAccessor) pState.getBlock()).excavein$stopCapturingDrops();
            CommonHooks.handleBlockDrops((ServerLevel) pLevel, pPos, pState, pBlockEntity, captured, pEntity, pTool);
        }
    }

    public static List<ItemStack> getDrops(BlockState pState, ServerLevel pLevel, BlockPos pPos, @Nullable BlockEntity pBlockEntity, @Nullable Entity pEntity, ItemStack pTool) {
        LootParams.Builder lootcontext$builder = (new LootParams.Builder(pLevel))
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pPos))
                .withParameter(LootContextParams.TOOL, pTool)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, pEntity)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, pBlockEntity);
        return pState.getDrops(lootcontext$builder);
    }

    public static void popResource(Level pLevel, Vec3 pPos, ItemStack pStack, boolean isPlayerPos, Block block) {
        double f  = (double) EntityType.ITEM.getHeight() / 2.0;
        double d0 = (double) ((float)pPos.x() + (isPlayerPos ? 0 : 0.5F)) + Mth.nextDouble(pLevel.random, -0.25, 0.25);
        double d1 = (double) ((float)pPos.y() + (isPlayerPos ? 0 : 0.5F)) + Mth.nextDouble(pLevel.random, -0.25, 0.25) - f;
        double d2 = (double) ((float)pPos.z() + (isPlayerPos ? 0 : 0.5F)) + Mth.nextDouble(pLevel.random, -0.25, 0.25);
        popResource(pLevel, () -> new ItemEntity(pLevel, d0, d1, d2, pStack), pStack, block);
    }

    private static void popResource(Level pLevel, Supplier<ItemEntity> pItemEntitySupplier, ItemStack pStack, Block block) {
        if (!pLevel.isClientSide && !pStack.isEmpty() && pLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && !pLevel.restoringBlockSnapshots) {
            ItemEntity itementity = pItemEntitySupplier.get();
            itementity.setPickUpDelay((player.isCreative() ? 0 : ITEM_PICKUP_DELAY.get()));
            if (((BlockAccessor) block).excavein$capturedDrops() != null) {
                ((BlockAccessor) block).excavein$capturedDrops().add(itementity);
            } else {
                pLevel.addFreshEntity(itementity);
            }
        }
    }
}
