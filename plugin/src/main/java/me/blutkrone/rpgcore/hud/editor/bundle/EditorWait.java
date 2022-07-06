package me.blutkrone.rpgcore.hud.editor.bundle;

import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.impl.CoreWait;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.root.EditorEffect;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorWait implements EditorEffect.IEditorEffectBundle {
    @EditorNumber(name = "Time", minimum = 0d)
    public double time;

    @Override
    public CoreEffect.IEffectPart build() {
        return new CoreWait(this);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aComplex Effect")
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
        instruction.add("§fWait");
        instruction.add("§fStall further execution by given number of ticks.");
        return instruction;
    }
}