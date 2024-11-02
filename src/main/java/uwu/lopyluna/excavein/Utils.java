package uwu.lopyluna.excavein;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import uwu.lopyluna.excavein.client.SelectionMode;
import uwu.lopyluna.excavein.config.ServerConfig;

import java.util.*;

import static uwu.lopyluna.excavein.Excavein.MOD_ID;
import static uwu.lopyluna.excavein.config.ServerConfig.*;

public class Utils {

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static final TagKey<Block> VEIN_MINE_WHITELIST = BlockTags.create(asResource("vein_mine_whitelist"));

    public static TagKey<Block> getBlockTagFromTool(ItemStack stack) {
        if (stack.is(Tags.Items.TOOLS)) {
            if ((stack.is(tag("tools/axes")) || stack.is(ItemTags.AXES)) || stack.getItem() instanceof AxeItem)
                return BlockTags.MINEABLE_WITH_AXE;
            if ((stack.is(tag("tools/pickaxes")) || stack.is(ItemTags.PICKAXES)) || stack.getItem() instanceof PickaxeItem)
                return BlockTags.MINEABLE_WITH_PICKAXE;
            if ((stack.is(tag("tools/shovels")) || stack.is(ItemTags.SHOVELS)) || stack.getItem() instanceof ShovelItem)
                return BlockTags.MINEABLE_WITH_SHOVEL;
            if ((stack.is(tag("tools/hoes")) || stack.is(ItemTags.HOES)) || stack.getItem() instanceof ShovelItem)
                return BlockTags.MINEABLE_WITH_HOE;
        }
        return null;
    }

