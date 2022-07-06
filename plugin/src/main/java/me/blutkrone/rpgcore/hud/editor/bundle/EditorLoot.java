package me.blutkrone.rpgcore.hud.editor.bundle;

import me.blutkrone.rpgcore.hud.editor.EditorIndex;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorName;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.constraint.StringConstraint;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@EditorName(name = "Loot")
public class EditorLoot implements IEditorBundle {

    // which tags to gain weight for
    @EditorWrite(name = "Tag", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = "Which item tag can be rolled.")
    public String tag = "ANY";
    @EditorNumber(name = "Weight")
    @EditorTooltip(tooltip = "Multiplier to the item weight.")
    public double weight = 0;

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aLoot")
                .appendLore("§fTag: " + tag)
                .appendLore("§fWeight: " + weight)
                .build();
    }

    @Override
    public String getName() {
        return "Loot";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§bLoot");
        instruction.add("Roll a random item which matches with a tag, all weights");
        instruction.add("are summed and multiplied with the base weight of the item.");
        return instruction;
    }
}
