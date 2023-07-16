package me.blutkrone.rpgcore.editor.bundle.effect;

import me.blutkrone.rpgcore.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.editor.root.other.EditorEffect;
import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.impl.CoreEffectWait;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorEffectWait implements EditorEffect.IEditorEffectBundle {
    @EditorNumber(name = "Time", minimum = 0d)
    @EditorTooltip(tooltip = "Ticks to wait before we continue")
    public double time = 20;

    @Override
    public CoreEffect.IEffectPart build() {
        return new CoreEffectWait(this);
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aWait Effect")
                .appendLore("§fTime: " + ((int) this.time) + " ticks")
                .build();
    }

    @Override
    public String getName() {
        return "Wait";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Wait");
        instruction.add("Stall further execution by given number of ticks.");
        return instruction;
    }
}