    public static boolean getValidTools(ItemStack stack) {
        return stack.isDamageableItem() || stack.is(Tags.Items.TOOLS) || stack.getItem() instanceof AxeItem || stack.getItem() instanceof PickaxeItem || stack.getItem() instanceof ShovelItem || stack.getItem() instanceof HoeItem ||
                stack.is(tag("tools/axes")) || stack.is(tag("tools/pickaxes")) || stack.is(tag("tools/shovels")) || stack.is(tag("tools/hoes")) ||
                stack.is(ItemTags.AXES) || stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.SHOVELS) || stack.is(ItemTags.HOES)
                ;
    }

    public static boolean isBlockWhitelisted(BlockState currentState) {
        boolean isWhitelisted = currentState.is(VEIN_MINE_WHITELIST);

        return ServerConfig.INVERT_WHITELIST.get() != isWhitelisted;
    }

    private static List<TagKey<Block>> getTagsFromState(BlockState state) {
        List<TagKey<Block>> veinTags = new ArrayList<>();
        for (String tag : ServerConfig.VEIN_BLOCK_TAGS.get()) {
            TagKey<Block> blockTag = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation(tag));
            if (state.is(blockTag)) {
                veinTags.add(blockTag);
            }
        }
        return veinTags;
    }

    private static boolean isBlockInTag(BlockState state, TagKey<Block> tag) {
        return state.is(tag);
    }

    private static boolean isBlockInTag(BlockState startState, BlockState currentState, List<TagKey<Block>> tags) {
        for (TagKey<Block> tag : tags) {
            if (startState.is(tag) && currentState.is(tag)) {
                return true;
            }
        }
        return false;
    }
    public static boolean isValidForPlacing(Level world, ServerPlayer player, BlockPos pos) {
        if (!player.isCreative()) { // if survival
            if (BLOCK_PLACING.get()) { // if block placing enabled
                if (player.getMainHandItem().getItem() instanceof BlockItem) { // if there is block in players hand
                    return true;
                } else if (player.getOffhandItem().getItem() instanceof BlockItem) { // if there is block in players offhand
                    if (player.getMainHandItem().isEmpty()) { // if player hand is empty
                        return true;
                    }
                }
            }
        } else {
            return false;
        }
        return isNotValidForMining(player, world, pos);
    }

    public static boolean isNotValidForMining(Level world, ServerPlayer player, BlockPos pos) {
        if (!player.isCreative()) { // if survival
            if (BLOCK_PLACING.get()) { // if block placing enabled
                if (player.getMainHandItem().getItem() instanceof BlockItem) { // if there is block in players hand
                    return false;
                } else if (player.getOffhandItem().getItem() instanceof BlockItem) { // if there is block in players offhand
                    if (player.getMainHandItem().isEmpty()) { // if player hand is empty
                        return false;
                    }
                }
            }
        } else {
            return false;
        }
        return isNotValidForMining(player, world, pos);
    }

    public static boolean isNotValidForMining(ServerPlayer player, Level world, BlockPos pos) {
        if (REQUIRES_TOOLS.get())
            return !isBlockInTag(world.getBlockState(pos), getBlockTagFromTool(player.getMainHandItem())) || (REQUIRES_MINEABLE.get() && !ForgeHooks.isCorrectToolForDrops(world.getBlockState(pos), player));
        if (REQUIRES_MINEABLE.get())
            return !ForgeHooks.isCorrectToolForDrops(world.getBlockState(pos), player);
        return false;
    }

    public static boolean isCorrectSpeeds(ServerPlayer player, Level world, BlockPos pos, BlockPos startPos) {
        BlockState startState = world.getBlockState(startPos);
        BlockState state = world.getBlockState(pos);
        float startSpeed = startState.getDestroySpeed(world, startPos) * player.getInventory().getDestroySpeed(startState);
        float speed = state.getDestroySpeed(world, startPos) * player.getInventory().getDestroySpeed(state);

        return (!player.isCreative() && startSpeed >= speed) || player.isCreative();
    }

    public static boolean isNotValidBlock(Level world, ServerPlayer player, BlockPos pos) {
        return (!isBlockWhitelisted(world.getBlockState(pos)) || !world.getWorldBorder().isWithinBounds(pos) || isNotValidForMining(world, player, pos) || world.getBlockState(pos).isAir() || (world.getFluidState(pos).getType() instanceof FlowingFluid) || ((world.getBlockState(pos).getDestroySpeed(world, pos) < 0) && !player.isCreative()));
    }

    public static Set<BlockPos> selectionInspection(Level world, ServerPlayer player, BlockHitResult rayTrace, BlockPos eyePos, int maxBlocks, int maxRange, SelectionMode mode) {
        if (mode == null)
            return new HashSet<>();

        Set<BlockPos> validBlocks = new HashSet<>();
        Set<BlockPos> checkedBlocks = new HashSet<>();
        Queue<BlockPos> toCheck = new LinkedList<>();
        BlockPos startPos = rayTrace.getBlockPos();
        Direction direction = rayTrace.getDirection().getOpposite();

        toCheck.add(startPos);

        while (!toCheck.isEmpty() && validBlocks.size() < maxBlocks) {
            BlockPos currentPos = toCheck.poll();

            assert currentPos != null;
            if (eyePos.distManhattan(currentPos) > maxRange) {
                continue;
            }

            if (checkedBlocks.contains(currentPos)) {
                continue;
            }
            BlockState currentState = world.getBlockState(currentPos);
            BlockState startState = world.getBlockState(startPos);

            if (REQUIRES_TOOLS.get() && player.getMainHandItem().isEmpty() && !player.isCreative())
                continue;

            if (isNotValidBlock(world, player, currentPos) || startState.isAir() || ((startState.getDestroySpeed(world, startPos) < 0) && !player.isCreative()))
                continue;
            if (!isCorrectSpeeds(player, world, currentPos, startPos))
                continue;

            switch (mode) {
                case SELECTION:
                    if (currentState.is(startState.getBlock())) {
                        validBlocks.add(currentPos);
                        checkedBlocks.add(currentPos);
                        toCheck.addAll(getNeighborsIncludingDiagonals(currentPos));
                    }
                    break;
                case VEIN:
                    if (isBlockInTag(startState, currentState, getTagsFromState(startState))) {
                        validBlocks.add(currentPos);
                        checkedBlocks.add(currentPos);

                        for (BlockPos neighbor : getNeighborsIncludingDiagonals(currentPos)) {
                            BlockState neighborState = world.getBlockState(neighbor);
                            if (!checkedBlocks.contains(neighbor) && isBlockWhitelisted(neighborState) && isCorrectSpeeds(player, world, neighbor, startPos)) {
                                toCheck.add(neighbor);
                            } else if (!checkedBlocks.contains(neighbor) && ForgeHooks.isCorrectToolForDrops(neighborState, player) && isBlockInTag(neighborState, getBlockTagFromTool(player.getMainHandItem()))) {
                                toCheck.add(neighbor);
                            }
                        }
                    }
                    break;
                case EXCAVATE:
                    validBlocks.add(currentPos);
                    checkedBlocks.add(currentPos);
                    toCheck.addAll(getNeighborsIncludingDiagonals(currentPos));
                    break;
                case SIDE_SELECTION:
                    BlockPos offsetC = currentPos.relative(direction.getOpposite());
                    BlockState stateC = world.getBlockState(offsetC);
                    if (currentState.is(startState.getBlock())) {
                        if (stateC.isAir() || stateC.canBeReplaced() || stateC.getCollisionShape(world, offsetC) == Shapes.empty()) {
                            validBlocks.add(currentPos);
                            checkedBlocks.add(currentPos);
                            toCheck.addAll(getNeighborsIncludingDiagonals(currentPos));
                        }
                    }
                    break;
                case SIDE_VEIN:
                    BlockPos offsetB = currentPos.relative(direction.getOpposite());
                    BlockState stateB = world.getBlockState(offsetB);
                    if (stateB.isAir() || stateB.canBeReplaced() || stateB.getCollisionShape(world, offsetB) == Shapes.empty()) {
                        if (isBlockInTag(startState, currentState, getTagsFromState(startState))) {
                            validBlocks.add(currentPos);
                            checkedBlocks.add(currentPos);

                            for (BlockPos neighbor : getNeighborsIncludingDiagonals(currentPos)) {
                                BlockState neighborState = world.getBlockState(neighbor);
                                if (!checkedBlocks.contains(neighbor) && isBlockWhitelisted(neighborState) && isCorrectSpeeds(player, world, neighbor, startPos)) {
                                    toCheck.add(neighbor);
                                } else if (!checkedBlocks.contains(neighbor) && ForgeHooks.isCorrectToolForDrops(neighborState, player) && isBlockInTag(neighborState, getBlockTagFromTool(player.getMainHandItem()))) {
                                    toCheck.add(neighbor);
                                }
                            }
                        }
                    }
                    break;
                case SIDE_EXCAVATE:
                    BlockPos offsetA = currentPos.relative(direction.getOpposite());
                    BlockState stateA = world.getBlockState(offsetA);
                    if (stateA.isAir() || stateA.canBeReplaced() || stateA.getCollisionShape(world, offsetA) == Shapes.empty()) {
                        validBlocks.add(currentPos);
                        checkedBlocks.add(currentPos);
                        toCheck.addAll(getNeighborsIncludingDiagonals(currentPos));
                    }
                    break;
                case SURFACE:
                    BlockPos offsetZ = currentPos.relative(direction.getOpposite());
                    BlockState state = world.getBlockState(offsetZ);
                    if (state.isAir() || state.canBeReplaced() || state.getCollisionShape(world, offsetZ) == Shapes.empty()) {
                        validBlocks.add(currentPos);
                        checkedBlocks.add(currentPos);
                        toCheck.addAll(getNeighborsDirectional(currentPos, direction));
                    }
                    break;
                case TUNNEL:
                    BlockPos nextPos = currentPos.relative(direction);
                    validBlocks.add(startPos);
                    checkedBlocks.add(startPos);
                    while (eyePos.distManhattan(nextPos) <= maxRange && validBlocks.size() < maxBlocks) {
                        if (isNotValidBlock(world, player, nextPos))
                            break;
                        if (!isCorrectSpeeds(player, world, nextPos, startPos))
                            break;
                        validBlocks.add(nextPos);
                        checkedBlocks.add(nextPos);
                        nextPos = nextPos.relative(direction);
                    }
                    break;
                case LARGE_TUNNEL:
                    BlockPos nextPosLarge = currentPos.relative(direction);
                    validBlocks.add(startPos);
                    checkedBlocks.add(startPos);
                    for (BlockPos offset : getLargeTunnelOffsets(direction)) {
                        BlockPos largeTunnelPos = startPos.offset(offset);

                        if (isNotValidBlock(world, player, largeTunnelPos))
                            continue;
                        if (!isCorrectSpeeds(player, world, largeTunnelPos, startPos))
                            continue;

                        if (!checkedBlocks.contains(largeTunnelPos)) {
                            validBlocks.add(largeTunnelPos);
                            checkedBlocks.add(largeTunnelPos);
                        }
                    }
                    while (eyePos.distManhattan(nextPosLarge) <= maxRange && validBlocks.size() < maxBlocks) {
                        for (BlockPos offset : getLargeTunnelOffsets(direction)) {
                            BlockPos largeTunnelPos = nextPosLarge.offset(offset);

                            if (isNotValidBlock(world, player, largeTunnelPos))
                                continue;
                            if (!isCorrectSpeeds(player, world, largeTunnelPos, startPos))
                                continue;

                            if (!checkedBlocks.contains(largeTunnelPos)) {
                                validBlocks.add(largeTunnelPos);
                                checkedBlocks.add(largeTunnelPos);
                            }
                        }

                        nextPosLarge = nextPosLarge.relative(direction);
                    }
                    break;
                case DIAGONAL_TUNNEL:
                    BlockPos diagonalPos = getDiagonalPosBasedOnView(player, startPos, direction);
                    validBlocks.add(startPos);
                    checkedBlocks.add(startPos);
                    while (eyePos.distManhattan(diagonalPos) <= maxRange && validBlocks.size() < maxBlocks) {
                        if (isNotValidBlock(world, player, diagonalPos))
                            break;
                        if (!isCorrectSpeeds(player, world, diagonalPos, startPos))
                            break;
                        validBlocks.add(diagonalPos);
                        checkedBlocks.add(diagonalPos);
                        diagonalPos = getDiagonalPosBasedOnView(player, diagonalPos, direction);
                    }
                    break;
            }
        }

        return validBlocks;
    }

    private static List<BlockPos> getLargeTunnelOffsets(Direction direction) {
        List<BlockPos> offsets = new ArrayList<>();

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if (direction == Direction.NORTH || direction == Direction.SOUTH) {
                    offsets.add(new BlockPos(x, y, 0));
                } else if (direction == Direction.EAST || direction == Direction.WEST) {
                    offsets.add(new BlockPos(0, y, x));
                }
                else if (direction == Direction.UP || direction == Direction.DOWN) {
                    offsets.add(new BlockPos(x, 0, y));
                }
            }
        }
        return offsets;
    }

    private static BlockPos getDiagonalPosBasedOnView(ServerPlayer player, BlockPos currentPos, Direction direction) {
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

    private static Set<BlockPos> getNeighborsDirectional(BlockPos pos, Direction direction) {
        Set<BlockPos> offsets = new HashSet<>();
        for (Direction directional : Direction.values()) {
            if (directional.getAxis() != direction.getAxis()) {
                offsets.add(pos.relative(directional));
            }
        }
        return offsets;
    }

    private static Set<BlockPos> getNeighborsIncludingDiagonals(BlockPos pos) {
        Set<BlockPos> neighbors = new HashSet<>();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx != 0 || dy != 0 || dz != 0) {
                        neighbors.add(pos.offset(dx, dy, dz));
                    }
                }
            }
        }

        return neighbors;
    }

    public static int calculateTimeFromBlocks(int maxTime, int currentAmountBlocks) {
        double multiplier = (double) currentAmountBlocks / SELECTION_MAX_BLOCK.get();
        return (int) (maxTime * multiplier);
    }

    @SuppressWarnings("all")
    public static String ticksToTime(int value, OffsetTime off) {
        boolean bT = off == OffsetTime.TICKS;
        boolean bS = off == OffsetTime.SECONDS || bT;
        boolean bM = off == OffsetTime.MINUTES || bS;
        boolean bH = off == OffsetTime.HOURS || bM;
        boolean bD = off == OffsetTime.DAYS || bH;
        boolean bMTH = off == OffsetTime.MONTHS || bD;
        int t = value;
        int s = t / 20;
        int m = s / 60;
        int h = m / 60;
        int d = h / 24;
        int mth = d / 30;
        int y = mth / 12;
        t %= 20;
        s %= 60;
        m %= 60;
        h %= 24;
        mth %= 30;
        String ticks = bT ? conversion(t, "t", d > 0 || mth > 0 || y > 0 || s > 0 || m > 0 || h > 0, bT) : "";
        String secs = bS ? conversion(s, "s", d > 0 || mth > 0 || y > 0 || m > 0 || h > 0, off == OffsetTime.SECONDS) : "";
        String mins = bM ? conversion(m, "m", d > 0 || mth > 0 || y > 0 || h > 0, off == OffsetTime.MINUTES) : "";
        String hours = bH ? conversion(h, "h", d > 0 || mth > 0 || y > 0, off == OffsetTime.HOURS) : "";
        String days = bD ? conversion(d, "d", mth > 0 || y > 0, off == OffsetTime.DAYS) : "";
        String months = bMTH ? conversion(mth, "m", y > 0, off == OffsetTime.MONTHS) : "";
        String years = y > 0 ? y + "y" : "";
        return years + months + days + hours + mins + secs + ticks;
    }
    public static String conversion(int value, String inc, boolean above, boolean isEnding) {
        return value > 0 ? above ? value < 10 ? ":0" + value + inc : ":" + value + inc : value + inc : above ? ":00" + inc : isEnding ? "0" + inc : "";
    }
    public enum OffsetTime {
        TICKS,
        SECONDS,
        MINUTES,
        HOURS,
        DAYS,
        MONTHS
    }

    private static TagKey<Item> tag(String name) {
        return ItemTags.create(new ResourceLocation("forge", name));
    }


    public static void removingFuelItems(Player player, int amount) {
        findAndRemoveInInventory(player, amount);
    }

    //Mostly copied from https://github.com/Creators-of-Create/Create/blob/mc1.20.1/dev/src/main/java/com/simibubi/create/foundation/utility/BlockHelper.java#L89

    public static int findInInventory(Player player) {
        int amountFound = 0;

        int preferredSlot = player.getInventory().selected;
        ItemStack itemstack = player.getInventory().getItem(preferredSlot);
        if (itemstack.is(Utils.tag("vein_fuels"))) {
            amountFound = amountFound + itemstack.getCount();
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            ItemStack itemstack2 = player.getInventory().getItem(i);
            if (itemstack2.is(Utils.tag("vein_fuels"))) {
                amountFound = amountFound + itemstack2.getCount();
            }
        }
        return amountFound;
    }

    private static void findAndRemoveInInventory(Player player, int amount) {
        int amountFound = 0;

        int preferredSlot = player.getInventory().selected;
        ItemStack itemstack = player.getInventory().getItem(preferredSlot);
        int count = itemstack.getCount();
        if (itemstack.is(Utils.tag("vein_fuels")) && count > 0) {
            int taken = Math.min(count, amount - amountFound);
            player.getInventory().setItem(preferredSlot, new ItemStack(itemstack.getItem(), count - taken));
            amountFound += taken;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            if (amountFound == amount)
                break;
            ItemStack itemstack2 = player.getInventory().getItem(i);
            int count2 = itemstack2.getCount();
            if (itemstack2.is(Utils.tag("vein_fuels")) && count2 > 0) {
                int taken = Math.min(count2, amount - amountFound);
                player.getInventory().setItem(i, new ItemStack(itemstack2.getItem(), count2 - taken));
                amountFound += taken;
            }
        }
    }
}
