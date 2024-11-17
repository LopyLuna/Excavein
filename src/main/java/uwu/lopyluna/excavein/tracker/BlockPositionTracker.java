package uwu.lopyluna.excavein.tracker;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static uwu.lopyluna.excavein.config.ServerConfig.*;
import static uwu.lopyluna.excavein.utils.Utils.calculatePercentage;
import static uwu.lopyluna.excavein.utils.Utils.removingFuelItems;

@SuppressWarnings("unused")
public class BlockPositionTracker {
    private static final int MAX_TICK_DELAY = 1;
    public static Set<BlockPos> currentBlocksPositions = new HashSet<>();
    public static Set<BlockPos> savedBlockPositions = new HashSet<>();
    public static BlockPos savedStartPos;
    public static UUID uuid;
    public static BlockHitResult cursorRayTrace;
    public static boolean keyIsDown = false;
    public static boolean save = false;
    public static Set<BlockPos> blocksToBreak = new HashSet<>();
    public static boolean isBreaking;
    static int i = 0;
    private static int currentTickDelay = 0;
    private static int currentBreakDelay = 0;
    private static ServerPlayer player;
    private static boolean simpleCheck = false;

    public static void setSavedBlocks(Set<BlockPos> blocks) {
        savedBlockPositions = blocks;
        savedStartPos = cursorRayTrace.getBlockPos();
    }

    public static void update(ServerPlayer p, BlockHitResult rayTrace, Set<BlockPos> blocks) {
        player = p;
        cursorRayTrace = rayTrace;
        currentBlocksPositions = blocks;
    }

    public static void update(boolean SelectionKeyIsDown, UUID playerID) {
        keyIsDown = SelectionKeyIsDown;
        uuid = playerID;
    }

    public static void resetTick() {
        currentTickDelay = MAX_TICK_DELAY;
    }

    public static Player verifyPlayer(Player player) {
        return player != null && uuid != null && player.getUUID().equals(uuid) ? player : null;
    }

