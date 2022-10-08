package me.blutkrone.rpgcore.hud.editor.bundle.npc;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.index.RefinerRecipeConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.other.LanguageConstraint;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import me.blutkrone.rpgcore.npc.trait.impl.CoreRefinerTrait;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EditorRefinerTrait extends AbstractEditorNPCTrait {

    @EditorCategory(info = "Refiner", icon = Material.CRAFTING_TABLE)
    @EditorList(name = "Recipes", constraint = RefinerRecipeConstraint.class)
    @EditorTooltip(tooltip = {"Recipes with any of these tags can be refined."})
    public List<String> recipes = new ArrayList<>();
    @EditorWrite(name = "Design", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Menu design defined in 'item.yml' file."})
    public String design = "default";
    @EditorWrite(name = "Inventory", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Same design and same inventory share contents."})
    public String inventory = "default";
    @EditorNumber(name = "Speed", minimum = 0.0d)
    @EditorTooltip(tooltip = {"Increases speed of refinement by a percentage."})
    public double speed = 0d;
    @EditorNumber(name = "Quantity")
    @EditorTooltip(tooltip = {"How many items to drop, decimals are percentages.", "Multiplies with refinement rule."})
    public double quantity = 1.0d;

    @EditorCategory(info = "Cortex", icon = Material.FURNACE)
    @EditorWrite(name = "Icon", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Symbol to show on the NPC menu", "Only relevant with multiple NPC traits."})
    public String symbol = "default";
    @EditorWrite(name = "Text", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Description of this trait", "§cThis is a language code, NOT plaintext."})
    public String lc_text = "NOTHINGNESS";
    @EditorWrite(name = "Unlock", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Flag needed to show the trait"})
    public String unlock = "none";

    public transient File file;

    public EditorRefinerTrait() {

    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOK)
                .name("§fRefiner Trait")
                .build();
    }

    @Override
    public String getName() {
        return "Refiner";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Refiner Trait");
        return instruction;
    }

    @Override
    public AbstractCoreTrait build() {
        return new CoreRefinerTrait(this);
    }

    @Override
    public String getCortexSymbol() {
        return this.symbol;
    }

    @Override
    public String getIconLC() {
        return this.lc_text;
    }

    @Override
    public String getUnlockFlag() {
        return this.unlock;
    }
}
