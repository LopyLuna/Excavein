package dev.lopyluna.excavein.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Set;

public class DiagonalTunnelShape extends Shape {
    public DiagonalTunnelShape(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean shapeFilter(Level pLevel, Player pPlayer, BlockHitResult pRayTrace, Set<BlockPos> pValidBlocks, Set<BlockPos> pCheckedBlocks, BlockPos pStartPos, BlockPos pCurrentPos, BlockState pStartState, BlockState pCurrentState, int pMaxBlocks, int pMaxRange, int pDirectionRange) {
        return true;
    }

    @Override
    public Set<BlockPos> shapeBuild(Level pLevel, Player pPlayer, BlockHitResult pRayTrace, BlockPos pStartPos, BlockPos pCurrentPos, BlockState pStartState, BlockState pCurrentState, int pMaxBlocks, int pMaxRange, int pDirectionRange) {
        return getDiagonalTunnel((ServerPlayer) pPlayer, pStartPos, pCurrentPos, pRayTrace);
    }

}
