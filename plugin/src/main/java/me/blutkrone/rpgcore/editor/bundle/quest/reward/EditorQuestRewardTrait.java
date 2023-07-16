package me.blutkrone.rpgcore.editor.bundle.quest.reward;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.quest.AbstractEditorQuestReward;
import me.blutkrone.rpgcore.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.quest.reward.AbstractQuestReward;
import me.blutkrone.rpgcore.quest.reward.impl.CoreTraitReward;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorQuestRewardTrait extends AbstractEditorQuestReward {

    @EditorWrite(name = "Tag", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = "Which tag to acquire.")
    public String trait = "none";

    public EditorQuestRewardTrait() {
    }

    @Override
    public AbstractQuestReward build() {
        return new CoreTraitReward(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aTag Reward")
                .appendLore("§fTrait: " + this.trait)
                .build();
    }

    @Override
    public String getName() {
        return "Trait";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Tag Reward");
        instruction.add("Can be used to unlock NPC traits or level gating.");
        return instruction;
    }
}
