package uwu.lopyluna.excavein.mixins;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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

    @Shadow
    private GameType gameModeForPlayer = GameType.DEFAULT_MODE;

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    public void useItemOn(ServerPlayer pPlayer, Level pLevel, ItemStack pStack, InteractionHand pHand, BlockHitResult pHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if ((BLOCK_PLACING.get() && (pStack.getItem() instanceof BlockItem || !getValidTools(pStack))) || (ITEM_INTERACTION.get() && (!(pStack.getItem() instanceof BlockItem) || getValidTools(pStack))) || (HAND_INTERACTION.get() && pStack.isEmpty())) {
            if (!(pPlayer instanceof FakePlayer) && keyIsDown) {
                if (CooldownTracker.isCooldownNotActive(pPlayer)) {

                    for (BlockPos pos : savedBlockPositions) {
                        if (REQUIRES_HUNGER.get() && !pPlayer.isCreative() && pPlayer.getFoodData().getFoodLevel() == 0)
                            continue;
                        net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock events = net.minecraftforge.common.ForgeHooks.onRightClickBlock(pPlayer, pHand, pos, pHitResult.withPosition(pos));
                        cir.setReturnValue(sweepSnap$useItemOn(pPlayer, pLevel, pStack, pHand, pHitResult.withPosition(pos), pLevel.getBlockState(pos), pos, events));
                    }
                    CooldownTracker.resetCooldown(pPlayer, BLOCK_PLACING.get() && !pPlayer.isCreative() ? savedBlockPositions.size() : 0);
                    resetTick();
                    savedBlockPositions.clear();
                }
            }
        }
    }

    @Unique
    public InteractionResult sweepSnap$useItemOn(ServerPlayer pPlayer, Level pLevel, ItemStack pStack, InteractionHand pHand,
                                                 BlockHitResult pHitResult, BlockState blockstate, BlockPos blockpos, PlayerInteractEvent.RightClickBlock event) {
        if (event.isCanceled()) return event.getCancellationResult();
        UseOnContext useoncontext = new UseOnContext(pPlayer, pHand, pHitResult);
        if ((event.getUseItem() != net.minecraftforge.eventbus.api.Event.Result.DENY)) {
            InteractionResult result = pStack.onItemUseFirst(useoncontext);
            if (result != InteractionResult.PASS) return result;
        }
        boolean flag = !pPlayer.getMainHandItem().isEmpty() || !pPlayer.getOffhandItem().isEmpty();
        boolean flag1 = (pPlayer.isSecondaryUseActive() && flag) && !(pPlayer.getMainHandItem().doesSneakBypassUse(pLevel, blockpos, pPlayer) && pPlayer.getOffhandItem().doesSneakBypassUse(pLevel, blockpos, pPlayer));
        ItemStack itemstack = pStack.copy();
        if ((event.getUseBlock() == net.minecraftforge.eventbus.api.Event.Result.ALLOW || (event.getUseBlock() != net.minecraftforge.eventbus.api.Event.Result.DENY && !flag1))) {
            if (HAND_INTERACTION.get()) {
                InteractionResult interactionresult = blockstate.use(pLevel, pPlayer, pHand, pHitResult);
                if (interactionresult.consumesAction()) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(pPlayer, blockpos, itemstack);
                    return interactionresult;
                }
            }
        }

        if ((event.getUseItem() == net.minecraftforge.eventbus.api.Event.Result.ALLOW || (!pStack.isEmpty() && !pPlayer.getCooldowns().isOnCooldown(pStack.getItem()) && !(pStack.isDamageableItem() && (pStack.getMaxDamage() - pStack.getDamageValue()) == 1)))) {
            if (event.getUseItem() == net.minecraftforge.eventbus.api.Event.Result.DENY)
                return InteractionResult.PASS;
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

