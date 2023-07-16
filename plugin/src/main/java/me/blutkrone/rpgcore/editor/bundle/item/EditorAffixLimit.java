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

@EditorName(name = "Affix Limit")
public class EditorAffixLimit implements IEditorBundle {

    // which tags to gain weight for
    @EditorWrite(name = "Tag", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = "Affects all affixes with this tag.")
    public String tag = "ANY";
    @EditorNumber(name = "Limit")
    @EditorTooltip(tooltip = "Sums with all other limits.")
    public double limit = 0;

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aAffix Limit")
                .appendLore("§fTag: " + tag)
                .appendLore("§fLimit: " + limit)
                .build();
    }

    @Override
    public String getName() {
        return "Affix Limit";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§bAffix Limit");
        instruction.add("An affix cannot roll, if any tags of preceding affixes");
        instruction.add("exceed the limit already.");
        return instruction;
    }
}
