package me.blutkrone.rpgcore.editor.bundle.mechanic;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.editor.constraint.other.StringConstraint;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.InstantMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditorInstantMechanic extends AbstractEditorMechanic {

    @EditorWrite(name = "ID", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = "Same ID will replace the instant cast.")
    public String id = UUID.randomUUID().toString();
    @EditorBundle(name = "Duration")
    @EditorTooltip(tooltip = "Duration of the instant cast")
    public EditorModifierNumber duration = new EditorModifierNumber();
    @EditorWrite(name = "Icon", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = "Icon shown on the HUD")
    public String icon = "none";
    @EditorList(name = "Tags", constraint = StringConstraint.class)
    @EditorTooltip(tooltip = "Skill tags or skill IDs to find")
    public List<String> tags = new ArrayList<>();

    @Override
    public AbstractCoreMechanic build() {
        return new InstantMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.SHIELD)
                .name("§fInstant Mechanic")
                .appendLore("§fID: " + this.id)
                .appendLore("§fDuration: " + this.duration.base_value)
                .appendLore("§fIcon: " + this.icon)
                .appendLore("§fTags: " + this.tags.size() + "X")
                .build();
    }

    @Override
    public String getName() {
        return "Ward";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Instant Mechanic");
        instruction.add("Reduces casting time to zero, only works for cast triggers.");
        instruction.add("Instant cast is consumed whenever a skill is being cast.");
        instruction.add("");
        instruction.add("Instant casts are an effect.");
        instruction.add("Only players are affected by instant casts.");
        return instruction;
    }
}
