package dev.lopyluna.excavein.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashSet;
import java.util.Set;

public class PacketHelper {
    public static void writeSetBlockPos(Set<BlockPos> positions, FriendlyByteBuf buffer) {
        buffer.writeInt(positions.size());
        positions.forEach(buffer::writeBlockPos);
    }

    public static Set<BlockPos> readSetBlockPos(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        Set<BlockPos> blockPositions = new HashSet<>();
        for (int i = 0; i < size; i++) blockPositions.add(buffer.readBlockPos());
        return blockPositions;
    }
}
