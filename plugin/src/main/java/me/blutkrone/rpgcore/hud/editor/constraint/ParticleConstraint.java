package me.blutkrone.rpgcore.hud.editor.constraint;

import me.blutkrone.rpgcore.hud.editor.IEditorConstraint;
import org.bukkit.Particle;

import java.util.ArrayList;
import java.util.List;

public class ParticleConstraint implements IEditorConstraint {

    @Override
    public List<String> getHint(String value) {
        value = value.toUpperCase();
        List<String> matched = new ArrayList<>();
        for (Particle particle : Particle.values()) {
            if (particle.name().startsWith(value)) {
                matched.add(particle.name());
            }
        }
        return matched;
    }

    @Override
    public boolean isDefined(String value) {
        try {
            Particle.valueOf(value.toUpperCase());
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
        return ((Particle) container.get(index)).name();
    }

    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, Particle.valueOf(value));
    }

    @Override
    public void addElement(List container, String value) {
        container.add(Particle.valueOf(value));
    }

    @Override
    public Object asTypeOf(String value) {
        return Particle.valueOf(value);
    }

    @Override
    public String toTypeOf(Object value) {
        return ((Particle) value).name();
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

            preview.add("... And " + (list.size()-16) + " More!");
        }

        return preview;
    }

    @Override
    public String getPreview(Object object) {
        return String.valueOf(object);
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§fParticle");
        instruction.add("§fSelect a particle to use");
        return instruction;
    }

}
