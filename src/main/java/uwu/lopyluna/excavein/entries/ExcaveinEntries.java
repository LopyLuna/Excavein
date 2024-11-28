package uwu.lopyluna.excavein.entries;

import net.minecraft.resources.ResourceLocation;
import uwu.lopyluna.excavein.shape_modifiers.ShapeModifier;
import uwu.lopyluna.excavein.shapes.Shape;

import java.util.HashMap;
import java.util.Map;

public class ExcaveinEntries {
    protected static final Map<Integer, ShapeEntry<? extends Shape>> shapeEntries = new HashMap<>();
    protected static final Map<Integer, ShapeModifierEntry<? extends ShapeModifier>> shapeModifierEntries = new HashMap<>();

    private static final Map<ResourceLocation, Shape> shapes = new HashMap<>();
    private static final Map<ResourceLocation, ShapeModifier> shapeModifiers = new HashMap<>();
    private static final Map<Integer, ResourceLocation> shapeValue = new HashMap<>();
    private static final Map<Integer, ResourceLocation> modifierValue = new HashMap<>();
    private static final Map<ResourceLocation, Integer> shapeLoc = new HashMap<>();
    private static final Map<ResourceLocation, Integer> modifierLoc = new HashMap<>();
    public static int sizeShape = 0;
    public static int sizeModifier = 0;

    public static <T extends Shape> ShapeEntryBuilder<T> registerShape(T shape) {
        if (shape == null)
            throw new IllegalStateException("Registry Shape is null");
        if (shapes.containsKey(shape.getId()))
            throw new IllegalStateException(shape.getId() + " is duplicate");
        shapes.put(shape.getId(), shape);
        shapeValue.put(sizeShape, shape.getId());
        shapeLoc.put(shape.getId(), sizeShape);
        return new ShapeEntryBuilder<>(shape.getId(), shape, sizeShape);
    }

    public static <T extends ShapeModifier> ShapeModifierEntryBuilder<T> registerShapeModifier(T shapeModifier) {
        if (shapeModifier == null)
            throw new IllegalStateException("Registry Shape Modifier is null");
        if (shapeModifiers.containsKey(shapeModifier.getId()))
            throw new IllegalStateException(shapeModifier.getId() + " is duplicate");
        shapeModifiers.put(shapeModifier.getId(), shapeModifier);
        modifierValue.put(sizeModifier, shapeModifier.getId());
        modifierLoc.put(shapeModifier.getId(), sizeModifier);
        return new ShapeModifierEntryBuilder<>(shapeModifier.getId(), shapeModifier, sizeModifier);
    }


    //VARIABLES

    public static Map<Integer, ShapeEntry<? extends Shape>> getShapeEntries() {
        return shapeEntries;
    }

    public static Map<Integer, ShapeModifierEntry<? extends ShapeModifier>> getShapeModifierEntries() {
        return shapeModifierEntries;
    }

    public static Map<Integer, ResourceLocation> getShapeValue() {
        return shapeValue;
    }

    public static Map<Integer, ResourceLocation> getModifierValue() {
        return modifierValue;
    }

    public static Map<ResourceLocation, Integer> getShapeLoc() {
        return shapeLoc;
    }

    public static Map<ResourceLocation, Integer> getModifierLoc() {
        return modifierLoc;
    }

    public static Map<ResourceLocation, Shape> getShapes() {
        return shapes;
    }

    public static Map<ResourceLocation, ShapeModifier> getShapeModifiers() {
        return shapeModifiers;
    }
}
