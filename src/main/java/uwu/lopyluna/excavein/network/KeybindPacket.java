package uwu.lopyluna.excavein.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import uwu.lopyluna.excavein.Utils;
import uwu.lopyluna.excavein.tracker.BlockPositionTracker;

public record KeybindPacket(boolean selectionKeyIsDown) implements CustomPacketPayload {

    public static final Type<KeybindPacket> TYPE = new Type<>(Utils.asResource("keybind"));
    public static final StreamCodec<FriendlyByteBuf, KeybindPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, KeybindPacket::selectionKeyIsDown,
            KeybindPacket::new
    );

    public static void handle(KeybindPacket msg, IPayloadContext context) {
        context.enqueueWork(() -> BlockPositionTracker.update(msg.selectionKeyIsDown));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
