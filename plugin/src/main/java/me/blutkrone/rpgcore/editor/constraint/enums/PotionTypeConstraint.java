package me.blutkrone.rpgcore.editor.constraint.enums;

import me.blutkrone.rpgcore.editor.IEditorConstraint;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class PotionTypeConstraint implements IEditorConstraint {

    @Override
    public List<String> getHint(String value) {
        value = value.toUpperCase();
        List<String> matched = new ArrayList<>();
        for (PotionEffectType type : PotionEffectType.values()) {
            if (type.getName().startsWith(value)) {
                matched.add(type.getName());
            }
        }
        return matched;
    }

    @Override
    public boolean isDefined(String value) {
        try {
            PotionEffectType.getByName(value.toUpperCase());
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
        return (String) container.get(index);
    }

    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, value);
    }

    @Override
    public void addElement(List container, String value) {
        container.add(value);
    }

    @Override
    public Object asTypeOf(String value) {
        return value;
    }

    @Override
    public String toTypeOf(Object value) {
        return (String) value;
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

    @Override
    public String getPreview(Object object) {
        return String.valueOf(object);
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Potion Effect Type");
        instruction.add("Select a potion effect type to use");
        return instruction;
    }
}
