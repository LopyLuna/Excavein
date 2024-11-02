package uwu.lopyluna.excavein.tracker;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import uwu.lopyluna.excavein.Excavein;
import uwu.lopyluna.excavein.network.CooldownPacket;
import uwu.lopyluna.excavein.network.IsBreakingPacket;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

import static uwu.lopyluna.excavein.Utils.*;
import static uwu.lopyluna.excavein.config.ServerConfig.*;
import static uwu.lopyluna.excavein.tracker.CooldownTracker.getCoolDownCheck;
import static uwu.lopyluna.excavein.tracker.CooldownTracker.getRemainingCooldown;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber
public class BlockPositionTracker {
    public static Set<BlockPos> currentBlocksPositions = new HashSet<>();
    public static Set<BlockPos> savedBlockPositions = new HashSet<>();
    public static BlockPos savedStartPos;

    private static final int MAX_TICK_DELAY = 1;
    private static int currentTickDelay = 0;

    private static int currentBreakDelay = 0;

    public static ServerPlayer player;
    public static BlockHitResult cursorRayTrace;
    public static boolean keyIsDown = false;
    public static boolean save = false;

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
    public static void onWorldTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && player != null && cursorRayTrace != null) {
            Excavein.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new IsBreakingPacket(isBreaking));
            BlockPos cursorBlockPos = cursorRayTrace.getBlockPos();
            boolean isAir = player.getLevel().isEmptyBlock(cursorBlockPos);

            if (isAir && (currentTickDelay != MAX_TICK_DELAY))
                resetTick();
            else if (currentBlocksPositions != null && savedBlockPositions != null && ((!currentBlocksPositions.equals(savedBlockPositions) && save) || (currentTickDelay == 0 && (!currentBlocksPositions.equals(savedBlockPositions)) && (!WAIT_TILL_BROKEN.get() || !isBreaking)))) {
                setSavedBlocks(new HashSet<>(currentBlocksPositions));
                if (save) save = false;
            }
            if (savedBlockPositions == null)
                savedBlockPositions = new HashSet<>();

            getCoolDownCheck(player);
            int cooldownTicks = getRemainingCooldown(player);
            Excavein.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new CooldownPacket(cooldownTicks));

            if (currentTickDelay > 0)
                currentTickDelay--;

            if (DELAY_BETWEEN_BREAK.get() > 0 && isBreaking)
                if (currentBreakDelay == 0) {
                    for (int i = 0; i < BLOCK_PER_BREAK.get(); i++)
                        performBlockBreakTick();
                    currentBreakDelay = DELAY_BETWEEN_BREAK.get();
                }
            if (currentBreakDelay > 0)
                currentBreakDelay--;
        }
    }

    public static Set<BlockPos> blocksToBreak = new HashSet<>();
    static int i = 0;

    public static void onBlockDrop(Player pBreaker, ItemEntity drop) {
        if (pBreaker.is(player) && flag()) {
            drop.setPickUpDelay((player.isCreative() ? 0 : ITEM_PICKUP_DELAY.get()));
            Vec3 pos = player.position();
            if (BLOCKS_AT_PLAYER.get()) {
                drop.teleportTo(pos.x, pos.y, pos.z);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (isBreaking && WAIT_TILL_BROKEN.get()) {
            event.setCanceled(true);
        } else if (DELAY_BETWEEN_BREAK.get() > 0 && (!isBreaking || !WAIT_TILL_BROKEN.get()) && flag()) {
            blocksToBreak.addAll(savedBlockPositions);
            isBreaking = true;
            save = true;
        } else if (DELAY_BETWEEN_BREAK.get() == 0 && !isBreaking && flag()) {
            performBlockBreak();
        }
        if (event.getPlayer().is(player) && flag()) {
            event.getLevel().getEntities(EntityTypeTest.forClass(ItemEntity.class), new AABB(event.getPos()), EntitySelector.NO_SPECTATORS).forEach(itemEntity -> {
                itemEntity.setPickUpDelay((player.isCreative() ? 0 : ITEM_PICKUP_DELAY.get()));
                if (BLOCKS_AT_PLAYER.get()) itemEntity.teleportTo(player.position().x, player.position().y, player.position().z);
            });
            Vec3 pos = player.position();
            if (BLOCKS_AT_PLAYER.get()) {
                player.giveExperiencePoints(event.getExpToDrop());
                event.setExpToDrop(0);
            }
        }
    }

    public static boolean isBreaking;

    public static void performBlockBreakTick() {
        if (!blocksToBreak.isEmpty()) {
            ServerLevel level = player.getLevel();

            BlockPos pos = blocksToBreak.stream().toList().get(getPos(blocksToBreak));
            if (blockBreak(level, pos)) {
                blocksToBreak.remove(pos);
            } else reset();

            if (blocksToBreak.isEmpty())
                reset();
        }
    }

    public static int getPos(Set<BlockPos> pos) {
        int size = pos.toArray().length;
        if (size != 0) {
            int v = player.getLevel().getRandom().nextInt(0, size);
            v = v < 0 ? v * -1 : v;
            v = v > size ? size : Math.max(v, 0);
            return v;
        }
        return 0;
    }

    public static void performBlockBreak() {
        isBreaking = true;

        savedBlockPositions.forEach(pos -> blockBreak(player.getLevel(), pos));
        reset();
    }

    public static void reset() {
        CooldownTracker.resetCooldown(player, player.isCreative() ? 0 : i);
        if (!player.isCreative())
            if (i > 0) removingFuelItems(player, FUEL_EXHAUSTION_AMOUNT.get() * i);
        if (isBreaking) isBreaking = false;

        i = 0;
        resetTick();
        if (!savedBlockPositions.isEmpty()) savedBlockPositions.clear();
        if (!blocksToBreak.isEmpty()) blocksToBreak.clear();
    }

    public static boolean flag() {
        return (!(player instanceof FakePlayer) && keyIsDown) && CooldownTracker.isCooldownNotActive(player) && !savedBlockPositions.isEmpty();
    }

    public static boolean blockBreak(ServerLevel level, BlockPos pos) {
        if (pos == null)
            return false;

        boolean valid = (!REQUIRES_XP.get() || player.isCreative() || player.totalExperience != 0) &&
                (!REQUIRES_HUNGER.get() || player.isCreative() || player.getFoodData().getFoodLevel() != 0) &&
                (!REQUIRES_FUEL_ITEM.get() || player.isCreative() || findInInventory(player) != 0) &&
                !(PREVENT_BREAKING_TOOL.get() && !player.isCreative() && player.getMainHandItem().isDamageableItem() && player.getMainHandItem().getMaxDamage() - player.getMainHandItem().getDamageValue() == 1) &&
                (!REQUIRES_TOOLS.get() || !player.getMainHandItem().isEmpty() || player.isCreative()) && (!isValidForPlacing(player.getLevel(), player, pos));

        if (REQUIRES_XP.get() && !player.isCreative() && player.totalExperience == 0)
            player.displayClientMessage(Component.translatable("excavein.warning.require_xp").withStyle(ChatFormatting.RED), true); else
        if (REQUIRES_HUNGER.get() && !player.isCreative() && player.getFoodData().getFoodLevel() == 0)
            player.displayClientMessage(Component.translatable("excavein.warning.require_hunger").withStyle(ChatFormatting.RED), true); else
        if (REQUIRES_FUEL_ITEM.get() && !player.isCreative() && findInInventory(player) == 0)
            player.displayClientMessage(Component.translatable("excavein.warning.require_fuel").withStyle(ChatFormatting.RED), true);

        if (valid) {
            destroyBlock(level, player, pos);
            i++;
            return true;
        }
        return false;
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

            level.removeBlock(pPos, false);
            if (!player.isCreative()) {
                ItemStack itemstack = player.getMainHandItem();
                ItemStack itemstack1 = itemstack.copy();

                if (!NO_DURABILITY_LOSS.get() && (!level.isClientSide && blockstate.getDestroySpeed(level, pPos) != 0.0F)) {
                    itemstack.mineBlock(level, blockstate, pPos, player);
                    player.awardStat(Stats.ITEM_USED.get(itemstack.getItem()));
                }

                playerDestroy(block, level, player, pPos, blockstate, blockentity, itemstack);
                if (itemstack.isEmpty() && !itemstack1.isEmpty())
                    net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, itemstack, InteractionHand.MAIN_HAND);
            }
        }
    }

    public static void playerDestroy(Block block, Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState, @Nullable BlockEntity pBlockEntity, ItemStack pTool) {
        pPlayer.awardStat(Stats.BLOCK_MINED.get(block));
        if (!pPlayer.isCreative()) {
            if ((savedBlockPositions.size() * FOOD_EXHAUSTION_MULTIPLIER.get()) != 0)
                pPlayer.causeFoodExhaustion((float) (0.005F * (savedBlockPositions.size() * FOOD_EXHAUSTION_MULTIPLIER.get())));
            if (XP_EXHAUSTION_AMOUNT.get() != 0)
                pPlayer.giveExperiencePoints(-XP_EXHAUSTION_AMOUNT.get());
        }
        dropResources(pState, pLevel, pPos, pBlockEntity, pPlayer, pTool, BLOCKS_AT_PLAYER.get());
    }

    public static void dropResources(BlockState pState, Level pLevel, BlockPos pPos, @Nullable BlockEntity pBlockEntity, @Nullable Entity pEntity, ItemStack pTool, boolean isPlayerPos) {
        if (pLevel instanceof ServerLevel) {
            Vec3 vec;
            if (isPlayerPos) {
                assert pEntity != null;
                vec = pEntity.position();
            } else {
                vec = Vec3.atCenterOf(pPos);
            }
            getDrops(pState, (ServerLevel)pLevel, pPos, pBlockEntity, pEntity, pTool).forEach(stack ->
                    popResource(pLevel, vec, stack, isPlayerPos, pState.getBlock()));
            pState.spawnAfterBreak((ServerLevel)pLevel, pPos, pTool, false);

            int fortuneLevel = pTool.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
            int silkTouchLevel = pTool.getEnchantmentLevel(Enchantments.SILK_TOUCH);
            int exp = pLevel.getBlockState(pPos).getExpDrop(pLevel, pLevel.random, pPos, fortuneLevel, silkTouchLevel);
            if (exp > 0) popExperience((ServerLevel)pLevel, vec, exp);
        }
    }

    public static void popExperience(ServerLevel pLevel, Vec3 pPos, int pAmount) {
        if (pLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && !pLevel.restoringBlockSnapshots)
            ExperienceOrb.award(pLevel, pPos, pAmount);
    }

    public static List<ItemStack> getDrops(BlockState pState, ServerLevel pLevel, BlockPos pPos, @Nullable BlockEntity pBlockEntity, @Nullable Entity pEntity, ItemStack pTool) {
        LootContext.Builder lootcontext$builder = (new LootContext.Builder(pLevel))
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pPos))
                .withParameter(LootContextParams.TOOL, pTool)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, pEntity)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, pBlockEntity);
        return pState.getDrops(lootcontext$builder);
    }

    public static void popResource(Level pLevel, Vec3 pPos, ItemStack pStack, boolean isPlayerPos, Block block) {
        double f  = (double) EntityType.ITEM.getHeight() / 2.0;
        double d0 = (double)((float)pPos.x() + (isPlayerPos ? 0 : 0.5F)) + Mth.nextDouble(pLevel.random, -0.25, 0.25);
        double d1 = (double)((float)pPos.y() + (isPlayerPos ? 0 : 0.5F)) + Mth.nextDouble(pLevel.random, -0.25, 0.25) - f;
        double d2 = (double)((float)pPos.z() + (isPlayerPos ? 0 : 0.5F)) + Mth.nextDouble(pLevel.random, -0.25, 0.25);
        popResource(pLevel, () -> new ItemEntity(pLevel, d0, d1, d2, pStack), pStack, block);
    }

    private static void popResource(Level pLevel, Supplier<ItemEntity> pItemEntitySupplier, ItemStack pStack, Block block) {
        if (!pLevel.isClientSide && !pStack.isEmpty() && pLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && !pLevel.restoringBlockSnapshots) {
            ItemEntity itementity = pItemEntitySupplier.get();
            itementity.setPickUpDelay((player.isCreative() ? 0 : ITEM_PICKUP_DELAY.get()));
            pLevel.addFreshEntity(itementity);
            onBlockDrop(player, itementity);
        }
    }
}
