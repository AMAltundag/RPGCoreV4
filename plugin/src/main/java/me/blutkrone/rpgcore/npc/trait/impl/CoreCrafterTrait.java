package me.blutkrone.rpgcore.npc.trait.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.index.IndexAttachment;
import me.blutkrone.rpgcore.hud.editor.root.npc.EditorCrafterTrait;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.item.crafting.CoreCraftingRecipe;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Combine certain items to get another item out of it.
 */
public class CoreCrafterTrait extends AbstractCoreTrait {

    public IndexAttachment<CoreCraftingRecipe, List<CoreCraftingRecipe>> recipes;
    public IndexAttachment<CoreCraftingRecipe, Map<String, List<CoreCraftingRecipe>>> reverse;

    public CoreCrafterTrait(EditorCrafterTrait editor) {
        super(editor);
        List<String> recipes = new ArrayList<>(editor.recipes);
        recipes.replaceAll(String::toLowerCase);
        // offer all recipes available
        this.recipes = RPGCore.inst().getItemManager().getCraftIndex().createFiltered((recipe -> {
            for (String tag : recipe.getTags()) {
                if (recipes.contains(tag)) {
                    return true;
                }
            }

            return false;
        }));
        // reverse map to hop to ingredients
        this.reverse = RPGCore.inst().getItemManager().getCraftIndex().createAttachment((index -> {
            Map<String, List<CoreCraftingRecipe>> reversed = new HashMap<>();
            for (CoreCraftingRecipe recipe : index.getAll()) {
                CoreItem output = recipe.getOutput();
                reversed.computeIfAbsent(output.getId(), (k -> new ArrayList<>())).add(recipe);
            }
            return reversed;
        }));
    }

    @Override
    public void engage(Player player) {
        RPGCore.inst().getHUDManager().getCrafterMenu().present(player, this);
    }
}