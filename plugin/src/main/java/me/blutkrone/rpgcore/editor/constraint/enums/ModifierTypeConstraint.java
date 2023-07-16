package me.blutkrone.rpgcore.editor.constraint.enums;

import me.blutkrone.rpgcore.item.modifier.ModifierType;

import java.util.ArrayList;
import java.util.List;

public class ModifierTypeConstraint extends AbstractEnumConstraint {

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Modifier Type");
        instruction.add("Entity: Affects entity, while modifier is active.");
        instruction.add("Consume: Affects entity, after being consumed.");
        instruction.add("Item: Affects the item itself.");
        return instruction;
    }

    @Override
    protected Enum<?> valueOf(String string) {
        return ModifierType.valueOf(string.toUpperCase());
    }

    @Override
    protected Enum<?>[] values() {
        return ModifierType.values();
    }
}
