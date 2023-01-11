package me.blutkrone.rpgcore.quest.reward.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.quest.reward.EditorQuestRewardTag;
import me.blutkrone.rpgcore.language.LanguageManager;
import me.blutkrone.rpgcore.quest.reward.AbstractQuestReward;
import org.bukkit.inventory.ItemStack;

public class CoreTagReward extends AbstractQuestReward {

    private final String tag;

    public CoreTagReward(EditorQuestRewardTag editor) {
        this.tag = "quest" + editor.tag.toLowerCase();
    }

    @Override
    public ItemStack getPreview(CorePlayer player) {
        LanguageManager lpm = RPGCore.inst().getLanguageManager();
        return lpm.getAsItem("quest_reward_tag_" + this.tag).build();
    }

    @Override
    public void giveReward(CorePlayer player) {
        // flag player to do their job advancement
        player.getPersistentTags().add(this.tag);
    }
}
