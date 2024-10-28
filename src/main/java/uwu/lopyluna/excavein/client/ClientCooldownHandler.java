package uwu.lopyluna.excavein.client;

public class ClientCooldownHandler {

    private static int remainingCooldown = 0;

    public static void setCooldown(int cooldownTicks) {
        remainingCooldown = cooldownTicks;
    }

    public static boolean isCooldownActive() {
        return remainingCooldown > 0;
    }

    public static int getRemainingCooldown() {
        return remainingCooldown;
    }
}
