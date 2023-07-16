package me.blutkrone.rpgcore.editor.bundle.modifier;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBoolean;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.skill.modifier.CoreModifierBoolean;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorModifierBoolean implements IEditorBundle {

    @EditorBoolean(name = "Value (Default)")
    @EditorTooltip(tooltip = "Original value of the modifier")
    public boolean default_value;
    @EditorList(name = "Disable", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Sets to false if any tag is present", "Overrides enable"})
    public List<String> disable = new ArrayList<>();
    @EditorList(name = "Enable", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Sets to true if any tag is present"})
    public List<String> enable = new ArrayList<>();

    public EditorModifierBoolean(boolean default_value) {
        this.default_value = default_value;
    }

    public EditorModifierBoolean() {
        this.default_value = false;
    }

    public CoreModifierBoolean build() {
        return new CoreModifierBoolean(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.PAPER)
                .name("Modifier Boolean")
                .build();
    }

    @Override
    public String getName() {
        return "Modifier";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Modifier Boolean");
        instruction.add("Evaluates into a boolean.");
        return instruction;
    }
}
