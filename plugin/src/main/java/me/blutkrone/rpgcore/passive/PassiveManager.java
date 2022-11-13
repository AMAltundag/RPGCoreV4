package me.blutkrone.rpgcore.passive;

import me.blutkrone.rpgcore.hud.editor.index.EditorIndex;
import me.blutkrone.rpgcore.hud.editor.root.passive.EditorPassiveNode;
import me.blutkrone.rpgcore.hud.editor.root.passive.EditorPassiveTree;

public class PassiveManager {

    // nodes accessible to relevant trees
    private EditorIndex<CorePassiveNode, EditorPassiveNode> node_index;
    private EditorIndex<CorePassiveTree, EditorPassiveTree> tree_index;

    public PassiveManager() {
        this.node_index = new EditorIndex<>("passive", EditorPassiveNode.class, EditorPassiveNode::new);
        this.tree_index = new EditorIndex<>("tree", EditorPassiveTree.class, EditorPassiveTree::new);
    }

    public EditorIndex<CorePassiveTree, EditorPassiveTree> getTreeIndex() {
        return tree_index;
    }

    public EditorIndex<CorePassiveNode, EditorPassiveNode> getNodeIndex() {
        return node_index;
    }
}
