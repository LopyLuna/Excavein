package dev.lopyluna.excavein.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import dev.lopyluna.excavein.client.BlockOutlineRenderer;
import dev.lopyluna.excavein.utils.Utils;

import java.util.List;
import java.util.Set;

public record SelectedBlocksPacket(Set<BlockPos> breaking, Set<BlockPos> interaction) implements CustomPacketPayload {

    public static final Type<SelectedBlocksPacket> TYPE = new Type<>(Utils.asResource("block_selection"));
    public static final StreamCodec<FriendlyByteBuf, SelectedBlocksPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()).map(Set::copyOf, List::copyOf), SelectedBlocksPacket::breaking,
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()).map(Set::copyOf, List::copyOf), SelectedBlocksPacket::interaction,
            SelectedBlocksPacket::new
    );

    public static void handle(SelectedBlocksPacket msg, IPayloadContext context) {
        context.enqueueWork(() -> BlockOutlineRenderer.updateBlocks(msg.breaking, msg.interaction));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
