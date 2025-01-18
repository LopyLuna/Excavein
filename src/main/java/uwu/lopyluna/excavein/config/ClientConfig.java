package uwu.lopyluna.excavein.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {

    public static final ForgeConfigSpec.BooleanValue ADDITIVE_SHADER_SELECTION;

    public static final ForgeConfigSpec.BooleanValue RGB;
    public static final ForgeConfigSpec.DoubleValue RGB_TRANSITION_SPEED;

    public static final ForgeConfigSpec.IntValue MIXED_SELECTION_COLOR_R;
    public static final ForgeConfigSpec.IntValue MIXED_SELECTION_COLOR_G;
    public static final ForgeConfigSpec.IntValue MIXED_SELECTION_COLOR_B;
    public static final ForgeConfigSpec.IntValue MIXED_SELECTION_ALPHA;
    public static final ForgeConfigSpec.IntValue DESTROY_SELECTION_COLOR_R;
    public static final ForgeConfigSpec.IntValue DESTROY_SELECTION_COLOR_G;
    public static final ForgeConfigSpec.IntValue DESTROY_SELECTION_COLOR_B;
    public static final ForgeConfigSpec.IntValue DESTROY_SELECTION_ALPHA;
    public static final ForgeConfigSpec.IntValue INTERACTION_SELECTION_COLOR_R;
    public static final ForgeConfigSpec.IntValue INTERACTION_SELECTION_COLOR_G;
    public static final ForgeConfigSpec.IntValue INTERACTION_SELECTION_COLOR_B;
    public static final ForgeConfigSpec.IntValue INTERACTION_SELECTION_ALPHA;
    public static final ForgeConfigSpec.IntValue WARN_SELECTION_COLOR_R;
    public static final ForgeConfigSpec.IntValue WARN_SELECTION_COLOR_G;
    public static final ForgeConfigSpec.IntValue WARN_SELECTION_COLOR_B;
    public static final ForgeConfigSpec.IntValue WARN_SELECTION_ALPHA;

    public static final ForgeConfigSpec.BooleanValue MIXED_BLUR_FACE;
    public static final ForgeConfigSpec.BooleanValue DESTROY_BLUR_FACE;
    public static final ForgeConfigSpec.BooleanValue INTERACTION_BLUR_FACE;
    public static final ForgeConfigSpec.BooleanValue WARN_BLUR_FACE;

    public static final ForgeConfigSpec.BooleanValue TEXT_SHADOW;
    public static final ForgeConfigSpec.BooleanValue TEXT_BACKGROUND;
    public static final ForgeConfigSpec.BooleanValue TEXT_LEFT_SIDE;
    public static final ForgeConfigSpec.BooleanValue TOGGLEABLE_KEY;
    public static final ForgeConfigSpec.BooleanValue DISABLE_SCROLL;
    public static final ForgeConfigSpec.DoubleValue OUTLINE_THICKNESS;
    public static final ForgeConfigSpec.BooleanValue RENDER_OUTLINE;
    public static final ForgeConfigSpec.BooleanValue RENDER_FACE;
    public static final ForgeConfigSpec.BooleanValue XRAY_OUTLINE_SELECTION;
    public static final ForgeConfigSpec.IntValue SELECTION_OFFSET_Y;
    public static final ForgeConfigSpec.IntValue SELECTION_OFFSET_X;
    public static final ForgeConfigSpec.BooleanValue DISPLAY_SELECTION_CHAT;
    public static final ForgeConfigSpec.BooleanValue SELECTION_ACTION_TEXT;
    public static final ForgeConfigSpec.IntValue MAX_BLOCK_VIEW;
    public static final ForgeConfigSpec.IntValue COSMETIC_TYPE;
    public static final ForgeConfigSpec.BooleanValue DEBUG;

    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        ADDITIVE_SHADER_SELECTION = builder
                .comment("Whether to have selection render additive (default = false)")
                .define("AdditiveShaderSelection", false);

        RGB = builder
                .comment("RGB Gayming with style :3 (default = false)")
                .define("RGB", false);
        RGB_TRANSITION_SPEED = builder
                .comment("Color Fading between Transitions (default = 0.5)")
                .defineInRange("RGBTransitionSpeed", 0.5, 0.1, 16.0);

        MIXED_SELECTION_COLOR_R = builder.comment("Outline Red color value (default = 232)").defineInRange("MixedSelectionColorR", 232, 1, 255);
        MIXED_SELECTION_COLOR_G = builder.comment("Outline Green color value (default = 232)").defineInRange("MixedSelectionColorG", 232, 1, 255);
        MIXED_SELECTION_COLOR_B = builder.comment("Outline Blue color value (default = 232)").defineInRange("MixedSelectionColorB", 232, 1, 255);
        MIXED_SELECTION_ALPHA = builder.comment("Outline alpha value (default = 64)").defineInRange("MixedSelectionAlpha", 64, 1, 255);
        MIXED_BLUR_FACE = builder.comment("Whether to blur the Face Texture of the Mixed Selection (default = false)").define("BlurFace", false);

        DESTROY_SELECTION_COLOR_R = builder.comment("Outline Red color value (default = 232)").defineInRange("DestroySelectionColorR", 232, 1, 255);
        DESTROY_SELECTION_COLOR_G = builder.comment("Outline Green color value (default = 128)").defineInRange("DestroySelectionColorG", 128, 1, 255);
        DESTROY_SELECTION_COLOR_B = builder.comment("Outline Blue color value (default = 128)").defineInRange("DestroySelectionColorB", 128, 1, 255);
        DESTROY_SELECTION_ALPHA = builder.comment("Outline alpha value (default = 96)").defineInRange("DestroySelectionAlpha", 96, 1, 255);
        DESTROY_BLUR_FACE = builder.comment("Whether to blur the Face Texture of the Destroy Selection (default = true)").define("DestroyBlurFace", true);

        INTERACTION_SELECTION_COLOR_R = builder.comment("Outline Red color value (default = 96)").defineInRange("InteractionSelectionColorR", 96, 1, 255);
        INTERACTION_SELECTION_COLOR_G = builder.comment("Outline Green color value (default = 232)").defineInRange("InteractionSelectionColorG", 232, 1, 255);
        INTERACTION_SELECTION_COLOR_B = builder.comment("Outline Blue color value (default = 128)").defineInRange("InteractionSelectionColorB", 128, 1, 255);
        INTERACTION_SELECTION_ALPHA = builder.comment("Outline alpha value (default = 96)").defineInRange("InteractionSelectionAlpha", 96, 1, 255);
        INTERACTION_BLUR_FACE = builder.comment("Whether to blur the Face Texture of the Interaction Selection (default = true)").define("InteractionBlurFace", true);

        WARN_SELECTION_COLOR_R = builder.comment("Outline Red color value (default = 232)").defineInRange("WarnSelectionColorR", 232, 1, 255);
        WARN_SELECTION_COLOR_G = builder.comment("Outline Green color value (default = 96)").defineInRange("WarnSelectionColorG", 96, 1, 255);
        WARN_SELECTION_COLOR_B = builder.comment("Outline Blue color value (default = 96)").defineInRange("WarnSelectionColorB", 96, 1, 255);
        WARN_SELECTION_ALPHA = builder.comment("Outline alpha value (default = 96)").defineInRange("WarnSelectionAlpha", 96, 1, 255);
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
                .comment("Ability to see your selection through Blocks whether the server allows it also (default = true)")
                .define("XrayOutlineSelection", true);

        SELECTION_OFFSET_Y = builder
                .comment("Offset Y position of Selection Text (default = 0)")
                .defineInRange("SelectionTextOffsetY", 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);

        SELECTION_OFFSET_X = builder
                .comment("Offset Z position of Selection Text (default = 0)")
                .defineInRange("SelectionTextOffsetX", 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);

        DISPLAY_SELECTION_CHAT = builder
                .comment("Whether to display Current Mode Text above the hotbar or in chat (default = false)")
                .define("DisplaySelectionInChat", false);

        SELECTION_ACTION_TEXT = builder
                .comment("Whether to display Current Mode Text (default = true)")
                .define("SelectionActionText", true);

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
