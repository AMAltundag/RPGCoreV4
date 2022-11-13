package me.blutkrone.rpgcore.hud.editor.bundle.passive;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorAttributeAndFactor;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono.AttributeAndFactorConstraint;
import me.blutkrone.rpgcore.passive.node.AbstractCorePassive;
import me.blutkrone.rpgcore.passive.node.CorePassiveSkillAttribute;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorPassiveSkillAttribute extends AbstractEditorPassive {

    @EditorList(name = "Factors", constraint = AttributeAndFactorConstraint.class)
    @EditorTooltip(tooltip = "What attributes to be provided.")
    public List<EditorAttributeAndFactor> factors = new ArrayList<>();

    @Override
    public AbstractCorePassive build() {
        return new CorePassiveSkillAttribute(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.EXPERIENCE_BOTTLE)
                .name("§fAttribute Passive")
                .lore("§fTotal Attributes: " + this.factors.size() + "X")
                .build();
    }

    @Override
    public String getName() {
        return "Attribute";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Skill Attribute Passive");
        instruction.add("");
        instruction.add("Grants attributes only during evaluation of the associated skill.");
        instruction.add("This will only affect the skill the tree belongs to.");
        instruction.add("");
        instruction.add("§cCompatible: Skill");
        return instruction;
    }
}