package uwu.lopyluna.excavein.tracker;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import uwu.lopyluna.excavein.Excavein;
import uwu.lopyluna.excavein.network.CooldownPacket;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static uwu.lopyluna.excavein.Utils.randomChance;
import static uwu.lopyluna.excavein.client.KeybindHandler.SELECTION_ACTIVATION;
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

    private static ServerPlayer player;
    private static BlockHitResult cursorRayTrace;
    private static boolean keyIsDown = false;

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
            BlockPos cursorBlockPos = cursorRayTrace.getBlockPos();

            boolean isAir = player.getLevel().isEmptyBlock(cursorBlockPos);

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
            Excavein.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new CooldownPacket(cooldownTicks));

            if (currentTickDelay > 0) {
                currentTickDelay--;
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(player instanceof FakePlayer) && keyIsDown) {
            if (CooldownTracker.isCooldownNotActive(player)) {
                ServerLevel level = player.getLevel();

                for (BlockPos pos : savedBlockPositions) {
                    if (pos.equals(savedStartPos) && savedStartPos.equals(event.getPos()))
                        continue;
                    if (REQUIRES_HUNGER.get() && player.getFoodData().getFoodLevel() == 0)
                        continue;
                    BlockState blockState = level.getBlockState(pos);
                    Block block = blockState.getBlock();
                    BlockEntity be = level.getBlockEntity(pos);
                    destroyBlock(level, player, pos, blockState, be, player.getMainHandItem(), BLOCKS_AT_PLAYER.get());
                }
                if (BLOCKS_AT_PLAYER.get()) {
                    List<ItemStack> stack = getDrops(event.getState(), level, event.getPos(), level.getBlockEntity(event.getPos()), player, player.getMainHandItem());
                    for (ItemStack pStack : stack) {
                        level.getEntities(EntityType.ITEM, new AABB(event.getPos()).inflate(1), EntitySelector.NO_SPECTATORS).forEach(entity -> {
                            if (entity.getItem().equals(pStack)) {
                                entity.setPickUpDelay(ITEM_PICKUP_DELAY.get());
                                Vec3 pos = player.position();
                                entity.teleportTo(pos.x, pos.y, pos.z);
                            }
                        });
                    }
                }
                CooldownTracker.resetCooldown(player, savedBlockPositions.size());
                resetTick();
                savedBlockPositions.clear();
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() != null && !(event.getEntity() instanceof FakePlayer) && !(player instanceof FakePlayer) && (keyIsDown || SELECTION_ACTIVATION.isDown())) {
            if (CooldownTracker.isCooldownNotActive(player)) {
                Player pPlayer = event.getEntity();
                Level pLevel = event.getLevel();

                for (BlockPos pos : savedBlockPositions) {
                    if (pos.equals(savedStartPos) && savedStartPos.equals(event.getPos()))
                        continue;
                    if (REQUIRES_HUNGER.get() && player.getFoodData().getFoodLevel() == 0)
                        continue;
                    BlockState blockState = pLevel.getBlockState(pos);
                    interaction(player, (LocalPlayer) pPlayer, pos);
                }
                if (pPlayer.getItemInHand(pPlayer.getUsedItemHand()).getItem() instanceof BlockItem && BLOCK_PLACING.get())
                    CooldownTracker.resetCooldown(player, savedBlockPositions.size());
                resetTick();
                savedBlockPositions.clear();
            }
        }
    }


    public static void interaction(ServerPlayer pPlayer, LocalPlayer localPlayer, BlockPos pPos) {
        InteractionHand hand = pPlayer.getUsedItemHand();
        ItemStack stack = pPlayer.getItemInHand(hand);
        InteractionHand handClient = pPlayer.getUsedItemHand();
        ItemStack stackClient = pPlayer.getItemInHand(hand);
        boolean clientSideUse = localPlayer.getLevel().getBlockState(pPos).use(localPlayer.getLevel(), localPlayer, handClient, cursorRayTrace.withPosition(pPos)) == InteractionResult.sidedSuccess(true);
        boolean clientUse = localPlayer.getLevel().getBlockState(pPos).use(localPlayer.getLevel(), localPlayer, handClient, cursorRayTrace.withPosition(pPos)).consumesAction();
        boolean serverUse = pPlayer.getLevel().getBlockState(pPos).use(pPlayer.getLevel(), pPlayer, hand, cursorRayTrace.withPosition(pPos)).consumesAction();
        boolean clientSideUseOn = stack.useOn(new UseOnContext(localPlayer, handClient, cursorRayTrace.withPosition(pPos))) == InteractionResult.sidedSuccess(true);
        boolean clientUseOn = stack.useOn(new UseOnContext(localPlayer, handClient, cursorRayTrace.withPosition(pPos))).consumesAction();
        boolean serverUseOn = stack.useOn(new UseOnContext(pPlayer, hand, cursorRayTrace.withPosition(pPos))).consumesAction();
        if (pPlayer.getLevel().mayInteract(pPlayer, pPos) || localPlayer.getLevel().mayInteract(pPlayer, pPos)) {
            if (HAND_INTERACTION.get() && clientSideUse || clientUse || serverUse) {
                pPlayer.closeContainer();
            } else if ((stack.getItem() instanceof BlockItem && BLOCK_PLACING.get()) != (!(stack.getItem() instanceof BlockItem) && ITEM_INTERACTION.get())) {
                if (clientSideUseOn || clientUseOn || serverUseOn) {
                    pPlayer.closeContainer();
                }
            }
        }
    }

    public static void destroyBlock(ServerLevel pLevel, ServerPlayer pPlayer, BlockPos pPos, BlockState pState, @Nullable BlockEntity pBlockEntity, ItemStack pTool, boolean isPlayerPos) {
        assert pTool != null;
        Vec3 vec = isPlayerPos ? pPlayer.position() : Vec3.atCenterOf(pPos);
        ItemStack pTool1 = pTool.copy();

        pPlayer.awardStat(Stats.BLOCK_MINED.get(pState.getBlock()));
        pPlayer.causeFoodExhaustion((float) (0.005F * (savedBlockPositions.size() * FOOD_EXHAUSTION_MULTIPLIER.get())));
        if (pState.getDestroySpeed(pLevel, pPos) != 0.0F && !pTool.isEmpty() && randomChance(15, pLevel))
            pTool.hurtAndBreak(1, pPlayer, (flag) -> flag.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        pState.spawnAfterBreak(pLevel, pPos, pTool, true);

        pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
        pLevel.removeBlock(pPos, false);
        pTool.mineBlock(pLevel, pState, pPos, pPlayer);
        if (pTool.isEmpty() && !pTool1.isEmpty())
            ForgeEventFactory.onPlayerDestroyItem(pPlayer, pTool1, InteractionHand.MAIN_HAND);
        if (ForgeHooks.isCorrectToolForDrops(pState, pPlayer) && !pPlayer.isCreative()) {
            getDrops(pState, pLevel, pPos, pBlockEntity, pPlayer, pTool).forEach((pStack) ->
                    popResource(pLevel, vec, pStack, isPlayerPos));

            int fortuneLevel = pTool.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
            int silkTouchLevel = pTool.getEnchantmentLevel(Enchantments.SILK_TOUCH);
            int exp = pState.getExpDrop(pLevel, pLevel.random, pPos, fortuneLevel, silkTouchLevel);
            if (exp > 0)
                if (pLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && !pLevel.restoringBlockSnapshots) {
                    ExperienceOrb.award(pLevel, vec, exp);
                }
        }
    }

    public static List<ItemStack> getDrops(BlockState pState, ServerLevel pLevel, BlockPos pPos, @Nullable BlockEntity pBlockEntity, @Nullable Entity pEntity, ItemStack pTool) {
        LootContext.Builder lootcontext$builder = (new LootContext.Builder(pLevel))
                .withRandom(pLevel.random)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pPos))
                .withParameter(LootContextParams.TOOL, pTool)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, pEntity)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, pBlockEntity);
        return pState.getDrops(lootcontext$builder);
    }

    public static void popResource(Level pLevel, Vec3 pPos, ItemStack pStack, boolean isPlayerPos) {
        float f = EntityType.ITEM.getHeight() / 2.0F;
        double d0 = (double)((float)pPos.x() + (isPlayerPos ? 0 : 0.5F)) + Mth.nextDouble(pLevel.random, -0.25D, 0.25D);
        double d1 = (double)((float)pPos.y() + (isPlayerPos ? 0 : 0.5F)) + Mth.nextDouble(pLevel.random, -0.25D, 0.25D) - (double)f;
        double d2 = (double)((float)pPos.z() + (isPlayerPos ? 0 : 0.5F)) + Mth.nextDouble(pLevel.random, -0.25D, 0.25D);
        ItemEntity item = new ItemEntity(pLevel, d0, d1, d2, pStack);
        item.setPickUpDelay(ITEM_PICKUP_DELAY.get());
        popResource(pLevel, () -> item, pStack);
    }

    private static void popResource(Level pLevel, Supplier<ItemEntity> pItemEntitySupplier, ItemStack pStack) {
        if (!pLevel.isClientSide && !pStack.isEmpty() && pLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && !pLevel.restoringBlockSnapshots) {
            ItemEntity itementity = pItemEntitySupplier.get();
            itementity.setDefaultPickUpDelay();
            pLevel.addFreshEntity(itementity);
        }
    }
}
