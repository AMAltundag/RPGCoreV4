package me.blutkrone.rpgcore.editor.bundle.mechanic;

import me.blutkrone.rpgcore.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierString;
import me.blutkrone.rpgcore.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.editor.constraint.reference.index.SkillConstraint;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.TriggerMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditorTriggerMechanic extends AbstractEditorMechanic {

    @EditorList(name = "Manual", constraint = SkillConstraint.class)
    @EditorTooltip(tooltip = {"Passive tree provides linked skills", "These add to the additional linkable skills"})
    public List<String> manual_options = new ArrayList<>();
    @EditorList(name = "Manual", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Filters skills by their relevant tags", "Leave empty to ignore filter"})
    public List<String> skill_filter = new ArrayList<>();
    @EditorBundle(name = "Chance")
    @EditorTooltip(tooltip = "Chance to trigger")
    public EditorModifierNumber chance = new EditorModifierNumber(1.0d);
    @EditorBundle(name = "Multi")
    @EditorTooltip(tooltip = "If multiple targets exists, limit to this.")
    public EditorModifierNumber multi = new EditorModifierNumber(1.0d);

    @EditorCategory(icon = Material.CLOCK, info = "Cooldown")
    @EditorBundle(name = "Reduction")
    @EditorTooltip(tooltip = "Percentage removed from cooldown")
    public EditorModifierNumber cooldown_reduction = new EditorModifierNumber(0.0d);
    @EditorBundle(name = "Duration")
    @EditorTooltip(tooltip = "How many ticks of a cooldown apply")
    public EditorModifierNumber cooldown_time = new EditorModifierNumber(0.0d);
    @EditorBundle(name = "Recovery")
    @EditorTooltip(tooltip = "Divide cooldown by recovery rate")
    public EditorModifierNumber cooldown_recovery = new EditorModifierNumber(0.0d);
    @EditorBundle(name = "Identifier")
    @EditorTooltip(tooltip = "Identifier of the cooldown, this is a GLOBAL cooldown!")
    public EditorModifierString cooldown_id = new EditorModifierString(UUID.randomUUID().toString());

    @Override
    public AbstractCoreMechanic build() {
        return new TriggerMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.ELYTRA)
                .name("Trigger Mechanic")
                .build();
    }

    @Override
    public String getName() {
        return "Trigger";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Trigger Mechanic");
        instruction.add("Invokes another skill, any skill socketed into the passive");
        instruction.add("Tree is added to the options.");
        instruction.add("");
        instruction.add("The skill must have a cast trigger.");
        instruction.add("The cast trigger must allow triggering.");
        instruction.add("Multi cast applies the cooldown after all casts.");
        instruction.add("");
        instruction.add("Never allow unrestricted triggering, you may create a");
        instruction.add("Combination that can instantly crash your server.");
        return instruction;
    }
}
