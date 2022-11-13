package me.blutkrone.rpgcore.hud.editor.bundle.effect;

import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.impl.CoreEffectDirection;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.root.other.EditorEffect;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorEffectDirection implements EditorEffect.IEditorEffectBundle {

    @EditorNumber(name = "Pitch")
    @EditorTooltip(tooltip = "Override pitch to this value")
    public double pitch = 0;
    @EditorNumber(name = "Yaw")
    @EditorTooltip(tooltip = "Override yaw to this value")
    public double yaw = 0;

    public EditorEffectDirection() {
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aDirection")
                .appendLore("§fPitch: " + pitch)
                .appendLore("§fYaw: " + yaw)
                .build();
    }

    @Override
    public String getName() {
        return "Direction";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Direction Effect");
        instruction.add("Overrides direction effect is facing.");
        return instruction;
    }

    @Override
    public CoreEffect.IEffectPart build() {
        return new CoreEffectDirection(this);
    }
}
