package me.blutkrone.rpgcore.quest.reward.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.bundle.quest.reward.EditorQuestRewardItem;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.quest.reward.AbstractQuestReward;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CoreItemReward extends AbstractQuestReward {

    private String item;
    private int amount;

    public CoreItemReward(EditorQuestRewardItem editor) {
        this.item = editor.item;
        this.amount = (int) editor.amount;
    }

    @Override
    public ItemStack getPreview(CorePlayer player) {
        CoreItem core_item = RPGCore.inst().getItemManager().getItemIndex().get(this.item);
        return core_item.unidentified();
    }

    @Override
    public void giveReward(CorePlayer player) {
        CoreItem core_item = RPGCore.inst().getItemManager().getItemIndex().get(this.item);
        ItemStack stack = core_item.acquire(player, 0d);
        if (!core_item.isUnstackable()) {
            stack.setAmount(Math.min(stack.getMaxStackSize(), this.amount));
        }

        Player p = player.getEntity();
        if (p.getInventory().firstEmpty() == -1) {
            p.getWorld().dropItem(p.getLocation(), stack);
        } else {
            p.getInventory().addItem(stack);
        }
    }
}
