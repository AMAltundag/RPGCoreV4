package me.blutkrone.rpgcore.hud.editor.constraint.enums;

import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.List;

public class SoundConstraint extends AbstractEnumConstraint {

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Sound");
        instruction.add("Select a sound to use.");
        instruction.add("Use 'namespace:sound' for custom sounds!");
        return instruction;
    }
    @Override
    protected Enum<?> valueOf(String string) {
        return Sound.valueOf(string.toUpperCase());
    }

    @Override
    protected Enum<?>[] values() {
        return Sound.values();
    }
}
