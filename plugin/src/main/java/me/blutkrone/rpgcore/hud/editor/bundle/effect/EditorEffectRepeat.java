package me.blutkrone.rpgcore.hud.editor.bundle.effect;

import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.impl.CoreEffectRepeat;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.multi.EffectPartConstraint;
import me.blutkrone.rpgcore.hud.editor.root.other.EditorEffect;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorEffectRepeat implements EditorEffect.IEditorEffectBundle {

    @EditorList(name = "Parts", constraint = EffectPartConstraint.class)
    @EditorTooltip(tooltip = "Parts looped per cycle")
    public List<IEditorBundle> parts = new ArrayList<>();
    @EditorNumber(name = "Cycles")
    @EditorTooltip(tooltip = "How often to repeat the effect")
    public double cycles = 0.0d;
    @EditorNumber(name = "Expansion")
    @EditorTooltip(tooltip = {"Multiplier to scale per cycle"})
    public double expansion_per_cycle = 0.0d;

    public EditorEffectRepeat() {
    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§fRepeat Effect")
                .appendLore("§fParts: " + this.parts.size() + "X")
                .appendLore("§fCycles: " + ((int) this.cycles))
                .appendLore("§fExpansion: " + this.expansion_per_cycle)
                .build();
    }

    @Override
    public String getName() {
        return "Repeat";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Repeat Effect");
        instruction.add("Repeats the effect for the given number of cycles.");
        instruction.add("A new cycle starts when the previous cycle finished.");
        instruction.add("Expansion is multiplicative to original scale.");
        instruction.add("Delay can be used to create an interval.");
        instruction.add("");
        instruction.add("This does not stall the next effect part.");
        return instruction;
    }

    @Override
    public CoreEffect.IEffectPart build() {
        return new CoreEffectRepeat(this);
    }
}
