package uwu.lopyluna.excavein.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uwu.lopyluna.excavein.tracker.BlockPositionTracker;

import java.util.ArrayList;
import java.util.List;

import static uwu.lopyluna.excavein.CapturedDrops.capturedDrops;

@Mixin(Block.class)
public class BlockMixin {

    @Inject(
            method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/Block;getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"
            )
    )
    private static void onDropResources(BlockState state, Level level, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack tool, CallbackInfo ci) {
        beginCapturingDrops();
    }

    @WrapOperation(
            method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;spawnAfterBreak(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;Z)V"
            )
    )
    private static void onDropResourcesWrap(BlockState instance, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean b, Operation<Void> original, @Local(argsOnly = true) Entity entity) {
        List<ItemEntity> captured = stopCapturingDrops();
        BlockPositionTracker.onBlockDrop(entity, captured);
        original.call(instance, serverLevel, blockPos, itemStack, b);
    }

    @WrapOperation(
            method = "popResource(Lnet/minecraft/world/level/Level;Ljava/util/function/Supplier;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"
            )
    )
    private static boolean onPopResourceWrap(Level instance, Entity entity, Operation<Boolean> original) {
        if (capturedDrops != null) capturedDrops.add((ItemEntity) entity);
        return original.call(instance, entity);
    }

    @ModifyArg(
            method = "tryDropExperience",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/Block;popExperience(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;I)V"
            ),
            index = 2
    )
    private int onPopExperienceWrap(int amount, @Local(argsOnly = true) ItemStack heldItem, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) BlockPos pos) {
        return BlockPositionTracker.onExperienceDrop(level, pos, amount, heldItem);
    }

    @Unique
    private static void beginCapturingDrops() {
        capturedDrops = new ArrayList<>();
    }

    @Unique
    private static List<ItemEntity> stopCapturingDrops() {
        List<ItemEntity> drops = capturedDrops;
        capturedDrops = null;
        return drops;
    }
}
