package me.blutkrone.rpgcore.editor.constraint.reference.other;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.IEditorConstraint;

import java.util.ArrayList;
import java.util.List;

public class NodeConstraint implements IEditorConstraint {

    public NodeConstraint() {
    }

    @Override
    public List<String> getHint(String value) {
        List<String> options = new ArrayList<>();

        for (String node : RPGCore.inst().getNodeManager().getIndexCollectible().getKeys()) {
            options.add("collectible:"+node);
        }
        for (String node : RPGCore.inst().getNodeManager().getIndexBox().getKeys()) {
            options.add("box:"+node);
        }
        for (String node : RPGCore.inst().getNodeManager().getIndexSpawner().getKeys()) {
            options.add("spawner:"+node);
        }
        for (String node : RPGCore.inst().getNodeManager().getIndexHotspot().getKeys()) {
            options.add("hotspot:"+node);
        }
        for (String node : RPGCore.inst().getNodeManager().getIndexGate().getKeys()) {
            options.add("gate:"+node);
        }
        for (String node : RPGCore.inst().getNPCManager().getIndex().getKeys()) {
            options.add("npc:"+node);
        }

        options.removeIf(option -> !option.startsWith(value.toLowerCase()));

        return options;
    }

    @Override
    public boolean isDefined(String value) {
        try {
            return getHint("").contains(value);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void extend(String value) {
        // extend doesn't actually do anything
    }

    @Override
    public boolean canExtend() {
        return false;
    }

    @Override
    public String getConstraintAt(List container, int index) {
        return (String) container.get(index);
    }

    @Override
    public void setElementAt(List container, int index, String value) {
        container.set(index, value);
    }

    @Override
    public void addElement(List container, String value) {
        container.add(value);
    }

    @Override
    public Object asTypeOf(String value) {
        return value;
    }

    @Override
    public String toTypeOf(Object value) {
        return ((String) value);
    }

    @Override
    public List<String> getPreview(List<Object> list) {
        List<String> preview = new ArrayList<>();

        if (list.size() <= 16) {
            for (int i = 0; i < list.size(); i++) {
                preview.add(i + ": " + list.get(i));
            }
        } else {
            for (int i = 0; i < 16; i++) {
                preview.add(i + ": " + list.get(i));
            }

            preview.add("... And " + (list.size() - 16) + " More!");
        }

        return preview;
    }

    @Override
    public List<String> getInstruction() {
        List<String> instruction = new ArrayList<>();
        instruction.add("Node");
        instruction.add("This can reference nodes, however it will not create");
        instruction.add("Nodes within their index. Prefix with their type.");
        instruction.add("");
        instruction.add("Types: collectible, box, spawner, hotspot, gate, npc");
        return instruction;
    }

    @Override
    public String getPreview(Object object) {
        return String.valueOf(object);
    }
}
