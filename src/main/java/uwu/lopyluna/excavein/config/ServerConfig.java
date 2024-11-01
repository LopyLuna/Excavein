package uwu.lopyluna.excavein.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

@SuppressWarnings("deprecation")
public class ServerConfig {

    public static final ModConfigSpec.IntValue SELECTION_COOLDOWN;
    public static final ModConfigSpec.IntValue SELECTION_ADD_COOLDOWN;
    public static final ModConfigSpec.IntValue SELECTION_ADD_RANGE;
    public static final ModConfigSpec.IntValue SELECTION_MAX_BLOCK;
    public static final ModConfigSpec.BooleanValue REQUIRES_FUEL_ITEM; //NEW
    public static final ModConfigSpec.IntValue FUEL_EXHAUSTION_AMOUNT; //NEW
    public static final ModConfigSpec.BooleanValue REQUIRES_HUNGER;
    public static final ModConfigSpec.DoubleValue FOOD_EXHAUSTION_MULTIPLIER;
    public static final ModConfigSpec.BooleanValue REQUIRES_XP; //NEW
    public static final ModConfigSpec.IntValue XP_EXHAUSTION_AMOUNT; //NEW
    public static final ModConfigSpec.IntValue ITEM_PICKUP_DELAY;
    public static final ModConfigSpec.BooleanValue XRAY_OUTLINE_SELECTION; //NEW
    public static final ModConfigSpec.BooleanValue NO_DURABILITY_LOSS; //NEW
    public static final ModConfigSpec.DoubleValue MINING_SPEED_NERF_MIN; //NEW
    public static final ModConfigSpec.DoubleValue MINING_SPEED_NERF_MAX; //NEW
    public static final ModConfigSpec.IntValue DELAY_BETWEEN_BREAK; //NEW
    public static final ModConfigSpec.IntValue BLOCK_PER_BREAK; //NEW
    public static final ModConfigSpec.BooleanValue WAIT_TILL_BROKEN; //NEW
    public static final ModConfigSpec.BooleanValue INVERT_WHITELIST;
    public static final ModConfigSpec.BooleanValue REQUIRES_MINEABLE;
    public static final ModConfigSpec.BooleanValue REQUIRES_TOOLS;
    public static final ModConfigSpec.BooleanValue BLOCKS_AT_PLAYER;
    public static final ModConfigSpec.BooleanValue BLOCK_PLACING;
    public static final ModConfigSpec.BooleanValue HAND_INTERACTION;
    public static final ModConfigSpec.BooleanValue ITEM_INTERACTION;
    public static ModConfigSpec.ConfigValue<List<? extends String>> VEIN_BLOCK_TAGS;

    public static final ModConfigSpec SERVER_SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

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

        REQUIRES_HUNGER = builder
                .comment("Require Hunger for said selected blocks (default = true)")
                .define("RequiresHunger", true);

        FOOD_EXHAUSTION_MULTIPLIER = builder
                .comment("Causes Food Loss to be Multiplied when mining amount of  blocks (default = 2.0)")
                .defineInRange("FoodExhaustionMultiplier", 2.0, 0.0, 1000.0);

        REQUIRES_XP = builder
                .comment("Require XP for said selected blocks (default = false)")
                .define("RequiresXP", false);

        XP_EXHAUSTION_AMOUNT = builder
                .comment("Amount of XP ''Points'' to be taken when mining for said block in selection (default = 0)")
                .defineInRange("XPExhaustionAmount", 0, 0, Integer.MAX_VALUE);

        REQUIRES_FUEL_ITEM = builder
                .comment("Require Fuel for said selected blocks (default = false)")
                .define("RequiresFuelItem", false);

        FUEL_EXHAUSTION_AMOUNT = builder
                .comment("Takes said Vein miner Item Fuel Amount for every Interact/Destroyable Blocks (default = 0)")
                .defineInRange("FuelExhaustionAmount", 0, 0, Integer.MAX_VALUE);

        ITEM_PICKUP_DELAY = builder
                .comment("Amount of ticks for till you are able to pick up items after blocks drops from selection (default = 0)")
                .defineInRange("ItemPickupDelay", 0, 0, 30000);

        DELAY_BETWEEN_BREAK = builder
                .comment("Amount of ticks for till next block gets broken (default = 5)")
                .defineInRange("DelayBetweenBreak", 5, 0, 100);

        BLOCK_PER_BREAK = builder
                .comment("Amount of blocks between broken blocks (default = 4)")
                .defineInRange("BlockPerBreak", 4, 1, 256);

        WAIT_TILL_BROKEN = builder
                .comment("Waits for all blocks to be broken before doing another selection (default = true)")
                .define("WaitTillBroken", true);

        INVERT_WHITELIST = builder
                .comment("Invert the whitelist behavior (default = true)")
                .define("InvertWhitelist", true);

        REQUIRES_MINEABLE = builder
                .comment("Require Hand/Tools for said selected blocks that requires hand/tools to drop //Highly Recommended (default = true)")
                .define("RequiresMineable", true);
        
        REQUIRES_TOOLS = builder
                .comment("Require Tools for said selected blocks (default = false)")
                .define("RequiresTools", false);

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
                .comment("Ability to Interact Blocks using said Item by Selection (default = true)")
                .define("ItemInteraction", true);

        XRAY_OUTLINE_SELECTION = builder
                .comment("Ability to Allow Players to see there selection through Blocks (default = true)")
                .define("XrayOutlineSelection", true);

        NO_DURABILITY_LOSS = builder
                .comment("Ability to Interact Blocks using said Item by Selection (default = false)")
                .define("NoDurabilityLoss", false);

        MINING_SPEED_NERF_MIN = builder
                .comment("Causes to multiply the Mining Speed for every block it accounted for 'Min Speed Multiplier (Set this Higher than Max will Invert)' (default = 0.1)")
                .defineInRange("MiningSpeedNerfMin", 0.1, 0.0, 1);

        MINING_SPEED_NERF_MAX = builder
                .comment("Causes to multiply the Mining Speed for every block it accounted for 'Max Speed Multiplier (Set this to 0 to disable the speed nerf)' (default = 0.1)")
                .defineInRange("MiningSpeedNerfMax", 0.9, 0.0, 1);

        VEIN_BLOCK_TAGS = builder
                .comment("List of block tags for vein mining as in Start Pos Block Tag needs to match said block in selection with the same tag.")
                .defineList("VeinBlockTags", defaultVeinTags(), obj -> obj instanceof String);

        SERVER_SPEC = builder.build();
    }

    private static List<String> defaultVeinTags() {
        return List.of(
                "c:ores",
                "c:glass_blocks",
                "c:glass_panes",
                "c:sands",
                "c:end_stones",
                "c:cobblestones",
                "c:gravels",
                "c:netherracks",
                "c:obsidians",
                "c:ropes",
                "c:stones",
                "c:chests",
                "c:barrels",
                "c:villager_job_sites",
                "c:skulls",
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
