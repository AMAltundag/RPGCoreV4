package me.blutkrone.rpgcore.hud.editor.constraint;

import me.blutkrone.rpgcore.hud.editor.IEditorConstraint;
import me.blutkrone.rpgcore.item.modifier.ModifierStyle;

import java.util.ArrayList;
import java.util.List;

public class ModifierStyleConstraint implements IEditorConstraint {

    @Override
    public List<String> getHint(String value) {
        value = value.toUpperCase();
        List<String> matched = new ArrayList<>();
        for (ModifierStyle modifier : ModifierStyle.values()) {
            if (modifier.name().startsWith(value)) {
                matched.add(modifier.name());
            }
        }
        return matched;
    }

    @Override
    public boolean isDefined(String value) {
        try {
            ModifierStyle.valueOf(value.toUpperCase());
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
        return ((ModifierStyle) container.get(index)).name();
    }

    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, ModifierStyle.valueOf(value));
    }

    @Override
    public void addElement(List container, String value) {
        container.add(ModifierStyle.valueOf(value));
    }

    @Override
    public Object asTypeOf(String value) {
        return ModifierStyle.valueOf(value);
    }

    @Override
    public String toTypeOf(Object value) {
        return ((ModifierStyle) value).name();
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

            preview.add("... And " + (list.size()-16) + " More!");
        }

        return preview;
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§fModifier Style");
        instruction.add("Generic Format: Description#Value");
        instruction.add("Ability Format: Icon#Upper#Left#Right");
        return instruction;
    }

    @Override
    public String getPreview(Object object) {
        return String.valueOf(object);
    }
}
