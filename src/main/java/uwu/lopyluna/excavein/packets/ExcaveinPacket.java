package uwu.lopyluna.excavein.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import uwu.lopyluna.excavein.tracker.ExcaveinTacker;

import java.util.UUID;
import java.util.function.Supplier;

public record ExcaveinPacket(UUID playerID, boolean keyPressed, boolean displayChat) {
    public static void encode(ExcaveinPacket msg, FriendlyByteBuf buffer) {
        buffer.writeUUID(msg.playerID);
        buffer.writeBoolean(msg.keyPressed);
        buffer.writeBoolean(msg.displayChat);
    }
    public static ExcaveinPacket decode(FriendlyByteBuf buffer) {
        return new ExcaveinPacket(buffer.readUUID(), buffer.readBoolean(), buffer.readBoolean());
    }
    public static void handle(ExcaveinPacket msg, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) context.get().enqueueWork(() -> ExcaveinTacker.updateTick(player, player.getUUID().equals(msg.playerID) ? player.getUUID() : msg.playerID, msg.keyPressed, msg.displayChat));
    }
}
