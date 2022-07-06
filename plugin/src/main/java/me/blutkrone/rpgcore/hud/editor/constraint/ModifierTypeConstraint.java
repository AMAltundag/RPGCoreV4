package me.blutkrone.rpgcore.hud.editor.constraint;

import me.blutkrone.rpgcore.hud.editor.IEditorConstraint;
import me.blutkrone.rpgcore.item.modifier.ModifierType;

import java.util.ArrayList;
import java.util.List;

public class ModifierTypeConstraint implements IEditorConstraint {

    @Override
    public List<String> getHint(String value) {
        value = value.toUpperCase();
        List<String> matched = new ArrayList<>();
        for (ModifierType modifier : ModifierType.values()) {
            if (modifier.name().startsWith(value)) {
                matched.add(modifier.name());
            }
        }
        return matched;
    }

    @Override
    public boolean isDefined(String value) {
        try {
            ModifierType.valueOf(value.toUpperCase());
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
        return ((ModifierType) container.get(index)).name();
    }

    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, ModifierType.valueOf(value));
    }

    @Override
    public void addElement(List container, String value) {
        container.add(ModifierType.valueOf(value));
    }

    @Override
    public Object asTypeOf(String value) {
        return ModifierType.valueOf(value);
    }

    @Override
    public String toTypeOf(Object value) {
        return ((ModifierType) value).name();
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
        instruction.add("§fModifier Type");
        instruction.add("Entity: Affects entity, while modifier is active.");
        instruction.add("Consume: Affects entity, after being consumed.");
        instruction.add("Item: Affects the item itself.");
        return instruction;
    }

    @Override
    public String getPreview(Object object) {
        return String.valueOf(object);
    }
}
