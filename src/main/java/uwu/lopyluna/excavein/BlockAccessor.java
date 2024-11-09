package uwu.lopyluna.excavein;

import net.minecraft.world.entity.item.ItemEntity;

import java.util.List;

public interface BlockAccessor {
    void excavein$capturedDrops(List<ItemEntity> drops);
    List<ItemEntity> excavein$capturedDrops();
}
