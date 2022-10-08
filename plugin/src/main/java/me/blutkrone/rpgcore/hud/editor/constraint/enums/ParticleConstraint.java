package me.blutkrone.rpgcore.hud.editor.constraint.enums;

import org.bukkit.Particle;

import java.util.ArrayList;
import java.util.List;

public class ParticleConstraint extends AbstractEnumConstraint {

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
