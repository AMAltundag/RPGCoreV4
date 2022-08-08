package me.blutkrone.rpgcore.quest.task.impl;

import me.blutkrone.rpgcore.api.event.CoreEntityKilledEvent;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.quest.task.EditorQuestTaskKill;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.quest.task.AbstractQuestTask;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kills tracked based on entity ID.
 */
public class CoreQuestTaskKill extends AbstractQuestTask<CoreEntityKilledEvent> {

    public Map<String, Integer> hitlist = new HashMap<>();

    public CoreQuestTaskKill(CoreQuest quest, EditorQuestTaskKill editor) {
        super(quest, editor);

        Bukkit.getLogger().severe("not implemented (kill task)");
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
    public void updateQuest(CorePlayer player, CoreEntityKilledEvent param) {
        if (param.getKilled() instanceof CorePlayer) {
            if (hitlist.containsKey("player")) {
                player.getProgressQuests().merge(this.getUniqueId() + "_player", 1, (a, b) -> a + b);
            }
        } else {
            Bukkit.getLogger().severe("not implemented (quest tracker non-player)");
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
