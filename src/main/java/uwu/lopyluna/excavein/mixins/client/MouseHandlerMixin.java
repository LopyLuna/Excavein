package uwu.lopyluna.excavein.mixins.client;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uwu.lopyluna.excavein.client.KeybindHandler;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(
            method = "onScroll",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z", ordinal = 0, shift = At.Shift.BEFORE),
            cancellable = true
    )
    private void onScroll(long windowPointer, double xOffset, double yOffset, CallbackInfo ci) {
        if(KeybindHandler.onMouseScroll()) {
            ci.cancel();
        };
    }
}
