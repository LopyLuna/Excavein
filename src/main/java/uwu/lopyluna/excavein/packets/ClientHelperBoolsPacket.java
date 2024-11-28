package uwu.lopyluna.excavein.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import uwu.lopyluna.excavein.client.ClientHelper;

import java.util.function.Supplier;

public record ClientHelperBoolsPacket(boolean currentlyBreaking, boolean requiredFlags, boolean flag) {
    public static void encode(ClientHelperBoolsPacket msg, FriendlyByteBuf buffer) {
        buffer.writeBoolean(msg.currentlyBreaking);
        buffer.writeBoolean(msg.requiredFlags);
        buffer.writeBoolean(msg.flag);
    }
    public static ClientHelperBoolsPacket decode(FriendlyByteBuf buffer) {
        return new ClientHelperBoolsPacket(buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
    }
    public static void handle(ClientHelperBoolsPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> ClientHelper.update(msg.currentlyBreaking, msg.requiredFlags, msg.flag));
    }
}
