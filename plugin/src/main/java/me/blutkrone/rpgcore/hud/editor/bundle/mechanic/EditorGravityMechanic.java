package me.blutkrone.rpgcore.hud.editor.bundle.mechanic;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.GravityMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorGravityMechanic extends AbstractEditorMechanic {

    @EditorBundle(name = "Power")
    @EditorTooltip(tooltip = {"Power to push down with"})
    public EditorModifierNumber power = new EditorModifierNumber();

    @Override
    public AbstractCoreMechanic build() {
        return new GravityMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.ELYTRA)
                .name("Gravity Mechanic")
                .build();
    }

    @Override
    public String getName() {
        return "Gravity";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Gravity Mechanic");
        instruction.add("Applies up/downward force.");
        return instruction;
    }
}
