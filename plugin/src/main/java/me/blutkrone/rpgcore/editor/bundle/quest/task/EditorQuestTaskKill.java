package me.blutkrone.rpgcore.editor.bundle.quest.task;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.quest.AbstractEditorQuestTask;
import me.blutkrone.rpgcore.editor.constraint.bundle.mono.MobCountConstraint;
import me.blutkrone.rpgcore.editor.constraint.reference.other.LanguageConstraint;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.quest.task.AbstractQuestTask;
import me.blutkrone.rpgcore.quest.task.impl.CoreQuestTaskKill;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditorQuestTaskKill extends AbstractEditorQuestTask {

    @EditorWrite(name = "Info", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Task progress information", "Placeholder {...} with mob ids", "§cThis is a language code, NOT plaintext."})
    public String lc_info = "NOTHINGNESS";
    @EditorList(name = "Mobs", constraint = MobCountConstraint.class)
    @EditorTooltip(tooltip = {"Number of mobs to be slain."})
    public List<IEditorBundle> mobs = new ArrayList<>();

    public UUID uuid = UUID.randomUUID();

    @Override
    public AbstractQuestTask<?> build(CoreQuest quest) {
        return new CoreQuestTaskKill(quest, this);
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
                .name("§fKill")
                .appendLore("§fDemand: 0")
                .build();
    }

    @Override
    public String getName() {
        return "Kill";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§bKill Task");
        instruction.add("Counts kills for certain mob types.");
        return instruction;
    }
}
