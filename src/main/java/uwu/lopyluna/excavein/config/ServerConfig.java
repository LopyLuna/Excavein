package uwu.lopyluna.excavein.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class ServerConfig {

    public static final ForgeConfigSpec.IntValue SELECTION_COOLDOWN;
    public static final ForgeConfigSpec.IntValue SELECTION_ADD_COOLDOWN;
    public static final ForgeConfigSpec.IntValue SELECTION_ADD_RANGE;
    public static final ForgeConfigSpec.IntValue SELECTION_MAX_BLOCK;
    public static final ForgeConfigSpec.BooleanValue INVERT_WHITELIST;
    public static final ForgeConfigSpec.BooleanValue REQUIRES_TOOLS;
    public static final ForgeConfigSpec.BooleanValue BLOCKS_AT_PLAYER;
    public static final ForgeConfigSpec.BooleanValue BLOCK_PLACING;
    public static final ForgeConfigSpec.BooleanValue HAND_INTERACTION;
    public static final ForgeConfigSpec.BooleanValue ITEM_INTERACTION;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> VEIN_BLOCK_TAGS;

    public static final ForgeConfigSpec SERVER_SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        SELECTION_COOLDOWN = builder
                .comment("Amount of ticks for block selection cooldown (default = " + (20 * 5) + ")")
                .defineInRange("SelectionCooldown", 20 * 5, 0, Integer.MAX_VALUE);

        SELECTION_ADD_COOLDOWN = builder
                .comment("Amount of ticks that get added to the block selection cooldown (default = 0)")
                .defineInRange("SelectionAddedCooldown", 0, 0, Integer.MAX_VALUE);

        SELECTION_ADD_RANGE = builder
                .comment("Add range for block selection (default = 12)")
                .defineInRange("SelectionMaxRange", 12, -16, 128);

        SELECTION_MAX_BLOCK = builder
                .comment("Maximum number of blocks that can be selected (default = 64)")
                .defineInRange("SelectionMaxBlock", 64, 1, 2048);

        INVERT_WHITELIST = builder
                .comment("Invert the whitelist behavior (default = true)")
                .define("InvertWhitelist", true);
        
        REQUIRES_TOOLS = builder
                .comment("Require Tool for Said block (default = false)")
                .define("RequiresTools", false);

        BLOCKS_AT_PLAYER = builder
                .comment("Sends all dropped Items from Blocks to the Player (default = false)")
                .define("SendBlocksToPlayer", false);

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
                .comment("List of block tags for vein mining. Example: 'forge:ores', 'minecraft:logs'")
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