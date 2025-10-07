package dev.lopyluna.excavein.shape_modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import dev.lopyluna.excavein.utils.ShapeAdditions;

import java.util.Set;

public abstract class ShapeModifier implements ShapeAdditions {
    ResourceLocation id;

    public ShapeModifier(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation getId() {
        return id;
    }

    public String getName() {
        return Component.translatable(id.getNamespace() + ".modifier." + id.getPath()).getString();
    }

    public abstract boolean shapeModifierFilter(Level pLevel, Player pPlayer, BlockHitResult pRayTrace,
                                                Set<BlockPos> pValidBlocks, Set<BlockPos> pCheckedBlocks, BlockPos pStartPos, BlockPos pCurrentPos, BlockState pStartState, BlockState pCurrentState,
                                                int pMaxBlocks, int pMaxRange, int pDirectionRange);
}
