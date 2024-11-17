package uwu.lopyluna.excavein.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import uwu.lopyluna.excavein.config.ServerConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface ShapeAdditions {

    default List<TagKey<Block>> getTagsFromState(BlockState state) {
        List<TagKey<Block>> veinTags = new ArrayList<>();
        for (String tag : ServerConfig.VEIN_BLOCK_TAGS.get()) {
            TagKey<Block> blockTag = TagKey.create(BuiltInRegistries.BLOCK.key(), ResourceLocation.parse(tag));
            if (state.is(blockTag)) {
                veinTags.add(blockTag);
            }
        }
        return state != null ? veinTags : new ArrayList<>();
    }

    default boolean isBlockInTag(BlockState startState, BlockState currentState, List<TagKey<Block>> tags) {
        if (tags != null && !tags.isEmpty() && startState != null && currentState != null) {
            for (TagKey<Block> tag : tags) {
                if (startState.is(tag) && currentState.is(tag)) {
                    return true;
                }
            }
        }
        return false;
    }

    default Set<BlockPos> getNeighborsDirectional(BlockPos pos, Direction direction) {
        Set<BlockPos> offsets = new HashSet<>();
        for (Direction directional : Direction.values()) {
            if (directional.getAxis() != direction.getAxis()) {
                offsets.add(pos.relative(directional));
            }
        }
        return offsets;
    }

    default BlockPos getDiagonalPosBasedOnView(ServerPlayer player, BlockPos currentPos, Direction direction) {
        float pitch = player.getXRot();
        Direction horizontalDirection = direction.getAxis().isVertical() ? player.getDirection() : direction;

        BlockPos diagonalPos = currentPos;

        diagonalPos = diagonalPos.relative(horizontalDirection);
        if (pitch <= 0) {
            diagonalPos = diagonalPos.above();
        } else if (pitch > 0) {
            diagonalPos = diagonalPos.below();
        }

        return diagonalPos;
    }

    default Set<BlockPos> getNeighborsIncludingDiagonals(BlockPos pos) {
        Set<BlockPos> neighbors = new HashSet<>();
        for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) for (int dz = -1; dz <= 1; dz++)
            if (dx != 0 || dy != 0 || dz != 0) neighbors.add(pos.offset(dx, dy, dz));
        return neighbors;
    }

    default Set<BlockPos> getNeighborsTunnel(BlockPos start, BlockPos pos, Direction direction, int size) {
        Set<BlockPos> offsets = new HashSet<>();
        Set<Integer> offsetStart = new HashSet<>();
        int i = Mth.clamp(size, 0, 64);
        if (i != 0) for (int x = -i; x <= i; x++) for (int y = -i; y <= i; y++)
            switch (direction.getAxis()) {
                case X -> { offsets.add(pos.offset(0, y, x)); offsetStart.add(start.offset(0, y, x).get(direction.getAxis())); }
                case Y -> { offsets.add(pos.offset(x, 0, y)); offsetStart.add(start.offset(x, 0, y).get(direction.getAxis())); }
                case Z -> { offsets.add(pos.offset(x, y, 0)); offsetStart.add(start.offset(x, y, 0).get(direction.getAxis())); }
            }
        offsets.add(pos.relative(direction));
        offsets.removeIf(blockPos -> offsetStart.contains(blockPos.get(direction.getAxis())));
        return offsets;
    }
}
