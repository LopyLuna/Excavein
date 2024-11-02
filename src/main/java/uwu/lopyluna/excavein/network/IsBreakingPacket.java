package uwu.lopyluna.excavein.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import uwu.lopyluna.excavein.client.BlockOutlineRenderer;

import java.util.function.Supplier;

public class IsBreakingPacket {
    private final boolean isBreaking;

    public IsBreakingPacket(boolean isBreaking) {
        this.isBreaking = isBreaking;
    }

    public static void encode(IsBreakingPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.isBreaking);
    }

    public static IsBreakingPacket decode(FriendlyByteBuf buffer) {
        return new IsBreakingPacket(buffer.readBoolean());
    }

    public static void handle(IsBreakingPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> BlockOutlineRenderer.setBreaking(packet.isBreaking));
        context.setPacketHandled(true);
    }
}
