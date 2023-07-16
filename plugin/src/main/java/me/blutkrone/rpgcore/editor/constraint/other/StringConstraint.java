package me.blutkrone.rpgcore.editor.constraint.other;

import me.blutkrone.rpgcore.editor.IEditorConstraint;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StringConstraint implements IEditorConstraint {

    @Override
    public Map<String, ItemStack> getAllForList() {
        return null;
    }

    @Override
    public List<String> getHint(String value) {
        return new ArrayList<>();
    }

    @Override
    public boolean isDefined(String value) {
        return true;
    }

    @Override
    public void extend(String value) {
        // why extend string values?
    }

    @Override
    public boolean canExtend() {
        return true;
    }

    @Override
    public String getConstraintAt(List container, int index) {
        return ((String) container.get(index));
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
        return ((String) value);
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
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("String");
        instruction.add("Text without any requirement.");
        return instruction;
    }

    @Override
    public String getPreview(Object object) {
        return String.valueOf(object);
    }
}
