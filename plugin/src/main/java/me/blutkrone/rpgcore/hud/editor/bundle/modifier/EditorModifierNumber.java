package me.blutkrone.rpgcore.hud.editor.bundle.modifier;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono.NumberModifierConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.SelectorConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.index.AttributeConstraint;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierNumber;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorModifierNumber implements IEditorBundle {

    @EditorNumber(name = "Value (Fixed)")
    @EditorTooltip(tooltip = "Fixed base value")
    public double base_value = 0d;
    @EditorNumber(name = "Multiplier (Fixed)")
    @EditorTooltip(tooltip = "Fixed multiplier value")
    public double multi_value = 0d;
    @EditorList(name = "Value (Attribute)", constraint = AttributeConstraint.class)
    @EditorTooltip(tooltip = "Contributes to base value")
    public List<String> base_attribute = new ArrayList<>();
    @EditorList(name = "Multiplier (Attribute)", constraint = AttributeConstraint.class)
    @EditorTooltip(tooltip = "Contributes to multiplier value")
    public List<String> multi_attribute = new ArrayList<>();
    @EditorList(name = "Condition", constraint = SelectorConstraint.class)
    @EditorTooltip(tooltip = "Apply only if context holder matches condition")
    public List<IEditorBundle> condition = new ArrayList<>();
    @EditorList(name = "Children", constraint = NumberModifierConstraint.class)
    @EditorTooltip(tooltip = "Child modifiers we operate with")
    public List<IEditorBundle> children = new ArrayList<>();

    public EditorModifierNumber() {
    }

    public EditorModifierNumber(double base_value) {
        this.base_value = base_value;
    }

    public CoreModifierNumber build() {
        return new CoreModifierNumber(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.PAPER)
                .name("Modifier Number")
                .appendLore(String.format("Base: %s (And %s attributes)", this.base_value, this.base_attribute.size()))
                .appendLore(String.format("Multi: %s (And %s attributes)", this.multi_value, this.multi_attribute.size()))
                .build();
    }

    @Override
    public String getName() {
        return "Modifier";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Modifier Number");
        instruction.add("Multiplies base with multiplier, child modifiers sum with");
        instruction.add("each other.");
        instruction.add("");
        instruction.add("Conditions are ran off the context owner, child modifiers");
        instruction.add("Respect parent conditions.");
        instruction.add("");
        instruction.add("The multiplier internally starts at 1.0");
        return instruction;
    }
}
