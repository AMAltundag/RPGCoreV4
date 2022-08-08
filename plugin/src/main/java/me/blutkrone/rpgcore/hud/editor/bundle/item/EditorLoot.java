package me.blutkrone.rpgcore.hud.editor.bundle.item;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorName;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.hud.editor.index.IndexAttachment;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EditorName(name = "Loot")
public class EditorLoot implements IEditorBundle {

    // which tags to gain weight for
    @EditorWrite(name = "Tag", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = "Which item tag can be rolled.")
    public String tag = "ANY";
    @EditorNumber(name = "Weight")
    @EditorTooltip(tooltip = "Multiplier to the item weight.")
    public double weight = 0;

    /**
     * Transform a tag-to-weight list into an attachment.
     *
     * @param options the tag-to-weight listing
     * @return all attachments we've created
     */
    public static IndexAttachment<CoreItem, WeightedRandomMap<CoreItem>> build(List<IEditorBundle> options) {
        return RPGCore.inst().getItemManager().getItemIndex().createAttachment((index -> {
            WeightedRandomMap<CoreItem> output = new WeightedRandomMap<>();

            // cache the weight modifiers
            Map<String, Double> weights = new HashMap<>();
            for (IEditorBundle option : options) {
                if (option instanceof EditorLoot) {
                    weights.merge(((EditorLoot) option).tag.toLowerCase(), ((EditorLoot) option).weight, (a, b) -> a + b);
                }
            }

            // construct a randomize unit
            for (CoreItem item : index.getAll()) {
                // fetch the weight multiplier we got
                double weight = 0d;
                for (String tag : item.getTags()) {
                    weight += weights.getOrDefault(tag, 0d);
                }
                // do not roll with negative multiplier
                if (weight <= 0d) {
                    continue;
                }
                // append the item to our choices
                output.add(weight, item);
            }
            return output;
        }));
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

    @Override
    public String toString() {
        return this.tag.toLowerCase() + "X" + this.weight;
    }
}
