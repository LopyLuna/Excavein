package uwu.lopyluna.excavein.client;

import net.minecraft.network.chat.Component;

public enum SelectionMode {
    SELECTION,
    VEIN,
    EXCAVATE,
    TUNNEL,
    LARGE_TUNNEL,
    DIAGONAL_TUNNEL,
    SIDE_SELECTION,
    SIDE_VEIN,
    SIDE_EXCAVATE,
    SURFACE;

    private static final SelectionMode[] VALUES = values();
    private static int currentIndex = 0;

    public static SelectionMode getCurrentMode() {
        return VALUES[currentIndex];
    }

    public String getName() {
        return Component.translatable("excavein.modes." + name().toLowerCase()).getString();
    }

    public static SelectionMode getNextMode() {
        int index = getCurrentMode().ordinal() - 1;
        if (index < 0) {
            index = SelectionMode.values().length - 1;
        }
        return SelectionMode.values()[index];
    }

    public static SelectionMode getPreviousMode() {
        int index = (getCurrentMode().ordinal() + 1) % SelectionMode.values().length;
        return SelectionMode.values()[index];
    }

    public static void setMode(int mode) {
        currentIndex = mode;
    }

    public static void nextMode() {
        currentIndex = (currentIndex + 1) % VALUES.length;
    }

    public static void previousMode() {
        currentIndex = (currentIndex - 1 + VALUES.length) % VALUES.length;
    }
}