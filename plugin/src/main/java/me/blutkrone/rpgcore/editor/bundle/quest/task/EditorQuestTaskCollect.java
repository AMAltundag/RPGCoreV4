package me.blutkrone.rpgcore.editor.bundle.quest.task;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.quest.AbstractEditorQuestTask;
import me.blutkrone.rpgcore.editor.constraint.bundle.mono.EditorItemWithQuantityConstraint;
import me.blutkrone.rpgcore.editor.constraint.reference.other.LanguageConstraint;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.quest.task.AbstractQuestTask;
import me.blutkrone.rpgcore.quest.task.impl.CoreQuestTaskCollect;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditorQuestTaskCollect extends AbstractEditorQuestTask {

    @EditorWrite(name = "Info", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Task progress information", "Placeholder {...} with item ids", "§cThis is a language code, NOT plaintext."})
    public String lc_info = "NOTHINGNESS";
    @EditorList(name = "Demand", constraint = EditorItemWithQuantityConstraint.class)
    @EditorTooltip(tooltip = {"Which items are requested"})
    public List<IEditorBundle> demand = new ArrayList<>();

    public UUID uuid = UUID.randomUUID();

    @Override
    public AbstractQuestTask<?> build(CoreQuest quest) {
        return new CoreQuestTaskCollect(quest, this);
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
                .name("§fCollect")
                .appendLore("§fDemand: " + this.demand.size())
                .build();
    }

    @Override
    public String getName() {
        return "Collect";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§bCollect Task");
        instruction.add("Hold X number of items in your inventory.");
        return instruction;
    }
}
