package me.blutkrone.rpgcore.hud.editor.constraint.enums;

import me.blutkrone.rpgcore.item.type.ItemType;

import java.util.ArrayList;
import java.util.List;

public class ItemTypeConstraint extends AbstractEnumConstraint {

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
    protected Enum<?> valueOf(String string) {
        return ItemType.valueOf(string.toUpperCase());
    }

    @Override
    protected Enum<?>[] values() {
        return ItemType.values();
    }
}
