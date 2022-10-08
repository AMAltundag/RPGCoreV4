package me.blutkrone.rpgcore.hud.editor.bundle.loot;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono.AttributeAndFactorConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono.LootConstraint;
import me.blutkrone.rpgcore.mob.loot.AbstractCoreLoot;
import me.blutkrone.rpgcore.mob.loot.ItemCoreLoot;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorLootItem extends AbstractEditorLoot {

    @EditorCategory(info = "Item", icon = Material.CHEST)
    @EditorList(name = "Items", constraint = LootConstraint.class)
    public List<IEditorBundle> item_weights = new ArrayList<>();
    @EditorCategory(info = {"Quantity", "How many individual rolls to apply"}, icon = Material.BUNDLE)
    @EditorNumber(name = "Quantity")
    public double quantity = 0.0d;
    @EditorList(name = "Killer", constraint = AttributeAndFactorConstraint.class)
    public List<IEditorBundle> quantity_killer = new ArrayList<>();
    @EditorList(name = "Killed", constraint = AttributeAndFactorConstraint.class)
    public List<IEditorBundle> quantity_killed = new ArrayList<>();
    @EditorCategory(info = {"Rarity", "Rolls multiple times to pick rarest outcome"}, icon = Material.DIAMOND)
    @EditorNumber(name = "Rarity")
    public double rarity = 0.0d;
    @EditorList(name = "Killer", constraint = AttributeAndFactorConstraint.class)
    public List<IEditorBundle> rarity_killer = new ArrayList<>();
    @EditorList(name = "Killed", constraint = AttributeAndFactorConstraint.class)
    public List<IEditorBundle> rarity_killed = new ArrayList<>();
    @EditorCategory(info = {"Quality", "Enhances random properties of the item"}, icon = Material.ANVIL)
    @EditorNumber(name = "Quality")
    public double quality = 0.0d;
    @EditorList(name = "Killer", constraint = AttributeAndFactorConstraint.class)
    public List<IEditorBundle> quality_killer = new ArrayList<>();
    @EditorList(name = "Killed", constraint = AttributeAndFactorConstraint.class)
    public List<IEditorBundle> quality_killed = new ArrayList<>();

    @Override
    public AbstractCoreLoot build() {
        return new ItemCoreLoot(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BUNDLE)
                .name("Item Loot")
                .build();
    }

    @Override
    public String getName() {
        return "Item";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();

        instruction.add("Item Loot");
        instruction.add("Drops an item to be looted by the killers.");
        instruction.add("");
        instruction.add("Rarity will roll items multiple times, diminishing returns");
        instruction.add("do apply on how often we will re-roll.");

        return instruction;
    }
}
