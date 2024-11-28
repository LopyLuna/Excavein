package uwu.lopyluna.excavein.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import uwu.lopyluna.excavein.client.BlockOutlineRenderer;

import java.util.Set;
import java.util.function.Supplier;

import static uwu.lopyluna.excavein.packets.PacketHelper.readSetBlockPos;
import static uwu.lopyluna.excavein.packets.PacketHelper.writeSetBlockPos;

public record SelectedBlocksPacket(Set<BlockPos> breaking, Set<BlockPos> interaction) {
    public static void encode(SelectedBlocksPacket msg, FriendlyByteBuf buffer) {
        writeSetBlockPos(msg.breaking, buffer);
        writeSetBlockPos(msg.interaction, buffer);
    }
    public static SelectedBlocksPacket decode(FriendlyByteBuf buffer) {
        return new SelectedBlocksPacket(readSetBlockPos(buffer), readSetBlockPos(buffer));
    }
    public static void handle(SelectedBlocksPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> BlockOutlineRenderer.updateBlocks(msg.breaking, msg.interaction));
        context.get().setPacketHandled(true);
    }
}
