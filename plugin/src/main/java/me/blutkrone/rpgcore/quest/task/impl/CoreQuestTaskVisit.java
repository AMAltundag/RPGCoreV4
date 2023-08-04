package me.blutkrone.rpgcore.quest.task.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.bundle.quest.task.EditorQuestTaskVisit;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.node.struct.NodeActive;
import me.blutkrone.rpgcore.node.struct.NodeWorld;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.quest.task.AbstractQuestTask;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This links with "hotspot" type nodes, allowing to
 * progress a quest by being near a hotspot.
 */
public class CoreQuestTaskVisit extends AbstractQuestTask<String> {

    private Set<String> need_to_visit = new HashSet<>();
    private int distance;

    public CoreQuestTaskVisit(CoreQuest quest, EditorQuestTaskVisit editor) {
        super(quest, editor);

        this.need_to_visit.addAll(editor.nodes);
        this.distance = (int) editor.distance;
    }

    /**
     * How close we need to get to count as a pass.
     *
     * @return How close we need to be to a location.
     */
    public int getDistance() {
        return distance;
    }

    @Override
    public List<Location> getHints(CorePlayer core, Player bukkit) {
        List<Location> output = new ArrayList<>();

        // all hotspots that we need to visit
        NodeWorld node_world = RPGCore.inst().getNodeManager().getNodeWorld(bukkit.getWorld());
        if (node_world != null) {
            for (String hotspot : this.need_to_visit) {
                boolean unvisited = core.getProgressQuests().getOrDefault(getUniqueId() + "_" + hotspot, 0) == 0;
                if (unvisited) {
                    for (NodeActive node : node_world.getNodesOfType("hotspot:"+hotspot)) {
                        output.add(new Location(bukkit.getWorld(), node.getX(), node.getY(), node.getZ()));
                    }
                }
            }
        }

        return output;
    }

    @Override
    public List<String> getInfo(CorePlayer player) {
        List<String> output = super.getInfo(player);
        for (String hotspot : this.need_to_visit) {
            boolean visited = player.getProgressQuests().getOrDefault(getUniqueId() + "_" + hotspot, 0) == 1;
            output.replaceAll(line -> line.replace("{" + hotspot + "}", visited ? "§a[+]" : "§c[-]"));
        }
        return output;
    }

    @Override
    public boolean taskIsComplete(CorePlayer player) {
        for (String id : this.need_to_visit) {
            boolean visited = player.getProgressQuests().getOrDefault(getUniqueId() + "_" + id, 0) == 1;
            if (!visited) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void updateQuest(CorePlayer player, String param) {
        if (this.need_to_visit.contains(param)) {
            player.getProgressQuests().put(getUniqueId() + "_" + param, 1);
        }
    }

    @Override
    public List<String> getDataIds() {
        List<String> output = new ArrayList<>();
        for (String id : this.need_to_visit) {
            output.add(getUniqueId() + "_" + id);
        }
        return output;
    }
}
