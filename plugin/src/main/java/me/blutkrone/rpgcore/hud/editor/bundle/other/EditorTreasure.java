package me.blutkrone.rpgcore.hud.editor.bundle.other;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono.LootConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.SelectorConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorTreasure implements IEditorBundle {

    @EditorBundle(name = "Model")
    @EditorTooltip(tooltip = "What model to use for the treasure chest")
    public EditorItemModel model = new EditorItemModel();
    @EditorWrite(name = "Design", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = "6-Row Inventory Design")
    public String menu_design = "loot_default";
    @EditorNumber(name = "Count", minimum = 1d, maximum = 54d)
    @EditorTooltip(tooltip = {"How many slots to populate."})
    public double count = 1.0d;
    @EditorList(name = "Slots", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Leave empty to utilise random slots", "Must be between 1-54", "Can use ranges like x~y", "Rounded to integers"})
    public List<String> exact = new ArrayList<>();
    @EditorList(name = "Items", constraint = LootConstraint.class)
    @EditorTooltip(tooltip = "Weight multipliers to roll with.")
    public List<IEditorBundle> item_weight = new ArrayList<>();
    @EditorNumber(name = "Weight", minimum = 1d, maximum = 54d)
    @EditorTooltip(tooltip = {"Chance to spawn this treasure instead of others."})
    public double weight = 1.0d;
    @EditorList(name = "Condition", constraint = SelectorConstraint.class)
    @EditorTooltip(tooltip = {"Can only spawn if condition is met."})
    public List<IEditorBundle> condition = new ArrayList<>();

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.CHEST)
                .name("Treasure")
                .appendLore("§fDesign: " + this.menu_design)
                .appendLore("§fCount: " + this.count)
                .appendLore("§fSlots: " + this.exact)
                .appendLore("§fWeight: X" + this.item_weight.size())
                .build();
    }

    @Override
    public String getName() {
        return "Treasure";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Treasure");
        instruction.add("Treasure chest which holds items, these hold personal loot");
        instruction.add("For every player.");
        return instruction;
    }
}
