package me.blutkrone.rpgcore.editor.bundle.quest.task;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.quest.AbstractEditorQuestTask;
import me.blutkrone.rpgcore.editor.constraint.bundle.mono.TalkConstraint;
import me.blutkrone.rpgcore.editor.constraint.reference.other.LanguageConstraint;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.quest.task.AbstractQuestTask;
import me.blutkrone.rpgcore.quest.task.impl.CoreQuestTaskTalk;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditorQuestTaskTalk extends AbstractEditorQuestTask {

    @EditorList(name = "Talks", constraint = TalkConstraint.class)
    @EditorTooltip(tooltip = {"Dialogues shown based on NPC", "NPC cannot be present twice.", "Same NPC cannot be listed twice."})
    public List<IEditorBundle> talks = new ArrayList<>();
    @EditorWrite(name = "Info", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Task progress information", "Placeholder {...} with NPC ids", "§cThis is a language code, NOT plaintext."})
    public String lc_info = "NOTHINGNESS";

    public UUID uuid = UUID.randomUUID();

    @Override
    public AbstractQuestTask<?> build(CoreQuest quest) {
        return new CoreQuestTaskTalk(quest, this);
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
                .appendLore("§fTalks: " + this.talks.size())
                .build();
    }

    @Override
    public String getName() {
        return "Talk";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§bTalk Task");
        instruction.add("Interact with NPCs to present dialogue.");
        return instruction;
    }
}
