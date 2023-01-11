package me.blutkrone.rpgcore.item.crafting;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.item.EditorItemWithQuantity;
import me.blutkrone.rpgcore.hud.editor.root.item.EditorCraftingRecipe;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.item.data.ItemDataGeneric;
import me.blutkrone.rpgcore.job.CoreProfession;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class CoreCraftingRecipe {

    private String id;
    private Map<String, Integer> ingredients = new HashMap<>();
    private String output;
    private double quantity;
    private String effect_crafted;
    private String effect_failed;
    private List<String> tags;
    private Set<String> tag_required_to_craft;
    private String profession;
    private int profession_exp_gained;
    private int profession_exp_maximum_level;
    private int profession_level_required;

    public CoreCraftingRecipe(String id, EditorCraftingRecipe editor) {
        this.id = id;
        this.tags = new ArrayList<>(editor.tags);
        this.tags.add("DIRECT_" + id);
        this.tags.replaceAll(String::toLowerCase);
        for (IEditorBundle ingredient : editor.ingredients) {
            EditorItemWithQuantity bundle = ((EditorItemWithQuantity) ingredient);
            this.ingredients.merge(bundle.item, Math.max(0, (int) bundle.quantity), (a, b) -> a + b);
        }
        this.ingredients.entrySet().forEach(entry -> entry.setValue(Math.min(64, entry.getValue())));
        this.output = editor.output;
        this.quantity = editor.quantity;
        this.effect_crafted = editor.effect_crafted;
        this.effect_failed = editor.effect_failed;

        this.tag_required_to_craft = editor.tag_requirement.stream()
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

    public String getProfession() {
        return profession;
    }

    /**
     * Check if player has the resources to craft this item.
     *
     * @param bukkit_player whose inventory do we check
     * @return true if we are craftable
     */
    public boolean hasEnoughToCraftOnce(Player bukkit_player) {
        // count items in player inventory
        Map<String, Integer> total_items_of_player = new HashMap<>();
        for (ItemStack stack : bukkit_player.getInventory().getContents()) {
            ItemDataGeneric data = RPGCore.inst().getItemManager().getItemData(stack, ItemDataGeneric.class);
            if (data != null) {
                total_items_of_player.merge(data.getItem().getId(), stack.getAmount(), (a, b) -> a + b);
            }
        }
        // check if we have enough items
        for (Map.Entry<String, Integer> entry : this.ingredients.entrySet()) {
            if (total_items_of_player.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                return false;
            }
        }
        // offer true if we have any ingredients
        return true;
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
        for (String tag : this.tag_required_to_craft) {
            if (!core_player.checkForTag(tag)) {
                return false;
            }
        }
        // we can craft the recipe
        return true;
    }

    /**
     * Attempt to craft a given amount of an item, this amount
     * should never be more then 1 stack of it. Since this method
     * will consume the materials.
     *
     * @param amount        how many to craft at most.
     * @param bukkit_player who is doing the crafting
     * @return how much we've crafted, less-equal to amount
     */
    public int craftAndConsume(int amount, Player bukkit_player) {
        // identify exact quantity of materials available
        Map<String, Integer> total_items_of_player = new HashMap<>();
        for (ItemStack stack : bukkit_player.getInventory().getContents()) {
            ItemDataGeneric data = RPGCore.inst().getItemManager().getItemData(stack, ItemDataGeneric.class);
            if (data != null) {
                total_items_of_player.merge(data.getItem().getId(), stack.getAmount(), (a, b) -> a + b);
            }
        }
        // cap the amount by how much the player is carrying
        for (Map.Entry<String, Integer> entry : this.ingredients.entrySet()) {
            int need = entry.getValue();
            int have = total_items_of_player.getOrDefault(entry.getKey(), 0);
            amount = Math.min(amount, have / need);
        }
        // if amount is zero, we cannot afford the craft
        if (amount <= 0) {
            return 0;
        }
        // destroy items from the player inventory
        for (Map.Entry<String, Integer> cost : this.ingredients.entrySet()) {
            int want = cost.getValue();
            if (want <= 0) {
                continue;
            }
            for (ItemStack stack : bukkit_player.getInventory().getContents()) {
                if (want <= 0) {
                    break;
                }
                ItemDataGeneric data = RPGCore.inst().getItemManager().getItemData(stack, ItemDataGeneric.class);
                if (data == null || !cost.getKey().equalsIgnoreCase(data.getItem().getId())) {
                    continue;
                }
                int have = stack.getAmount();
                int absorbed = Math.min(have, want);
                stack.setAmount(have - absorbed);
                want -= absorbed;
            }
        }
        // offer relevant experience reward
        if (getProfession() != null) {
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(bukkit_player);
            CoreProfession profession = RPGCore.inst().getJobManager().getIndexProfession().get(getProfession());
            profession.gainExpFromCrafting(core_player, this, amount);
        }
        // offer the volume we can merge
        return amount;
    }

    /**
     * Retrieve the recipe we are working with.
     *
     * @return the identifier we got.
     */
    public String getId() {
        return id;
    }

    /**
     * The tags who identify this recipe.
     *
     * @return tags for this recipe.
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * What ingredients are needed to craft this recipe.
     *
     * @return the ingredients
     */
    public Map<CoreItem, Integer> getIngredients() {
        Map<CoreItem, Integer> ingredients = new HashMap<>();
        this.ingredients.forEach((id, amount) -> {
            CoreItem item = RPGCore.inst().getItemManager().getItemIndex().get(id);
            ingredients.put(item, amount);
        });
        return ingredients;
    }

    /**
     * How many items are output from this, decimals are treated as a chance.
     *
     * @return integers are stacks, decimals are a chance.
     */
    public double getQuantity() {
        return quantity;
    }

    /**
     * What item is offered as the output.
     *
     * @return output item.
     */
    public CoreItem getOutput() {
        return RPGCore.inst().getItemManager().getItemIndex().get(this.output);
    }
}
