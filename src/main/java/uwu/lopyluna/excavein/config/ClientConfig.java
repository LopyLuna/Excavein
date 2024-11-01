package uwu.lopyluna.excavein.config;


import net.neoforged.neoforge.common.ModConfigSpec;

public class  ClientConfig {

    public static final ModConfigSpec.IntValue SELECTION_COLOR_R;
    public static final ModConfigSpec.IntValue SELECTION_COLOR_G;
    public static final ModConfigSpec.IntValue SELECTION_COLOR_B;
    public static final ModConfigSpec.IntValue SELECTION_ALPHA;
    public static final ModConfigSpec.BooleanValue TEXT_SHADOW;
    public static final ModConfigSpec.BooleanValue TEXT_BACKGROUND;
    public static final ModConfigSpec.BooleanValue TEXT_LEFT_SIDE;
    public static final ModConfigSpec.BooleanValue TOGGLEABLE_KEY;
    public static final ModConfigSpec.BooleanValue DISABLE_SCROLL;
    public static final ModConfigSpec.DoubleValue OUTLINE_THICKNESS;
    public static final ModConfigSpec.BooleanValue RENDER_OUTLINE;
    public static final ModConfigSpec.BooleanValue RENDER_FACE;
    public static final ModConfigSpec.BooleanValue BLUR_FACE;
    public static final ModConfigSpec.BooleanValue XRAY_OUTLINE_SELECTION;
    public static final ModConfigSpec.IntValue SELECTION_OFFSET_Y;
    public static final ModConfigSpec.IntValue SELECTION_OFFSET_X;
    public static final ModConfigSpec.BooleanValue DISPLAY_SELECTION_CHAT;
    public static final ModConfigSpec.IntValue MAX_BLOCK_VIEW;

    public static final ModConfigSpec CLIENT_SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        SELECTION_COLOR_R = builder
                .comment("Outline Red color value (default = 232)")
                .defineInRange("SelectionColorR", 232, 1, 255);

        SELECTION_COLOR_G = builder
                .comment("Outline Green color value (default = 232)")
                .defineInRange("SelectionColorG", 232, 1, 255);

        SELECTION_COLOR_B = builder
                .comment("Outline Blue color value (default = 232)")
                .defineInRange("SelectionColorB", 232, 1, 255);

        SELECTION_ALPHA = builder
                .comment("Outline alpha value (default = 32)")
                .defineInRange("SelectionAlpha", 32, 1, 255);

        TEXT_SHADOW = builder
                .comment("Whether to have shadow behind of the Selection Text (default = true)")
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

        BLUR_FACE = builder
                .comment("Whether to blur the Face Texture of the Selection (default = true)")
                .define("BlurFace", true);

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

        MAX_BLOCK_VIEW = builder
                .comment("Maximum of Selection Blocks that can be viewed (default = 128)")
                .defineInRange("MaxBlockViewing", 128, 0, 2048);

        CLIENT_SPEC = builder.build();
    }
}
