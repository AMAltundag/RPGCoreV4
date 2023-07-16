package me.blutkrone.rpgcore.editor.bundle.item;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.editor.constraint.reference.index.ItemConstraint;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorItemPrice implements IEditorBundle {

    @EditorWrite(name = "Item", constraint = ItemConstraint.class)
    @EditorTooltip(tooltip = "The item to check against")
    public String item = "NOTHINGNESS";
    @EditorWrite(name = "Currency", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Only bank-able items can be used as currency", "Any denomination can be used"})
    public String currency = "undefined";
    @EditorNumber(name = "Price", minimum = 1.0)
    @EditorTooltip(tooltip = "Total cost to purchase")
    public double price = 0d;

    public EditorItemPrice() {
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.CHEST)
                .name("§fItem With Price")
                .appendLore("§fItem: " + this.item)
                .appendLore("§fCurrency: " + this.currency)
                .appendLore("§fPrice: " + this.price)
                .build();
    }

    @Override
    public String getName() {
        return "Purchase";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Item With Price");
        instruction.add("Meant for a vendor NPC, allows you to assign");
        instruction.add("A price for purchase to an item.");
        instruction.add("");
        instruction.add("The currency is a bank group, but it has to be");
        instruction.add("Itemized in the player inventory.");
        return instruction;
    }
}
