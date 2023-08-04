package me.blutkrone.rpgcore.editor.bundle.quest.task;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.quest.AbstractEditorQuestTask;
import me.blutkrone.rpgcore.editor.constraint.reference.other.LanguageConstraint;
import me.blutkrone.rpgcore.editor.constraint.reference.other.NodeConstraint;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.quest.task.AbstractQuestTask;
import me.blutkrone.rpgcore.quest.task.impl.CoreQuestTaskVisit;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditorQuestTaskVisit extends AbstractEditorQuestTask {

    @EditorList(name = "Nodes", constraint = NodeConstraint.class)
    @EditorTooltip(tooltip = {"The nodes we need to visit"})
    public List<String> nodes = new ArrayList<>();
    @EditorWrite(name = "Info", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Task progress information", "Placeholder {...} with hotspot ids", "§cThis is a language code, NOT plaintext."})
    public String lc_info = "NOTHINGNESS";
    @EditorNumber(name = "Distance", minimum = 1d, maximum = 48d)
    @EditorTooltip(tooltip = {"How close we need to get"})
    public double distance = 10d;

    public UUID uuid = UUID.randomUUID();

    @Override
    public AbstractQuestTask<?> build(CoreQuest quest) {
        return new CoreQuestTaskVisit(quest, this);
    }

    @Override
    public String getInfoLC() {
        return this.lc_info;
    }

    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOK)
                .name("§fTalk")
                .appendLore("§fNodes: " + this.nodes.size())
                .build();
    }

    @Override
    public String getName() {
        return "Visit";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§bVisit Task");
        instruction.add("Stand close enough to a node.");
        return instruction;
    }
}
