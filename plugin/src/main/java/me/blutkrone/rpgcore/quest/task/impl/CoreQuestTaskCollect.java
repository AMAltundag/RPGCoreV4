package me.blutkrone.rpgcore.quest.task.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.item.EditorItemWithQuantity;
import me.blutkrone.rpgcore.editor.bundle.quest.task.EditorQuestTaskCollect;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.node.struct.NodeActive;
import me.blutkrone.rpgcore.node.struct.NodeWorld;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.quest.task.AbstractQuestTask;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoreQuestTaskCollect extends AbstractQuestTask<Object> {

    // what items need to be delivered
    private Map<String, Integer> items = new HashMap<>();
    // where we can find a certain item
    private Map<String, List<String>> where_to_find = new HashMap<>();

    public CoreQuestTaskCollect(CoreQuest quest, EditorQuestTaskCollect editor) {
        super(quest, editor);

        for (IEditorBundle bundle : editor.demand) {
            EditorItemWithQuantity demand = (EditorItemWithQuantity) bundle;
            this.items.merge(demand.item, ((int) demand.quantity), Integer::sum);
            this.where_to_find.put(demand.item, new ArrayList<>(demand.gathering_area));
        }
    }

    @Override
    public List<Location> getHints(CorePlayer core, Player bukkit) {
        List<Location> output = new ArrayList<>();

        NodeWorld node_world = RPGCore.inst().getNodeManager().getNodeWorld(bukkit.getWorld());

        // where items we are missing can be gathered
        if (node_world != null) {
            Map<String, Integer> carried = core.getSnapshotForQuestItems();

            for (Map.Entry<String, Integer> demand : this.items.entrySet()) {
                int offer = carried.getOrDefault(demand.getKey(), 0);
                if (demand.getValue() > offer) {
                    List<String> where_to_find = this.where_to_find.get(demand.getKey());
                    for (String node_type : where_to_find) {
                        for (NodeActive node : node_world.getNodesOfType(node_type)) {
                            output.add(new Location(bukkit.getWorld(), node.getX(), node.getY(), node.getZ()));
                        }
                    }
                }
            }
        }

        return output;
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
