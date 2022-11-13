package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.passive.CorePassiveTree;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class JobTreeMenu extends AbstractCoreMenu {

    private String category = null;

    public JobTreeMenu() {
        super(5);
    }

    private void rebuildAsCategories(MagicStringBuilder msb, List<CorePassiveTree> trees) {
        // narrow down into categories
        List<String> categories = new ArrayList<>();
        for (CorePassiveTree tree : trees) {
            categories.add(tree.getJobCategory());
        }
        categories = new ArrayList<>(new HashSet<>(categories));

        // render portraits for the categories
        for (int i = 0; i < 3; i++) {
            String category = categories.size() > i ? categories.get(i) : null;

            String portrait = "tree_nothing";
            ItemStack itemize = null;

            if (category != null) {
                itemize = language().getAsItem("lc_tree_category_" + category).build();
                IChestMenu.setBrand(itemize, RPGCore.inst(), "job-category-id", category);
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

    private void rebuildAsJobs(MagicStringBuilder msb, List<CorePassiveTree> trees) {
        for (int i = 0; i < 3; i++) {
            CorePassiveTree tree = trees.size() > i ? trees.get(i) : null;

            String portrait = "tree_nothing";
            ItemStack itemize = null;

            if (tree != null) {
                itemize = language().getAsItem("lc_job_tree_" + tree.getId()).build();
                IChestMenu.setBrand(itemize, RPGCore.inst(), "job-tree-id", tree.getId());
                portrait = tree.getJobPortrait();
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

    @Override
    public void rebuild() {
        this.getMenu().clearItems();

        // grab jobs of the player
        List<CorePassiveTree> trees = new ArrayList<>();
        CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());
        for (String s : player.getJob().getPassiveTree()) {
            trees.add(RPGCore.inst().getPassiveManager().getTreeIndex().get(s));
        }

        // build basic background
        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-208);
        msb.append(resourcepack().texture("menu_job_tree"), ChatColor.WHITE);

        if (trees.size() <= 3) {
            // up to three jobs can just be directly rendered
            rebuildAsJobs(msb, trees);
        } else if (this.category != null) {
            // if categorized, we can render jobs of the category
            trees.removeIf(tree -> !tree.getJobCategory().equalsIgnoreCase(this.category));
            rebuildAsJobs(msb, trees);
        } else {
            // otherwise present up to three categories
            rebuildAsCategories(msb, trees);
        }

        InstructionBuilder instructions = new InstructionBuilder();
        instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_job_tree"));
        instructions.apply(msb);

        this.getMenu().setTitle(msb.compile());
    }

    @Override
    public void click(InventoryClickEvent event) {
        event.setCancelled(true);

        // open a passive tree of the job
        String instruction = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "job-tree-id", null);
        if (instruction != null) {
            CorePassiveTree tree = RPGCore.inst().getPassiveManager().getTreeIndex().get(instruction);
            getMenu().stalled(() -> {
                if (event.getWhoClicked().isValid()) {
                    new PassiveMenu(tree.getId()).finish(((Player) event.getWhoClicked()));
                }
            });
        }
        // focus on a category instead
        instruction = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "job-category-id", null);
        if (instruction != null) {
            this.category = instruction;
            getMenu().queryRebuild();
        }
    }
}
