package me.blutkrone.rpgcore.hud.editor.bundle.effect;

import me.blutkrone.rpgcore.effect.CoreEffect;
import me.blutkrone.rpgcore.effect.impl.CoreEffectBlock;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorNumber;
import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorTransmutation;
import me.blutkrone.rpgcore.hud.editor.constraint.bundle.mono.TransmutationConstraint;
import me.blutkrone.rpgcore.hud.editor.root.other.EditorEffect;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorEffectBlock implements EditorEffect.IEditorEffectBundle {

    @EditorList(name = "Transmute", constraint = TransmutationConstraint.class)
    @EditorTooltip(tooltip = "Rules to transmute blocks by")
    public List<EditorTransmutation> transmutations = new ArrayList<>();
    @EditorNumber(name = "Spread")
    @EditorTooltip(tooltip = {"Spread to sample blocks", "This is performed as a best-effort"})
    public double spread = 4d;
    @EditorNumber(name = "Samples")
    @EditorTooltip(tooltip = {"Total blocks that can be sampled"})
    public double samples = 3d;
    @EditorNumber(name = "Duration")
    @EditorTooltip(tooltip = {"Ticks to stay disguised"})
    public double duration = 30d;
    @EditorNumber(name = "Particle")
    @EditorTooltip(tooltip = {"Block dust scattered on transformation"})
    public boolean particle = false;

    public EditorEffectBlock() {

    }

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aBlock Effect")
                .appendLore("§fTransmutations: " + this.transmutations.size() + "X")
                .appendLore("§fSpread: " + this.spread)
                .appendLore("§fSamples: " + ((int) this.samples))
                .build();
    }

    @Override
    public String getName() {
        return "Block";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Block Effect");
        instruction.add("Disguises a block as another one.");
        instruction.add("");
        instruction.add("Do NOT make any collider shape alteration!");
        return instruction;
    }

    @Override
    public CoreEffect.IEffectPart build() {
        return new CoreEffectBlock(this);
    }
}
