package me.blutkrone.rpgcore.hud.menu;

import me.blutkrone.rpgcore.util.collection.TreeGraph;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerMenu {

    // a graph mapping out the menu structure
    final TreeGraph<String> structure;
    // custom configurations of the menu
    final Map<String, List<String>> custom_options;

    /**
     * A menu serving as a collective for any sort of menu
     * related to RPGCore.
     */
    public PlayerMenu() throws IOException {
        ConfigWrapper config = FileUtil.asConfigYML(FileUtil.file("menu", "player.yml"));

        // prepare the graph structure we're working with
        this.structure = new TreeGraph<>(null);
        this.custom_options = new HashMap<>();

        // serialize custom configuration
        config.forEachUnder("custom-options", (path, root) -> {
            this.custom_options.put(path, root.getStringList(path));
        });

        // serialize the structure of the menu
        TreeGraph.TreeNode<String> current = this.structure.getRoot();
        for (String keying : config.getStringList("player-menu")) {
            keying = keying.replace(" ", "");
            int depth = keying.indexOf(">") - 1;

            // ensure parameters are fine
            if (depth < 0 || (depth - current.getDepth()) > 1) {
                Bukkit.getLogger().severe("Bad depth in 'menu/player.yml' from 'player-menu'");
                return;
            }

            // adjust depth if necessary
            keying = keying.split("\\>")[1];
            if (current.getDepth() > depth) {
                // move to parent
                while (current.getDepth() != depth) {
                    current = current.getParent();
                }
            } else if (current.getDepth() < depth) {
                // move to children
                List<TreeGraph.TreeNode<String>> children = current.getChildren();
                current = children.get(children.size() - 1);
            }

            // add element under this depth
            current.add(keying);
        }
    }

    public void present(Player viewer) {
        new me.blutkrone.rpgcore.menu.PlayerMenu(structure, custom_options).finish(viewer);
    }
}