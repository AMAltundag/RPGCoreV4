package me.blutkrone.rpgcore.editor.bundle.item;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.constraint.reference.index.ItemConstraint;
import me.blutkrone.rpgcore.editor.constraint.reference.other.NodeConstraint;
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
    public double quantity = 0.0d;
    @EditorList(name = "Gathering", constraint = NodeConstraint.class)
    @EditorTooltip(tooltip = {"Nodes that are related to where to find this item."})
    public List<String> gathering_area = new ArrayList<>();

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
        instruction.add("Item With Quantity");
        instruction.add("Check if at-least the given quantity of the item");
        instruction.add("Is available to use.");
        instruction.add("");
        instruction.add("Gathering area is only relevant for quest hints.");
        return instruction;
    }
}
