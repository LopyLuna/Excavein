package dev.lopyluna.excavein.shape_modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.Set;

public class SurfaceShapeModifier extends ShapeModifier {
    public SurfaceShapeModifier(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean shapeModifierFilter(Level pLevel, Player pPlayer, BlockHitResult pRayTrace, Set<BlockPos> pValidBlocks, Set<BlockPos> pCheckedBlocks, BlockPos pStartPos, BlockPos pCurrentPos, BlockState pStartState, BlockState pCurrentState, int pMaxBlocks, int pMaxRange, int pDirectionRange) {
        var direction = pRayTrace.getDirection();
        var axis = direction.getAxis();
        BlockPos offset = pCurrentPos.relative(direction);
        BlockState state = pLevel.getBlockState(offset);
        return pStartPos.get(axis) == pCurrentPos.get(axis) && (state.isAir() || state.canBeReplaced() || state.getCollisionShape(pLevel, offset) == Shapes.empty());
    }
}
