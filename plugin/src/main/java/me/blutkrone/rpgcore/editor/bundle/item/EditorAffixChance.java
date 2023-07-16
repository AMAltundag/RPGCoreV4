package me.blutkrone.rpgcore.editor.bundle.item;

import me.blutkrone.rpgcore.editor.annotation.EditorName;
import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@EditorName(name = "Affix Chance")
public class EditorAffixChance implements IEditorBundle {

    // which tags to gain weight for
    @EditorWrite(name = "Tag", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = "Affects all affixes with this tag.")
    public String tag = "ANY";
    // the weight multiplier for those tags
    @EditorNumber(name = "Weight")
    @EditorTooltip(tooltip = "Sums with all other weights.")
    public double weight = 0d;

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aAffix Chance")
                .appendLore("§fTag: " + tag)
                .appendLore("§fWeight: " + String.format("%.3f", weight))
                .build();
    }

    @Override
    public String getName() {
        return "Affix Chance";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§bAffix Chance");
        instruction.add("This multiplies with the weight declared on the affix");
        instruction.add("but sums with other weight types.");
        return instruction;
    }
}
