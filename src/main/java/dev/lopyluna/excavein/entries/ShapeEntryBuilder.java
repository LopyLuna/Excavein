package dev.lopyluna.excavein.entries;

import net.minecraft.resources.ResourceLocation;
import dev.lopyluna.excavein.shapes.Shape;

public class ShapeEntryBuilder<T extends Shape> {
    ResourceLocation id;
    T shape;
    String lang;
    boolean keybind;
    int index;

    public ShapeEntryBuilder(ResourceLocation id, T shape, int index) {
        this.id = id;
        this.shape = shape;
        this.lang = id.toString().replace(":", ".shape.");
        this.index = index;
        this.keybind = false;
        ExcaveinEntries.sizeShape++;
    }

    public ShapeEntryBuilder<T> shape() {
        return this;
    }

    public ShapeEntryBuilder<T> lang(ResourceLocation id) {
        lang = id.toString().replace(":", ".shape.");
        return this;
    }

    public ShapeEntryBuilder<T> keybind() {
        keybind = true;
        return this;
    }

    public ShapeEntry<T> register() {
        return new ShapeEntry<>(id, shape, lang, keybind, index);
    }
}
