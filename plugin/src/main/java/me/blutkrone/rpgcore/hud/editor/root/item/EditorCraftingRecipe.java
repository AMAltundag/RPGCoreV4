package me.blutkrone.rpgcore.hud.editor.root.item;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono.EditorItemWithQuantityConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.index.EffectConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.index.ItemConstraint;
import me.blutkrone.rpgcore.hud.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.item.crafting.CoreCraftingRecipe;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class EditorCraftingRecipe implements IEditorRoot<CoreCraftingRecipe> {

    @EditorCategory(info = "Recipe", icon = Material.CRAFTING_TABLE)
    @EditorList(name = "Ingredients", constraint = EditorItemWithQuantityConstraint.class)
    @EditorTooltip(tooltip = "What items are consumed to craft")
    public List<IEditorBundle> ingredients = new ArrayList<>();
    @EditorWrite(name = "Output", constraint = ItemConstraint.class)
    @EditorTooltip(tooltip = "What item comes out of the craft")
    public String output = "NOTHINGNESS";
    @EditorNumber(name = "Quantity", minimum = 0.01d, maximum = 64.0d)
    @EditorTooltip(tooltip = "Decimals are treated as a chance")
    public double quantity = 1.0d;
    @EditorList(name = "Tags", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Used to filter recipes", "Tag 'DIRECT_#' is always added."})
    public List<String> tags = new ArrayList<>();

    @EditorCategory(info = "Effect", icon = Material.PAPER)
    @EditorWrite(name = "Crafted", constraint = EffectConstraint.class)
    @EditorTooltip(tooltip = "Effect invoked when item is crafted")
    public String effect_crafted = "NOTHINGNESS";
    @EditorWrite(name = "Failed", constraint = EffectConstraint.class)
    @EditorTooltip(tooltip = "Effect invoked when item failed crafting")
    public String effect_failed = "NOTHINGNESS";

    private transient File file;

    public EditorCraftingRecipe() {
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public void save() throws IOException {
        try (FileWriter fw = new FileWriter(file, Charset.forName("UTF-8"))) {
            RPGCore.inst().getGsonPretty().toJson(this, fw);
        }
    }

    @Override
    public CoreCraftingRecipe build(String id) {
        return new CoreCraftingRecipe(id, this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.CRAFTING_TABLE)
                .name("§fCrafting Recipe")
                .appendLore("§fIngredients: " + this.ingredients.size())
                .appendLore("§fOutput: " + this.output)
                .appendLore("§fQuantity: " + this.quantity)
                .appendLore("§fEffect: " + this.effect_crafted)
                .build();
    }

    @Override
    public String getName() {
        return "Crafting";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Crafting Recipe");
        instruction.add("Combine multiple items into another item.");
        return instruction;
    }
}
