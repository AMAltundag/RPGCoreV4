package me.blutkrone.rpgcore.item.refinement;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.hud.editor.bundle.item.EditorLoot;
import me.blutkrone.rpgcore.hud.editor.index.IndexAttachment;
import me.blutkrone.rpgcore.hud.editor.root.item.EditorRefineRecipe;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.item.ItemManager;
import me.blutkrone.rpgcore.item.data.ItemDataGeneric;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A progression path for collected resources.
 */
public class CoreRefinerRecipe {

    private IndexAttachment<CoreItem, WeightedRandomMap<CoreItem>> item_choices;
    private String id;
    private List<String> ingredients = new ArrayList<>();
    private double quantity;
    private int duration;
    private double priority;
    private String effect_working;
    private String effect_finished;
    private List<String> tags;

    public CoreRefinerRecipe(String id, EditorRefineRecipe editor) {
        this.item_choices = EditorLoot.build(new ArrayList<>(editor.outputs));
        this.tags = new ArrayList<>(editor.tags);
        this.tags.add("DIRECT_" + id);
        this.tags.replaceAll(String::toLowerCase);
        this.id = id;
        this.ingredients.addAll(editor.ingredients);
        this.quantity = editor.quantity;
        this.duration = (int) editor.duration;
        this.priority = editor.priority;
        this.effect_working = editor.effect_working;
        this.effect_finished = editor.effect_finished;
    }

    public String getId() {
        return id;
    }

    /**
     * Tags identifying this recipe.
     *
     * @return recipes by tag.
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Craft this recipe several times, offering up the total
     * crafts - however often it fit in the crafting attempt.
     *
     * @param attempt how many attempts to craft
     * @param items   materials to consume for crafting
     * @return how often we could craft
     */
    public int craftAndConsume(int attempt, List<ItemStack> items) {
        ItemManager manager = RPGCore.inst().getItemManager();
        // identify the material types
        List<String> types = new ArrayList<>();
        for (ItemStack item : items) {
            ItemDataGeneric data = manager.getItemData(item, ItemDataGeneric.class);
            if (data != null) {
                types.add(data.getItem().getId().toLowerCase());
            } else {
                types.add("none");
            }
        }
        // consume stacks until no longer meeting condition
        int success = 0;
        for (int i = 0; i < attempt; i++) {
            // check if can still craft
            List<String> required = new ArrayList<>(this.ingredients);
            for (String type : types) {
                required.remove(type);
            }
            // if we got left-over, we cannot craft
            if (!required.isEmpty()) {
                break;
            }
            // increment our counter
            success++;
            // consume material from our stack
            for (String ingredient : this.ingredients) {
                for (int j = 0; j < types.size(); j++) {
                    if (types.get(j).equalsIgnoreCase(ingredient)) {
                        ItemStack consumed = items.get(j);
                        consumed.setAmount(consumed.getAmount() - 1);
                        if (consumed.getAmount() <= 0) {
                            types.set(j, "none");
                        }
                    }
                }
            }
        }
        return success;
    }


    /**
     * Check if the given ingredients archive this rule.
     *
     * @param items ingredient items
     * @return true if rule is matched.
     */
    public boolean isMatched(List<ItemStack> items) {
        ItemManager manager = RPGCore.inst().getItemManager();
        // pop away all ingredients we've got
        List<String> required = new ArrayList<>(this.ingredients);
        for (ItemStack item : items) {
            ItemDataGeneric data = manager.getItemData(item, ItemDataGeneric.class);
            if (data != null) {
                required.remove(data.getItem().getId().toLowerCase());
            }
        }
        // if no ingredients remain, we can refine this
        return required.isEmpty();
    }

    /**
     * Amount of items to reward, this may differ with each call
     * since we cover percentages.
     *
     * @return amount to reward.
     */
    public double getQuantity() {
        int amount = (int) this.quantity;
        double chance = this.quantity - amount;
        if (Math.random() <= chance) {
            amount += 1;
        }
        return amount;
    }

    /**
     * Receive a random item from the rewards.
     *
     * @return rewards offered by the refiner rule.
     */
    public CoreItem getRandomOutput() {
        // generate a drop table for the node
        WeightedRandomMap<CoreItem> items = this.item_choices.get();

        // nothing in case no items could be discovered
        if (items.isEmpty()) {
            return null;
        }

        // offer up a random item from our sample
        return items.next();
    }

    /**
     * Duration of refinement process, counted in ticks.
     *
     * @return refinement duration in ticks.
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Priority for this rule, only highest priority rule
     * will be crafted.
     *
     * @return priority of this rule.
     */
    public double getPriority() {
        return priority;
    }

    /**
     * Who to present the refinement effect to.
     *
     * @param player who receives the effect
     */
    public void playEffectWorking(Player player) {
        if (!this.effect_working.equalsIgnoreCase("NOTHINGNESS")) {
            CoreEffect effect = RPGCore.inst().getEffectManager().getIndex().get(this.effect_working);
            effect.show(player.getLocation(), 1d, Collections.singletonList(player));
        }
    }

    /**
     * Who to present the refinement effect to.
     *
     * @param player who receives the effect
     */
    public void playEffectFinished(Player player) {
        if (!this.effect_finished.equalsIgnoreCase("NOTHINGNESS")) {
            CoreEffect effect = RPGCore.inst().getEffectManager().getIndex().get(this.effect_finished);
            effect.show(player.getLocation(), 1d, Collections.singletonList(player));
        }
    }
}
