package uwu.lopyluna.excavein.config;


import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {

    public static final ModConfigSpec.BooleanValue ADDITIVE_SHADER_SELECTION;

    public static final ModConfigSpec.IntValue MIXED_SELECTION_COLOR_R;
    public static final ModConfigSpec.IntValue MIXED_SELECTION_COLOR_G;
    public static final ModConfigSpec.IntValue MIXED_SELECTION_COLOR_B;
    public static final ModConfigSpec.IntValue MIXED_SELECTION_ALPHA;
    public static final ModConfigSpec.IntValue DESTROY_SELECTION_COLOR_R;
    public static final ModConfigSpec.IntValue DESTROY_SELECTION_COLOR_G;
    public static final ModConfigSpec.IntValue DESTROY_SELECTION_COLOR_B;
    public static final ModConfigSpec.IntValue DESTROY_SELECTION_ALPHA;
    public static final ModConfigSpec.IntValue INTERACTION_SELECTION_COLOR_R;
    public static final ModConfigSpec.IntValue INTERACTION_SELECTION_COLOR_G;
    public static final ModConfigSpec.IntValue INTERACTION_SELECTION_COLOR_B;
    public static final ModConfigSpec.IntValue INTERACTION_SELECTION_ALPHA;
    public static final ModConfigSpec.IntValue WARN_SELECTION_COLOR_R;
    public static final ModConfigSpec.IntValue WARN_SELECTION_COLOR_G;
    public static final ModConfigSpec.IntValue WARN_SELECTION_COLOR_B;
    public static final ModConfigSpec.IntValue WARN_SELECTION_ALPHA;

    public static final ModConfigSpec.BooleanValue MIXED_BLUR_FACE;
    public static final ModConfigSpec.BooleanValue DESTROY_BLUR_FACE;
    public static final ModConfigSpec.BooleanValue INTERACTION_BLUR_FACE;
    public static final ModConfigSpec.BooleanValue WARN_BLUR_FACE;

    public static final ModConfigSpec.BooleanValue TEXT_SHADOW;
    public static final ModConfigSpec.BooleanValue TEXT_BACKGROUND;
    public static final ModConfigSpec.BooleanValue TEXT_LEFT_SIDE;
    public static final ModConfigSpec.BooleanValue TOGGLEABLE_KEY;
    public static final ModConfigSpec.BooleanValue DISABLE_SCROLL;
    public static final ModConfigSpec.DoubleValue OUTLINE_THICKNESS;
    public static final ModConfigSpec.BooleanValue RENDER_OUTLINE;
    public static final ModConfigSpec.BooleanValue RENDER_FACE;
    public static final ModConfigSpec.BooleanValue XRAY_OUTLINE_SELECTION;
    public static final ModConfigSpec.IntValue SELECTION_OFFSET_Y;
    public static final ModConfigSpec.IntValue SELECTION_OFFSET_X;
    public static final ModConfigSpec.BooleanValue DISPLAY_SELECTION_CHAT;
    public static final ModConfigSpec.IntValue MAX_BLOCK_VIEW;
    public static final ModConfigSpec.IntValue COSMETIC_TYPE;
    public static final ModConfigSpec.BooleanValue DEBUG;

    public static final ModConfigSpec CLIENT_SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        ADDITIVE_SHADER_SELECTION = builder
                .comment("Whether to have selection render additive (default = true)")
                .define("AdditiveShaderSelection", true);

        MIXED_SELECTION_COLOR_R = builder.comment("Outline Red color value (default = 232)").defineInRange("SelectionColorR", 232, 1, 255);
        MIXED_SELECTION_COLOR_G = builder.comment("Outline Green color value (default = 232)").defineInRange("SelectionColorG", 232, 1, 255);
        MIXED_SELECTION_COLOR_B = builder.comment("Outline Blue color value (default = 232)").defineInRange("SelectionColorB", 232, 1, 255);
        MIXED_SELECTION_ALPHA = builder.comment("Outline alpha value (default = 32)").defineInRange("SelectionAlpha", 64, 1, 255);
        MIXED_BLUR_FACE = builder.comment("Whether to blur the Face Texture of the Mixed Selection (default = false)").define("BlurFace", false);

        DESTROY_SELECTION_COLOR_R = builder.comment("Outline Red color value (default = 232)").defineInRange("DestroySelectionColorR", 232, 1, 255);
        DESTROY_SELECTION_COLOR_G = builder.comment("Outline Green color value (default = 232)").defineInRange("DestroySelectionColorG", 128, 1, 255);
        DESTROY_SELECTION_COLOR_B = builder.comment("Outline Blue color value (default = 232)").defineInRange("DestroySelectionColorB", 128, 1, 255);
        DESTROY_SELECTION_ALPHA = builder.comment("Outline alpha value (default = 32)").defineInRange("DestroySelectionAlpha", 96, 1, 255);
        DESTROY_BLUR_FACE = builder.comment("Whether to blur the Face Texture of the Destroy Selection (default = true)").define("DestroyBlurFace", true);

        INTERACTION_SELECTION_COLOR_R = builder.comment("Outline Red color value (default = 232)").defineInRange("InteractionSelectionColorR", 96, 1, 255);
        INTERACTION_SELECTION_COLOR_G = builder.comment("Outline Green color value (default = 232)").defineInRange("InteractionSelectionColorG", 232, 1, 255);
        INTERACTION_SELECTION_COLOR_B = builder.comment("Outline Blue color value (default = 232)").defineInRange("InteractionSelectionColorB", 128, 1, 255);
        INTERACTION_SELECTION_ALPHA = builder.comment("Outline alpha value (default = 32)").defineInRange("InteractionSelectionAlpha", 96, 1, 255);
        INTERACTION_BLUR_FACE = builder.comment("Whether to blur the Face Texture of the Interaction Selection (default = true)").define("InteractionBlurFace", true);

        WARN_SELECTION_COLOR_R = builder.comment("Outline Red color value (default = 232)").defineInRange("WarnSelectionColorR", 232, 1, 255);
        WARN_SELECTION_COLOR_G = builder.comment("Outline Green color value (default = 232)").defineInRange("WarnSelectionColorG", 96, 1, 255);
        WARN_SELECTION_COLOR_B = builder.comment("Outline Blue color value (default = 232)").defineInRange("WarnSelectionColorB", 96, 1, 255);
        WARN_SELECTION_ALPHA = builder.comment("Outline alpha value (default = 32)").defineInRange("WarnSelectionAlpha", 96, 1, 255);
        WARN_BLUR_FACE = builder.comment("Whether to blur the Face Texture of the Warn Selection (default = true)").define("WarnBlurFace", true);

        TEXT_SHADOW = builder
                .comment("Whether to have shadow behind of the Selection Text (default = false)")
                .define("TextShadow", false);

        TEXT_BACKGROUND = builder
                .comment("Whether to have background behind of the Selection Text (default = false)")
                .define("TextBackground", true);

        TEXT_LEFT_SIDE = builder
                .comment("Whether to have Selection Text on the left side of the screen (default = true)")
                .define("TextLeftSide", true);

        TOGGLEABLE_KEY = builder
                .comment("Whether to make the Activation Key Toggleable (default = false)")
                .define("ToggleableKey", false);

        DISABLE_SCROLL = builder
                .comment("Whether to disable selection scroll (default = false)")
                .define("DisableScroll", false);

        OUTLINE_THICKNESS = builder
                .comment("Outline thickness (default = 0.5)")
                .defineInRange("OutlineThickness", 0.5, 0.1, 16.0);

        RENDER_OUTLINE = builder
                .comment("Whether to render the Outline of the Selection (default = true)")
                .define("RenderOutline", true);

        RENDER_FACE = builder
                .comment("Whether to render the Face Texture of the Selection (default = true)")
                .define("RenderFace", true);

        XRAY_OUTLINE_SELECTION = builder
                .comment("Ability to see your selection through Blocks whether the server allows it also (default = false)")
                .define("XrayOutlineSelection", false);

        SELECTION_OFFSET_Y = builder
                .comment("Offset Y position of Selection Text (default = 0)")
                .defineInRange("SelectionTextOffsetY", 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);

        SELECTION_OFFSET_X = builder
                .comment("Offset Z position of Selection Text (default = 0)")
                .defineInRange("SelectionTextOffsetX", 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);

        DISPLAY_SELECTION_CHAT = builder
                .comment("Whether to display Current Mode Text above the hotbar or in chat (default = false)")
                .define("DisplaySelectionInChat", false);

        MAX_BLOCK_VIEW = builder
                .comment("Maximum of Selection Blocks that can be viewed (default = 128)")
                .defineInRange("MaxBlockViewing", 128, 0, 2048);

        COSMETIC_TYPE = builder
                .comment("Whether to switch your Excavein Special Cosmetic Types if have one (default = 1) \n" +
                        "0=DISABLE|1=ANNIVERSARY|2=CONTRIBUTOR|3=TEAM|4=･ω･")
                .defineInRange("CosmeticType", 1, 0, 9999);

        DEBUG = builder
                .comment("for Devs mainly (default = false)")
                .define("Debug", false);

        CLIENT_SPEC = builder.build();
    }
}
