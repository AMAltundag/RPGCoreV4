package me.blutkrone.rpgcore.hud.editor.bundle.item;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.index.ItemConstraint;
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
        instruction.add("§fItem With Price");
        instruction.add("§fMeant for a vendor NPC, allows you to assign");
        instruction.add("§fA price for purchase to an item.");
        instruction.add("§f");
        instruction.add("§fThe currency is a bank group, but it has to be");
        instruction.add("§fItemized in the player inventory.");
        return instruction;
    }
}
