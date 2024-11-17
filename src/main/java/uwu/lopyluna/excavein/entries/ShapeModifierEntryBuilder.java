package uwu.lopyluna.excavein.entries;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import uwu.lopyluna.excavein.client.ClientHandler;
import uwu.lopyluna.excavein.shape_modifiers.ShapeModifier;

public class ShapeModifierEntryBuilder<T extends ShapeModifier> {
    ResourceLocation id;
    T modifier;
    String lang;
    KeyMapping keybind;
    int index;

    public ShapeModifierEntryBuilder(ResourceLocation id, T modifier, int index) {
        this.id = id;
        this.modifier = modifier;
        this.lang = id.toString().replace(":", ".modifier.");
        this.index = index;
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
        keybind = ClientHandler.create(lang, GLFW.GLFW_KEY_UNKNOWN);
        ClientHandler.KEYBINDS.add(keybind);
        return this;
    }

    public ShapeModifierEntry<T> register() {
        return new ShapeModifierEntry<>(id, modifier, lang, keybind, index);
    }
}
