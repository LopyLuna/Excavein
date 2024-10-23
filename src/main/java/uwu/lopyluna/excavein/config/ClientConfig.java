package uwu.lopyluna.excavein.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {

    public static final ForgeConfigSpec.IntValue SELECTION_COLOR_R;
    public static final ForgeConfigSpec.IntValue SELECTION_COLOR_G;
    public static final ForgeConfigSpec.IntValue SELECTION_COLOR_B;
    public static final ForgeConfigSpec.IntValue SELECTION_ALPHA;
    public static final ForgeConfigSpec.DoubleValue OUTLINE_THICKNESS;
    public static final ForgeConfigSpec.BooleanValue RENDER_OUTLINE;
    public static final ForgeConfigSpec.BooleanValue RENDER_FACE;
    public static final ForgeConfigSpec.BooleanValue BLUR_FACE;
    public static final ForgeConfigSpec.IntValue SELECTION_OFFSET_Y;
    public static final ForgeConfigSpec.IntValue SELECTION_OFFSET_X;
    public static final ForgeConfigSpec.BooleanValue DISPLAY_SELECTION_CHAT;
    public static final ForgeConfigSpec.IntValue MAX_BLOCK_VIEW;

    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        SELECTION_COLOR_R = builder
                .comment("Outline Red color value (default = 187)")
                .defineInRange("SelectionColorR", 187, 1, 255);

        SELECTION_COLOR_G = builder
                .comment("Outline Green color value (default = 247)")
                .defineInRange("SelectionColorG", 247, 1, 255);

        SELECTION_COLOR_B = builder
                .comment("Outline Blue color value (default = 252)")
                .defineInRange("SelectionColorB", 252, 1, 255);

        SELECTION_ALPHA = builder
                .comment("Outline alpha value (default = 64)")
                .defineInRange("SelectionAlpha", 64, 1, 255);

        OUTLINE_THICKNESS = builder
                .comment("Outline thickness (default = 1.0)")
                .defineInRange("OutlineThickness", 1.0, 0.1, 16.0);

        RENDER_OUTLINE = builder
                .comment("Whether to render the Outline of the Selection (default = true)")
                .define("RenderOutline", true);

        RENDER_FACE = builder
                .comment("Whether to render the Face Texture of the Selection (default = true)")
                .define("RenderFace", true);

        BLUR_FACE = builder
                .comment("Whether to blur the Face Texture of the Selection (default = false)")
                .define("BlurFace", false);

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
                .comment("Maximum of Selection Blocks that can be viewed (default = 256)")
                .defineInRange("MaxBlockViewing", 256, 0, 2048);

        CLIENT_SPEC = builder.build();
    }
}
