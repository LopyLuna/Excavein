package uwu.lopyluna.excavein.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import uwu.lopyluna.excavein.client.ClientCooldownHandler;

import java.util.function.Supplier;

public record CooldownPacket(int cooldownTicks) {
    public static void encode(CooldownPacket msg, FriendlyByteBuf buffer) {
        buffer.writeInt(msg.cooldownTicks);
    }
    public static CooldownPacket decode(FriendlyByteBuf buffer) {
        return new CooldownPacket(buffer.readInt());
    }
    public static void handle(CooldownPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> ClientCooldownHandler.setCooldown(msg.cooldownTicks));
    }
}
