package me.blutkrone.rpgcore.editor.bundle.mechanic;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorBundle;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierBoolean;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierNumber;
import me.blutkrone.rpgcore.editor.bundle.modifier.EditorModifierString;
import me.blutkrone.rpgcore.skill.mechanic.AbstractCoreMechanic;
import me.blutkrone.rpgcore.skill.mechanic.AnimateMechanic;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorModelAnimateMechanic extends AbstractEditorMechanic {

    @EditorBundle(name = "Animation")
    @EditorTooltip(tooltip = {"ID of the animation to play"})
    public EditorModifierString animation = new EditorModifierString("attack");
    @EditorBundle(name = "Speed")
    @EditorTooltip(tooltip = {"Playback speed of the animation"})
    public EditorModifierNumber speed = new EditorModifierNumber(1.0f);
    @EditorBundle(name = "Stop")
    @EditorTooltip(tooltip = {"Abruptly stop an animation"})
    public EditorModifierBoolean stop = new EditorModifierBoolean(false);
    @EditorBundle(name = "Fade")
    @EditorTooltip(tooltip = {"Fade out an active animation"})
    public EditorModifierBoolean fade = new EditorModifierBoolean(false);

    @Override
    public AbstractCoreMechanic build() {
        return new AnimateMechanic(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.ANVIL)
                .name("§fAnimate")
                .build();
    }

    @Override
    public String getName() {
        return "Animate";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Model Animation");
        instruction.add("Play an animation on a blockbench model, nothing will.");
        instruction.add("Happen if you use this on a common entity.");
        return instruction;
    }
}
