package me.blutkrone.rpgcore.editor.constraint.enums;

import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParticleConstraint extends AbstractEnumConstraint {
    @Override
    public Map<String, ItemStack> getAllForList() {
        return null;
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Particle");
        instruction.add("Select a particle to use");
        return instruction;
    }

    @Override
    protected Enum<?> valueOf(String string) {
        return Particle.valueOf(string.toUpperCase());
    }

    @Override
    protected Enum<?>[] values() {
        return Particle.values();
    }
}
