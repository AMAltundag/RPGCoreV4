package me.blutkrone.rpgcore.hud.editor.bundle.item;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.index.ItemConstraint;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorItemWithQuantity implements IEditorBundle {

    @EditorWrite(name = "Item", constraint = ItemConstraint.class)
    @EditorTooltip(tooltip = "The item to check against")
    public String item = "NOTHINGNESS";
    @EditorNumber(name = "Quantity", minimum = 1.0)
    @EditorTooltip(tooltip = "Rounded down to the nearest integer.")
    public double quantity;

    public EditorItemWithQuantity() {
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.CHEST)
                .name("§fItem With Quantity")
                .appendLore("§fItem: " + this.item)
                .appendLore("§fQuantity: " + this.quantity)
                .build();
    }

    @Override
    public String getName() {
        return "Item";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§fItem With Quantity");
        instruction.add("§fCheck if at-least the given quantity of the item");
        instruction.add("§fIs available to use.");
        return instruction;
    }
}
