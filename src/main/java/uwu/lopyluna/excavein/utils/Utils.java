package uwu.lopyluna.excavein.utils;

import net.minecraft.core.BlockPos;
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
import net.neoforged.neoforge.common.Tags;
import uwu.lopyluna.excavein.data.SelectionPlayerData;
import uwu.lopyluna.excavein.shape_modifiers.ShapeModifier;
import uwu.lopyluna.excavein.shapes.Shape;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import static uwu.lopyluna.excavein.Excavein.MOD_ID;
import static uwu.lopyluna.excavein.config.ServerConfig.*;

public class Utils {

    public static final TagKey<Block> VEIN_MINE_WHITELIST = BlockTags.create(asResource("vein_whitelist"));

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static TagKey<Block> getBlockTagFromTool(ItemStack stack) {
        if (stack != null && stack.is(Tags.Items.TOOLS)) {
            if ((stack.is(universalTag("tools/axes")) || stack.is(ItemTags.AXES)) || stack.getItem() instanceof AxeItem)
                return BlockTags.MINEABLE_WITH_AXE;
            if ((stack.is(universalTag("tools/pickaxes")) || stack.is(ItemTags.PICKAXES)) || stack.getItem() instanceof PickaxeItem)
                return BlockTags.MINEABLE_WITH_PICKAXE;
            if ((stack.is(universalTag("tools/shovels")) || stack.is(ItemTags.SHOVELS)) || stack.getItem() instanceof ShovelItem)
                return BlockTags.MINEABLE_WITH_SHOVEL;
            if ((stack.is(universalTag("tools/hoes")) || stack.is(ItemTags.HOES)) || stack.getItem() instanceof ShovelItem)
                return BlockTags.MINEABLE_WITH_HOE;
        }
        return null;
    }

