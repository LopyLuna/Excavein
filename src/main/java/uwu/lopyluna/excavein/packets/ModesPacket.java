package uwu.lopyluna.excavein.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import uwu.lopyluna.excavein.tracker.ExcaveinTacker;

import java.util.UUID;
import java.util.function.Supplier;

public record ModesPacket(UUID playerID, boolean keyPressed, int switchMode) {
    public static void encode(ModesPacket msg, FriendlyByteBuf buffer) {
        buffer.writeUUID(msg.playerID);
        buffer.writeBoolean(msg.keyPressed);
        buffer.writeInt(msg.switchMode);
    }
    public static ModesPacket decode(FriendlyByteBuf buffer) {
        return new ModesPacket(buffer.readUUID(), buffer.readBoolean(), buffer.readInt());
    }
    public static void handle(ModesPacket msg, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) context.get().enqueueWork(() -> ExcaveinTacker.update(player, player.getUUID().equals(msg.playerID) ? player.getUUID() : msg.playerID, msg.keyPressed, msg.switchMode));
    }
}
