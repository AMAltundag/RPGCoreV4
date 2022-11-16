package me.blutkrone.rpgcore.hud.editor.bundle.mechanic;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.SelectorConstraint;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.WalkMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorWalkMechanic extends AbstractEditorMechanic {

    @EditorList(name = "Selectors", constraint = SelectorConstraint.class)
    @EditorTooltip(tooltip = {"After passing thorough all selectors, we got our targets"})
    public List<IEditorBundle> selectors = new ArrayList<>();
    @EditorBundle(name = "Speed")
    @EditorTooltip(tooltip = {"Speed to stroll around with"})
    public EditorModifierNumber speed = new EditorModifierNumber(0.8d);

    @Override
    public AbstractCoreMechanic build() {
        return new WalkMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.ELYTRA)
                .name("Stroll Mechanic")
                .build();
    }

    @Override
    public String getName() {
        return "Stroll";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Walk Mechanic");
        instruction.add("Walks to a random target that passed the selector.");
        instruction.add("Ignores targets, always applies on context holder.");
        return instruction;
    }
}
