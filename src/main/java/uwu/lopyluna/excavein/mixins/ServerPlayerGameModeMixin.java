package uwu.lopyluna.excavein.mixins;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import uwu.lopyluna.excavein.utils.Utils;

import java.util.concurrent.atomic.AtomicReference;

import static uwu.lopyluna.excavein.config.ServerConfig.*;
import static uwu.lopyluna.excavein.tracker.ExcaveinTacker.getSelectionData;
import static uwu.lopyluna.excavein.utils.Utils.getValidTools;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @Shadow protected ServerLevel level;
    @Shadow @Final protected ServerPlayer player;
    @Unique
    int excavein$i = 0;
    @Unique
    AtomicReference<InteractionResult> excavein$result = new AtomicReference<>(InteractionResult.FAIL);
    @Shadow
    private GameType gameModeForPlayer = GameType.DEFAULT_MODE;

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    public void destroyBlock(CallbackInfoReturnable<Boolean> cir) {
        if (getSelectionData(player.getUUID()).blockBreak(gameModeForPlayer)) {
            cir.setReturnValue(true);
        }
    }


    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    public void useItemOn(ServerPlayer pPlayer, Level pLevel, ItemStack pStack, InteractionHand pHand, BlockHitResult pHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        //InteractionResult interactionResult = excavein$performInteraction(pPlayer, pLevel, pStack, pHand, pHitResult);
        //if (interactionResult.consumesAction())
        //    cir.setReturnValue(interactionResult);
    }

    @Unique
    private InteractionResult excavein$performInteraction(ServerPlayer pPlayer, Level pLevel, ItemStack pStack, InteractionHand pHand, BlockHitResult pHitResult) {
        if (pPlayer != null && pLevel != null && pStack != null && pHand != null && pHitResult != null) {
            if ((BLOCK_PLACING.get() && (pStack.getItem() instanceof BlockItem || !getValidTools(pStack))) || (ITEM_INTERACTION.get() && (!(pStack.getItem() instanceof BlockItem) || getValidTools(pStack))) || (HAND_INTERACTION.get() && pStack.isEmpty())) {
                //if ((!(pPlayer instanceof FakePlayer) && keyIsDown) && (CooldownTracker.isCooldownNotActive(pPlayer.getUUID()) && !isBreaking) && !savedBlockPositions.isEmpty()) {
                //savedBlockPositions.forEach(pos -> excavein$performInteraction(pPlayer, pLevel, pStack, pHand, pHitResult, pos)
                //);
                //excavein$reset(pPlayer);
                //CooldownTracker.resetCooldown(pPlayer.getUUID(), BLOCK_PLACING.get() && !pPlayer.isCreative() ? excavein$i : 0);
                //return excavein$result.get();
                //}
            }
        }
        excavein$result.set(InteractionResult.FAIL);
        return InteractionResult.FAIL;
    }

    @Unique
    private void excavein$reset(ServerPlayer pPlayer) {
        //CooldownTracker.resetCooldown(pPlayer.getUUID(), pPlayer.isCreative() ? 0 : excavein$i);
        if (!pPlayer.isCreative())
            if (excavein$i > 0) Utils.removingFuelItems(pPlayer, FUEL_EXHAUSTION_AMOUNT.get() * excavein$i);
        //if (isBreaking) isBreaking = false;
//
        //excavein$i = 0;
        //resetTick();
        //if (!savedBlockPositions.isEmpty()) savedBlockPositions.clear();
        //if (!blocksToBreak.isEmpty()) blocksToBreak.clear();
    }

    @Unique
    private void excavein$performInteraction(ServerPlayer pPlayer, Level pLevel, ItemStack pStack, InteractionHand pHand, BlockHitResult pHitResult, BlockPos pos) {
        boolean valid = !(REQUIRES_XP.get() && !pPlayer.isCreative() && pPlayer.totalExperience == 0) &&
                !(REQUIRES_HUNGER.get() && !pPlayer.isCreative() && pPlayer.getFoodData().getFoodLevel() == 0) &&
                !(REQUIRES_FUEL_ITEM.get() && !pPlayer.isCreative() && Utils.findInInventory(pPlayer) == 0) &&
                !(PREVENT_BREAKING_TOOL.get() && !pPlayer.isCreative() && pStack.isDamageableItem() && pStack.getMaxDamage() - pStack.getDamageValue() == 1);

        if (REQUIRES_XP.get() && !pPlayer.isCreative() && pPlayer.totalExperience == 0)
            pPlayer.displayClientMessage(Component.translatable("excavein.warning.require_xp").withStyle(ChatFormatting.RED), true);
        else if (REQUIRES_HUNGER.get() && !pPlayer.isCreative() && pPlayer.getFoodData().getFoodLevel() == 0)
            pPlayer.displayClientMessage(Component.translatable("excavein.warning.require_hunger").withStyle(ChatFormatting.RED), true);
        else if (REQUIRES_FUEL_ITEM.get() && !pPlayer.isCreative() && Utils.findInInventory(pPlayer) == 0)
            pPlayer.displayClientMessage(Component.translatable("excavein.warning.require_fuel").withStyle(ChatFormatting.RED), true);

        if (valid) {
            net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock events = net.neoforged.neoforge.common.CommonHooks.onRightClickBlock(pPlayer, pHand, pos, pHitResult.withPosition(pos));
            InteractionResult resulting = excavein$useItemOn(pPlayer, pLevel, pStack, pHand, pHitResult.withPosition(pos), pLevel.getBlockState(pos), pos, events);
            if (pos.equals(pHitResult.getBlockPos())) {
                excavein$result.set(resulting);
            }
            if (resulting.consumesAction()) {
                excavein$i++;

                //if (!pPlayer.isCreative()) {
                //    if ((savedBlockPositions.size() * FOOD_EXHAUSTION_MULTIPLIER.get()) != 0)
                //        pPlayer.causeFoodExhaustion((float) (0.005F * (savedBlockPositions.size() * FOOD_EXHAUSTION_MULTIPLIER.get())));
                //    if (XP_EXHAUSTION_AMOUNT.get() != 0)
                //        pPlayer.giveExperiencePoints(-XP_EXHAUSTION_AMOUNT.get());
                //}
            }
        }
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

