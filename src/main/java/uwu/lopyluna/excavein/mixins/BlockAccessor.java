package uwu.lopyluna.excavein.mixins;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Block.class)
public interface BlockAccessor {
    @Accessor("capturedDrops")
    static List<ItemEntity> excavein$capturedDrops() {
        throw new AssertionError();
    }

    @Accessor("capturedDrops")
    static void excavein$capturedDrops(List<ItemEntity> set) {
        throw new AssertionError();
    }
}
