package uwu.lopyluna.excavein.tracker;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import uwu.lopyluna.excavein.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static uwu.lopyluna.excavein.config.ServerConfig.SELECTION_ADD_COOLDOWN;
import static uwu.lopyluna.excavein.config.ServerConfig.SELECTION_COOLDOWN;

@SuppressWarnings("unused")
public class CooldownTracker {
    private static final Map<UUID, Integer> playerCooldowns = new HashMap<>();

    public static void resetCooldown(ServerPlayer player, int amountOfBlocks) {
        playerCooldowns.put(player.getUUID(), Mth.clamp(Utils.calculateTimeFromBlocks(SELECTION_COOLDOWN.get(), amountOfBlocks) + SELECTION_ADD_COOLDOWN.get(), 0, SELECTION_COOLDOWN.get()));
    }

    public static boolean isCooldownNotActive(ServerPlayer player) {
        return playerCooldowns.getOrDefault(player.getUUID(), 0) <= 0;
    }

    public static int getRemainingCooldown(ServerPlayer player) {
        return playerCooldowns.getOrDefault(player.getUUID(), 0);
    }

    public static void getCoolDownCheck(ServerPlayer player) {
        playerCooldowns.putIfAbsent(player.getUUID(), 0);
    }

    public static void onWorldTick(Level level) {
        for (Map.Entry<UUID, Integer> entry : playerCooldowns.entrySet()) {
            int remainingTicks = entry.getValue();
            if (remainingTicks > 0) {
                playerCooldowns.put(entry.getKey(), remainingTicks - 1);
            }
        }
    }

    public static void onPlayerLogin(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
        playerCooldowns.putIfAbsent(handler.player.getUUID(), 0);
    }
}
