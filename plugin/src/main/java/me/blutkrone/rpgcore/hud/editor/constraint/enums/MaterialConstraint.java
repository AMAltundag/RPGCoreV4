package me.blutkrone.rpgcore.hud.editor.constraint.enums;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class MaterialConstraint extends AbstractEnumConstraint {

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Material");
        instruction.add("Select a material to work with");
        return instruction;
    }

    @Override
    protected Enum<?> valueOf(String string) {
        return Material.valueOf(string.toUpperCase());
    }

    @Override
    protected Enum<?>[] values() {
        List<Enum<?>> materials = new ArrayList<>();
        for (Material material : Material.values()) {
            if (!material.isLegacy()) {
                materials.add(material);
            }
        }
        return materials.toArray(new Enum<?>[0]);
    }
}
