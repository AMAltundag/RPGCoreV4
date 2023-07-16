package me.blutkrone.rpgcore.quest.reward.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.bundle.quest.reward.EditorQuestRewardExp;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.language.LanguageManager;
import me.blutkrone.rpgcore.level.LevelManager;
import me.blutkrone.rpgcore.quest.reward.AbstractQuestReward;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CoreExpReward extends AbstractQuestReward {

    private List<String> attributes;
    private int scaling_level;
    private int experience;

    public CoreExpReward(EditorQuestRewardExp editor) {
        this.experience = (int) editor.experience;
        this.scaling_level = (int) editor.scaling_level;
        this.attributes = new ArrayList<>(editor.scaling_attributes);
    }

    @Override
    public ItemStack getPreview(CorePlayer player) {
        LanguageManager lpm = RPGCore.inst().getLanguageManager();
        return lpm.getAsItem("quest_reward_exp", this.experience).build();
    }

    @Override
    public void giveReward(CorePlayer player) {
        LevelManager manager = RPGCore.inst().getLevelManager();
        // base amount of exp
        double experience = this.experience;
        // exp scaled by level difference
        int self = player.getCurrentLevel();
        if (this.scaling_level > 0) {
            experience *= manager.getMultiplier(self, this.scaling_level);
        }
        // exp multiplied by player attributes
        double multiplier = 1d;
        multiplier += player.evaluateAttribute("exp_multi_quest");
        for (String attribute : this.attributes) {
            multiplier += player.evaluateAttribute(attribute);
        }
        experience = experience * multiplier;
        // grant the experience
        if (experience > 0d) {
            player.setCurrentExp(player.getCurrentExp() + experience);
        }
    }
}
