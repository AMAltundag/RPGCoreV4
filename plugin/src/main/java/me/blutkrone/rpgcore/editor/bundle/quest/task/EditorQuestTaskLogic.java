package me.blutkrone.rpgcore.editor.bundle.quest.task;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.quest.AbstractEditorQuestTask;
import me.blutkrone.rpgcore.editor.constraint.bundle.mono.ActionConstraint;
import me.blutkrone.rpgcore.editor.constraint.bundle.multi.QuestRewardConstraint;
import me.blutkrone.rpgcore.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.quest.task.AbstractQuestTask;
import me.blutkrone.rpgcore.quest.task.impl.CoreQuestTaskLogic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditorQuestTaskLogic extends AbstractEditorQuestTask {

    @EditorList(name = "Commands", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Will invoke these commands via console", "Placeholders: %player% %world% %x% %y% %z%"})
    public List<String> commands = new ArrayList<>();
    @EditorList(name = "Rewards", constraint = QuestRewardConstraint.class)
    @EditorTooltip(tooltip = {"Apply all rewards to the player"})
    public List<IEditorBundle> rewards = new ArrayList<>();
    @EditorList(name = "Actions", constraint = ActionConstraint.class)
    @EditorTooltip(tooltip = {"Invoke all the actions on the player"})
    public List<IEditorBundle> actions = new ArrayList<>();

    public UUID uuid = UUID.randomUUID();

    @Override
    public AbstractQuestTask<?> build(CoreQuest quest) {
        return new CoreQuestTaskLogic(quest, this);
    }

    @Override
    public String getInfoLC() {
        return "NOTHINGNESS";
    }

    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOK)
                .name("§fLogic")
                .appendLore("§fCommands: X" + this.commands.size())
                .appendLore("§fRewards: X" + this.rewards.size())
                .appendLore("§fActions: X" + this.actions.size())
                .build();
    }

    @Override
    public String getName() {
        return "Logic";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§bLogic Task");
        instruction.add("Will move on to next task by itself, checked once");
        instruction.add("per second.");
        return instruction;
    }
}