    public static boolean getValidTools(ItemStack stack) {
        return stack != null && (stack.isDamageableItem() || stack.is(Tags.Items.TOOLS) || stack.getItem() instanceof AxeItem || stack.getItem() instanceof PickaxeItem || stack.getItem() instanceof ShovelItem || stack.getItem() instanceof HoeItem ||
                stack.is(universalTag("tools/axes")) || stack.is(universalTag("tools/pickaxes")) || stack.is(universalTag("tools/shovels")) || stack.is(universalTag("tools/hoes")) ||
                stack.is(ItemTags.AXES) || stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.SHOVELS) || stack.is(ItemTags.HOES))
                ;
    }

    public static boolean isBlockWhitelisted(BlockState currentState) {
        boolean isWhitelisted = currentState.is(VEIN_MINE_WHITELIST);
        return INVERT_WHITELIST.get() != isWhitelisted;
    }

    private static boolean isBlockInTag(BlockState state, TagKey<Block> tag) {
        return state != null && tag != null && state.is(tag);
    }


    public static boolean isNotValidForSelection(ServerPlayer player, Level pLevel, BlockPos pos, BlockState state, ItemStack mainStack) {
        if (REQUIRES_TOOLS.get())
            return !isBlockInTag(state, getBlockTagFromTool(mainStack)) || (REQUIRES_MINEABLE.get() && !player.hasCorrectToolForDrops(state, pLevel, pos));
        if (REQUIRES_MINEABLE.get())
            return !player.hasCorrectToolForDrops(state, pLevel, pos);
        return false;
    }

    public static boolean isCorrectSpeeds(ServerPlayer player, Level pLevel, BlockPos pos, BlockPos startPos, BlockState startState, BlockState state) {
        var inv = player.getInventory();
        float startSpeed = startState.getDestroySpeed(pLevel, startPos) * inv.getDestroySpeed(startState);
        float speed = state.getDestroySpeed(pLevel, startPos) * inv.getDestroySpeed(state);
        return startSpeed >= speed;
    }

    public static boolean isNotValidBlock(Level pLevel, ServerPlayer player, BlockPos pos, BlockState state) {
        var main = player.getMainHandItem();
        return (!isBlockWhitelisted(state) ||
                !pLevel.getWorldBorder().isWithinBounds(pos) ||
                isNotValidForSelection(player, pLevel, pos, state, main) ||
                state.isAir() ||
                (pLevel.getFluidState(pos).getType() instanceof FlowingFluid) ||
                ((state.getDestroySpeed(pLevel, pos) < 0) && !player.isCreative()));
    }

    public static <T extends Shape, I extends ShapeModifier> Set<BlockPos> constructSelection(SelectionPlayerData data, BlockHitResult rayTrace, BlockPos eyePos, int maxBlocks, int maxRange, T shape, I modifier) {
        if (data == null || shape == null || modifier == null || rayTrace == null || eyePos == null)
            return new HashSet<>();
        Player pPlayer = data.getPlayer();
        Level pLevel = data.getLevel();
        if (!(pPlayer instanceof ServerPlayer player))
            return new HashSet<>();

        Set<BlockPos> validBlocks = new HashSet<>();
        Set<BlockPos> checkedBlocks = new HashSet<>();
        Queue<BlockPos> toCheck = new LinkedList<>();
        BlockPos startPos = rayTrace.getBlockPos();

        toCheck.add(startPos);

        while (!toCheck.isEmpty() && validBlocks.size() < maxBlocks) {
            BlockPos currentPos = toCheck.poll();

            if (currentPos == null)
                continue;

            if (checkedBlocks.contains(currentPos)) {
                toCheck.remove(currentPos);
                continue;
            }

            boolean creative = player.isCreative();

            BlockState currentState = pLevel.getBlockState(currentPos);
            BlockState startState = pLevel.getBlockState(startPos);
            boolean quickCheck = (eyePos.distManhattan(currentPos) > maxRange) ||
                    (REQUIRES_TOOLS.get() && player.getMainHandItem().isEmpty() && !creative) ||
                    (isNotValidBlock(pLevel, player, currentPos, currentState) || startState.isAir() || ((startState.getDestroySpeed(pLevel, startPos) < 0) && !creative)) ||
                    (!isCorrectSpeeds(player, pLevel, currentPos, startPos, startState, currentState) && !creative);

            if (quickCheck) {
                checkedBlocks.add(currentPos);
                toCheck.remove(currentPos);
                continue;
            }

            if (shape.shapeFilter(pLevel, pPlayer, rayTrace, validBlocks, checkedBlocks, startPos, currentPos, startState, currentState, maxBlocks, maxRange))
                if (modifier.shapeModifierFilter(pLevel, pPlayer, rayTrace, validBlocks, checkedBlocks, startPos, currentPos, startState, currentState, maxBlocks, maxRange)) {
                    validBlocks.add(currentPos);
                    checkedBlocks.add(currentPos);
                    Set<BlockPos> building = new HashSet<>(Set.of());
                    building.addAll(shape.shapeBuild(pLevel, pPlayer, rayTrace, startPos, currentPos, startState, currentState, maxBlocks, maxRange));
                    building.removeIf(checkedBlocks::contains);
                    building.removeIf(toCheck::contains);
                    toCheck.addAll(building);
                }
        }

        return validBlocks;
    }

    public static int calculateTimeFromBlocks(int maxTime, int currentAmountBlocks) {
        double multiplier = (double) currentAmountBlocks / SELECTION_MAX_BLOCK.get();
        return (int) (maxTime * multiplier);
    }

    public static double calculatePercentage(double currentValue, double maxValue, double minOutputValue, double maxOutputValue, boolean invert) {
        double ratio = Math.max(0, Math.min(currentValue / maxValue, 1));
        return Math.round((invert ? maxOutputValue - ratio * (maxOutputValue - minOutputValue) : minOutputValue + ratio * (maxOutputValue - minOutputValue)) * 1000.0) / 1000.0;
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

    public static TagKey<Item> universalTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", name));
    }

    public static TagKey<Item> tag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath(MOD_ID, name));
    }

    public static void removingFuelItems(Player player, int amount) {
        findAndRemoveInInventory(player, amount);
    }

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

    //Mostly copied from https://github.com/Creators-of-Create/Create/blob/mc1.20.1/dev/src/main/java/com/simibubi/create/foundation/utility/BlockHelper.java#L89

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

    public enum OffsetTime {
        TICKS,
        SECONDS,
        MINUTES,
        HOURS,
        DAYS,
        MONTHS
    }
}
