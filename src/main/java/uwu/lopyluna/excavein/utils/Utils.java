package uwu.lopyluna.excavein.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.FakePlayer;
import uwu.lopyluna.excavein.data.SelectionPlayerData;
import uwu.lopyluna.excavein.shape_modifiers.ShapeModifier;
import uwu.lopyluna.excavein.shapes.Shape;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import static uwu.lopyluna.excavein.Excavein.MOD_ID;
import static uwu.lopyluna.excavein.config.ServerConfig.*;

public class    Utils {
    public static final TagKey<Block> VEIN_MINE_WHITELIST = BlockTags.create(asResource("whitelist"));

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static TagKey<Block> getBlockTagFromTool(ItemStack stack) {
        if (stack != null && stack.is(Tags.Items.TOOLS)) {
            if ((stack.is(universalTag("tools/axes")) || stack.is(Tags.Items.TOOLS_AXES)) || stack.getItem() instanceof AxeItem)
                return BlockTags.MINEABLE_WITH_AXE;
            if ((stack.is(universalTag("tools/pickaxes")) || stack.is(Tags.Items.TOOLS_PICKAXES)) || stack.getItem() instanceof PickaxeItem)
                return BlockTags.MINEABLE_WITH_PICKAXE;
            if ((stack.is(universalTag("tools/shovels")) || stack.is(Tags.Items.TOOLS_SHOVELS)) || stack.getItem() instanceof ShovelItem)
                return BlockTags.MINEABLE_WITH_SHOVEL;
            if ((stack.is(universalTag("tools/hoes")) || stack.is(Tags.Items.TOOLS_HOES)) || stack.getItem() instanceof ShovelItem)
                return BlockTags.MINEABLE_WITH_HOE;
        }
        return null;
    }

    //public static boolean getValidTools(ItemStack stack) {*
    //    return stack != null && (stack.isDamageableItem() || stack.is(Tags.Items.TOOLS) || stack.getItem() instanceof AxeItem || stack.getItem() instanceof PickaxeItem || stack.getItem() instanceof ShovelItem || stack.getItem() instanceof HoeItem ||
    //            stack.is(universalTag("tools/axes")) || stack.is(universalTag("tools/pickaxes")) || stack.is(universalTag("tools/shovels")) || stack.is(universalTag("tools/hoes")) ||
    //            stack.is(ItemTags.AXES) || stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.SHOVELS) || stack.is(ItemTags.HOES))
    //            ;
    //}

    public static boolean isBlockWhitelisted(BlockState currentState) {
        boolean isWhitelisted = currentState.is(VEIN_MINE_WHITELIST);
        return INVERT_WHITELIST.get() != isWhitelisted;
    }

    private static boolean isBlockInTag(BlockState state, TagKey<Block> tag) {
        return state != null && tag != null && state.is(tag);
    }


    public static boolean isNotValidForSelection(ServerPlayer player, BlockState state, ItemStack mainStack) {
        if (REQUIRES_TOOLS.get())
            return !isBlockInTag(state, getBlockTagFromTool(mainStack)) || (REQUIRES_MINEABLE.get() && !player.hasCorrectToolForDrops(state));
        if (REQUIRES_MINEABLE.get())
            return !player.hasCorrectToolForDrops(state);
        return false;
    }

    public static boolean isCorrectSpeeds(ServerPlayer player, Level pLevel, BlockPos pos, BlockPos startPos, BlockState startState, BlockState state) {
        var inv = player.getInventory();
        float startSpeed = startState.getDestroySpeed(pLevel, startPos) * inv.getDestroySpeed(startState);
        float speed = state.getDestroySpeed(pLevel, pos) * inv.getDestroySpeed(state);
        return startSpeed >= speed;
    }

    public static boolean isNotValidBlock(Level pLevel, BlockPos pos, BlockState state) {
        return (!pLevel.getWorldBorder().isWithinBounds(pos) || state.isAir() || (pLevel.getFluidState(pos).getType() instanceof FlowingFluid));
    }

    public static boolean isNotValidBlock(Level pLevel, ServerPlayer player, BlockPos pos, BlockState state, boolean isCreative) {
        var main = player.getMainHandItem();
        return (!isBlockWhitelisted(state) ||
                !pLevel.getWorldBorder().isWithinBounds(pos) ||
                (isNotValidForSelection(player, state, main) && !isCreative) ||
                state.isAir() ||
                (pLevel.getFluidState(pos).getType() instanceof FlowingFluid) ||
                ((state.getDestroySpeed(pLevel, pos) < 0) && !isCreative));
    }

    public static boolean check(ServerPlayer player, ServerLevel pLevel, BlockPos startPos, BlockPos currentPos, BlockState startState, BlockState currentState, BlockPos eyePos, int maxRange, boolean creative) {
        return (eyePos.distManhattan(currentPos) > maxRange) || startState.isAir() || ((startState.getDestroySpeed(pLevel, startPos) < 0) && !creative) ||
                isNotValidBlock(pLevel, player, currentPos, currentState, creative) ||
                (REQUIRES_TOOLS.get() && player.getMainHandItem().isEmpty() ||
                !isCorrectSpeeds(player, pLevel, currentPos, startPos, startState, currentState) && !creative);
    }

    public static boolean check(ServerLevel pLevel, BlockPos currentPos, BlockState startState, BlockState currentState, BlockPos eyePos, int maxRange) {
        return (eyePos.distManhattan(currentPos) > maxRange) || startState.isAir() || isNotValidBlock(pLevel, currentPos, currentState);
    }

    public static boolean isNotFakePlayer(Player player) {
        return player != null && !(player instanceof FakePlayer);
    }

