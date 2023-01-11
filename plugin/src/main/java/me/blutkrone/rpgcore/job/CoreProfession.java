package me.blutkrone.rpgcore.job;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorStringAndNumber;
import me.blutkrone.rpgcore.hud.editor.index.EditorIndex;
import me.blutkrone.rpgcore.hud.editor.root.job.EditorProfession;
import me.blutkrone.rpgcore.hud.editor.root.passive.EditorPassiveTree;
import me.blutkrone.rpgcore.item.crafting.CoreCraftingRecipe;
import me.blutkrone.rpgcore.item.refinement.CoreRefinerRecipe;
import me.blutkrone.rpgcore.menu.PassiveMenu;
import me.blutkrone.rpgcore.node.impl.CoreNodeCollectible;
import me.blutkrone.rpgcore.passive.CorePassiveTree;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Profession is meant to allow life-skill scaling
 */
public class CoreProfession {

    private String id;
    // quest tags to gate level (prevents levelling every quest up)
    private Map<Integer, Set<String>> raw_level_tag;
    private Map<Integer, Set<String>> cache_level_tag = new HashMap<>();
    // the passive tree layout to use
    private String tree_id;

    public CoreProfession(String id, EditorProfession editor) {
        this.id = id;
        this.tree_id = "profession_" + id;
        this.raw_level_tag = EditorStringAndNumber.transformIntegerToStringList(editor.level_requirement);

        Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
            // create a tree dedicated to the profession
            EditorIndex<CorePassiveTree, EditorPassiveTree> tree_index = RPGCore.inst().getPassiveManager().getTreeIndex();
            if (!tree_index.has(this.tree_id)) {
                tree_index.create(this.tree_id, (created -> {
                    created.point = this.tree_id;
                }));
            }
        });
    }

    public String getId() {
        return id;
    }

    /*
     * Internal function to handle experience gain
     *
     * @param player who gains the experience
     * @param experience how much experience to be gained
     * @param level_maximum maximum level we can gain exp at
     */
    private void gainExpInternal(CorePlayer player, int experience, int level_maximum) {
        // ensure there is experience to be gained
        if (experience <= 0) {
            return;
        }
        // check if source is too low levelled
        int level_current = player.getProfessionLevel().getOrDefault(this.id, 1);
        if (level_current > level_maximum) {
            return;
        }
        // check if player is over-levelled
        if (!canGainProfessionExpAtLevel(player, level_current)) {
            return;
        }
        // join with the experience to be gained
        double have_experience = player.getProfessionExp().merge(this.id, 0d+experience, (a,b) -> a+b);
        double want_exp = RPGCore.inst().getLevelManager().getExpToLevelUpProfession(level_current);
        // inform player about the experience gained
        if (have_experience < want_exp) {
            String message = RPGCore.inst().getLanguageManager().getTranslation("profession_" + this.id + "_gain_exp");
            message = message.replace("%exp%", "" + experience);
            message = message.replace("%ratio%", String.format("%.1f%%", 100d*(have_experience/want_exp)));
            player.notify(message);
            return;
        }
        // check if profession is gated by tags
        if (!canGainProfessionExpAtLevel(player, level_current+1)) {
            player.getProfessionExp().put(this.id, want_exp - 1d);
            String message = RPGCore.inst().getLanguageManager().getTranslation("profession_" + this.id + "_stuck");
            player.notify(message);
            return;
        }
        // level up the profession
        player.getProfessionExp().put(this.id, 0d);
        player.getProfessionLevel().put(this.id, level_current + 1);
        String message = RPGCore.inst().getLanguageManager().getTranslation("profession_" + this.id + "_gain_level");
        message = message.replace("%level%", "" + (level_current + 1));
        player.notify(message);
        player.getEntity().sendMessage(message);
    }

    /**
     * Attempt to gain experience from completing a recipe for X times.
     *
     * @param player who wants to gain experience
     * @param recipe the recipe completed
     * @param count how often recipe was completed
     */
    public void gainExpFromCrafting(CorePlayer player, CoreCraftingRecipe recipe, int count) {
        this.gainExpInternal(player, recipe.getProfessionExpGained() * count, recipe.getProfessionExpMaximumLevel());
    }

    /**
     * Attempt to gain experience from completing a recipe for X times.
     *
     * @param player who wants to gain experience
     * @param recipe the recipe completed
     * @param count how often recipe was completed
     */
    public void gainExpFromRefinement(CorePlayer player, CoreRefinerRecipe recipe, int count) {
        this.gainExpInternal(player, recipe.getProfessionExpGained() * count, recipe.getProfessionExpMaximumLevel());
    }

    /**
     * Attempt to gain experience from a collectible node, also provides a notification
     * on the experience gained
     *
     * @param player who wants to gain experience
     * @param node the node that was collected
     */
    public void gainExpFromCollectible(CorePlayer player, CoreNodeCollectible node) {
        this.gainExpInternal(player, node.getProfessionExpGained(), node.getProfessionExpMaximumLevel());
    }

    /**
     * Will open the passive tree, and override the passive point
     * count to match the level (fixed 1 point 1 level)
     *
     * @param player
     * @param core_player
     */
    public void openPassiveTree(Player player, CorePlayer core_player) {
        // grab the passive tree mapped to the profession
        CorePassiveTree tree = RPGCore.inst().getPassiveManager().getTreeIndex().get(this.tree_id);
        // flush the job level to match our requirement
        core_player.getPassivePoints().put(tree.getPoint(), core_player.getProfessionLevel().getOrDefault(id, 1));
        // allow the player to spec into their passive tree
        new PassiveMenu(this.tree_id).finish(player);
    }

    /**
     * Fetch the passive tree of the job.
     *
     * @param core_player who wants the tree
     * @return the passive tree for the player
     */
    public CorePassiveTree getTree(CorePlayer core_player) {
        // grab the passive tree mapped to the profession
        CorePassiveTree tree = RPGCore.inst().getPassiveManager().getTreeIndex().get(this.tree_id);
        // flush the job level to match our requirement
        core_player.getPassivePoints().put(tree.getPoint(), core_player.getProfessionLevel().getOrDefault(id, 1));
        // offer up the tree
        return tree;
    }

    /*
     * Check if the player meets the condition to gain experience for the
     * profession at the given level.
     *
     * @param player whose profession do we check
     * @param level the exact level to check out
     * @return true if we can gain exp
     */
    private boolean canGainProfessionExpAtLevel(CorePlayer player, int level) {
        // accumulate relevant tags
        Set<String> required = this.cache_level_tag.computeIfAbsent(level, (lvl -> {
            Set<String> output = new HashSet<>();
            for (int i = 1; i <= lvl; i++) {
                Set<String> tags = raw_level_tag.get(i);
                if (tags != null) {
                    for (String tag : tags) {
                        output.add(tag.toLowerCase());
                    }
                }
            }
            return output;
        }));
        // ensure tags are all present
        for (String tag : required) {
            if (!player.getPersistentTags().contains(tag)) {
                return false;
            }
        }
        // we can gain exp at this level
        return true;
    }
}
