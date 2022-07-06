package me.blutkrone.rpgcore.hud.editor.constraint;

import me.blutkrone.rpgcore.hud.editor.IEditorConstraint;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class MaterialConstraint implements IEditorConstraint {

    @Override
    public List<String> getHint(String value) {
        value = value.toUpperCase();
        List<String> matched = new ArrayList<>();
        for (Material material : Material.values()) {
            if (material.isLegacy()) {
                continue;
            }
            if (material.name().startsWith(value)) {
                matched.add(material.name());
            }
        }
        return matched;
    }

    @Override
    public boolean isDefined(String value) {
        try {
            Material.valueOf(value.toUpperCase());
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
        return ((Material) container.get(index)).name();
    }

    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, Material.valueOf(value.toUpperCase()));
    }

    @Override
    public void addElement(List container, String value) {
        container.add(Material.valueOf(value.toUpperCase()));
    }

    @Override
    public Object asTypeOf(String value) {
        return Material.valueOf(value.toUpperCase());
    }

    @Override
    public String toTypeOf(Object value) {
        return ((Material) value).name();
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
        instruction.add("§fMaterial");
        instruction.add("Select a material to work with");
        return instruction;
    }

    @Override
    public String getPreview(Object object) {
        return String.valueOf(object);
    }
}
