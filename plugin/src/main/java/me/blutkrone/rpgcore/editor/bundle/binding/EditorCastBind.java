package me.blutkrone.rpgcore.editor.bundle.binding;

import me.blutkrone.rpgcore.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBoolean;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierBoolean;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierString;
import me.blutkrone.rpgcore.editor.constraint.bundle.mono.ActionConstraint;
import me.blutkrone.rpgcore.editor.constraint.bundle.multi.CostConstraint;
import me.blutkrone.rpgcore.skill.CoreSkill;
import me.blutkrone.rpgcore.skill.skillbar.ISkillBind;
import me.blutkrone.rpgcore.skill.skillbar.bound.SkillBindCast;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditorCastBind extends AbstractEditorSkillBinding {
    @EditorCategory(icon = Material.CLOCK, info = "Cooldown")
    @EditorBundle(name = "Time")
    @EditorTooltip(tooltip = "Ticks to be on cooldown after finishing")
    public EditorModifierNumber cooldown_time = new EditorModifierNumber(0d);
    @EditorBundle(name = "Recovery")
    @EditorTooltip(tooltip = "Lowers cooldown, diminishing returns")
    public EditorModifierNumber cooldown_recovery = new EditorModifierNumber(0d);
    @EditorBundle(name = "ID")
    @EditorTooltip(tooltip = "Identifier to share cooldowns")
    public EditorModifierString cooldown_id = new EditorModifierString(UUID.randomUUID().toString());

    @EditorCategory(icon = Material.PAPER, info = "General")
    @EditorBundle(name = "Icon")
    @EditorTooltip(tooltip = "Icon used on the skillbar")
    public EditorModifierString icon = new EditorModifierString("default");
    @EditorList(name = "Cost", constraint = CostConstraint.class)
    @EditorTooltip(tooltip = "Cost consumed after casting")
    public List<IEditorBundle> costs = new ArrayList<>();
    @EditorList(name = "Actions", constraint = ActionConstraint.class)
    @EditorTooltip(tooltip = "Actions triggered after casting")
    public List<IEditorBundle> actions = new ArrayList<>();
    @EditorBundle(name = "Interrupt")
    @EditorTooltip(tooltip = "Can be interrupted via interruption mechanic?")
    public EditorModifierBoolean interruptable = new EditorModifierBoolean();

    @EditorBoolean(name = "Trigger")
    @EditorTooltip(tooltip = {"The 'actions' can be triggered by other skills.", "Will bypass cooldown, casting and cost from this."})
    public EditorModifierBoolean triggerable = new EditorModifierBoolean();

    @EditorCategory(icon = Material.BOOK, info = "Casting")
    @EditorBundle(name = "Stability")
    @EditorTooltip(tooltip = "Counteracts entity movement, moving slows casting")
    public EditorModifierNumber stability = new EditorModifierNumber(0d);
    @EditorBundle(name = "Time")
    @EditorTooltip(tooltip = "Duration to cast the skill")
    public EditorModifierNumber cast_time = new EditorModifierNumber(100d);
    @EditorBundle(name = "Faster")
    @EditorTooltip(tooltip = "Reduces cast time by dividing it")
    public EditorModifierNumber cast_faster = new EditorModifierNumber(0d);

    @Override
    public ISkillBind build(CoreSkill skill) {
        return new SkillBindCast(skill, this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOK)
                .name("Cast Bind")
                .build();
    }

    @Override
    public String getName() {
        return "Casting";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Channeling Binding");
        instruction.add("Actions invoke once cast time has passed.");
        instruction.add("Moving slows casting speed, stability counteracts moving.");
        instruction.add("Cooldown applies after invoking.");
        return instruction;
    }
}
