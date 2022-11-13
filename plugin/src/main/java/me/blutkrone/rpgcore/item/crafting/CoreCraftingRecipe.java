package me.blutkrone.rpgcore.item.crafting;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.item.EditorItemWithQuantity;
import me.blutkrone.rpgcore.hud.editor.root.item.EditorCraftingRecipe;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.item.data.ItemDataGeneric;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoreCraftingRecipe {

    private String id;
    private Map<String, Integer> ingredients = new HashMap<>();
    private String output;
    private double quantity;
    private String effect_crafted;
    private String effect_failed;
    private List<String> tags;

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
    }

    /**
     * Check if player has the resources to craft this item.
     *
     * @param bukkit_player whose inventory do we check
     * @return true if we are craftable
     */
    public boolean isMatched(Player bukkit_player) {
        // count items in player inventory
        Map<String, Integer> counted = new HashMap<>();
        for (ItemStack stack : bukkit_player.getInventory().getContents()) {
            ItemDataGeneric data = RPGCore.inst().getItemManager().getItemData(stack, ItemDataGeneric.class);
            if (data != null) {
                counted.merge(data.getItem().getId(), stack.getAmount(), (a, b) -> a + b);
            }
        }
        // check if we have enough items
        for (Map.Entry<String, Integer> entry : this.ingredients.entrySet()) {
            if (counted.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                return false;
            }
        }
        // offer true if we have any ingredients
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
        Map<String, Integer> available = new HashMap<>();
        for (ItemStack stack : bukkit_player.getInventory().getContents()) {
            ItemDataGeneric data = RPGCore.inst().getItemManager().getItemData(stack, ItemDataGeneric.class);
            if (data != null) {
                available.merge(data.getItem().getId(), stack.getAmount(), (a, b) -> a + b);
            }
        }
        // search for lowest common availability
        int capacity = amount;
        for (Map.Entry<String, Integer> entry : this.ingredients.entrySet()) {
            int need = entry.getValue();
            int have = available.getOrDefault(entry.getKey(), 0);
            capacity = Math.min(capacity, have / need);
        }
        if (capacity <= 0) {
            return 0;
        }
        // consume materials for the stack
        for (ItemStack stack : bukkit_player.getInventory().getContents()) {
            ItemDataGeneric data = RPGCore.inst().getItemManager().getItemData(stack, ItemDataGeneric.class);
            if (data != null) {
                int consume = this.ingredients.getOrDefault(data.getItem().getId(), 0) * capacity;
                if (consume > 0) {
                    int absorbed = Math.min(consume, stack.getAmount());
                    stack.setAmount(stack.getAmount() - absorbed);
                    this.ingredients.put(data.getItem().getId(), consume - absorbed);
                }
            }
        }
        // offer the volume we can merge
        return capacity;
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
