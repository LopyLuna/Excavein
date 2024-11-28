package uwu.lopyluna.excavein.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import uwu.lopyluna.excavein.tracker.ExcaveinTacker;

import java.util.UUID;
import java.util.function.Supplier;

public record KeybindPacket(UUID playerID, boolean keyPressed, int id, String mode) {
    public static void encode(KeybindPacket msg, FriendlyByteBuf buffer) {
        buffer.writeUUID(msg.playerID);
        buffer.writeBoolean(msg.keyPressed);
        buffer.writeInt(msg.id);
        buffer.writeUtf(msg.mode);
    }
    public static KeybindPacket decode(FriendlyByteBuf buffer) {
        return new KeybindPacket(buffer.readUUID(), buffer.readBoolean(), buffer.readInt(), buffer.readUtf());
    }
    public static void handle(KeybindPacket msg, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) context.get().enqueueWork(() -> ExcaveinTacker.updateKey(player, player.getUUID().equals(msg.playerID) ? player.getUUID() : msg.playerID, msg.keyPressed, msg.id, msg.mode));
    }
}
