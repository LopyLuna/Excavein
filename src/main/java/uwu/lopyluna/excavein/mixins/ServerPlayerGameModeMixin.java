package uwu.lopyluna.excavein.mixins;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import uwu.lopyluna.excavein.tracker.CooldownTracker;

import static uwu.lopyluna.excavein.Utils.getValidTools;
import static uwu.lopyluna.excavein.config.ServerConfig.*;
import static uwu.lopyluna.excavein.tracker.BlockPositionTracker.*;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @Shadow private GameType gameModeForPlayer = GameType.DEFAULT_MODE;

    @Unique
    int excavein$i = 0;
    @Unique
    InteractionResult excavein$result = InteractionResult.FAIL;

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    public void useItemOn(ServerPlayer pPlayer, Level pLevel, ItemStack pStack, InteractionHand pHand, BlockHitResult pHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult interactionResult = excavein$performInteraction(pPlayer, pLevel, pStack, pHand, pHitResult);
        if (interactionResult.consumesAction())
            cir.setReturnValue(interactionResult);
    }

    @Unique
    private InteractionResult excavein$performInteraction(ServerPlayer pPlayer, Level pLevel, ItemStack pStack, InteractionHand pHand, BlockHitResult pHitResult) {
        if ((BLOCK_PLACING.get() && (pStack.getItem() instanceof BlockItem || !getValidTools(pStack))) || (ITEM_INTERACTION.get() && (!(pStack.getItem() instanceof BlockItem) || getValidTools(pStack))) || (HAND_INTERACTION.get() && pStack.isEmpty())) {
            if (!(pPlayer instanceof FakePlayer) && keyIsDown) {
                if (CooldownTracker.isCooldownNotActive(pPlayer)) {

                    savedBlockPositions.forEach(pos -> {
                        boolean valid = !REQUIRES_HUNGER.get() || pPlayer.isCreative() || pPlayer.getFoodData().getFoodLevel() != 0;
                        if (valid && (pos != pHitResult.getBlockPos())) {
                            net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock events = net.neoforged.neoforge.common.CommonHooks.onRightClickBlock(pPlayer, pHand, pos, pHitResult.withPosition(pos));
                            InteractionResult result = excavein$useItemOn(pPlayer, pLevel, pStack, pHand, pHitResult.withPosition(pos), pLevel.getBlockState(pos), pos, events);
                            if (pos.equals(pHitResult.getBlockPos())) {
                                excavein$result = result;
                            }
                            if (result.consumesAction())
                                excavein$i++;
                        }
                    });
                    CooldownTracker.resetCooldown(pPlayer, BLOCK_PLACING.get() && !pPlayer.isCreative() ? excavein$i : 0);
                    resetTick();
                    savedBlockPositions.clear();
                    return excavein$result;
                }
            }
        }
        excavein$result = InteractionResult.FAIL;
        return InteractionResult.FAIL;
    }


    @Unique
    public InteractionResult excavein$useItemOn(ServerPlayer pPlayer, Level pLevel, ItemStack pStack, InteractionHand pHand,
                                                BlockHitResult pHitResult, BlockState blockstate, BlockPos blockpos, PlayerInteractEvent.RightClickBlock event) {
        if (!blockstate.getBlock().isEnabled(pLevel.enabledFeatures())) {
            return InteractionResult.FAIL;
        }

        if (event.isCanceled()) return event.getCancellationResult();

        UseOnContext useoncontext = new UseOnContext(pPlayer, pHand, pHitResult);
        if (event.getUseItem() != net.neoforged.neoforge.common.util.TriState.FALSE) {
            InteractionResult result = pStack.onItemUseFirst(useoncontext);
            if (result != InteractionResult.PASS) return result;
        }
        boolean flag = !pPlayer.getMainHandItem().isEmpty() || !pPlayer.getOffhandItem().isEmpty();
        boolean flag1 = (pPlayer.isSecondaryUseActive() && flag) && !(pPlayer.getMainHandItem().doesSneakBypassUse(pLevel, blockpos, pPlayer) && pPlayer.getOffhandItem().doesSneakBypassUse(pLevel, blockpos, pPlayer));
        ItemStack itemstack = pStack.copy();
        if (event.getUseBlock().isTrue() || (event.getUseBlock().isDefault() && !flag1)) {
            ItemInteractionResult iteminteractionresult = blockstate.useItemOn(pPlayer.getItemInHand(pHand), pLevel, pPlayer, pHand, pHitResult);
            if (ITEM_INTERACTION.get()) {
                if (iteminteractionresult.consumesAction()) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(pPlayer, blockpos, itemstack);
                    return iteminteractionresult.result();
                }
            }
            if (HAND_INTERACTION.get()) {
                if (iteminteractionresult == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION && pHand == InteractionHand.MAIN_HAND) {
                    InteractionResult interactionresult = blockstate.useWithoutItem(pLevel, pPlayer, pHitResult);
                    if (interactionresult.consumesAction()) {
                        CriteriaTriggers.DEFAULT_BLOCK_USE.trigger(pPlayer, blockpos);
                        return interactionresult;
                    }
                }
            }
        }

        if (event.getUseItem().isTrue() || (!pStack.isEmpty() && !pPlayer.getCooldowns().isOnCooldown(pStack.getItem()))) {
            if (event.getUseItem().isFalse()) return InteractionResult.PASS;
            InteractionResult interactionresult1;
            if (this.isCreative()) {
                int i = pStack.getCount();
                interactionresult1 = pStack.useOn(useoncontext);
                pStack.setCount(i);
            } else {
                interactionresult1 = pStack.useOn(useoncontext);
            }

            if (interactionresult1.consumesAction()) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(pPlayer, blockpos, itemstack);
            }

            return interactionresult1;
        } else {
            return InteractionResult.PASS;
        }

    }

    @Shadow
    public boolean isCreative() {
        return this.gameModeForPlayer.isCreative();
    }
}

