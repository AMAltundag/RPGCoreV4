package me.blutkrone.rpgcore.editor.bundle.trigger;

import me.blutkrone.rpgcore.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierString;
import me.blutkrone.rpgcore.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.skill.trigger.AbstractCoreTrigger;
import me.blutkrone.rpgcore.skill.trigger.CoreKillTrigger;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditorKillTrigger extends AbstractEditorTrigger {

    @EditorCategory(icon = Material.LEVER, info = "Trigger")
    @EditorList(name = "Tags", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Triggers if entity has this tag", "Leave empty to work with any mob"})
    public List<String> tags = new ArrayList<>();
    @EditorBundle(name = "Accumulate")
    @EditorTooltip(tooltip = "How many kills before trigger goes off")
    public EditorModifierNumber kills = new EditorModifierNumber();

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
        return new CoreKillTrigger(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.LEVER)
                .name("§fKill Trigger")
                .build();
    }

    @Override
    public String getName() {
        return "Kill";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Kill Trigger");
        return instruction;
    }
}
