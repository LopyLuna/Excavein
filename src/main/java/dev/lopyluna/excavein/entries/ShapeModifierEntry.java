package dev.lopyluna.excavein.entries;

import net.minecraft.resources.ResourceLocation;
import dev.lopyluna.excavein.shape_modifiers.ShapeModifier;

import static dev.lopyluna.excavein.entries.ExcaveinEntries.shapeModifierEntries;

public class ShapeModifierEntry<T extends ShapeModifier> {
    ResourceLocation id;
    T modifier;
    String lang;
    int index;
    boolean hasKeybind;

    public ShapeModifierEntry(ResourceLocation id, T modifier, String lang, boolean keybind, int index) {
        this.id = id;
        this.modifier = modifier;
        this.lang = lang;
        this.hasKeybind = keybind;
        shapeModifierEntries.put(index, this);
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

    public T getShapeModifier() {
        return modifier;
    }

    public String getLang() {
        return lang;
    }
}
