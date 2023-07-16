package me.blutkrone.rpgcore.editor.bundle.selector;

import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.constraint.reference.index.JobConstraint;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import me.blutkrone.rpgcore.skill.selector.JobSelector;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorJobSelector extends AbstractEditorSelector {

    @EditorList(name = "Jobs", constraint = JobConstraint.class)
    public List<String> jobs = new ArrayList<>();

    @Override
    public AbstractCoreSelector build() {
        return new JobSelector(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.GLASS)
                .name("Job Selector")
                .appendLore("Jobs: " + jobs)
                .build();
    }

    @Override
    public String getName() {
        return "Job";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Job Selector");
        instruction.add("Filter first for players, then for players who have any of the");
        instruction.add("Jobs listed in the job list.");
        return instruction;
    }
}
