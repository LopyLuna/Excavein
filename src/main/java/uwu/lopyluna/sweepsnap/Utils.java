package uwu.lopyluna.sweepsnap;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.Random;

import static uwu.lopyluna.sweepsnap.SS.MOD_ID;

@SuppressWarnings({"unused"})
public class Utils {
    public static final Random RANDOM = new Random();

    public static boolean RANDOM_B = RANDOM.nextBoolean();
    public static int RANDOM_I = RANDOM.nextInt();
    public static double RANDOM_D = RANDOM.nextDouble();
    public static float RANDOM_F = RANDOM.nextFloat();
    public static long RANDOM_L = RANDOM.nextLong();

    public static double RANDOM_G = RANDOM.nextGaussian();
    public static double RANDOM_E = RANDOM.nextExponential();


    public static boolean randomChance(int chance) {
        int newChance = Mth.clamp(chance, 0, 100);
        return RANDOM.nextInt(1,  100) <= newChance;
    }

    public static boolean randomChance(int chance, Level level) {
        int newChance = Mth.clamp(chance, 0, 100);
        return level.getRandom().nextInt(1,  100) <= newChance;
    }

    public static boolean randomChance(double chance) {
        int newChance = Mth.clamp(((int) chance * 100), 0, 100);
        return RANDOM.nextInt(1,  100) <= newChance;
    }
    public static boolean randomChance(double chance, Level level) {
        int newChance = Mth.clamp(((int) chance * 100), 0, 100);
        return level.getRandom().nextInt(1,  100) <= newChance;
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
