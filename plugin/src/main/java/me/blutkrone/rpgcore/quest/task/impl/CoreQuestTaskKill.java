package me.blutkrone.rpgcore.quest.task.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.bundle.other.EditorMobCount;
import me.blutkrone.rpgcore.editor.bundle.quest.task.EditorQuestTaskKill;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.node.impl.CoreNodeSpawner;
import me.blutkrone.rpgcore.node.struct.NodeActive;
import me.blutkrone.rpgcore.node.struct.NodeWorld;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.quest.task.AbstractQuestTask;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Kills tracked based on entity ID.
 */
public class CoreQuestTaskKill extends AbstractQuestTask<String> {

    private Map<String, Integer> hitlist;

    public CoreQuestTaskKill(CoreQuest quest, EditorQuestTaskKill editor) {
        super(quest, editor);

        this.hitlist = EditorMobCount.unwrap(editor.mobs);
    }

    @Override
    public List<Location> getHints(CorePlayer core, Player bukkit) {
        List<Location> output = new ArrayList<>();

        // all mobs we still need to kill
        NodeWorld node_world = RPGCore.inst().getNodeManager().getNodeWorld(bukkit.getWorld());
        if (node_world != null) {
            hitlist.forEach((mob_type, want) -> {
                // not shown if requirement is already met
                int have = core.getProgressQuests().getOrDefault(this.getUniqueId() + "_" + mob_type, 0);
                if (have >= want) {
                    return;
                }
                // highlight spawners for this creature
                List<NodeActive> mob_spawners = node_world.query("spawner_" + mob_type, (node -> {
                    if (node.getNode() instanceof CoreNodeSpawner) {
                        return (((CoreNodeSpawner) node.getNode()).getMobs().contains(mob_type));
                    } else {
                        return false;
                    }
                }));
                // list up the nodes we have discovered
                for (NodeActive node : mob_spawners) {
                    output.add(new Location(bukkit.getWorld(), node.getX(), node.getY(), node.getZ()));
                }
            });
        }

        return output;
    }

    @Override
    public List<String> getInfo(CorePlayer player) {
        List<String> output = super.getInfo(player);
        for (Map.Entry<String, Integer> entry : hitlist.entrySet()) {
            int have = player.getProgressQuests().getOrDefault(this.getUniqueId() + "_" + entry.getKey(), 0);
            int want = entry.getValue();

            output.replaceAll((line -> {
                if (have >= want) {
                    return line.replace("{" + entry.getKey() + "}", "§a" + want + "/" + want);
                } else {
                    return line.replace("{" + entry.getKey() + "}", "§c" + have + "/" + want);
                }
            }));
        }
        return output;
    }

    @Override
    public boolean taskIsComplete(CorePlayer player) {
        for (Map.Entry<String, Integer> entry : hitlist.entrySet()) {
            int have = player.getProgressQuests().getOrDefault(this.getUniqueId() + "_" + entry.getKey(), 0);
            int want = entry.getValue();

            if (have < want) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void updateQuest(CorePlayer player, String param) {
        if (hitlist.containsKey(param)) {
            player.getProgressQuests().merge(this.getUniqueId() + "_" + param, 1, (a, b) -> a + b);
        }
    }

    @Override
    public List<String> getDataIds() {
        List<String> output = new ArrayList<>();
        hitlist.forEach((id, count) -> {
            output.add(getUniqueId() + "_" + id);
        });
        return output;
    }
}
