package me.blutkrone.rpgcore.editor.bundle.mechanic;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.editor.constraint.bundle.mono.AttributeAndModifierConstraint;
import me.blutkrone.rpgcore.editor.constraint.bundle.multi.SelectorConstraint;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.ReviveMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorReviveMechanic extends AbstractEditorMechanic {

    @EditorBundle(name = "Minimum")
    @EditorTooltip(tooltip = "Radius to revive entities in")
    public EditorModifierNumber minimum_radius = new EditorModifierNumber(0d);
    @EditorBundle(name = "Maximum")
    @EditorTooltip(tooltip = "Radius to revive entities in")
    public EditorModifierNumber maximum_radius = new EditorModifierNumber(8d);

    @EditorList(name = "Filter", constraint = SelectorConstraint.class)
    @EditorTooltip(tooltip = "Additional constraint on who we can resurrect")
    public List<IEditorBundle> filter = new ArrayList<>();

    @EditorList(name = "Attributes", constraint = AttributeAndModifierConstraint.class)
    @EditorTooltip(tooltip = "Attributes gained AFTER resurrection")
    public List<IEditorBundle> attributes = new ArrayList<>();
    @EditorBundle(name = "Duration")
    @EditorTooltip(tooltip = "Duration of attribute effects gained")
    public EditorModifierNumber duration = new EditorModifierNumber(100d);

    @EditorBundle(name = "Health")
    @EditorTooltip(tooltip = "Percentage of health to revive with")
    public EditorModifierNumber health = new EditorModifierNumber(0.2d);
    @EditorBundle(name = "Stamina")
    @EditorTooltip(tooltip = "Percentage of stamina to revive with")
    public EditorModifierNumber stamina = new EditorModifierNumber(0.2d);
    @EditorBundle(name = "Mana")
    @EditorTooltip(tooltip = "Percentage of mana to revive with")
    public EditorModifierNumber mana = new EditorModifierNumber(0.2d);

    @Override
    public AbstractCoreMechanic build() {
        return new ReviveMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.NETHER_STAR)
                .name("Revive Mechanic")
                .build();
    }

    @Override
    public String getName() {
        return "Revive";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Revive Mechanic");
        instruction.add("Resurrect players around the target location, only");
        instruction.add("Affects those with a grave out.");
        instruction.add("");
        instruction.add("§cTarget must accept the offer to be resurrected!");
        instruction.add("§cTargets dead players in a radius around target!");
        instruction.add("§cNo skill is able to target dead players!");
        return instruction;
    }
}
