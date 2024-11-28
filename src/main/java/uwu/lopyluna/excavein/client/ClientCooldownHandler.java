package uwu.lopyluna.excavein.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
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
