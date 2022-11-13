package me.blutkrone.rpgcore.hud.editor.bundle.other;

import me.blutkrone.rpgcore.effect.impl.CoreEffectBlock;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorHideWhen;
import me.blutkrone.rpgcore.hud.editor.annotation.EditorTooltip;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorList;
import me.blutkrone.rpgcore.hud.editor.annotation.value.EditorWrite;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.constraint.enums.BlockMaskConstraint;
import me.blutkrone.rpgcore.hud.editor.constraint.enums.MaterialConstraint;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EditorTransmutation implements IEditorBundle {

    @EditorWrite(name = "Mask", constraint = BlockMaskConstraint.class)
    @EditorTooltip(tooltip = "Masks can target certain types of blocks")
    public CoreEffectBlock.BlockMask mask = CoreEffectBlock.BlockMask.NONE;
    @EditorHideWhen(field = "mask", value = "NONE", invert = true)
    @EditorList(name = "Source", constraint = MaterialConstraint.class)
    @EditorTooltip(tooltip = "Targets certain blocks, only if mask is none.")
    public List<Material> source = new ArrayList<>();
    @EditorList(name = "Target", constraint = MaterialConstraint.class)
    @EditorTooltip(tooltip = "Try to transmute into a random block.")
    public List<Material> target = new ArrayList<>();

    @Override
    public ItemStack getPreview() {
        return ItemBuilder.of(Material.BOOKSHELF)
                .name("§aTransmutation")
                .appendLore("§fSource: " + (this.mask == CoreEffectBlock.BlockMask.NONE ? this.source : this.mask))
                .appendLore("§fTarget: " + this.target)
                .build();
    }

    @Override
    public String getName() {
        return "Transmutation";
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Transmutation");
        instruction.add("Disguises a block as another one.");
        instruction.add("");
        instruction.add("Do NOT make any collider shape alteration!");
        return instruction;
    }
}
