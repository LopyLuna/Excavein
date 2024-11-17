package uwu.lopyluna.excavein.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import uwu.lopyluna.excavein.utils.ShapeAdditions;

import java.util.Set;

public abstract class Shape implements ShapeAdditions {
    ResourceLocation id;

    public Shape(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation getId() {
        return id;
    }

    public String getName() {
        return Component.translatable(id.getNamespace() + ".shape." + id.getPath()).getString();
    }

    public abstract boolean shapeFilter(Level pLevel, Player pPlayer, BlockHitResult pRayTrace,
                                        Set<BlockPos> pValidBlocks, Set<BlockPos> pCheckedBlocks, BlockPos pStartPos, BlockPos pCurrentPos, BlockState pStartState, BlockState pCurrentState,
                                        int pMaxBlocks, int pMaxRange);

    public abstract Set<BlockPos> shapeBuild(Level pLevel, Player pPlayer, BlockHitResult pRayTrace,
                                             BlockPos pStartPos, BlockPos pCurrentPos, BlockState pStartState, BlockState pCurrentState,
                                             int pMaxBlocks, int pMaxRange);
}
