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
    public EditorModifierNumber duration = new EditorModifierNumber();
    @EditorBundle(name = "Radius")
    @EditorTooltip(tooltip = "Triggers trap if entity is within radius.")
    public EditorModifierNumber radius = new EditorModifierNumber();
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
        instruction.add("The summoner will use the skill at the location of the trap, once");
        instruction.add("A valid entity approached it. The summoner themselves will never be");
        instruction.add("Able to set off the trap.");
        return instruction;
    }
}
