package uwu.lopyluna.excavein.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import uwu.lopyluna.excavein.data.SelectionPlayerData;
import uwu.lopyluna.excavein.utils.Interact;

import java.util.List;

import static uwu.lopyluna.excavein.tracker.ExcaveinTacker.getSelectionData;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @Shadow protected ServerLevel level;
    @Shadow @Final protected ServerPlayer player;
    @Shadow private GameType gameModeForPlayer = GameType.DEFAULT_MODE;

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    public void destroyBlock(BlockPos pPos, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayer pPlayer = player;
        SelectionPlayerData data = getSelectionData(pPlayer.getUUID());
        if (data != null && data.check()) if (data.blockBreak(gameModeForPlayer, pPos)) cir.setReturnValue(true);
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    public void useItemOn(ServerPlayer pPlayer, Level pLevel, ItemStack pStack, InteractionHand pHand, BlockHitResult pHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        SelectionPlayerData data = getSelectionData(pPlayer.getUUID());
        if (data != null && data.check()) {
            List<InteractionResult> results = data.blockInteract(gameModeForPlayer, new Interact(pPlayer, pLevel, pStack, pHand, pHitResult));
            if (!results.isEmpty()) results.forEach(cir::setReturnValue);
        }
    }
}