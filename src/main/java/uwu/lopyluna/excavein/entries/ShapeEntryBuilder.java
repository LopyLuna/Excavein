package uwu.lopyluna.excavein.entries;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import uwu.lopyluna.excavein.client.ClientHandler;
import uwu.lopyluna.excavein.shapes.Shape;

public class ShapeEntryBuilder<T extends Shape> {
    ResourceLocation id;
    T shape;
    String lang;
    KeyMapping keybind;
    int index;

    public ShapeEntryBuilder(ResourceLocation id, T shape, int index) {
        this.id = id;
        this.shape = shape;
        this.lang = id.toString().replace(":", ".shape.");
        this.index = index;
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
        keybind = ClientHandler.create(lang, GLFW.GLFW_KEY_UNKNOWN);
        ClientHandler.KEYBINDS.add(keybind);
        return this;
    }

    public ShapeEntry<T> register() {
        return new ShapeEntry<>(id, shape, lang, keybind, index);
    }
}
