package uwu.lopyluna.excavein.entries;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import uwu.lopyluna.excavein.shape_modifiers.ShapeModifier;

import static uwu.lopyluna.excavein.entries.ExcaveinEntries.shapeModifierEntries;

public class ShapeModifierEntry<T extends ShapeModifier> {
    ResourceLocation id;
    T modifier;
    String lang;
    KeyMapping keybind;
    int index;

    public ShapeModifierEntry(ResourceLocation id, T modifier, String lang, KeyMapping keybind, int index) {
        this.id = id;
        this.modifier = modifier;
        this.lang = lang;
        this.keybind = keybind;
        shapeModifierEntries.put(index, this);
    }

    public int getIndex() {
        return index;
    }

    public KeyMapping getKeybind() {
        return keybind;
    }

    public ResourceLocation getId() {
        return id;
    }

    public T getShapeModifier() {
        return modifier;
    }

    public String getLang() {
        return lang;
    }
}
