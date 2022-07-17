package me.blutkrone.rpgcore.hud.editor.constraint.enums;

import me.blutkrone.rpgcore.hud.editor.IEditorConstraint;
import me.blutkrone.rpgcore.item.type.ItemType;

import java.util.ArrayList;
import java.util.List;

public class ItemTypeConstraint implements IEditorConstraint {

    @Override
    public List<String> getHint(String value) {
        if (value.isBlank()) {

        }
        value = value.toUpperCase();
        List<String> matched = new ArrayList<>();
        for (ItemType type : ItemType.values()) {
            if (type.name().startsWith(value)) {
                matched.add(type.name());
            }
        }
        return matched;
    }

    @Override
    public boolean isDefined(String value) {
        try {
            ItemType.valueOf(value.toUpperCase());
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
        return ((ItemType) container.get(index)).name();
    }

    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, ItemType.valueOf(value));
    }

    @Override
    public void addElement(List container, String value) {
        container.add(ItemType.valueOf(value));
    }

    @Override
    public Object asTypeOf(String value) {
        return ItemType.valueOf(value);
    }

    @Override
    public String toTypeOf(Object value) {
        return ((ItemType) value).name();
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
        instruction.add("Item Type");
        instruction.add("Heavy: Right click for heavy strike");
        instruction.add("Throw: Throw a copy of the weapon");
        instruction.add("Dual: Clones item into off-hand (2H forced)");
        instruction.add("Magic: Magic wand to shoot projectiles");
        instruction.add("Bomb: Throwable item that uses a skill");
        instruction.add("Consume: Right click on hotbar to consume");
        instruction.add("Quiver: Regenerating arrow item");
        instruction.add("Shield: Offhand to block damage");
        instruction.add("None: Applies no special logic");
        return instruction;
    }

    @Override
    public String getPreview(Object object) {
        return String.valueOf(object);
    }
}
