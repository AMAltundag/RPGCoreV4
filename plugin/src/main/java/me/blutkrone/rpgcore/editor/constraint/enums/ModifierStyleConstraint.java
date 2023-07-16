package me.blutkrone.rpgcore.editor.constraint.enums;

import me.blutkrone.rpgcore.item.modifier.ModifierStyle;

import java.util.ArrayList;
import java.util.List;

public class ModifierStyleConstraint extends AbstractEnumConstraint {

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Modifier Style");
        instruction.add("Generic Format: Description#Value");
        instruction.add("Ability Format: Icon#Upper#Left#Right");
        return instruction;
    }

    @Override
    protected Enum<?> valueOf(String string) {
        return ModifierStyle.valueOf(string.toUpperCase());
    }

    @Override
    protected Enum<?>[] values() {
        return ModifierStyle.values();
    }
}
