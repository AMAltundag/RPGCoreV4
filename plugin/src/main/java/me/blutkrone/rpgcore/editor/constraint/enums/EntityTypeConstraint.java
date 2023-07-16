package me.blutkrone.rpgcore.editor.constraint.enums;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntityTypeConstraint extends AbstractEnumConstraint {

    @Override
    public Map<String, ItemStack> getAllForList() {
        return null;
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Entity Type");
        instruction.add("Sometimes only living entities can be processed, a Sheep or");
        instruction.add("Cow is living while an Item or Frame isn't.");
        return instruction;
    }

    @Override
    protected Enum<?> valueOf(String string) {
        return EntityType.valueOf(string.toUpperCase());
    }

    @Override
    protected Enum<?>[] values() {
        return EntityType.values();
    }
}
