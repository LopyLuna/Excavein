package uwu.lopyluna.excavein.tracker;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import uwu.lopyluna.excavein.data.SelectionPlayerData;
import uwu.lopyluna.excavein.entries.ExcaveinEntries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
@EventBusSubscriber
public class ExcaveinTacker {
    private static final Map<UUID, SelectionPlayerData> selectionDataMap = new HashMap<>();

    public static void update(Player player, UUID uuid, boolean keyPressed, int switchMode) {
        if (player != null && uuid != null && player.getUUID().equals(uuid)) {
            if (!selectionDataMap.containsKey(uuid))
                selectionDataMap.put(uuid, new SelectionPlayerData(player.level(), uuid));

            SelectionPlayerData data = getSelectionData(uuid);
            if (data != null && data.getPlayer() != null && data.getLevel() != null && data.getPlayerUUID() != null) {
                data.updateKey(keyPressed);

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

    public static SelectionPlayerData getSelectionData(UUID uuid) {
        return uuid != null ? selectionDataMap.getOrDefault(uuid, null) : null;
    }

    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        List<? extends Player> players = level.players();
        if (level.isClientSide)
            return;
        players.forEach(pPlayer -> {
            if (!(pPlayer instanceof ServerPlayer player))
                return;
            SelectionPlayerData selectionData = getSelectionData(player.getUUID());
            if (selectionData != null && selectionData.getPlayer() != null && selectionData.getLevel() != null && selectionData.getPlayerUUID() != null) {
                selectionData.tick();
                selectionData.updateCheck();

                ExcaveinEntries.getShapeEntries().forEach((integer, shapeEntry) -> {
                    if (shapeEntry.getKeybind().consumeClick())
                        selectionData.setShapeMode(integer);
                });
                ExcaveinEntries.getShapeModifierEntries().forEach((integer, modifierEntry) -> {
                    if (modifierEntry.getKeybind().consumeClick())
                        selectionData.setModifierMode(integer);
                });
            }
        });
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        getSelectionData(event.getPlayer().getUUID()).blockBreak(event.getPos());
    }
}
