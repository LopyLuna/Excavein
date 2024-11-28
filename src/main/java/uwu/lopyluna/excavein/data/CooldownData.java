package uwu.lopyluna.excavein.data;

import net.minecraft.util.Mth;
import uwu.lopyluna.excavein.utils.Utils;

import static uwu.lopyluna.excavein.config.ServerConfig.SELECTION_ADD_COOLDOWN;
import static uwu.lopyluna.excavein.config.ServerConfig.SELECTION_COOLDOWN;
import static uwu.lopyluna.excavein.tracker.ExcaveinTacker.playerCooldowns;

public class CooldownData {
    SelectionPlayerData data;
    int value = 0;

    public CooldownData(SelectionPlayerData data) {
        this.data = data;
        playerCooldowns.put(data, this);
    }

    public void tick() {
        if (value > 0) {
            value--;
        }
    }

    public void resetCooldown(int amountOfBlocks) {
        value = Mth.clamp(Utils.calculateValueFromAmount(SELECTION_COOLDOWN.get(), amountOfBlocks) + SELECTION_ADD_COOLDOWN.get(), 0, SELECTION_COOLDOWN.get());
    }

    public boolean isCooldownNotActive() {
        return value <= 0;
    }

    public int getRemainingCooldown() {
        return value;
    }
}
