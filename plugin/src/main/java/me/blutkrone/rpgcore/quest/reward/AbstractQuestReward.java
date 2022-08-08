package me.blutkrone.rpgcore.quest.reward;

import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractQuestReward {
    /**
     * Construct a preview of a reward.
     *
     * @param player reward preview.
     * @return reward preview.
     */
    public abstract ItemStack getPreview(CorePlayer player);

    /**
     * Grant the reward to the player, this should be a best
     * effort (i.E: If items cannot be picked, drop them on
     * the ground.)
     *
     * @param player who wants to claim the reward
     */
    public abstract void giveReward(CorePlayer player);
}
