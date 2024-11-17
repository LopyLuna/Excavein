package uwu.lopyluna.excavein.entries;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import uwu.lopyluna.excavein.shapes.Shape;

import static uwu.lopyluna.excavein.entries.ExcaveinEntries.shapeEntries;

public class ShapeEntry<T extends Shape> {
    ResourceLocation id;
    T shape;
    String lang;
    KeyMapping keybind;
    int index;

    public ShapeEntry(ResourceLocation id, T shape, String lang, KeyMapping keybind, int index) {
        this.id = id;
        this.shape = shape;
        this.lang = lang;
        this.keybind = keybind;
        this.index = index;
        shapeEntries.put(index, this);
    }

    public KeyMapping getKeybind() {
        return keybind;
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
