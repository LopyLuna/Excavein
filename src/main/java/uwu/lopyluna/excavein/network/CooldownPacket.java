package uwu.lopyluna.excavein.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import uwu.lopyluna.excavein.client.ClientCooldownHandler;

import java.util.function.Supplier;

public class CooldownPacket {
    private final int cooldownTicks;

    public CooldownPacket(int cooldownTicks) {
        this.cooldownTicks = cooldownTicks;
    }

    public static void encode(CooldownPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.cooldownTicks);
    }

    public static CooldownPacket decode(FriendlyByteBuf buffer) {
        return new CooldownPacket(buffer.readInt());
    }

    public static void handle(CooldownPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientCooldownHandler.setCooldown(packet.cooldownTicks));
        context.setPacketHandled(true);
    }
}
