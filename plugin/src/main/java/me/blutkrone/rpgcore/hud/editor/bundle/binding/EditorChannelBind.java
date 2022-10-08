package me.blutkrone.rpgcore.hud.editor.bundle.binding;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierString;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono.ActionConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.CostConstraint;
import me.blutkrone.rpgcore.skill.CoreSkill;
import me.blutkrone.rpgcore.skill.skillbar.ISkillBind;
import me.blutkrone.rpgcore.skill.skillbar.bound.SkillBindChannel;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditorChannelBind extends AbstractEditorSkillBinding {

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
    @EditorTooltip(tooltip = "Cost consumed before and while channeling")
    public List<IEditorBundle> costs = new ArrayList<>();
    @EditorList(name = "Actions", constraint = ActionConstraint.class)
    @EditorTooltip(tooltip = "Actions triggered while channeling")
    public List<IEditorBundle> actions = new ArrayList<>();

    @EditorCategory(icon = Material.BOOK, info = "Channeling")
    @EditorBundle(name = "Stability")
    @EditorTooltip(tooltip = "Counteracts entity movement, moving reduces time")
    public EditorModifierNumber stability = new EditorModifierNumber(0d);
    @EditorBundle(name = "Time")
    @EditorTooltip(tooltip = "How long we can remain channeling")
    public EditorModifierNumber channel_time = new EditorModifierNumber(100d);
    @EditorBundle(name = "Interval")
    @EditorTooltip(tooltip = "Interval to channel at, measured in ticks")
    public EditorModifierNumber channel_interval = new EditorModifierNumber(7d);
    @EditorBundle(name = "Faster")
    @EditorTooltip(tooltip = "Reduces interval by dividing it")
    public EditorModifierNumber channel_faster = new EditorModifierNumber(0d);

    @Override
    public ISkillBind build(CoreSkill skill) {
        return new SkillBindChannel(skill, this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOK)
                .name("Channeling Bind")
                .build();
    }

    @Override
    public String getName() {
        return "Channeling";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Channeling Binding");
        instruction.add("While channeling cost and actions activate at the interval.");
        instruction.add("Moving reduces duration, stability counteracts moving.");
        instruction.add("Cooldown applies afterwards.");
        instruction.add("");
        instruction.add("§cDelay will also stall the interval/duration");
        return instruction;
    }
}
