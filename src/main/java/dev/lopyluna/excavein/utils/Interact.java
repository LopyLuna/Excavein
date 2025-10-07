package dev.lopyluna.excavein.utils;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class Interact {
    public ServerPlayer pPlayer;
    public Level pLevel;
    public ItemStack pStack;
    public InteractionHand pHand;
    public BlockHitResult pHitResult;

    public Interact(ServerPlayer pPlayer, Level pLevel, ItemStack pStack, InteractionHand pHand, BlockHitResult pHitResult) {
        this.pPlayer = pPlayer;
        this.pLevel = pLevel;
        this.pStack = pStack;
        this.pHand = pHand;
        this.pHitResult = pHitResult;
    }
}
