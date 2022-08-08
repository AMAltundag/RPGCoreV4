package me.blutkrone.rpgcore.quest.reward.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.quest.reward.EditorQuestRewardExp;
import me.blutkrone.rpgcore.language.LanguageManager;
import me.blutkrone.rpgcore.quest.reward.AbstractQuestReward;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public class CoreExpReward extends AbstractQuestReward {

    // flat amount of experience to gain
    private int experience;
    // percentage of levels to gain
    private double levels;

    public CoreExpReward(EditorQuestRewardExp editor) {
        this.experience = (int) editor.exp;
        this.levels = editor.level;
    }

    @Override
    public ItemStack getPreview(CorePlayer player) {
        LanguageManager lpm = RPGCore.inst().getLanguageManager();

        if (this.experience > 0 && this.levels > 0) {
            return lpm.getAsItem("quest_reward_exp_and_level", this.experience, (int) (100d * this.levels)).build();
        } else if (this.levels > 0) {
            return lpm.getAsItem("quest_reward_level", (int) (100d * this.levels)).build();
        } else {
            return lpm.getAsItem("quest_reward_exp", this.experience).build();
        }
    }

    @Override
    public void giveReward(CorePlayer player) {
        Bukkit.getLogger().severe("not implemented (experience reward)");
    }
}
