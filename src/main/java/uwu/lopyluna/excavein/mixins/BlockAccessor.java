package uwu.lopyluna.excavein.mixins;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(Block.class)
public interface BlockAccessor {
    @Accessor("capturedDrops")
    List<ItemEntity> excavein$capturedDrops();

    @Invoker("beginCapturingDrops")
    void excavein$beginCapturingDrops();

    @Invoker("stopCapturingDrops")
    List<ItemEntity> excavein$stopCapturingDrops();
}
