package me.blutkrone.rpgcore.mob.loot;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.item.EditorLoot;
import me.blutkrone.rpgcore.hud.editor.bundle.loot.EditorLootItem;
import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorAttributeAndFactor;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ItemCoreLoot extends AbstractCoreLoot {

    // which items we can drop
    private WeightedRandomMap<String> item_choice = new WeightedRandomMap<>();
    // number of items to be dropped
    private double quantity;
    private Map<String, Double> quantity_killed;
    private Map<String, Double> quantity_killer;
    // offers less likely to drop items
    private double rarity;
    private Map<String, Double> rarity_killed;
    private Map<String, Double> rarity_killer;
    // enhances random details of the item
    private double quality;
    private Map<String, Double> quality_killed;
    private Map<String, Double> quality_killer;

    public ItemCoreLoot(EditorLootItem editor) {
        for (IEditorBundle bundle : editor.item_weights) {
            EditorLoot casted = (EditorLoot) bundle;
            item_choice.add(casted.weight, casted.tag);
        }
        
        quality = editor.quality;
        quality_killer = EditorAttributeAndFactor.unwrap(editor.quality_killer);
        quality_killed = EditorAttributeAndFactor.unwrap(editor.quality_killed);

        rarity = editor.rarity;
        rarity_killer = EditorAttributeAndFactor.unwrap(editor.rarity_killer);
        rarity_killed = EditorAttributeAndFactor.unwrap(editor.rarity_killed);

        quantity = editor.quantity;
        quantity_killer = EditorAttributeAndFactor.unwrap(editor.quantity_killer);
        quantity_killed = EditorAttributeAndFactor.unwrap(editor.quantity_killed);
    }

    @Override
    public void offer(CoreEntity killed, List<CorePlayer> killers) {
        // random player given reward
        CorePlayer killer = killers.get(ThreadLocalRandom.current().nextInt(killers.size()));

        // ensure we got a valid location to drop at
        Location location = killed.getLocation();
        if (location == null || location.getWorld() == null) {
            throw new NullPointerException("No location to drop items at!");
        }

        // base information on our drops
        double rarity = Math.max(0d, getRarity(killed, killer));
        double quantity = Math.max(0d, getQuantity(killed, killer));
        double quality = Math.max(0d, getQuality(killed, killer));

        // quantity trims off at certain breakpoints
        quantity = Math.sqrt(quantity);
        if (Math.random() < quantity % 1d) {
            quantity = quantity + 1;
        }

        // fetch random items to drop
        for (int i = 0; i < quantity; i++) {
            List<String> picked = new ArrayList<>();
            // one item is always picked
            picked.add(item_choice.next());
            // additional items picked via rarity
            double working_rarity = Math.sqrt(100*rarity)*0.01;
            while (Math.random() <= working_rarity--) {
                picked.add(item_choice.next());
            }
            // only item with lowest weight is used
            String item_to_drop = picked.stream().min(Comparator.comparingDouble(item -> item_choice.weight(item))).orElse(null);
            if (item_to_drop == null) {
                continue;
            }
            CoreItem core_item = RPGCore.inst().getItemManager().getItemIndex().get(item_to_drop);
            // drop a physical copy of the item
            ItemStack bukkit_stack = core_item.acquire(killer, (0.3 * quality) + (0.7 * Math.random() * quality));
            Item item_entity = location.getWorld().dropItem(location, bukkit_stack);
            item_entity.setVelocity(new Vector(Math.random()*2-1, 0.7d, Math.random()*2-1));
        }
    }

    /*
     * Quantity is intended to increase how many instances of the
     * reward are rolled.
     *
     * @param killed who generated the reward
     * @param killer who receives the reward
     * @return quantity of the reward offered.
     */
    private double getQuantity(CoreEntity killed, CorePlayer killer) {
        // base factor independent of parties
        double output = this.quantity;
        // gained based on the killed
        if (killed != null) {
            for (Map.Entry<String, Double> entry : quantity_killed.entrySet()) {
                output += killed.getAttribute(entry.getKey()).evaluate() * entry.getValue();
            }
        }
        output = Math.max(0d, output);
        // gained based on the killer
        if (killer != null) {
            double multiplier = 1d;
            for (Map.Entry<String, Double> entry : quantity_killer.entrySet()) {
                multiplier += killer.getAttribute(entry.getKey()).evaluate() * entry.getValue();
            }
            output = output * multiplier;
        }
        // offer up our computed value
        return Math.max(0d, output);
    }

    /*
     * Rarity is intended to enhance the quality of the reward
     * received, however certain rewards may not be affected by
     * the rarity.
     *
     * @param killed who generated the reward
     * @param killer who receives the reward
     * @return rarity of the reward offered.
     */
    private double getRarity(CoreEntity killed, CorePlayer killer) {
        // base factor independent of parties
        double output = this.rarity;
        // gained based on the killed
        if (killed != null) {
            for (Map.Entry<String, Double> entry : this.rarity_killed.entrySet()) {
                output += killed.getAttribute(entry.getKey()).evaluate() * entry.getValue();
            }
        }
        output = Math.max(0d, output);
        // gained based on the killer
        if (killer != null) {
            double multiplier = 1d;
            for (Map.Entry<String, Double> entry : this.rarity_killer.entrySet()) {
                multiplier += killer.getAttribute(entry.getKey()).evaluate() * entry.getValue();
            }
            output = output * multiplier;
        }
        // offer up our computed value
        return Math.max(0d, output);
    }

    /*
     * Quantity affects how well the item is expected to be rolled.
     *
     * @param killed who generated the reward
     * @param killer who receives the reward
     * @return quantity of the reward offered.
     */
    private double getQuality(CoreEntity killed, CorePlayer killer) {
        // base factor independent of parties
        double output = this.quality;
        // gained based on the killed
        if (killed != null) {
            for (Map.Entry<String, Double> entry : quality_killed.entrySet()) {
                output += killed.getAttribute(entry.getKey()).evaluate() * entry.getValue();
            }
        }
        output = Math.max(0d, output);
        // gained based on the killer
        if (killer != null) {
            double multiplier = 1d;
            for (Map.Entry<String, Double> entry : quality_killer.entrySet()) {
                multiplier += killer.getAttribute(entry.getKey()).evaluate() * entry.getValue();
            }
            output = output * multiplier;
        }
        // offer up our computed value
        return Math.max(0d, output);
    }
}
