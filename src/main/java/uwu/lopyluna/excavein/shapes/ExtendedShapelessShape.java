package uwu.lopyluna.excavein.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Set;

import static uwu.lopyluna.excavein.utils.Utils.EXTENDED_WHITELIST;

public class ExtendedShapelessShape extends Shape {
    public ExtendedShapelessShape(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean shapeFilter(Level pLevel, Player pPlayer, BlockHitResult pRayTrace, Set<BlockPos> pValidBlocks, Set<BlockPos> pCheckedBlocks, BlockPos pStartPos, BlockPos pCurrentPos, BlockState pStartState, BlockState pCurrentState, int pMaxBlocks, int pMaxRange, int pDirectionRange) {
        return pStartState.is(EXTENDED_WHITELIST) && pCurrentState.is(EXTENDED_WHITELIST);
    }

    @Override
    public Set<BlockPos> shapeBuild(Level pLevel, Player pPlayer, BlockHitResult pRayTrace, BlockPos pStartPos, BlockPos pCurrentPos, BlockState pStartState, BlockState pCurrentState, int pMaxBlocks, int pMaxRange, int pDirectionRange) {
        return getNeighborsIncludingDiagonalsExtended(pCurrentPos);
    }

}
