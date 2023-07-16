package me.blutkrone.rpgcore.editor.bundle.selector;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBoolean;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import me.blutkrone.rpgcore.skill.selector.DungeonScoreSelector;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorDungeonScoreSelector extends AbstractEditorSelector {

    @EditorList(name = "Score", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Score identifiers", "Multiple are summed"})
    public List<String> scores = new ArrayList<>();
    @EditorNumber(name = "Minimum")
    @EditorTooltip(tooltip = "Minimum score threshold")
    public double minimum = 0.0;
    @EditorNumber(name = "Maximum")
    @EditorTooltip(tooltip = "Maximum score threshold")
    public double maximum = 9999.0;
    @EditorList(name = "Exact", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = {"Score can be any of these numbers", "Will override minimum/maximum if defined", "Can use ranges like x~y", "Rounded to integers"})
    public List<String> exact = new ArrayList<>();
    @EditorBoolean(name = "Party")
    @EditorTooltip(tooltip = "Check party or player score")
    public boolean party = true;

    @Override
    public AbstractCoreSelector build() {
        return new DungeonScoreSelector(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.WRITABLE_BOOK)
                .name("Dungeon Score Selector")
                .build();
    }

    @Override
    public String getName() {
        return "Score";
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
        instruction.add("total_alive: Total number of mobs alive (only party, readonly)");
        instruction.add("<id>_kill: Total kills by mob ID");
        instruction.add("<id>_alive: Total alive by mob ID (only party, readonly)");
        return instruction;
    }
}
