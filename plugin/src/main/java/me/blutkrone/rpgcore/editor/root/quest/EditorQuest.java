package me.blutkrone.rpgcore.editor.root.quest;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.constraint.bundle.multi.QuestRewardConstraint;
import me.blutkrone.rpgcore.editor.constraint.bundle.multi.QuestTaskConstraint;
import me.blutkrone.rpgcore.editor.constraint.bundle.multi.SelectorConstraint;
import me.blutkrone.rpgcore.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.editor.constraint.reference.index.NPCConstraint;
import me.blutkrone.rpgcore.editor.constraint.reference.index.QuestConstraint;
import me.blutkrone.rpgcore.editor.constraint.reference.other.LanguageConstraint;
import me.blutkrone.rpgcore.editor.root.IEditorRoot;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
    public String lc_info = "NOTHINGNESS";
    @EditorList(name = "Tasks", constraint = QuestTaskConstraint.class)
    @EditorTooltip(tooltip = "Tasks in sequential order to complete")
    public List<IEditorBundle> tasks = new ArrayList<>();
    @EditorWrite(name = "Symbol", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = "Symbol used to indicate if quest is available")
    public String symbol = "default";
    @EditorNumber(name = "Iteration")
    @EditorTooltip(tooltip = "Reset outdated quest progress, must be an integer")
    public double iteration = 0.0d;

    @EditorCategory(icon = Material.BUNDLE, info = "Requirement")
    @EditorList(name = "Condition", constraint = SelectorConstraint.class)
    @EditorTooltip(tooltip = "Condition must be met to accept quest.")
    public List<IEditorBundle> accept_requirement = new ArrayList<>();
    @EditorList(name = "Required", constraint = QuestConstraint.class)
    @EditorTooltip(tooltip = "Cannot accept quest if not completed.")
    public List<String> required_quest = new ArrayList<>();
    @EditorList(name = "Forbidden", constraint = QuestConstraint.class)
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
    public int migration_version = RPGCore.inst().getMigrationManager().getVersion();

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
            RPGCore.inst().getGsonPretty().toJson(this, fw);
        }
    }

    @Override
    public CoreQuest build(String id) {
        // if the iteration was updated, wipe progress if relevant
        if (RPGCore.inst().getQuestManager().getIndexQuest().has(id)) {
            int old_iteration = RPGCore.inst().getQuestManager().getIndexQuest().get(id).getIteration();
            if (old_iteration != (int) this.iteration) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
                    if (core_player != null) {
                        RPGCore.inst().getLanguageManager().sendMessage(player, "warning_corrupt_quest", id);
                        core_player.getProgressQuests().entrySet()
                                .removeIf(entry -> entry.getKey().startsWith(id + "#"));
                    }
                }
            }
        }

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
        instruction.add("Quest");
        instruction.add("A series of tasks for a player to complete. A quest is");
        instruction.add("Completed once the rewards have been claimed.");
        instruction.add("");
        instruction.add("Quests are designed to be linear, if you wish to branch");
        instruction.add("A quest create two follow ups and make their completion");
        instruction.add("Abandon each other.");
        instruction.add("");
        instruction.add("Increment iteration if you changed linked tasks, players who");
        instruction.add("Will reset progress during login if mismatching.");
        return instruction;
    }
}
