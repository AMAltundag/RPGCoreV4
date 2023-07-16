package me.blutkrone.rpgcore.quest.reward.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.bundle.quest.reward.EditorQuestRewardTrait;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.quest.reward.AbstractQuestReward;
import org.bukkit.inventory.ItemStack;

public class CoreTraitReward extends AbstractQuestReward {

    private String trait;

    public CoreTraitReward(EditorQuestRewardTrait editor) {
        this.trait = editor.trait.toLowerCase();
    }

    @Override
    public ItemStack getPreview(CorePlayer player) {
        return RPGCore.inst().getLanguageManager().getAsItem("quest_reward_trait_" + this.trait).build();
    }

    @Override
    public void giveReward(CorePlayer player) {
        player.getPersistentTags().add("quest_tag_" + this.trait);
    }
}
