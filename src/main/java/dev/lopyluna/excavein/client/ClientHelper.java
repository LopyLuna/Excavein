package dev.lopyluna.excavein.client;

public class ClientHelper {
    static String currentMode = "";
    static String previousMode = "";
    static String nextMode = "";

    static String currentModifier = "";
    static String previousModifier = "";
    static String nextModifier = "";

    static boolean currentlyBreaking = false;
    static boolean requiredFlags = false;
    static boolean flag = false;

    public static void update(String currentMode, String previousMode, String nextMode,
                              String currentModifier, String previousModifier, String nextModifier) {
        ClientHelper.currentMode = currentMode;
        ClientHelper.previousMode = previousMode;
        ClientHelper.nextMode = nextMode;
        ClientHelper.currentModifier = currentModifier;
        ClientHelper.previousModifier = previousModifier;
        ClientHelper.nextModifier = nextModifier;
    }

    public static void update(boolean currentlyBreaking, boolean requiredFlags, boolean flag) {
        ClientHelper.currentlyBreaking = currentlyBreaking;
        ClientHelper.requiredFlags = requiredFlags;
        ClientHelper.flag = flag;
    }
}
