package me.blutkrone.rpgcore.hud.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.item.crafting.CoreCraftingRecipe;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.npc.trait.impl.CoreCrafterTrait;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CrafterMenu {

    public CrafterMenu() {
    }

    public void present(Player player, CoreCrafterTrait trait) {
        new me.blutkrone.rpgcore.menu.CrafterMenu(trait, getPreview(player, trait)).finish(player);
    }

    /*
     * Retrieve all recipes the user is qualified to see, the stacks
     * are flagged with appropriate information.
     *
     * @param bukkit_player who to build relative toward
     * @param core_player who to build relative toward
     * @return stacks hinting at craft-ability
     */
    private List<ItemStack> getPreview(Player bukkit_player, CoreCrafterTrait trait) {
        List<ItemStack> previews_header = new ArrayList<>();
        List<ItemStack> previews_footer = new ArrayList<>();

        // get all items technically craft-able
        List<CoreCraftingRecipe> allowed = trait.recipes.get();

        // check if player got the ingredients
        for (CoreCraftingRecipe recipe : allowed) {
            ItemStack stack = recipe.getOutput().unidentified();
            IChestMenu.setBrand(stack, RPGCore.inst(), "recipe-id", recipe.getId());
            if (recipe.isMatched(bukkit_player)) {
                IChestMenu.setBrand(stack, RPGCore.inst(), "recipe-affordable", "1");
                previews_header.add(stack);
            } else {
                IChestMenu.setBrand(stack, RPGCore.inst(), "recipe-affordable", "0");
                previews_footer.add(stack);
            }
        }

        // pool into one collection (this keeps available recipes front-loaded.)
        previews_header.addAll(previews_footer);
        return previews_header;
    }
}
