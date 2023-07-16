package me.blutkrone.rpgcore.editor.bundle.selector;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.editor.constraint.reference.index.AttributeConstraint;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import me.blutkrone.rpgcore.skill.selector.AttributeSelector;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorAttributeSelector extends AbstractEditorSelector {

    @EditorWrite(name = "Attribute", constraint = AttributeConstraint.class)
    @EditorTooltip(tooltip = "Which attribute to affect!")
    public String attribute = "NOTHINGNESS";
    @EditorBundle(name = "Minimum")
    @EditorTooltip(tooltip = "Minimum amount of this attribute")
    public EditorModifierNumber minimum = new EditorModifierNumber(-99999);
    @EditorBundle(name = "Maximum")
    @EditorTooltip(tooltip = "Maximum amount of this attribute")
    public EditorModifierNumber maximum = new EditorModifierNumber(+99999);

    @Override
    public AbstractCoreSelector build() {
        return new AttributeSelector(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.CLOCK)
                .name("Attribute Selector")
                .build();
    }

    @Override
    public String getName() {
        return "Attribute Selector";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Attribute Selector");
        instruction.add("Filter entities for attribute requirement.");
        instruction.add("§cThis also scans negative attributes, setup accordingly!");
        return instruction;
    }
}
