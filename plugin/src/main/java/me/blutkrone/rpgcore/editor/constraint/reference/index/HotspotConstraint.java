package me.blutkrone.rpgcore.editor.constraint.reference.index;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.index.EditorIndex;

import java.util.ArrayList;
import java.util.List;

public class HotspotConstraint extends AbstractIndexConstraint {

    @Override
    public EditorIndex<?, ?> getIndex() {
        return RPGCore.inst().getNodeManager().getIndexHotspot();
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Hotspot Node");
        instruction.add("Used for quest tasks, will complete the task should you");
        instruction.add("Stand close enough to the node");
        return instruction;
    }
}
