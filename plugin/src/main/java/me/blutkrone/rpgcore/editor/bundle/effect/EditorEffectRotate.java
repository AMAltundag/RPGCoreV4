package me.blutkrone.rpgcore.editor.bundle.effect;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.editor.root.other.EditorEffect;
import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.impl.CoreEffectRotate;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorEffectRotate implements EditorEffect.IEditorEffectBundle {

    @EditorNumber(name = "Pitch")
    @EditorTooltip(tooltip = "Rotate pitch by this value")
    public double pitch = 0;
    @EditorNumber(name = "Yaw")
    @EditorTooltip(tooltip = "Rotate yaw by this value")
    public double yaw = 0;

    public EditorEffectRotate() {
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§fRotate Effect")
                .appendLore("§fPitch: " + pitch)
                .appendLore("§fYaw: " + yaw)
                .build();
    }

    @Override
    public String getName() {
        return "Rotate";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Rotate Effect");
        instruction.add("Rotate the direction the anchor is facing.");
        return instruction;
    }

    @Override
    public CoreEffect.IEffectPart build() {
        return new CoreEffectRotate(this);
    }
}
