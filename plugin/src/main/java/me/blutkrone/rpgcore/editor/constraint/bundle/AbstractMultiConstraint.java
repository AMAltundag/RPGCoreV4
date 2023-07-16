package me.blutkrone.rpgcore.editor.constraint.bundle;

import me.blutkrone.rpgcore.editor.IEditorConstraint;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public abstract class AbstractMultiConstraint implements IEditorConstraint {

    // private static Map<String, Supplier<IEditorBundle>> id_to_constructor = new HashMap<>();
    // private static Map<Class, String> class_to_id = new HashMap<>();
    // static {
    //     id_to_constructor.put("exp", EditorQuestRewardExp::new);
    //     class_to_id.put(EditorQuestRewardTrait.class, "trait");
    // }

    protected abstract Map<String, Supplier<IEditorBundle>> getIdToConstructor();

    protected abstract Map<Class, String> getClassToId();

    @Override
    public List<String> getHint(String value) {
        List<String> hints = new ArrayList<>();
        getIdToConstructor().forEach((k, v) -> {
            if (k.startsWith(value.toLowerCase())) {
                hints.add(k);
            }
        });
        return hints;
    }

    @Override
    public boolean isDefined(String value) {
        return getIdToConstructor().containsKey(value.toLowerCase());
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
        String value = getClassToId().get(o.getClass());
        if (value == null) {
            throw new IllegalArgumentException("Unknown class: " + o.getClass());
        }
        return value.toLowerCase();
    }

    @Override
    public void setElementAt(List container, int index, String value) {
        Supplier<IEditorBundle> constructor = getIdToConstructor().get(value.toLowerCase());
        if (constructor == null) {
            throw new IllegalArgumentException("Bad type " + value);
        }
        container.set(index, constructor.get());
    }

    @Override
    public void addElement(List container, String value) {
        Supplier<IEditorBundle> constructor = getIdToConstructor().get(value.toLowerCase());
        if (constructor == null) {
            throw new IllegalArgumentException("Bad type " + value);
        }
        container.add(constructor.get());
    }

    @Override
    public Object asTypeOf(String value) {
        Supplier<IEditorBundle> constructor = getIdToConstructor().get(value.toLowerCase());
        if (constructor == null) {
            throw new IllegalArgumentException("Bad type " + value);
        }
        return constructor.get();
    }

    @Override
    public String toTypeOf(Object o) {
        String value = getClassToId().get(o.getClass());
        if (value == null) {
            throw new IllegalArgumentException("Unknown class: " + o.getClass());
        }
        return value.toLowerCase();
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
}
