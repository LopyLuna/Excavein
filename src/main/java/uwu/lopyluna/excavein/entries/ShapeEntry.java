package uwu.lopyluna.excavein.entries;

import net.minecraft.resources.ResourceLocation;
import uwu.lopyluna.excavein.shapes.Shape;

import static uwu.lopyluna.excavein.entries.ExcaveinEntries.shapeEntries;

public class ShapeEntry<T extends Shape> {
    ResourceLocation id;
    T shape;
    String lang;
    int index;
    boolean hasKeybind;

    public ShapeEntry(ResourceLocation id, T shape, String lang, boolean keybind, int index) {
        this.id = id;
        this.shape = shape;
        this.lang = lang;
        this.index = index;
        this.hasKeybind = keybind;
        shapeEntries.put(index, this);
    }

    public boolean hasKeybind() {
        return hasKeybind;
    }

    public int getIndex() {
        return index;
    }

    public ResourceLocation getId() {
        return id;
    }

    public T getShape() {
        return shape;
    }

    public String getLang() {
        return lang;
    }
}
