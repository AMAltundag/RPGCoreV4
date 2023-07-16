package me.blutkrone.rpgcore.editor.bundle.quest.reward;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBoolean;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.quest.AbstractEditorQuestReward;
import me.blutkrone.rpgcore.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.quest.reward.AbstractQuestReward;
import me.blutkrone.rpgcore.quest.reward.impl.CoreTagReward;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorQuestRewardTag extends AbstractEditorQuestReward {

    @EditorWrite(name = "Tag", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"The tag to be gained"})
    public String tag = "nothingness";
    @EditorBoolean(name = "Remove")
    @EditorTooltip(tooltip = {"Remove instead of add the tag"})
    public boolean remove = false;

    public EditorQuestRewardTag() {
    }

    @Override
    public AbstractQuestReward build() {
        return new CoreTagReward(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aTag Reward")
                .appendLore("§fTag: quest_" + tag)
                .build();
    }

    @Override
    public String getName() {
        return "Tag";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Tag Reward");
        instruction.add("Add/Remove 'quest_#' as a permanent tag to the player.");
        return instruction;
    }
}
