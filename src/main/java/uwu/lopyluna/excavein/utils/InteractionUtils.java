package uwu.lopyluna.excavein.utils;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import uwu.lopyluna.excavein.data.SelectionPlayerData;

import java.util.*;

import static uwu.lopyluna.excavein.config.ServerConfig.*;


public class InteractionUtils {
    public Set<BlockPos> savedBlockPositions = new HashSet<>();
    private final SelectionPlayerData player;
    private int amount = 0;
    private GameType gameModeForPlayer = GameType.DEFAULT_MODE;
    private Interact interact;
    private List<InteractionResult> results;

    public InteractionUtils(SelectionPlayerData data) {
        player = data;
    }

    public void preformInteraction() {
        if (interact == null) return;
        if (getBlockPositions() != null && !getBlockPositions().isEmpty()) {
            randomizePositions(getBlockPositions()).forEach(pos -> interactBlockPos(interact.pPlayer, interact.pLevel, interact.pStack, interact.pHand, interact.pHitResult.withPosition(pos), pos));
            player.resetCooldown(amount);
            savedBlockPositions.clear();
            amount = 0;
        }
    }


    public List<InteractionResult> interactBlocks(GameType gameModeForPlayer, Interact interact) {
        if (player.flag()) {
            this.results = new ArrayList<>();
            this.interact = interact;
            this.gameModeForPlayer = gameModeForPlayer;
            saveBlockPositions(interact.pHitResult.getBlockPos());
            preformInteraction();
            return results;
        }
        return new ArrayList<>();
    }

    public Set<BlockPos> getBlockPositions() {
        return savedBlockPositions;
    }

    public void interactBlockPos(ServerPlayer pPlayer, Level pLevel, ItemStack pStack, InteractionHand pHand, BlockHitResult pHitResult, BlockPos pPos) {
        if (!player.flagMessage() || pPos == null) return;
        InteractionResult resulting = interactBlock(pPlayer, pLevel, pStack, pHand, pHitResult, pPos);
        if (results == null) results = new ArrayList<>();
        if (resulting != null && resulting.consumesAction()) {
            results.add(resulting);
            exhaustPlayer(pPlayer);
            amount++;
        }
    }

    public void saveBlockPositions(BlockPos initialPos) {
        Set<BlockPos> positions = randomizePositions(player.getBlocks(false));
        positions.remove(initialPos);
        interactBlockPos(interact.pPlayer, interact.pLevel, interact.pStack, interact.pHand, interact.pHitResult, initialPos);
        savedBlockPositions.addAll(positions);
    }

    public Set<BlockPos> randomizePositions(Set<BlockPos> blockPosSet) {
        List<BlockPos> blockPosList = new ArrayList<>(blockPosSet);
        Collections.shuffle(blockPosList, new Random());
        return new LinkedHashSet<>(blockPosList);
    }

    public void exhaustPlayer(ServerPlayer pPlayer) {
        if (!pPlayer.isCreative()) {
            if ((savedBlockPositions.size() * FOOD_EXHAUSTION_MULTIPLIER.get()) != 0) pPlayer.causeFoodExhaustion((float) (0.005F * (savedBlockPositions.size() * FOOD_EXHAUSTION_MULTIPLIER.get())));
            if (XP_EXHAUSTION_AMOUNT.get() != 0) pPlayer.giveExperiencePoints(-XP_EXHAUSTION_AMOUNT.get());
        }
    }

    public InteractionResult interactBlock(ServerPlayer pPlayer, Level pLevel, ItemStack pStack, InteractionHand pHand, BlockHitResult pHitResult, BlockPos pos) {
        return useItemOn(pPlayer, pLevel, pStack, pHand, pHitResult.withPosition(pos));
    }

    public boolean isProperItem(Item item) {
        return (BLOCK_PLACING.get() && item instanceof BlockItem) || (ITEM_INTERACTION.get() && !(item instanceof BlockItem));
    }

    public InteractionResult useItemOn(ServerPlayer pPlayer, Level pLevel, ItemStack pStack, InteractionHand pHand, BlockHitResult pHitResult) {
        BlockPos blockpos = pHitResult.getBlockPos();
        BlockState blockstate = pLevel.getBlockState(blockpos);
        if (!blockstate.getBlock().isEnabled(pLevel.enabledFeatures())) return InteractionResult.FAIL;
        PlayerInteractEvent.RightClickBlock event = CommonHooks.onRightClickBlock(pPlayer, pHand, blockpos, pHitResult);
        if (event.isCanceled()) return event.getCancellationResult();
        if (this.gameModeForPlayer != GameType.SPECTATOR) {
            UseOnContext useoncontext = new UseOnContext(pPlayer, pHand, pHitResult);
            if ((event.getUseItem() != TriState.FALSE) && isProperItem(pStack.getItem())) {
                InteractionResult result = pStack.onItemUseFirst(useoncontext);
                if (result != InteractionResult.PASS) return result;
            }
            boolean flag = !pPlayer.getMainHandItem().isEmpty() || !pPlayer.getOffhandItem().isEmpty();
            boolean flag1 = (pPlayer.isSecondaryUseActive() && flag) && !(pPlayer.getMainHandItem().doesSneakBypassUse(pLevel, blockpos, pPlayer) && pPlayer.getOffhandItem().doesSneakBypassUse(pLevel, blockpos, pPlayer));
            ItemStack itemstack = pStack.copy();
            if (event.getUseBlock().isTrue() || (event.getUseBlock().isDefault() && !flag1)) {
                ItemInteractionResult iteminteractionresult = blockstate.useItemOn(pPlayer.getItemInHand(pHand), pLevel, pPlayer, pHand, pHitResult);
                if (iteminteractionresult.consumesAction() && isProperItem(pStack.getItem())) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(pPlayer, blockpos, itemstack);
                    return iteminteractionresult.result();
                }
                if (iteminteractionresult == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION && pHand == InteractionHand.MAIN_HAND && HAND_INTERACTION.get()) {
                    InteractionResult interactionresult = blockstate.useWithoutItem(pLevel, pPlayer, pHitResult);
                    if (interactionresult.consumesAction()) {
                        CriteriaTriggers.DEFAULT_BLOCK_USE.trigger(pPlayer, blockpos);
                        return interactionresult;
                    }
                }
            }
            if ((event.getUseItem().isTrue() || (!pStack.isEmpty() && !pPlayer.getCooldowns().isOnCooldown(pStack.getItem()))) && isProperItem(pStack.getItem())) {
                if (event.getUseItem().isFalse()) return InteractionResult.PASS;
                InteractionResult interactionresult1;
                if (pPlayer.isCreative()) {
                    int i = pStack.getCount();
                    interactionresult1 = pStack.useOn(useoncontext);
                    pStack.setCount(i);
                } else interactionresult1 = pStack.useOn(useoncontext);
                if (interactionresult1.consumesAction()) CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(pPlayer, blockpos, itemstack);
                return interactionresult1;
            }
        }
        return InteractionResult.PASS;
    }
}
