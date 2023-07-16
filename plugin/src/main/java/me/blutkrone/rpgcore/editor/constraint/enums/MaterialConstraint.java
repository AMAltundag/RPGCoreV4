package me.blutkrone.rpgcore.editor.constraint.enums;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MaterialConstraint extends AbstractEnumConstraint {

    @Override
    public Map<String, ItemStack> getAllForList() {
        return null;
    }

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
