package me.blutkrone.rpgcore.editor.bundle.mechanic;

import me.blutkrone.rpgcore.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.constraint.bundle.mono.AttributeAndModifierConstraint;
import me.blutkrone.rpgcore.editor.constraint.bundle.mono.StringModifierConstraint;
import me.blutkrone.rpgcore.editor.constraint.other.DamageTypeConstraint;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.DamageMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorDamageMechanic extends AbstractEditorMechanic {

    @EditorCategory(icon = Material.BLUE_BANNER, info = "Generic")
    @EditorWrite(name = "Type", constraint = DamageTypeConstraint.class)
    @EditorTooltip(tooltip = "The type defines how the damage is computed.")
    public String type = "SPELL";
    @EditorList(name = "Attribute", constraint = AttributeAndModifierConstraint.class)
    @EditorTooltip(tooltip = "Attribute modifiers which apply to the damage.")
    public List<IEditorBundle> modifiers_always = new ArrayList<>();
    @EditorList(name = "Tag", constraint = StringModifierConstraint.class)
    @EditorTooltip(tooltip = "Tag modifiers which apply to the damage.")
    public List<IEditorBundle> tags_always = new ArrayList<>();

    @Override
    public AbstractCoreMechanic build() {
        return new DamageMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.DIAMOND_AXE)
                .name("§fDamage")
                .build();
    }

    @Override
    public String getName() {
        return "Damage";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Damage");
        instruction.add("Inflicts damage to each target individually.");
        return instruction;
    }
}
