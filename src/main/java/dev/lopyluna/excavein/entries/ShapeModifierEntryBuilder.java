package dev.lopyluna.excavein.entries;

import net.minecraft.resources.ResourceLocation;
import dev.lopyluna.excavein.shape_modifiers.ShapeModifier;

public class ShapeModifierEntryBuilder<T extends ShapeModifier> {
    ResourceLocation id;
    T modifier;
    String lang;
    boolean keybind;
    int index;

    public ShapeModifierEntryBuilder(ResourceLocation id, T modifier, int index) {
        this.id = id;
        this.modifier = modifier;
        this.lang = id.toString().replace(":", ".modifier.");
        this.index = index;
        this.keybind = false;
        ExcaveinEntries.sizeModifier++;
    }

    public ShapeModifierEntryBuilder<T> shapeModifier() {
        return this;
    }

    public ShapeModifierEntryBuilder<T> lang(ResourceLocation id) {
        lang = id.toString().replace(":", ".modifier.");
        return this;
    }

    public ShapeModifierEntryBuilder<T> keybind() {
        keybind = true;
        return this;
    }

    public ShapeModifierEntry<T> register() {
        return new ShapeModifierEntry<>(id, modifier, lang, keybind, index);
    }
}
