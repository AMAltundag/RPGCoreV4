package me.blutkrone.rpgcore.editor.bundle.npc;

import me.blutkrone.rpgcore.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.editor.constraint.reference.other.LanguageConstraint;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import me.blutkrone.rpgcore.npc.trait.impl.CoreTravelTrait;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EditorTravelTrait extends AbstractEditorNPCTrait {

    @EditorCategory(info = "Crafter", icon = Material.CRAFTING_TABLE)

    @EditorWrite(name = "Minimap", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Allows to pick a location within the given minimap."})
    public String minimap = "nothingness";
    @EditorWrite(name = "Currency", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Only bank-able items can be used as currency", "Any denomination can be used"})
    public String currency = "undefined";
    @EditorNumber(name = "Multiplier")
    @EditorTooltip(tooltip = {"Multiplies with distance to build cost."})
    public double multiplier = 1.0;

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

    public EditorTravelTrait() {

    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOK)
                .name("§fTravel Trait")
                .build();
    }

    @Override
    public String getName() {
        return "Travel";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Travel Trait");
        instruction.add("If you leave currency undefined, travel will be free.");
        instruction.add("Travel locations are pulled from minimap.yml");
        return instruction;
    }

    @Override
    public AbstractCoreTrait build() {
        return new CoreTravelTrait(this);
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
