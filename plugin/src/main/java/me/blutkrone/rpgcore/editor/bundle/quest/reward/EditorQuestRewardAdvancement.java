package me.blutkrone.rpgcore.editor.bundle.quest.reward;

import me.blutkrone.rpgcore.editor.bundle.quest.AbstractEditorQuestReward;
import me.blutkrone.rpgcore.quest.reward.AbstractQuestReward;
import me.blutkrone.rpgcore.quest.reward.impl.CoreAdvancementReward;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorQuestRewardAdvancement extends AbstractEditorQuestReward {

    public EditorQuestRewardAdvancement() {
    }

    @Override
    public AbstractQuestReward build() {
        return new CoreAdvancementReward(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aJob Advancement Reward")
                .build();
    }

    @Override
    public String getName() {
        return "Advance";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Job Advancement Reward");
        instruction.add("Will force the player to pick a job advancement.");
        instruction.add("");
        instruction.add("Be careful with quest configuration, this offers advancements");
        instruction.add("Based on your current job rather then specific advancements.");
        instruction.add("");
        instruction.add("Advancements are defined in the job itself.");
        return instruction;
    }
}
