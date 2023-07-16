package me.blutkrone.rpgcore.editor.constraint.reference.other;

import me.blutkrone.rpgcore.editor.IEditorConstraint;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SoundConstraint implements IEditorConstraint {

    private Set<String> sounds = new HashSet<>();

    public SoundConstraint() {
        for (Sound sound : Sound.values()) {
            sounds.add(sound.getKey().getKey());
            sounds.add("minecraft:" + sound.getKey().getKey());
        }
    }

    @Override
    public Map<String, ItemStack> getAllForList() {
        return null;
    }

    @Override
    public List<String> getHint(String value) {
        value = value.toLowerCase();
        List<String> matched = new ArrayList<>();
        for (String sound : sounds) {
            if (sound.startsWith(value.toLowerCase())) {
                matched.add(sound);
            }
        }
        return matched;
    }

    @Override
    public boolean isDefined(String value) {
        try {
            return sounds.contains(value.toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void extend(String value) {
        // extend doesn't actually do anything
    }

    @Override
    public boolean canExtend() {
        return true;
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
        instruction.add("Sound");
        instruction.add("Select a sound to use.");
        instruction.add("Use 'namespace:sound' for custom sounds!");
        return instruction;
    }


    @Override
    public String getPreview(Object object) {
        return String.valueOf(object);
    }
}
