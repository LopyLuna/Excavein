package uwu.lopyluna.excavein.shape_modifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Set;

public class VeinShapeModifier extends ShapeModifier {
    public VeinShapeModifier(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean shapeModifierFilter(Level pLevel, Player pPlayer, BlockHitResult pRayTrace, Set<BlockPos> pValidBlocks, Set<BlockPos> pCheckedBlocks, BlockPos pStartPos, BlockPos pCurrentPos, BlockState pStartState, BlockState pCurrentState, int pMaxBlocks, int pMaxRange, int pDirectionRange) {
        return isBlockInTag(pStartState, pCurrentState, getTagsFromState(pStartState));
    }
}
