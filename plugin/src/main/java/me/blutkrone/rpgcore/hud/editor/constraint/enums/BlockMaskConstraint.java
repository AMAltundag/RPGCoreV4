package me.blutkrone.rpgcore.hud.editor.constraint.enums;

import me.blutkrone.rpgcore.effect.impl.CoreEffectBlock;

import java.util.ArrayList;
import java.util.List;

public class BlockMaskConstraint extends AbstractEnumConstraint {

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Block Mask");
        instruction.add("Allows to select certain block groups");
        return instruction;
    }

    @Override
    protected Enum<?> valueOf(String string) {
        return CoreEffectBlock.BlockMask.valueOf(string.toUpperCase());
    }

    @Override
    protected Enum<?>[] values() {
        List<Enum<?>> masks = new ArrayList<>();
        for (CoreEffectBlock.BlockMask mask : CoreEffectBlock.BlockMask.values()) {
            masks.add(mask);
        }
        return masks.toArray(new Enum<?>[0]);
    }
}