    //@SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Post event) {
        ServerPlayer player = (ServerPlayer) verifyPlayer(BlockPositionTracker.player);
        if (player != null && cursorRayTrace != null) {
            BlockPos cursorBlockPos = cursorRayTrace.getBlockPos();
            boolean isAir = player.serverLevel().isEmptyBlock(cursorBlockPos);

            if (isAir && (currentTickDelay != MAX_TICK_DELAY))
                resetTick();
            else if (currentBlocksPositions != null && savedBlockPositions != null && ((!currentBlocksPositions.equals(savedBlockPositions) && save) || (currentTickDelay == 0 && (!currentBlocksPositions.equals(savedBlockPositions)) && (!WAIT_TILL_BROKEN.get() || !isBreaking)))) {
                setSavedBlocks(new HashSet<>(currentBlocksPositions));
                if (save) save = false;
            }
            if (savedBlockPositions == null)
                savedBlockPositions = new HashSet<>();

            //getCoolDownCheck(player);
            //int cooldownTicks = getRemainingCooldown(player);
            //PacketDistributor.sendToPlayer(player, new CooldownPacket(cooldownTicks));

            if (currentTickDelay > 0)
                currentTickDelay--;

            if (DELAY_BETWEEN_BREAK.get() > 0 && isBreaking)
                if (currentBreakDelay == 0) {
                    for (int i = 0; i < BLOCK_PER_BREAK.get(); i++)
                        performBlockBreakTick(player);
                    currentBreakDelay = DELAY_BETWEEN_BREAK.get();
                }
            if (currentBreakDelay > 0)
                currentBreakDelay--;


            if (MINING_SPEED_NERF_MAX.get() != 0 && savedBlockPositions != null) {
                //harvestCheck(!flag(), player);
                if (!simpleCheck) simpleCheck = true;
            } else if (simpleCheck) {
                AttributeInstance speed = player.getAttribute(Attributes.BLOCK_BREAK_SPEED);
                if (speed != null) {
                    speed.setBaseValue(speed.getAttribute().value().getDefaultValue());
                    simpleCheck = false;
                }
            }
        }
    }

    public static void harvestCheck(boolean reset, Player player) {
        AttributeInstance speed = player.getAttribute(Attributes.BLOCK_BREAK_SPEED);
        double v = calculatePercentage(savedBlockPositions.size(), SELECTION_MAX_BLOCK.get(), MINING_SPEED_NERF_MIN.get(), MINING_SPEED_NERF_MAX.get(), true);
        assert speed != null;
        if (reset) {
            speed.setBaseValue(speed.getAttribute().value().getDefaultValue());
        } else {
            speed.setBaseValue(v);
        }
    }

    //@SubscribeEvent
    public static void onBlockDrop(BlockDropsEvent event) {
        if (event.getBreaker() instanceof Player pPlayer && verifyPlayer(BlockPositionTracker.player).is(pPlayer)) {
            Vec3 pos = player.position();
            if (BLOCKS_AT_PLAYER.get()) {
                pPlayer.giveExperiencePoints(event.getDroppedExperience());
                event.setDroppedExperience(0);
            }
            event.getDrops().forEach(itemEntity -> {
                itemEntity.setPickUpDelay((player.isCreative() ? 0 : ITEM_PICKUP_DELAY.get()));
                if (BLOCKS_AT_PLAYER.get()) {
                    itemEntity.teleportTo(pos.x, pos.y, pos.z);
                }
            });
        }
    }

    //@SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (verifyPlayer(BlockPositionTracker.player).is(event.getPlayer())) {
            //if (isBreaking && WAIT_TILL_BROKEN.get()) {
            //    event.setCanceled(true);
            //} else if (DELAY_BETWEEN_BREAK.get() > 0 && (!isBreaking || !WAIT_TILL_BROKEN.get()) && flag()) {
            //    blocksToBreak.addAll(savedBlockPositions);
            //    isBreaking = true;
            //    save = true;
            //} else if (DELAY_BETWEEN_BREAK.get() == 0 && !isBreaking && flag()) {
            //    performBlockBreak(event.getPlayer());
            //}
        }
    }

    public static void performBlockBreakTick(ServerPlayer pPlayer) {
        if (verifyPlayer(BlockPositionTracker.player).is(pPlayer)) {
            if (!blocksToBreak.isEmpty()) {
                ServerLevel level = pPlayer.serverLevel();

                BlockPos pos = blocksToBreak.stream().toList().get(getPos(blocksToBreak));
                //if (blockBreak(level, pos)) {
                //    blocksToBreak.remove(pos);
                //} else reset();

                if (blocksToBreak.isEmpty())
                    reset();
            }
        }
    }

    public static int getPos(Set<BlockPos> pos) {
        int size = pos.toArray().length;
        if (size != 0) {
            int v = player.serverLevel().getRandom().nextInt(0, size);
            v = v < 0 ? v * -1 : v;
            v = v > size ? size : Math.max(v, 0);
            return v;
        }
        return 0;
    }

    public static void performBlockBreak(Player pPlayer) {
        if (verifyPlayer(BlockPositionTracker.player).is(pPlayer)) {
            isBreaking = true;

            savedBlockPositions.forEach(pos -> {//blockBreak(player.serverLevel(), pos)
            });
            reset();
        }
    }

    public static void reset() {
        CooldownTracker.resetCooldown(player.getUUID(), player.isCreative() ? 0 : i);
        if (!player.isCreative())
            if (i > 0) removingFuelItems(player, FUEL_EXHAUSTION_AMOUNT.get() * i);
        if (isBreaking) isBreaking = false;

        i = 0;
        resetTick();
        if (!savedBlockPositions.isEmpty()) savedBlockPositions.clear();
        if (!blocksToBreak.isEmpty()) blocksToBreak.clear();
    }


}
