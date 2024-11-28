package uwu.lopyluna.excavein.shape_modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.Set;

public class SideShapeModifier extends ShapeModifier {
    public SideShapeModifier(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean shapeModifierFilter(Level pLevel, Player pPlayer, BlockHitResult pRayTrace, Set<BlockPos> pValidBlocks, Set<BlockPos> pCheckedBlocks, BlockPos pStartPos, BlockPos pCurrentPos, BlockState pStartState, BlockState pCurrentState, int pMaxBlocks, int pMaxRange, int pDirectionRange) {
        BlockPos offset = pCurrentPos.relative(pRayTrace.getDirection());
        BlockState state = pLevel.getBlockState(offset);
        return state.isAir() || state.getMaterial().isReplaceable() || state.getCollisionShape(pLevel, offset) == Shapes.empty();
    }
}
