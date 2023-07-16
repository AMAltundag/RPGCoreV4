package me.blutkrone.rpgcore.editor.bundle.quest.task;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.quest.AbstractEditorQuestTask;
import me.blutkrone.rpgcore.editor.constraint.bundle.mono.EditorItemWithQuantityConstraint;
import me.blutkrone.rpgcore.editor.constraint.reference.index.NPCConstraint;
import me.blutkrone.rpgcore.editor.constraint.reference.other.LanguageConstraint;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.quest.task.AbstractQuestTask;
import me.blutkrone.rpgcore.quest.task.impl.CoreQuestTaskDeliver;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditorQuestTaskDeliver extends AbstractEditorQuestTask {

    @EditorWrite(name = "Info", constraint = LanguageConstraint.class)
    @EditorTooltip(tooltip = {"Task progress information", "Placeholder {...} with item ids", "§cThis is a language code, NOT plaintext."})
    public String lc_info = "NOTHINGNESS";
    @EditorWrite(name = "Drop-Off", constraint = NPCConstraint.class)
    @EditorTooltip(tooltip = {"The NPC to drop-off items."})
    public String npc = "NOTHINGNESS";
    @EditorList(name = "Demand", constraint = EditorItemWithQuantityConstraint.class)
    @EditorTooltip(tooltip = {"Which items are requested"})
    public List<IEditorBundle> demand = new ArrayList<>();

    public UUID uuid = UUID.randomUUID();

    @Override
    public AbstractQuestTask<?> build(CoreQuest quest) {
        return new CoreQuestTaskDeliver(quest, this);
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
                .name("§fDelivery")
                .appendLore("§fDemand: " + this.demand.size())
                .appendLore("§fDrop-Off: " + this.npc)
                .build();
    }

    @Override
    public String getName() {
        return "Delivery";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("§bDelivery Task");
        instruction.add("Deliver X amount of items to an NPC.");
        instruction.add("You can deliver partial amounts.");
        instruction.add("Items must be delivered, not collected!");
        return instruction;
    }
}
