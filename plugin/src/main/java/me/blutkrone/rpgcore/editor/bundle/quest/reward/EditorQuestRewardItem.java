package me.blutkrone.rpgcore.editor.bundle.quest.reward;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.quest.AbstractEditorQuestReward;
import me.blutkrone.rpgcore.editor.constraint.reference.index.ItemConstraint;
import me.blutkrone.rpgcore.quest.reward.AbstractQuestReward;
import me.blutkrone.rpgcore.quest.reward.impl.CoreItemReward;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorQuestRewardItem extends AbstractEditorQuestReward {

    @EditorWrite(name = "Item", constraint = ItemConstraint.class)
    @EditorTooltip(tooltip = "Which item to offer as a reward.")
    public String item = "NOTHINGNESS";
    @EditorNumber(name = "Amount", minimum = 0, maximum = 64)
    @EditorTooltip(tooltip = "Amount is limited by effective stack size.")
    public double amount = 1;

    public EditorQuestRewardItem() {
    }

    @Override
    public AbstractQuestReward build() {
        return new CoreItemReward(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aItem Reward")
                .appendLore("§fItem: " + this.item)
                .appendLore("§fAmount: " + this.amount)
                .build();
    }

    @Override
    public String getName() {
        return "Item";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Item Reward");
        instruction.add("Grants items to player, excess that cannot be stored");
        instruction.add("Will be dropped on the ground.");
        return instruction;
    }

}
