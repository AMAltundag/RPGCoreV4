package me.blutkrone.rpgcore.editor.bundle.mechanic;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBoolean;
import me.blutkrone.rpgcore.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.DungeonScoreMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorDungeonScoreMechanic extends AbstractEditorMechanic {

    @EditorWrite(name = "Score", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = "Score identifier")
    public String score = "rpgcore";
    @EditorNumber(name = "Grant")
    @EditorTooltip(tooltip = "How much score to grant")
    public double grant = 1.0;
    @EditorBoolean(name = "Party")
    @EditorTooltip(tooltip = "Add to party or personal score")
    public boolean party = true;

    @Override
    public AbstractCoreMechanic build() {
        return new DungeonScoreMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.WRITABLE_BOOK)
                .name("§fDungeon Score")
                .build();
    }

    @Override
    public String getName() {
        return "Dungeon Score";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Dungeon Score Selector");
        instruction.add("A score tracked for a dungeon, a score is zero if we");
        instruction.add("Are outside of a dungeon.");
        instruction.add("");
        instruction.add("Following default scores");
        instruction.add("total_kill: Total number of dungeon kills");
        instruction.add("<id>_kill: Total kills by mob ID");
        return instruction;
    }
}
