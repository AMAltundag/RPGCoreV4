package me.blutkrone.rpgcore.hud.editor.bundle.mechanic;

import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.hud.editor.constraint.reference.index.EffectConstraint;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.EffectMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorEffectMechanic extends AbstractEditorMechanic {

    @EditorList(name = "Effects", constraint = EffectConstraint.class)
    @EditorTooltip(tooltip = {"Picks an effect at random to invoke"})
    public List<String> effects = new ArrayList<>();
    @EditorBundle(name = "Scale")
    @EditorTooltip(tooltip = {"Increases size of the effect invoked"})
    public EditorModifierNumber scale = new EditorModifierNumber(1d);

    @Override
    public AbstractCoreMechanic build() {
        return new EffectMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.PAINTING)
                .name("Effect Mechanic")
                .build();
    }

    @Override
    public String getName() {
        return "Effect";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Effect Mechanic");
        instruction.add("Invokes a random effect at the given location.");
        return instruction;
    }
}
