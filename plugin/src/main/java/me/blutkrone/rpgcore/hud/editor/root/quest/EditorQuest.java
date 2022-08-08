package me.blutkrone.rpgcore.hud.editor.root.quest;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.QuestRewardConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.QuestTaskConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.index.NPCConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.index.QuestConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.other.LanguageConstraint;
import me.blutkrone.rpgcore.hud.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class EditorQuest implements IEditorRoot<CoreQuest> {

    @EditorCategory(icon = Material.BUNDLE, info = "Quest")
    @EditorWrite(name = "Info", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = "Description within Quest Log")
    public String lc_info;
    @EditorList(name = "Tasks", constraint = QuestTaskConstraint.class)
    @EditorTooltip(tooltip = "Tasks in sequential order to complete")
    public List<IEditorBundle> tasks = new ArrayList<>();
    @EditorWrite(name = "Symbol", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = "Symbol used to indicate if quest is available")
    public String symbol = "default";

    @EditorCategory(icon = Material.BUNDLE, info = "Requirement")
    @EditorList(name = "Forbidden", constraint = QuestRewardConstraint.class)
    @EditorTooltip(tooltip = "Cannot accept quest if not completed.")
    public List<String> required_quest = new ArrayList<>();
    @EditorList(name = "Forbidden", constraint = QuestRewardConstraint.class)
    @EditorTooltip(tooltip = "Cannot accept quest if completed OR accepted.")
    public List<String> forbidden_quest = new ArrayList<>();

    @EditorCategory(icon = Material.BUNDLE, info = "Reward")
    @EditorList(name = "Fixed", constraint = QuestRewardConstraint.class)
    @EditorTooltip(tooltip = "These rewards are always given.")
    public List<IEditorBundle> fixed_rewards = new ArrayList<>();
    @EditorList(name = "Choice", constraint = QuestRewardConstraint.class)
    @EditorTooltip(tooltip = "One of these rewards can be picked.")
    public List<IEditorBundle> choice_rewards = new ArrayList<>();
    @EditorWrite(name = "NPC", constraint = NPCConstraint.class)
    @EditorTooltip(tooltip = {"Claim rewards from this NPC", "If undefined can claim from quest giver"})
    public String npc_rewards = "NOTHINGNESS";

    @EditorCategory(icon = Material.BUNDLE, info = "Complete")
    @EditorList(name = "Abandon", constraint = QuestConstraint.class)
    @EditorTooltip(tooltip = {"Abandons quests upon completion"})
    public List<String> abandon_quest = new ArrayList<>();
    @EditorList(name = "Follow", constraint = QuestConstraint.class)
    @EditorTooltip(tooltip = {"Accept quests upon completion", "This can fail if there are requirements!"})
    public List<String> follow_quest = new ArrayList<>();

    public transient File file;

    public EditorQuest() {
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public void save() throws IOException {
        try (FileWriter fw = new FileWriter(file, Charset.forName("UTF-8"))) {
            RPGCore.inst().getGson().toJson(this, fw);
        }
    }

    @Override
    public CoreQuest build(String id) {
        return new CoreQuest(id, this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aQuest")
                .build();
    }

    @Override
    public String getName() {
        return "Quest";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§fQuest");
        instruction.add("§fA series of tasks for a player to complete. A quest is");
        instruction.add("§fCompleted once the rewards have been claimed.");
        instruction.add("§f");
        instruction.add("§fQuests are designed to be linear, if you wish to branch");
        instruction.add("§fA quest create two follow ups and make their completion");
        instruction.add("§fAbandon each other.");
        return instruction;
    }
}
