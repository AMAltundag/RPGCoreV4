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
        id_to_constructor.put("sound", EditorAudio::new);
        id_to_constructor.put("particle", EditorParticleBrush::new);
        id_to_constructor.put("point", EditorParticlePoint::new);
        id_to_constructor.put("wait", EditorWait::new);
        id_to_constructor.put("sphere", EditorParticleSphere::new);

        class_to_id.put(EditorAudio.class, "sound");
        class_to_id.put(EditorParticleBrush.class, "particle");
        class_to_id.put(EditorParticlePoint.class, "point");
        class_to_id.put(EditorWait.class, "wait");
        class_to_id.put(EditorParticleSphere.class, "sphere");
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
