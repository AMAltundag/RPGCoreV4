package me.blutkrone.rpgcore.hud.editor.bundle.effect;

import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.impl.CoreEffectForward;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.root.other.EditorEffect;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorEffectForward implements EditorEffect.IEditorEffectBundle {
    @EditorNumber(name = "Length")
    @EditorTooltip(tooltip = "Length to move forward by")
    public double length = 0d;

    public EditorEffectForward() {
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aForward")
                .appendLore("§fDistance: " + length)
                .build();
    }

    @Override
    public String getName() {
        return "Forward";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Forward Effect");
        instruction.add("Move effect anchor forward, negative distance works too.");
        return instruction;
    }

    @Override
    public CoreEffect.IEffectPart build() {
        return new CoreEffectForward(this);
    }
}
