package me.blutkrone.rpgcore.quest.reward.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.quest.reward.EditorQuestRewardAdvancement;
import me.blutkrone.rpgcore.language.LanguageManager;
import me.blutkrone.rpgcore.quest.reward.AbstractQuestReward;
import org.bukkit.inventory.ItemStack;

public class CoreAdvancementReward extends AbstractQuestReward {

    public CoreAdvancementReward(EditorQuestRewardAdvancement editor) {
    }

    @Override
    public ItemStack getPreview(CorePlayer player) {
        LanguageManager lpm = RPGCore.inst().getLanguageManager();
        return lpm.getAsItem("quest_reward_job_advance").build();
    }

    @Override
    public void giveReward(CorePlayer player) {
        // flag player to do their job advancement
        player.getPersistentTags().add("job_advancement_waiting");
    }
}
