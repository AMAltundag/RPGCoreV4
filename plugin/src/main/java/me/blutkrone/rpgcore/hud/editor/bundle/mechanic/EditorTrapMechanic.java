package me.blutkrone.rpgcore.hud.editor.bundle.mechanic;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorCategory;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorItemModel;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.SelectorConstraint;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.TrapMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorTrapMechanic extends AbstractEditorMechanic {
    @EditorCategory(info = "General", icon = Material.CHEST)
    @EditorBundle(name = "Duration")
    @EditorTooltip(tooltip = "Trap is removed after ticks have passed.")
    public EditorModifierNumber duration = new EditorModifierNumber(200.0d);
    @EditorBundle(name = "Multi")
    @EditorTooltip(tooltip = "Throws multiple traps at once.")
    public EditorModifierNumber multi = new EditorModifierNumber(1.0d);
    @EditorBundle(name = "Limit")
    @EditorTooltip(tooltip = "Maximum traps placed, 0 means we cannot place anymore.")
    public EditorModifierNumber limit = new EditorModifierNumber(12.0d);
    @EditorBundle(name = "Radius")
    @EditorTooltip(tooltip = "Triggers trap if entity is within radius.")
    public EditorModifierNumber radius = new EditorModifierNumber(3.0d);

    @EditorCategory(info = "Logic", icon = Material.ENDER_CHEST)
    @EditorBundle(name = "Impact")
    @EditorTooltip(tooltip = "Logic triggered on entities within radius.")
    public EditorLogicMultiMechanic impact = new EditorLogicMultiMechanic();
    @EditorList(name = "Filter", constraint = SelectorConstraint.class)
    @EditorTooltip(tooltip = "Additional filter to restrict triggering entities.")
    public List<IEditorBundle> filter = new ArrayList<>();
    @EditorCategory(info = "Visual", icon = Material.REDSTONE)
    @EditorBundle(name = "Model")
    @EditorTooltip(tooltip = {"Item based model at the proxy center."})
    public EditorItemModel item = new EditorItemModel();

    @Override
    public AbstractCoreMechanic build() {
        return new TrapMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.WITHER_SKELETON_SKULL)
                .name("§fTrap Mechanic")
                .build();
    }

    @Override
    public String getName() {
        return "Trap";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Trap Mechanic");
        instruction.add("Create a trap, when an enemy is close enough the trap");
        instruction.add("Will trigger and apply the logic.");
        instruction.add("");
        instruction.add("The caster will use the skill, NOT the trap. The cast");
        instruction.add("Just happens on the location of the trap.");
        return instruction;
    }
}
