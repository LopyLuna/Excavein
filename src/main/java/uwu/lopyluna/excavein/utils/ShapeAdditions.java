package uwu.lopyluna.excavein.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
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

    default Set<BlockPos> getNeighborsIncludingDiagonals(BlockPos pos) {
        Set<BlockPos> neighbors = new HashSet<>();
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++)
                for (int dz = -1; dz <= 1; dz++)
                    if (dx != 0 || dy != 0 || dz != 0) neighbors.add(pos.offset(dx, dy, dz));
        return neighbors;
    }

    default Set<BlockPos> getNeighborsIncludingDiagonalsExtended(BlockPos pos) {
        Set<BlockPos> neighbors = new HashSet<>();
        for (int dx = -2; dx <= 2; dx++)
            for (int dy = -2; dy <= 2; dy++)
                for (int dz = -2; dz <= 2; dz++)
                    if (dx != 0 || dy != 0 || dz != 0) neighbors.add(pos.offset(dx, dy, dz));
        return neighbors;
    }

    default Set<BlockPos> getDiagonalTunnel(ServerPlayer player, BlockPos startPosition, BlockPos currentPosition, BlockHitResult rayTrace) {
        Set<BlockPos> neighbors = new HashSet<>();
        Direction direction = rayTrace.getDirection();
        boolean isBottom = direction != Direction.DOWN && (direction == Direction.UP || !(rayTrace.getLocation().y - (double)startPosition.getY() > 0.5));

        Direction horizontalDirection = direction.getAxis().isVertical() ? player.getDirection() : direction.getOpposite();
        BlockPos diagonalPos = currentPosition;
        diagonalPos = diagonalPos.relative(horizontalDirection);

        if (isBottom) diagonalPos = diagonalPos.below();
        else diagonalPos = diagonalPos.above();

        if (player.level().getBlockState(diagonalPos).isAir())
            return neighbors;
        neighbors.add(diagonalPos);
        return neighbors;
    }

    default boolean makeTunnel(BlockPos startPosition, BlockPos currentPosition, Direction direction, int size, int pMaxRange) {
        int distance = switch (direction) {
            case NORTH -> startPosition.getZ() - currentPosition.getZ();
            case SOUTH -> currentPosition.getZ() - startPosition.getZ();
            case WEST -> startPosition.getX() - currentPosition.getX();
            case EAST -> currentPosition.getX() - startPosition.getX();
            case UP -> currentPosition.getY() - startPosition.getY();
            case DOWN -> startPosition.getY() - currentPosition.getY();
        };
        if (distance < 0 || distance > pMaxRange)
            return false;
        int offsetX = direction.getAxis() == Direction.Axis.X ? 0 : Math.abs(startPosition.getX() - currentPosition.getX());
        int offsetY = direction.getAxis() == Direction.Axis.Y ? 0 : Math.abs(startPosition.getY() - currentPosition.getY());
        int offsetZ = direction.getAxis() == Direction.Axis.Z ? 0 : Math.abs(startPosition.getZ() - currentPosition.getZ());

        return offsetX <= size && offsetY <= size && offsetZ <= size;
    }
}
