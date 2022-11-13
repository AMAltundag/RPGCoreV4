package me.blutkrone.rpgcore.hud.editor.bundle.trigger;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierString;
import me.blutkrone.rpgcore.hud.editor.constraint.other.DamageElementConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.other.DamageTypeConstraint;
import me.blutkrone.rpgcore.skill.trigger.AbstractCoreTrigger;
import me.blutkrone.rpgcore.skill.trigger.CoreTakeDamageTrigger;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditorTakeDamageTrigger extends AbstractEditorTrigger {

    @EditorCategory(icon = Material.LEVER, info = "Trigger")
    @EditorList(name = "Elements", constraint = DamageElementConstraint.class)
    @EditorTooltip(tooltip = {"Count only damage of this element", "Leave empty to work with any damage"})
    public List<String> elements = new ArrayList<>();
    @EditorList(name = "Types", constraint = DamageTypeConstraint.class)
    @EditorTooltip(tooltip = {"Triggers if damage is of this type", "Leave empty to work with any damage"})
    public List<String> types = new ArrayList<>();
    @EditorBundle(name = "Damage")
    @EditorTooltip(tooltip = "How much damage before the trigger goes off")
    public EditorModifierNumber damage = new EditorModifierNumber();

    @EditorCategory(icon = Material.CLOCK, info = "Cooldown")
    @EditorBundle(name = "Reduction")
    @EditorTooltip(tooltip = "Percentage removed from cooldown")
    public EditorModifierNumber cooldown_reduction = new EditorModifierNumber();
    @EditorBundle(name = "Duration")
    @EditorTooltip(tooltip = "How many ticks of a cooldown apply")
    public EditorModifierNumber cooldown_time = new EditorModifierNumber();
    @EditorBundle(name = "Recovery")
    @EditorTooltip(tooltip = "Divide cooldown by recovery rate")
    public EditorModifierNumber cooldown_recovery = new EditorModifierNumber();
    @EditorBundle(name = "Identifier")
    @EditorTooltip(tooltip = "Identifier of the cooldown")
    public EditorModifierString cooldown_id = new EditorModifierString(UUID.randomUUID().toString());

    @Override
    public AbstractCoreTrigger build() {
        return new CoreTakeDamageTrigger(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.LEVER)
                .name("§fTake Damage Trigger")
                .build();
    }

    @Override
    public String getName() {
        return "Take";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Take Damage Trigger");
        return instruction;
    }
}

