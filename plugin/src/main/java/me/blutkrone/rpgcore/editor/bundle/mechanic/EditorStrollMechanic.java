package me.blutkrone.rpgcore.editor.bundle.mechanic;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.StrollMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorStrollMechanic extends AbstractEditorMechanic {

    @EditorBundle(name = "Minimum")
    @EditorTooltip(tooltip = {"Minimum distance to stroll towards"})
    public EditorModifierNumber minimum = new EditorModifierNumber(7);
    @EditorBundle(name = "Horizontal")
    @EditorTooltip(tooltip = {"Maximum distance to stroll towards"})
    public EditorModifierNumber maximum = new EditorModifierNumber(15);
    @EditorBundle(name = "Speed")
    @EditorTooltip(tooltip = {"Speed to stroll around with"})
    public EditorModifierNumber speed = new EditorModifierNumber(0.8d);

    @Override
    public AbstractCoreMechanic build() {
        return new StrollMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.ELYTRA)
                .name("Stroll Mechanic")
                .build();
    }

    @Override
    public String getName() {
        return "Stroll";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Stroll Mechanic");
        instruction.add("Strolling does not happen while engaged or walking.");
        instruction.add("Ignores targets, always applies on context holder.");
        return instruction;
    }
}
