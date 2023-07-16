package me.blutkrone.rpgcore.editor.constraint.texture;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.IEditorConstraint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JewelTextureConstraint implements IEditorConstraint {

    private Set<String> candidates = new HashSet<>();

    @Override
    public List<String> getHint(String value) {
        if (this.candidates.isEmpty()) {
            RPGCore.inst().getResourcePackManager().textures().forEach((id, tex) -> {
                if (id.startsWith("lore_jewel_")) {
                    this.candidates.add(id.replace("lore_jewel_", ""));
                }
            });
        }

        value = value.toLowerCase();
        List<String> matched = new ArrayList<>();
        for (String candidate : this.candidates) {
            if (candidate.startsWith(value)) {
                matched.add(candidate);
            }
        }
        return matched;
    }

    @Override
    public boolean isDefined(String value) {
        value = value.toLowerCase();

        try {
            return this.candidates.contains(value);
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
        instruction.add("Jewel Texture");
        instruction.add("Texture used for item lore.");
        return instruction;
    }

    @Override
    public String getPreview(Object object) {
        return String.valueOf(object);
    }
}
