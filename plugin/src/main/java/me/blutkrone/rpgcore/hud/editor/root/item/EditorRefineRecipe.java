package me.blutkrone.rpgcore.hud.editor.root.item;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorName;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono.LootConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.index.EffectConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.index.ItemConstraint;
import me.blutkrone.rpgcore.hud.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.item.refinement.CoreRefinerRecipe;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@EditorName(name = "Refinement")
@EditorTooltip(tooltip = "A rule establishing refinement protocol.")
public class EditorRefineRecipe implements IEditorRoot<CoreRefinerRecipe> {

    @EditorCategory(info = "Recipe", icon = Material.CHEST)
    @EditorList(name = "Ingredients", constraint = ItemConstraint.class)
    @EditorTooltip(tooltip = {"Items to consume to trigger refinement"})
    public List<String> ingredients = new ArrayList<>();
    @EditorList(name = "Output", constraint = LootConstraint.class)
    @EditorTooltip(tooltip = {"Picks one random item when refining"})
    public List<IEditorBundle> outputs = new ArrayList<>();
    @EditorNumber(name = "Quantity")
    @EditorTooltip(tooltip = {"How many items to drop, decimals are percentages."})
    public double quantity = 1.0d;
    @EditorNumber(name = "Time", minimum = 0d)
    @EditorTooltip(tooltip = {"How long it takes to finish refinement"})
    public double duration = 60.0d;
    @EditorNumber(name = "Priority")
    @EditorTooltip(tooltip = {"Highest priority rule is applied"})
    public double priority = 1.0d;
    @EditorList(name = "Tags", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Used to filter recipes", "Tag 'DIRECT_#' is always added."})
    public List<String> tags = new ArrayList<>();

    @EditorCategory(info = "Effect", icon = Material.PAPER)
    @EditorWrite(name = "Working", constraint = EffectConstraint.class)
    @EditorTooltip(tooltip = {"Played while the refinement process happens"})
    public String effect_working = "NOTHINGNESS";
    @EditorWrite(name = "Finished", constraint = EffectConstraint.class)
    @EditorTooltip(tooltip = {"Played when the refinement process finishes"})
    public String effect_finished = "NOTHINGNESS";

    private transient File file;

    public EditorRefineRecipe() {
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aRefiner Rule")
                .appendLore("§fIngredients: " + this.ingredients)
                .appendLore("§fOutputs: " + this.outputs)
                .appendLore("§fQuantity: " + this.quantity)
                .appendLore("§fDuration: " + this.duration)
                .appendLore("§fPriority: " + this.priority)
                .build();
    }

    @Override
    public String getName() {
        return "Refinement";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§fRefinement Recipe");
        instruction.add("Establishes a processing path for collected resources.");
        return instruction;
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
            RPGCore.inst().getGson().toJson(this, fw);
        }
    }

    @Override
    public CoreRefinerRecipe build(String id) {
        return new CoreRefinerRecipe(id, this);
    }
}
