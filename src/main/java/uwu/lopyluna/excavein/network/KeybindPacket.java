package uwu.lopyluna.excavein.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import uwu.lopyluna.excavein.tracker.BlockPositionTracker;

import java.util.function.Supplier;

public class KeybindPacket {
    private final boolean selectionKeyIsDown;

    public KeybindPacket(boolean b) {
        selectionKeyIsDown = b;
    }

    public static void encode(KeybindPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.selectionKeyIsDown);
    }

    public static KeybindPacket decode(FriendlyByteBuf buf) {
        return new KeybindPacket(buf.readBoolean());
    }

    public static void handle(KeybindPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> BlockPositionTracker.update(msg.selectionKeyIsDown));
        context.setPacketHandled(true);
    }
}
