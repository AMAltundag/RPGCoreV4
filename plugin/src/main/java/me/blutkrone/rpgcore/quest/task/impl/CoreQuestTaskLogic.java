package me.blutkrone.rpgcore.quest.task.impl;

import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.other.EditorAction;
import me.blutkrone.rpgcore.editor.bundle.quest.AbstractEditorQuestReward;
import me.blutkrone.rpgcore.editor.bundle.quest.task.EditorQuestTaskLogic;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.quest.reward.AbstractQuestReward;
import me.blutkrone.rpgcore.quest.task.AbstractQuestTask;
import me.blutkrone.rpgcore.skill.behaviour.CoreAction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CoreQuestTaskLogic extends AbstractQuestTask<Object> {

    private final List<String> commands;
    private final List<AbstractQuestReward> rewards;
    private final List<CoreAction> actions;

    public CoreQuestTaskLogic(CoreQuest quest, EditorQuestTaskLogic editor) {
        super(quest, editor);
        this.commands = new ArrayList<>(editor.commands);
        this.rewards = new ArrayList<>();
        for (IEditorBundle bundle : editor.rewards) {
            AbstractEditorQuestReward reward = (AbstractEditorQuestReward) bundle;
            this.rewards.add(reward.build());
        }
        this.actions = new ArrayList<>();
        for (IEditorBundle action : editor.actions) {
            if (action instanceof EditorAction) {
                this.actions.add(((EditorAction) action).build());
            }
        }
    }

    @Override
    public boolean taskIsComplete(CorePlayer player) {
        return player.getProgressQuests().getOrDefault(this.getUniqueId() + "_done", 0) == 1;
    }

    @Override
    public void updateQuest(CorePlayer player, Object param) {
        if (player.getProgressQuests().getOrDefault(this.getUniqueId() + "_done", 0) == 0) {
            // only complete task once
            player.getProgressQuests().put(this.getUniqueId() + "_done", 1);
            // invoke the commands
            Player bukkit_player = player.getEntity();
            for (String command : this.commands) {
                command = command.replace("%player%", bukkit_player.getName());
                command = command.replace("%world%", bukkit_player.getWorld().getName());
                command = command.replace("%x%", String.valueOf(bukkit_player.getLocation().getX()));
                command = command.replace("%y%", String.valueOf(bukkit_player.getLocation().getY()));
                command = command.replace("%z%", String.valueOf(bukkit_player.getLocation().getZ()));
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
            // offer up the rewards
            for (AbstractQuestReward reward : this.rewards) {
                reward.giveReward(player);
            }
            // invoke the actions
            for (CoreAction action : this.actions) {
                CoreAction.ActionPipeline pipeline = action.pipeline(player, Collections.singletonList(player));
                player.addPipeline(pipeline);
            }
        }
    }

    @Override
    public List<String> getDataIds() {
        List<String> ids = new ArrayList<>();
        ids.add(this.getUniqueId() + "_done");
        return ids;
    }
}
