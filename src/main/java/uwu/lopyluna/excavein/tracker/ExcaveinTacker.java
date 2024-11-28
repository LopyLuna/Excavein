package uwu.lopyluna.excavein.tracker;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import uwu.lopyluna.excavein.data.CooldownData;
import uwu.lopyluna.excavein.data.SelectionPlayerData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
@EventBusSubscriber
public class ExcaveinTacker {
    private static final Map<UUID, SelectionPlayerData> selectionDataMap = new HashMap<>();

    public static void updateTick(ServerPlayer player, UUID uuid, boolean keyPressed, boolean displayChat) {
        if (player != null && uuid != null && player.getUUID().equals(uuid)) {
            SelectionPlayerData data = getSelectionData(uuid);
            if (data != null && data.getPlayer() != null && data.getLevel() != null && data.getPlayerUUID() != null)
                data.updateKey(keyPressed, displayChat);
            else selectionDataMap.remove(uuid);
        } else {
            boolean equals = selectionDataMap.remove(uuid) == null;
            if (equals) selectionDataMap.clear();
        }
    }

    public static void update(ServerPlayer player, UUID uuid, boolean keyPressed, int switchMode) {
        if (player != null && uuid != null && player.getUUID().equals(uuid)) {
            SelectionPlayerData data = getSelectionData(uuid);
            if (data != null && data.getPlayer() != null && data.getLevel() != null && data.getPlayerUUID() != null) {
                if (switchMode == 4) {
                    data.nextModifierMode();
                } else if (switchMode == 3) {
                    data.previousModifierMode();
                } else if (switchMode == 2) {
                    data.nextShapeMode();
                } else if (switchMode == 1) {
                    data.previousShapeMode();
                }
            }
        }
    }

    public static void updateKey(ServerPlayer player, UUID uuid, boolean keyPressed, int id, String type) {
        if (player != null && uuid != null && player.getUUID().equals(uuid)) {
            SelectionPlayerData data = getSelectionData(uuid);
            if (data != null && data.getPlayer() != null && data.getLevel() != null && data.getPlayerUUID() != null) {
                if (keyPressed) {
                    if (type.equals("shape"))
                        data.setShapeMode(id);
                    if (type.equals("modifier"))
                        data.setModifierMode(id);
                }
            }
        }
    }

    public static SelectionPlayerData getSelectionData(UUID uuid) {
        return uuid != null ? selectionDataMap.getOrDefault(uuid, null) : null;
    }

    public static final Map<SelectionPlayerData, CooldownData> playerCooldowns = new HashMap<>();

    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent event) {
        if (event.phase.equals(TickEvent.Phase.END)) {
            Level level = event.level;
            if (!level.isClientSide()) {
                List<? extends Player> players = level.players();
                players.forEach(pPlayer -> {
                    if (!(pPlayer instanceof ServerPlayer player))
                        return;
                    SelectionPlayerData selectionData = getSelectionData(player.getUUID());
                    if (selectionData != null && selectionData.getPlayer() != null && selectionData.getLevel() != null && selectionData.getPlayerUUID() != null) {
                        selectionData.tick();
                        selectionData.updateCheck();
                        if (selectionData.getCooldownData() != null) selectionData.getCooldownData().tick();
                    }
                });
            }
        }
    }

    public static void updateFixSelection(Level level, Player player) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            selectionDataMap.remove(serverPlayer.getUUID());
            if (!selectionDataMap.containsKey(serverPlayer.getUUID())) selectionDataMap.put(serverPlayer.getUUID(), new SelectionPlayerData(serverPlayer.serverLevel(), serverPlayer.getUUID()));
        }
    }

    @SubscribeEvent
    public static void onChangeDim(PlayerEvent.PlayerChangedDimensionEvent event) {
        updateFixSelection(event.getEntity().level(), event.getEntity());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        updateFixSelection(event.getEntity().level(), event.getEntity());
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        updateFixSelection(event.getEntity().level(), event.getEntity());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof ServerPlayer player)
            selectionDataMap.remove(player.getUUID());
    }
}
