package me.blutkrone.rpgcore.hud.editor.bundle.mechanic;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.SelectorConstraint;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.MobEngageMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorMobEngage extends AbstractEditorMechanic {

    @EditorBundle(name = "Distance")
    @EditorTooltip(tooltip = {"Range to pick a target within."})
    public EditorModifierNumber distance = new EditorModifierNumber();
    @EditorList(name = "Filter", constraint = SelectorConstraint.class)
    @EditorTooltip(tooltip = "Additional filter to limit random rage target.")
    public List<IEditorBundle> filter = new ArrayList<>();

    @Override
    public AbstractCoreMechanic build() {
        return new MobEngageMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.IRON_SWORD)
                .name("§fMob Engage")
                .build();
    }

    @Override
    public String getName() {
        return "Mob Engage";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Mob Engage");
        instruction.add("Randomly generates rage on a non-friendly target within");
        instruction.add("A given distance, if we have no rage.");
        instruction.add("");
        instruction.add("Target must be non-friendly.");
        instruction.add("Target must have a line-of-sight.");
        instruction.add("Target must be able to engage in combat.");
        return instruction;
    }
}