    public static <T extends Shape, I extends ShapeModifier> Set<BlockPos> constructSelection(boolean isBreaking, SelectionPlayerData data, BlockHitResult rayTrace, BlockPos eyePos, int maxBlocks, int maxRange, T shape, I modifier) {
        if (data == null || shape == null || modifier == null || rayTrace == null || eyePos == null) return new HashSet<>();
        ServerPlayer player = data.getPlayer();
        ServerLevel pLevel = data.getLevel();
        Set<BlockPos> validBlocks = new HashSet<>();
        Set<BlockPos> checkedBlocks = new HashSet<>();
        Queue<BlockPos> toCheck = new LinkedList<>();
        BlockPos startPos = rayTrace.getBlockPos();
        toCheck.add(startPos);
        boolean creative = player.isCreative();
        BlockState startState = pLevel.getBlockState(startPos);
        int straightRange = 0;

        for (int range = 0; range < maxRange; range++) {
            BlockPos currentPos = startPos.relative(rayTrace.getDirection().getOpposite(), range);
            BlockState currentState = pLevel.getBlockState(currentPos);
            boolean quickCheck = isBreaking ? check(player, pLevel, startPos, currentPos, startState, currentState, eyePos, maxRange, creative) : check(pLevel, currentPos, startState, currentState, eyePos, maxRange);
            if (quickCheck)
                break;
            straightRange++;
        }
        while (!toCheck.isEmpty() && validBlocks.size() < maxBlocks) {
            BlockPos currentPos = toCheck.poll();
            if (currentPos == null)
                continue;
            if (checkedBlocks.contains(currentPos)) {
                toCheck.remove(currentPos);
                continue;
            }
            BlockState currentState = pLevel.getBlockState(currentPos);
            boolean quickCheck = isBreaking ? check(player, pLevel, startPos, currentPos, startState, currentState, eyePos, maxRange, creative) : check(pLevel, currentPos, startState, currentState, eyePos, maxRange);
            if (quickCheck) {
                checkedBlocks.add(currentPos);
                toCheck.remove(currentPos);
                continue;
            }
            if (shape.shapeFilter(pLevel, player, rayTrace, validBlocks, checkedBlocks, startPos, currentPos, startState, currentState, maxBlocks, maxRange, straightRange))
                if (modifier.shapeModifierFilter(pLevel, player, rayTrace, validBlocks, checkedBlocks, startPos, currentPos, startState, currentState, maxBlocks, maxRange, straightRange)) {
                    validBlocks.add(currentPos);
                    checkedBlocks.add(currentPos);
                    Set<BlockPos> building = new HashSet<>(Set.of());
                    building.addAll(shape.shapeBuild(pLevel, player, rayTrace, startPos, currentPos, startState, currentState, maxBlocks, maxRange, straightRange));
                    building.removeIf(checkedBlocks::contains);
                    building.removeIf(toCheck::contains);
                    toCheck.addAll(building);
                }
        }
        return validBlocks;
    }

    public static int calculateValueFromAmount(int maxTime, int currentAmountBlocks) {
        double multiplier = (double) currentAmountBlocks / SELECTION_MAX_BLOCK.get();
        return (int) (maxTime * multiplier);
    }

    //public static double calculatePercentage(double currentValue, double maxValue, double minOutputValue, double maxOutputValue, boolean invert) {*
    //    double ratio = Math.max(0, Math.min(currentValue / maxValue, 1));
    //    return Math.round((invert ? maxOutputValue - ratio * (maxOutputValue - minOutputValue) : minOutputValue + ratio * (maxOutputValue - minOutputValue)) * 1000.0) / 1000.0;
    //}

    public static TagKey<Item> universalTag(String name) {
        return ItemTags.create(new ResourceLocation("c", name));
    }

    public static TagKey<Item> tag(String name) {
        return ItemTags.create(new ResourceLocation(MOD_ID, name));
    }

    public static void removingFuelItems(Player player, int amount) {
        findAndRemoveInInventory(player, amount);
    }

    public static int findInInventory(Player player) {
        int amountFound = 0;

        int preferredSlot = player.getInventory().selected;
        ItemStack itemstack = player.getInventory().getItem(preferredSlot);
        if (itemstack.is(Utils.tag("fuels"))) {
            amountFound = amountFound + itemstack.getCount();
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            ItemStack itemstack2 = player.getInventory().getItem(i);
            if (itemstack2.is(Utils.tag("fuels"))) {
                amountFound = amountFound + itemstack2.getCount();
            }
        }
        return amountFound;
    }

    //Mostly copied from https://github.com/Creators-of-Create/Create/blob/mc1.20.1/dev/src/main/java/com/simibubi/create/foundation/utility/BlockHelper.java#L89

    private static void findAndRemoveInInventory(Player player, int amount) {
        int amountFound = 0;

        int preferredSlot = player.getInventory().selected;
        ItemStack itemstack = player.getInventory().getItem(preferredSlot);
        int count = itemstack.getCount();
        if (itemstack.is(Utils.tag("fuels")) && count > 0) {
            int taken = Math.min(count, amount - amountFound);
            player.getInventory().setItem(preferredSlot, new ItemStack(itemstack.getItem(), count - taken));
            amountFound += taken;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            if (amountFound == amount)
                break;
            ItemStack itemstack2 = player.getInventory().getItem(i);
            int count2 = itemstack2.getCount();
            if (itemstack2.is(Utils.tag("fuels")) && count2 > 0) {
                int taken = Math.min(count2, amount - amountFound);
                player.getInventory().setItem(i, new ItemStack(itemstack2.getItem(), count2 - taken));
                amountFound += taken;
            }
        }
    }

    public enum OffsetTime {
        TICKS,
        SECONDS,
        MINUTES,
        HOURS,
        DAYS,
        MONTHS
    }
}
