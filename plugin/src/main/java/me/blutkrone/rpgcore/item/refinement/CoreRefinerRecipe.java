package me.blutkrone.rpgcore.item.refinement;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.bundle.item.EditorLoot;
import me.blutkrone.rpgcore.editor.index.IndexAttachment;
import me.blutkrone.rpgcore.editor.root.item.EditorRefineRecipe;
import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.item.ItemManager;
import me.blutkrone.rpgcore.item.data.ItemDataGeneric;
import me.blutkrone.rpgcore.job.CoreProfession;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    // levelling related information
    private Set<String> tag_required_to_refine;
    private String profession;
    private int profession_exp_gained;
    private int profession_exp_maximum_level;
    private int profession_level_required;

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

        this.tag_required_to_refine = editor.tag_requirement.stream()
                .map(String::toLowerCase).collect(Collectors.toSet());
        this.profession = editor.profession.equalsIgnoreCase("nothingness")
                ? null : editor.profession.toLowerCase();
        this.profession_exp_gained = (int) editor.profession_exp_gained;
        this.profession_exp_maximum_level = (int) editor.profession_exp_maximum_level;
        this.profession_level_required = (int) editor.profession_level_required;
    }

    public int getProfessionExpGained() {
        return profession_exp_gained;
    }

    public int getProfessionExpMaximumLevel() {
        return profession_exp_maximum_level;
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
     * @param player  who is crafting the recipe
     * @param attempt how many attempts to craft
     * @param items   materials to consume for crafting
     * @return how often we could craft
     */
    public int craftAndConsume(CorePlayer player, int attempt, List<ItemStack> items) {
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
        // offer relevant experience reward
        if (success > 0 && getProfession() != null) {
            CoreProfession profession = RPGCore.inst().getJobManager().getIndexProfession().get(getProfession());
            profession.gainExpFromRefinement(player, this, success);
        }
        // inform about how many refinements we did
        return success;
    }

    /**
     * The profession relevant for the recipe.
     *
     * @return relevant profession.
     */
    public String getProfession() {
        return profession;
    }

    /**
     * Check if this recipe is available to the targeted
     * player.
     *
     * @param core_player who do we check against
     * @return whether we've got the recipe unlocked
     */
    public boolean hasUnlocked(CorePlayer core_player) {
        // check if we meet the level requirement
        if (this.getProfession() != null) {
            int have_level = core_player.getProfessionLevel().getOrDefault(getProfession(), 1);
            if (have_level < this.profession_level_required) {
                return false;
            }
        }
        // check if we meet the tag requirement
        for (String tag : this.tag_required_to_refine) {
            if (!core_player.checkForTag(tag)) {
                return false;
            }
        }
        // we can craft the recipe
        return true;
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
            effect.show(player.getLocation(), player);
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
            effect.show(player.getLocation(), player);
        }
    }
}
