package me.blutkrone.rpgcore.quest.task.impl;

import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.item.EditorItemWithQuantity;
import me.blutkrone.rpgcore.hud.editor.bundle.quest.task.EditorQuestTaskCollect;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.quest.task.AbstractQuestTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoreQuestTaskCollect extends AbstractQuestTask<Object> {

    // what items need to be delivered
    private Map<String, Integer> items = new HashMap<>();

    public CoreQuestTaskCollect(CoreQuest quest, EditorQuestTaskCollect editor) {
        super(quest, editor);

        for (IEditorBundle bundle : editor.demand) {
            EditorItemWithQuantity demand = (EditorItemWithQuantity) bundle;
            this.items.merge(demand.item, ((int) demand.quantity), (a, b) -> a + b);
        }
    }

    @Override
    public List<String> getInfo(CorePlayer player) {
        Map<String, Integer> quantified = player.getSnapshotForQuestItems();

        // substitute the item patterns
        List<String> info = super.getInfo(player);
        info.replaceAll(line -> {
            for (Map.Entry<String, Integer> entry : items.entrySet()) {
                String item = entry.getKey();

                int want = entry.getValue();
                int holding = quantified.getOrDefault(item, 0);

                if (holding >= want) {
                    // holding enough to deliver
                    line = line.replace("{" + item + "}", "§e" + holding + "/" + want);
                } else {
                    // still need to gather items
                    line = line.replace("{" + item + "}", "§c" + holding + "/" + want);
                }
            }
            return line;
        });
        // offer the info pattern
        return info;
    }

    @Override
    public boolean taskIsComplete(CorePlayer player) {
        return player.getProgressQuests().getOrDefault(this.getUniqueId() + "_delivered", 0) == 1;
    }

    @Override
    public void updateQuest(CorePlayer player, Object param) {
        // an external check assured that we got enough
        player.getProgressQuests().put(this.getUniqueId() + "_delivered", 1);
    }

    @Override
    public List<String> getDataIds() {
        List<String> ids = new ArrayList<>();
        ids.add(this.getUniqueId() + "_delivered");
        return ids;
    }

    /**
     * Check if the player is capable of meeting the demand
     * for this delivery quest, this is an APPROXIMATION do
     * not expect accuracy off this method.
     *
     * @param player who wants to deliver the items.
     * @return true if they got all the items.
     */
    public boolean canMeetDemand(CorePlayer player) {
        Map<String, Integer> carried = player.getSnapshotForQuestItems();

        // ensure we carry enough to meet our demand
        boolean matched = true;
        for (Map.Entry<String, Integer> demand : this.items.entrySet()) {
            int offer = carried.getOrDefault(demand.getKey(), 0);
            if (demand.getValue() > offer) {
                matched = false;
            }
        }

        return matched;
    }

    /**
     * Item ids mapped to a quantity.
     *
     * @return the items we are demanding
     */
    public Map<String, Integer> getDemand() {
        return items;
    }
}
