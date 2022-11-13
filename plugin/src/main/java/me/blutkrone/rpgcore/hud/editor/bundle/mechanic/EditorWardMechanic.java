package me.blutkrone.rpgcore.hud.editor.bundle.mechanic;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.hud.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.WardMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditorWardMechanic extends AbstractEditorMechanic {

    @EditorWrite(name = "ID", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = "Same ID will replace the ward.")
    public String id = UUID.randomUUID().toString();
    @EditorBundle(name = "Duration")
    @EditorTooltip(tooltip = "Duration of the ward")
    public EditorModifierNumber duration = new EditorModifierNumber();

    @EditorBundle(name = "Absolute")
    @EditorTooltip(tooltip = {"Ward gained as a flat amount"})
    public EditorModifierNumber maximum_flat = new EditorModifierNumber(0d);
    @EditorBundle(name = "Health")
    @EditorTooltip(tooltip = {"Ward gained relative to maximum health", "Snapshot upon acquisition"})
    public EditorModifierNumber maximum_health = new EditorModifierNumber(0.15d);
    @EditorBundle(name = "Stamina")
    @EditorTooltip(tooltip = {"Ward gained relative to maximum stamina", "Snapshot upon acquisition"})
    public EditorModifierNumber maximum_stamina = new EditorModifierNumber(0.0d);
    @EditorBundle(name = "Mana")
    @EditorTooltip(tooltip = {"Ward gained relative to maximum health", "Snapshot upon acquisition"})
    public EditorModifierNumber maximum_mana = new EditorModifierNumber(0.0d);

    @EditorBundle(name = "Effectiveness")
    @EditorTooltip(tooltip = {"What % of damage can be absorbed by the ward"})
    public EditorModifierNumber effectiveness = new EditorModifierNumber(1.0d);
    @EditorBundle(name = "Restoration")
    @EditorTooltip(tooltip = {"Restore ticks after not taking any damage", "Negative value means never restored."})
    public EditorModifierNumber restoration = new EditorModifierNumber(100);

    @EditorWrite(name = "Icon", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = "Icon shown on the HUD")
    public String icon = "none";

    @Override
    public AbstractCoreMechanic build() {
        return new WardMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.SHIELD)
                .name("§fWard Mechanic")
                .appendLore("§fEffectiveness: " + this.effectiveness.base_value)
                .appendLore("§fMaximum: " + this.maximum_flat.base_value)
                .build();
    }

    @Override
    public String getName() {
        return "Ward";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Ward Mechanic");
        instruction.add("A ward can absorb damage instead of life, ward is removed either");
        instruction.add("When exhausted or running out of duration. Damage is only blocked");
        instruction.add("By one ward, applying after the barrier mechanic.");
        instruction.add("");
        instruction.add("Ward is an effect.");
        instruction.add("If ward is removed, it cannot be restored.");
        instruction.add("Restoration is can only happen every 0.5 seconds.");
        return instruction;
    }
}
