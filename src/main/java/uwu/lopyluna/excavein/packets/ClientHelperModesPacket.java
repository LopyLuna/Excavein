package uwu.lopyluna.excavein.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import uwu.lopyluna.excavein.client.ClientHelper;

import java.util.function.Supplier;

public record ClientHelperModesPacket(String currentMode, String previousMode, String nextMode,
                                      String currentModifier, String previousModifier, String nextModifier) {
    public static void encode(ClientHelperModesPacket msg, FriendlyByteBuf buffer) {
        buffer.writeUtf(msg.currentMode);
        buffer.writeUtf(msg.previousMode);
        buffer.writeUtf(msg.nextMode);
        buffer.writeUtf(msg.currentModifier);
        buffer.writeUtf(msg.previousModifier);
        buffer.writeUtf(msg.nextModifier);
    }
    public static ClientHelperModesPacket decode(FriendlyByteBuf buffer) {
        return new ClientHelperModesPacket(buffer.readUtf(), buffer.readUtf(), buffer.readUtf(), buffer.readUtf(), buffer.readUtf(), buffer.readUtf());
    }
    public static void handle(ClientHelperModesPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> ClientHelper.update(msg.currentMode, msg.previousMode, msg.nextMode, msg.currentModifier, msg.previousModifier, msg.nextModifier));
        context.get().setPacketHandled(true);
    }
}
