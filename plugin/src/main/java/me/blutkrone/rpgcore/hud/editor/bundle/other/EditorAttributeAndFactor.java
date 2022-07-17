package me.blutkrone.rpgcore.hud.editor.bundle.other;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorName;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.index.AttributeConstraint;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@EditorName(name = "Attribute And Factor")
@EditorTooltip(tooltip = "An attribute mapped to a numeric value")
public class EditorAttributeAndFactor implements IEditorBundle {
    @EditorWrite(name = "Attribute", constraint = AttributeConstraint.class)
    @EditorTooltip(tooltip = "Which attribute to affect!")
    public String attribute = "NOTHINGNESS";
    @EditorNumber(name = "Value")
    @EditorTooltip(tooltip = "Sums with other modifiers to this attribute.")
    public double factor = 0d;

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aAttribute And Factor")
                .appendLore("§fAttribute: " + attribute)
                .appendLore("§fFactor: " + String.format("%.3f", factor))
                .build();
    }

    @Override
    public String getName() {
        return "Attribute And Factor";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§fAttribute And Factor");
        instruction.add("Sum up with any other modifier to the attribute.");
        return instruction;
    }
}
