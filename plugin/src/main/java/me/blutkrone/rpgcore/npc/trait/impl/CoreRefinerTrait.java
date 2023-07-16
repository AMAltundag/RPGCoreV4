package me.blutkrone.rpgcore.npc.trait.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.bundle.npc.EditorRefinerTrait;
import me.blutkrone.rpgcore.editor.index.IndexAttachment;
import me.blutkrone.rpgcore.item.refinement.CoreRefinerRecipe;
import me.blutkrone.rpgcore.npc.CoreNPC;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Refine basic crafting items with a matching menu
 * design and reagents to increase their yield.
 */
public class CoreRefinerTrait extends AbstractCoreTrait {
    // recipes available to this refiner
    public IndexAttachment<CoreRefinerRecipe, List<CoreRefinerRecipe>> recipes;
    // which refinement design to use
    public String design;
    // which inventory to use
    public String inventory;
    // multiplier to refinement speed
    public double speed;
    // multiplier to refinement yield
    public double quantity;

    public CoreRefinerTrait(EditorRefinerTrait editor) {
        super(editor);

        List<String> recipes = new ArrayList<>(editor.recipes);
        recipes.replaceAll(String::toLowerCase);
        this.recipes = RPGCore.inst().getItemManager().getRefineIndex().createFiltered((recipe -> {
            // retain all recipes matching the tag
            for (String tag : recipes) {
                if (recipe.getTags().contains(tag)) {
                    return true;
                }
            }
            // drop recipes who did not match any tag
            return false;
        }));
        this.design = editor.design;
        this.inventory = editor.inventory;
        this.speed = Math.max(0d, editor.speed);
        this.quantity = editor.quantity;
    }

    @Override
    public void engage(Player _player, CoreNPC npc) {
        RPGCore.inst().getHUDManager().getRefinerMenu().present(_player, this);
    }
}
