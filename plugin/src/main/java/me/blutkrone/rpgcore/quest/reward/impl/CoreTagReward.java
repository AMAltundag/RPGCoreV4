package me.blutkrone.rpgcore.quest.reward.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.bundle.quest.reward.EditorQuestRewardTag;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.language.LanguageManager;
import me.blutkrone.rpgcore.quest.reward.AbstractQuestReward;
import org.bukkit.inventory.ItemStack;

public class CoreTagReward extends AbstractQuestReward {

    private final String tag;
    private final boolean remove;

    public CoreTagReward(EditorQuestRewardTag editor) {
        this.tag = "quest_" + editor.tag.toLowerCase();
        this.remove = editor.remove;
    }

    @Override
    public ItemStack getPreview(CorePlayer player) {
        LanguageManager lpm = RPGCore.inst().getLanguageManager();
        return lpm.getAsItem("quest_reward_tag_" + this.tag).build();
    }

    @Override
    public void giveReward(CorePlayer player) {
        if (this.remove) {
            player.getPersistentTags().remove(this.tag);
        } else {
            player.getPersistentTags().add(this.tag);
        }
    }
}
