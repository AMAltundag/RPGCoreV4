package me.blutkrone.rpgcore.editor.constraint.enums;

import me.blutkrone.rpgcore.editor.IEditorConstraint;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractEnumConstraint implements IEditorConstraint {

    @Override
    public List<String> getHint(String value) {
        value = value.toUpperCase();
        List<String> matched = new ArrayList<>();
        for (Enum<?> type : this.values()) {
            if (type.name().startsWith(value)) {
                matched.add(type.name());
            }
        }
        return matched;
    }

    @Override
    public boolean isDefined(String value) {
        try {
            this.valueOf(value.toUpperCase());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void extend(String value) {
        // unsupported
    }

    @Override
    public boolean canExtend() {
        return false; // unsupported
    }

    @Override
    public String getConstraintAt(List container, int index) {
        return ((Enum<?>) container.get(index)).name();
    }

    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, this.valueOf(value));
    }

    @Override
    public void addElement(List container, String value) {
        container.add(this.valueOf(value));
    }

    @Override
    public Object asTypeOf(String value) {
        return this.valueOf(value);
    }

    @Override
    public String toTypeOf(Object value) {
        return ((Enum<?>) value).name();
    }

    @Override
    public String getPreview(Object object) {
        return String.valueOf(object);
    }

    @Override
    public List<String> getPreview(List<Object> list) {
        List<String> preview = new ArrayList<>();

        if (list.size() <= 16) {
            for (int i = 0; i < list.size(); i++) {
                preview.add(i + ": " + list.get(i));
            }
        } else {
            for (int i = 0; i < 16; i++) {
                preview.add(i + ": " + list.get(i));
            }

            preview.add("... And " + (list.size() - 16) + " More!");
        }

        return preview;
    }

    protected abstract Enum<?> valueOf(String string);

    protected abstract Enum<?>[] values();
}
