package me.blutkrone.rpgcore.quest.task.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.item.EditorItemWithQuantity;
import me.blutkrone.rpgcore.hud.editor.bundle.quest.task.EditorQuestTaskDeliver;
import me.blutkrone.rpgcore.item.data.ItemDataGeneric;
import me.blutkrone.rpgcore.npc.CoreNPC;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.quest.task.AbstractQuestTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

/**
 * Deliver certain items to an NPC.
 */
public class CoreQuestTaskDeliver extends AbstractQuestTask<Object> {

    // what items need to be delivered
    private Map<String, Integer> items = new HashMap<>();
    // who takes the drop-off
    private String drop_off_npc;
    // approximation on delivery matching
    private final Set<UUID> approximate_delivery = new HashSet<>();

    public CoreQuestTaskDeliver(CoreQuest quest, EditorQuestTaskDeliver editor) {
        super(quest, editor);

        for (IEditorBundle bundle : editor.demand) {
            EditorItemWithQuantity demand = (EditorItemWithQuantity) bundle;
            this.items.merge(demand.item, ((int) demand.quantity), (a, b) -> a + b);
        }
        this.drop_off_npc = editor.npc;
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
        // identify how many items we request
        Map<String, Integer> ask = new HashMap<>(this.items);

        // consume the items still on the ask
        PlayerInventory inventory = player.getEntity().getInventory();
        for (ItemStack item : inventory.getContents()) {
            // make sure we got a core item
            ItemDataGeneric data = RPGCore.inst().getItemManager().getItemData(item, ItemDataGeneric.class);
            if (data == null) {
                continue;
            }
            // identify basic data for the task
            String id = data.getItem().getId();
            int asking = ask.getOrDefault(id, 0);
            if (asking <= 0) {
                continue;
            }
            int absorb = Math.min(asking, item.getAmount());
            // consume from the stack of the player
            item.setAmount(item.getAmount() - absorb);
            // reduce the ask of items
            ask.merge(id, -absorb, (a, b) -> a + b);
        }

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
    public boolean canMeetDemandApproximation(UUID player) {
        Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
            // gc for players who quit the server
            synchronized (approximate_delivery) {
                if (approximate_delivery.size() > 200) {
                    approximate_delivery.clear();
                }
            }

            Player bukkit_player = Bukkit.getPlayer(player);
            if (bukkit_player != null) {
                // identify how many items we do carry
                Map<String, Integer> carried = new HashMap<>();
                for (ItemStack stack : bukkit_player.getInventory().getContents()) {
                    ItemDataGeneric data = RPGCore.inst().getItemManager().getItemData(stack, ItemDataGeneric.class);
                    if (data != null) {
                        carried.merge(data.getItem().getId(), stack.getAmount(), (a, b) -> a + b);
                    }
                }

                // ensure we carry enough to meet our demand
                boolean matched = true;
                for (Map.Entry<String, Integer> demand : this.items.entrySet()) {
                    int offer = carried.getOrDefault(demand.getKey(), 0);
                    if (demand.getValue() > offer) {
                        matched = false;
                    }
                }

                // track within the approximation
                synchronized (approximate_delivery) {
                    if (matched) {
                        approximate_delivery.add(player);
                    } else {
                        approximate_delivery.remove(player);
                    }
                }
            }
        });

        synchronized (approximate_delivery) {
            return approximate_delivery.contains(player);
        }
    }

    /**
     * Verify if the given NPC is a valid drop-off point.
     *
     * @param npc who to check against.
     * @return true if we accept a drop-off here.
     */
    public boolean isDropOff(CoreNPC npc) {
        return npc.getId().equalsIgnoreCase(this.drop_off_npc);
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
