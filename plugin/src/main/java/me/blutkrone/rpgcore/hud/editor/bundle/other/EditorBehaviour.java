package me.blutkrone.rpgcore.hud.editor.bundle.other;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierBoolean;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierString;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono.ActionConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.CostConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.TriggerConstraint;
import me.blutkrone.rpgcore.skill.CoreSkill;
import me.blutkrone.rpgcore.skill.behaviour.CoreBehaviour;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorBehaviour implements IEditorBundle {

    @EditorBundle(name = "Icon")
    @EditorTooltip(tooltip = "Icon shown while behaviour is shown")
    public EditorModifierString icon = new EditorModifierString("none");
    @EditorBundle(name = "Debuff")
    @EditorTooltip(tooltip = "Whether to render as a debuff")
    public EditorModifierBoolean debuff = new EditorModifierBoolean();
    @EditorBundle(name = "Hidden")
    @EditorTooltip(tooltip = "Hide icon while on cooldown")
    public EditorModifierBoolean hidden = new EditorModifierBoolean();
    @EditorList(name = "Trigger", singleton = true, constraint = TriggerConstraint.class)
    @EditorTooltip(tooltip = "Trigger for the behaviour")
    public List<IEditorBundle> trigger = new ArrayList<>();
    @EditorList(name = "Cost", constraint = CostConstraint.class)
    @EditorTooltip(tooltip = "Costs consumed after triggering")
    public List<IEditorBundle> costs = new ArrayList<>();
    @EditorList(name = "Action", constraint = ActionConstraint.class)
    @EditorTooltip(tooltip = "Actions to invoke when conditions met")
    public List<IEditorBundle> actions = new ArrayList<>();

    public CoreBehaviour build(CoreSkill skill) {
        return new CoreBehaviour(skill, this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOK)
                .name("Behaviour")
                .build();
    }

    @Override
    public String getName() {
        return "Behaviour";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Behaviour");
        instruction.add("A passive behaviour attached to an entity, which can run");
        instruction.add("Run actions once a trigger condition was archived.");
        return instruction;
    }
}
