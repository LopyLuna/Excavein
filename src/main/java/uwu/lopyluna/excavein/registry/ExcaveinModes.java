package uwu.lopyluna.excavein.registry;

import uwu.lopyluna.excavein.entries.ExcaveinEntries;
import uwu.lopyluna.excavein.entries.ShapeEntry;
import uwu.lopyluna.excavein.entries.ShapeModifierEntry;
import uwu.lopyluna.excavein.shape_modifiers.NoneShapeModifier;
import uwu.lopyluna.excavein.shape_modifiers.SideShapeModifier;
import uwu.lopyluna.excavein.shape_modifiers.SurfaceShapeModifier;
import uwu.lopyluna.excavein.shapes.*;
import uwu.lopyluna.excavein.utils.Utils;

@SuppressWarnings("unused")
public class ExcaveinModes {

    //SHAPES
    public static ShapeEntry<SelectionShape> SELECTION = ExcaveinEntries.registerShape(new SelectionShape(Utils.asResource("selection")))
            .keybind()
            .register();
    public static ShapeEntry<VeinShape> VEIN = ExcaveinEntries.registerShape(new VeinShape(Utils.asResource("vein")))
            .keybind()
            .register();
    public static ShapeEntry<ExcavateShape> EXCAVATE = ExcaveinEntries.registerShape(new ExcavateShape(Utils.asResource("excavate")))
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

    //MODIFIERS
    public static ShapeModifierEntry<NoneShapeModifier> NONE = ExcaveinEntries.registerShapeModifier(new NoneShapeModifier(Utils.asResource("none")))
            .keybind()
            .register();
    public static ShapeModifierEntry<SideShapeModifier> SIDE = ExcaveinEntries.registerShapeModifier(new SideShapeModifier(Utils.asResource("side")))
            .keybind()
            .register();
    public static ShapeModifierEntry<SurfaceShapeModifier> SURFACE = ExcaveinEntries.registerShapeModifier(new SurfaceShapeModifier(Utils.asResource("surface")))
            .keybind()
            .register();

    public static void register() {
    }
}
