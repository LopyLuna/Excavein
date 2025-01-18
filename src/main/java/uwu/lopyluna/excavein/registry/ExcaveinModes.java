package uwu.lopyluna.excavein.registry;

import uwu.lopyluna.excavein.entries.ExcaveinEntries;
import uwu.lopyluna.excavein.entries.ShapeEntry;
import uwu.lopyluna.excavein.entries.ShapeModifierEntry;
import uwu.lopyluna.excavein.shape_modifiers.*;
import uwu.lopyluna.excavein.shapes.*;
import uwu.lopyluna.excavein.utils.Utils;

@SuppressWarnings("unused")
public class ExcaveinModes {

    //SHAPES
    public static ShapeEntry<ShapelessShape> SHAPELESS = ExcaveinEntries.registerShape(new ShapelessShape(Utils.asResource("shapeless")))
            .keybind()
            .register();
    public static ShapeEntry<TunnelShape> TUNNEL = ExcaveinEntries.registerShape(new TunnelShape(Utils.asResource("tunnel")))
            .keybind()
            .register();
    public static ShapeEntry<LargeTunnelShape> LARGE_TUNNEL = ExcaveinEntries.registerShape(new LargeTunnelShape(Utils.asResource("large_tunnel")))
            .keybind()
            .register();
    public static ShapeEntry<DiagonalTunnelShape> DIAGONAL_TUNNEL = ExcaveinEntries.registerShape(new DiagonalTunnelShape(Utils.asResource("diagonal_tunnel")))
            .keybind()
            .register();
    public static ShapeEntry<BoreShape> BORE = ExcaveinEntries.registerShape(new BoreShape(Utils.asResource("bore")))
            .keybind()
            .register();
    public static ShapeEntry<ExtendedShapelessShape> EXTENDED_SHAPELESS = ExcaveinEntries.registerShape(new ExtendedShapelessShape(Utils.asResource("extended_shapeless")))
            .keybind()
            .register();

    //MODIFIERS
    public static ShapeModifierEntry<SelectionShapeModifier> SELECTION = ExcaveinEntries.registerShapeModifier(new SelectionShapeModifier(Utils.asResource("selection")))
            .keybind()
            .register();
    public static ShapeModifierEntry<VeinShapeModifier> VEIN = ExcaveinEntries.registerShapeModifier(new VeinShapeModifier(Utils.asResource("vein")))
            .keybind()
            .register();
    public static ShapeModifierEntry<SideShapeModifier> SIDE = ExcaveinEntries.registerShapeModifier(new SideShapeModifier(Utils.asResource("side")))
            .keybind()
            .register();
    public static ShapeModifierEntry<SurfaceShapeModifier> SURFACE = ExcaveinEntries.registerShapeModifier(new SurfaceShapeModifier(Utils.asResource("surface")))
            .keybind()
            .register();
    public static ShapeModifierEntry<NoneShapeModifier> NONE = ExcaveinEntries.registerShapeModifier(new NoneShapeModifier(Utils.asResource("none")))
            .keybind()
            .register();

    public static void register() {
    }
}
