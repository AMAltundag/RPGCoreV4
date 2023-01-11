package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.passive.CorePassiveTree;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public abstract class MultiTreeMenu extends AbstractCoreMenu {

    private String category = null;

    public MultiTreeMenu() {
        super(5);
    }

    /**
     * Get a list of all passive trees we do care about
     * @param player
     * @return
     */
    public abstract List<CorePassiveTree> getTrees(CorePlayer player);

    @Override
    public void rebuild() {
        this.getMenu().clearItems();

        // grab trees of the player
        CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());
        List<CorePassiveTree> trees = new ArrayList<>(getTrees(player));

        // build basic background
        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-208);
        msb.append(resourcepack().texture("menu_passive_tree"), ChatColor.WHITE);

        if (trees.size() <= 3) {
            // up to three trees can just be directly rendered
            rebuildAsTree(msb, trees);
        } else if (this.category != null) {
            // if categorized, we can render jobs of the category
            trees.removeIf(tree -> !tree.getCategory().equalsIgnoreCase(this.category));
            rebuildAsTree(msb, trees);
        } else {
            // otherwise present up to three categories
            rebuildAsCategories(msb, trees);
        }

        InstructionBuilder instructions = new InstructionBuilder();
        instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_passive"));
        instructions.apply(msb);

        this.getMenu().setTitle(msb.compile());
    }

    @Override
    public void click(InventoryClickEvent event) {
        event.setCancelled(true);

        // open a passive tree of the job
        String instruction = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "tree-id", null);
        if (instruction != null) {
            CorePassiveTree tree = RPGCore.inst().getPassiveManager().getTreeIndex().get(instruction);
            getMenu().stalled(() -> {
                if (event.getWhoClicked().isValid()) {
                    new PassiveMenu(tree.getId()).finish(((Player) event.getWhoClicked()));
                }
            });
        }
        // focus on a category instead
        instruction = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "category-id", null);
        if (instruction != null) {
            this.category = instruction;
            getMenu().queryRebuild();
        }
    }

    private void rebuildAsCategories(MagicStringBuilder msb, List<CorePassiveTree> trees) {
        // narrow down into categories
        List<String> categories = new ArrayList<>();
        for (CorePassiveTree tree : trees) {
            categories.add(tree.getCategory());
        }
        categories = new ArrayList<>(new HashSet<>(categories));

        // render portraits for the categories
        for (int i = 0; i < 3; i++) {
            String category = categories.size() > i ? categories.get(i) : null;

            String portrait = "tree_nothing";
            ItemStack itemize = null;

            if (category != null) {
                itemize = language().getAsItem("lc_tree_category_" + category).build();
                IChestMenu.setBrand(itemize, RPGCore.inst(), "category-id", category);
                portrait = category;
            }

            // set the tooltip items
            if (itemize != null) {
                for (int j = 0; j < 3; j++) {
                    for (int k = 0; k < 4; k++) {
                        getMenu().setItemAt(k * 9 + (3 * i) + j, itemize);
                    }
                }
            }

            // contribute to the title
            msb.shiftToExact(54 * i).append(resourcepack().texture("static_" + portrait + "_portrait"), ChatColor.WHITE);
        }
    }

    private void rebuildAsTree(MagicStringBuilder msb, List<CorePassiveTree> trees) {
        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());

        for (int i = 0; i < 3; i++) {
            CorePassiveTree tree = trees.size() > i ? trees.get(i) : null;

            String portrait = "tree_nothing";
            ItemStack itemize = null;

            if (tree != null) {
                int have_points = core_player.getPassivePoints().getOrDefault(tree.getPoint(), 0);
                int used_points = core_player.getPassiveAllocated().getOrDefault(tree.getId(), new HashSet<>()).size();
                ItemBuilder builder = language().getAsItem("lc_tree_" + tree.getId(), Math.max(1, used_points), Math.max(1, have_points));
                itemize = builder.build();
                IChestMenu.setBrand(itemize, RPGCore.inst(), "tree-id", tree.getId());
                portrait = tree.getPortrait();
            }

            // set the tooltip items
            if (itemize != null) {
                for (int j = 0; j < 3; j++) {
                    for (int k = 0; k < 4; k++) {
                        getMenu().setItemAt(k * 9 + (3 * i) + j, itemize);
                    }
                }
            }

            // contribute to the title
            msb.shiftToExact(54 * i).append(resourcepack().texture("static_" + portrait + "_portrait"), ChatColor.WHITE);
        }
    }
}
