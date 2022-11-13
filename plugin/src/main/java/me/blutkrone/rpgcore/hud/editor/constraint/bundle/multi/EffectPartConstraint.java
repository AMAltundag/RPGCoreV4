package me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi;

import me.blutkrone.rpgcore.hud.editor.IEditorConstraint;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.effect.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class EffectPartConstraint implements IEditorConstraint {

    private static Map<String, Supplier<IEditorBundle>> id_to_constructor = new HashMap<>();
    private static Map<Class, String> class_to_id = new HashMap<>();

    static {
        id_to_constructor.put("sound", EditorEffectAudio::new);
        id_to_constructor.put("particle", EditorEffectBrush::new);
        id_to_constructor.put("circle", EditorEffectCircle::new);
        id_to_constructor.put("direction", EditorEffectDirection::new);
        id_to_constructor.put("forward", EditorEffectForward::new);
        id_to_constructor.put("line", EditorEffectLine::new);
        id_to_constructor.put("model", EditorEffectModel::new);
        id_to_constructor.put("point", EditorEffectPoint::new);
        id_to_constructor.put("radiator", EditorEffectRadiator::new);
        id_to_constructor.put("rotate", EditorEffectRotate::new);
        id_to_constructor.put("rotor", EditorEffectRotor::new);
        id_to_constructor.put("sphere", EditorEffectSphere::new);
        id_to_constructor.put("wait", EditorEffectWait::new);
        id_to_constructor.put("repeat", EditorEffectRepeat::new);
        id_to_constructor.put("block", EditorEffectBlock::new);

        class_to_id.put(EditorEffectAudio.class, "sound");
        class_to_id.put(EditorEffectBrush.class, "particle");
        class_to_id.put(EditorEffectCircle.class, "circle");
        class_to_id.put(EditorEffectDirection.class, "direction");
        class_to_id.put(EditorEffectForward.class, "forward");
        class_to_id.put(EditorEffectLine.class, "line");
        class_to_id.put(EditorEffectModel.class, "model");
        class_to_id.put(EditorEffectPoint.class, "point");
        class_to_id.put(EditorEffectRadiator.class, "radiator");
        class_to_id.put(EditorEffectRotate.class, "rotate");
        class_to_id.put(EditorEffectRotor.class, "rotor");
        class_to_id.put(EditorEffectSphere.class, "sphere");
        class_to_id.put(EditorEffectWait.class, "wait");
        class_to_id.put(EditorEffectRepeat.class, "repeat");
        class_to_id.put(EditorEffectBlock.class, "block");
    }

    @Override
    public List<String> getHint(String value) {
        List<String> hints = new ArrayList<>();
        id_to_constructor.forEach((k, v) -> {
            if (k.startsWith(value.toLowerCase())) {
                hints.add(k);
            }
        });
        return hints;
    }

    @Override
    public boolean isDefined(String value) {
        return id_to_constructor.containsKey(value);
    }

    @Override
    public void extend(String value) {
        // unsupported
    }

    @Override
    public boolean canExtend() {
        return false;
    }

    @Override
    public String getConstraintAt(List container, int index) {
        Object o = container.get(index);
        String value = class_to_id.get(o.getClass());
        if (value == null) {
            throw new IllegalArgumentException("Unknown class: " + o.getClass());
        }
        return value;
    }

    @Override
    public void setElementAt(List container, int index, String value) {
        Supplier<IEditorBundle> constructor = id_to_constructor.get(value);
        if (constructor == null) {
            throw new IllegalArgumentException("Bad type " + value);
        }
        container.set(index, constructor.get());
    }

    @Override
    public void addElement(List container, String value) {
        Supplier<IEditorBundle> constructor = id_to_constructor.get(value);
        if (constructor == null) {
            throw new IllegalArgumentException("Bad type " + value);
        }
        container.add(constructor.get());
    }

    @Override
    public Object asTypeOf(String value) {
        Supplier<IEditorBundle> constructor = id_to_constructor.get(value);
        if (constructor == null) {
            throw new IllegalArgumentException("Bad type " + value);
        }
        return constructor.get();
    }

    @Override
    public String toTypeOf(Object o) {
        String value = class_to_id.get(o.getClass());
        if (value == null) {
            throw new IllegalArgumentException("Unknown class: " + o.getClass());
        }
        return value;
    }

    @Override
    public List<String> getPreview(List<Object> list) {
        List<String> preview = new ArrayList<>();

        if (list.size() <= 16) {
            for (int i = 0; i < list.size(); i++) {
                preview.add(i + ": " + toTypeOf(list.get(i)));
            }
        } else {
            for (int i = 0; i < 16; i++) {
                preview.add(i + ": " + toTypeOf(list.get(i)));
            }

            preview.add("... And " + (list.size() - 16) + " More!");
        }

        return preview;
    }

    @Override
    public String getPreview(Object object) {
        return toTypeOf(object);
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Complex Effect");
        instruction.add("Chain together multiple effect components to.");
        instruction.add("build a visual effect.");
        return instruction;
    }
}
