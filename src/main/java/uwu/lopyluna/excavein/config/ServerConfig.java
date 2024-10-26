package uwu.lopyluna.excavein.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class ServerConfig {

    public static final ForgeConfigSpec.IntValue SELECTION_COOLDOWN;
    public static final ForgeConfigSpec.IntValue SELECTION_ADD_COOLDOWN;
    public static final ForgeConfigSpec.IntValue SELECTION_ADD_RANGE;
    public static final ForgeConfigSpec.IntValue SELECTION_MAX_BLOCK;
    public static final ForgeConfigSpec.DoubleValue FOOD_EXHAUSTION_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue ITEM_PICKUP_DELAY;
    public static final ForgeConfigSpec.BooleanValue INVERT_WHITELIST;
    public static final ForgeConfigSpec.BooleanValue REQUIRES_MINEABLE;
    public static final ForgeConfigSpec.BooleanValue REQUIRES_TOOLS;
    public static final ForgeConfigSpec.BooleanValue REQUIRES_HUNGER;
    public static final ForgeConfigSpec.BooleanValue BLOCKS_AT_PLAYER;
    public static final ForgeConfigSpec.BooleanValue BLOCK_PLACING;
    public static final ForgeConfigSpec.BooleanValue HAND_INTERACTION;
    public static final ForgeConfigSpec.BooleanValue ITEM_INTERACTION;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> VEIN_BLOCK_TAGS;

    public static final ForgeConfigSpec SERVER_SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        SELECTION_COOLDOWN = builder
                .comment("Amount of ticks for block selection cooldown (default = 2s - " + (20 * 2) + ")")
                .defineInRange("SelectionCooldown", 20 * 2, 0, 20 * 60 * 60 * 24 * 7);

        SELECTION_ADD_COOLDOWN = builder
                .comment("Amount of ticks that get added to the block selection cooldown (default = 0)")
                .defineInRange("SelectionAddedCooldown", 0, 0, 20 * 60 * 60 * 24 * 7);

        SELECTION_ADD_RANGE = builder
                .comment("Add range for block selection (default = 12)")
                .defineInRange("SelectionMaxRange", 12, -8, 128);

        SELECTION_MAX_BLOCK = builder
                .comment("Maximum number of blocks that can be selected (default = 64)")
                .defineInRange("SelectionMaxBlock", 64, 1, 2048);

        FOOD_EXHAUSTION_MULTIPLIER = builder
                .comment("Causes Food Exhaustion to be Multiplied when mining blocks (default = 2)")
                .defineInRange("FoodExhaustionMultiplier", 2.0, 0.0, 1000);

        ITEM_PICKUP_DELAY = builder
                .comment("Amount of ticks for till you are able to pick up items after blocks drops from selection (default = 0)")
                .defineInRange("ItemPickupDelay", 0, 0, 30000);

        INVERT_WHITELIST = builder
                .comment("Invert the whitelist behavior (default = true)")
                .define("InvertWhitelist", true);

        REQUIRES_MINEABLE = builder
                .comment("Require Hand/Tools for said selected blocks that requires hand/tools to drop (default = true)")
                .define("RequiresMineable", true);
        
        REQUIRES_TOOLS = builder
                .comment("Require Tools for said selected blocks (default = false)")
                .define("RequiresTools", false);

        REQUIRES_HUNGER = builder
                .comment("Require Hunger for said selected blocks (default = true)")
                .define("RequiresHunger", true);

        BLOCKS_AT_PLAYER = builder
                .comment("Sends all dropped Items from Blocks to the Player (default = true)")
                .define("SendBlocksToPlayer", true);

        BLOCK_PLACING = builder
                .comment("Ability to Place Blocks by Selection (default = false)")
                .define("BlockPlacing", false);

        HAND_INTERACTION = builder
                .comment("Ability to Interact Blocks using said Hand by Selection (default = true)")
                .define("HandInteraction", true);

        ITEM_INTERACTION = builder
                .comment("Ability to Interact Blocks using said Item by Selection (default = true")
                .define("ItemInteraction", true);

        VEIN_BLOCK_TAGS = builder
                .comment("List of block tags for vein mining as in Start Pos Block Tag needs to match said block in selection with the same tag.")
                .defineList("VeinBlockTags", defaultVeinTags(), obj -> obj instanceof String);

        SERVER_SPEC = builder.build();
    }

    private static List<String> defaultVeinTags() {
        return List.of(
                "forge:ores",
                "forge:glass",
                "minecraft:planks",
                "minecraft:wool",
                "minecraft:terracotta",
                "minecraft:leaves",
                "minecraft:flowers",
                "minecraft:replaceable_plants",
                "minecraft:saplings",
                "minecraft:fences",
                "minecraft:walls",
                "minecraft:wool_carpets",
                "minecraft:anvil",
                "minecraft:shulker_boxes",
                "minecraft:crops",
                "minecraft:coral_blocks",
                "minecraft:trap_doors",
                "minecraft:pressure_plates",
                "minecraft:buttons",
                "minecraft:fence_gates",
                "minecraft:doors",
                "minecraft:stairs",
                "minecraft:slabs",
                "minecraft:sand",
                "minecraft:dirt",
                "minecraft:logs",
                "minecraft:base_stone_overworld",
                "minecraft:base_stone_nether"
        );
    }
}
