package me.blutkrone.rpgcore.util.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A wrapper for a branching tree graph.
 *
 * @param <T>
 */
public class TreeGraph<T> {

    // highest level element of the tree
    private TreeNode<T> root;

    /**
     * A wrapper for a branching tree graph.
     *
     * @param root what element to serve as our ancestor.
     */
    public TreeGraph(T root) {
        this.root = new TreeNode<>(null, root, 0);
    }

    /**
     * Search the entire graph for a node which matches
     * the predicate.
     *
     * @param predicate the predicate to check for
     * @return the node or null if absent.
     */
    public TreeNode<T> find(Predicate<T> predicate) {
        List<TreeNode<T>> nodes = new ArrayList<>();
        nodes.add(this.root);

        while (!nodes.isEmpty()) {
            TreeNode<T> node = nodes.remove(0);
            if (predicate.test(node.getData())) {
                return node;
            }
            nodes.addAll(node.getChildren());
        }

        return null;
    }

    /**
     * The root node within our tree structure.
     *
     * @return what sort of node we have.
     */
    public TreeNode<T> getRoot() {
        return root;
    }

    /**
     * A node within the tree collection.
     *
     * @param <T>
     */
    public static class TreeNode<T> {
        // parent of this node
        final TreeNode<T> parent;
        // children of this node
        final List<TreeNode<T>> children;
        // value of the node
        final T data;
        // how many ancestors we have
        final int depth;

        TreeNode(TreeNode<T> parent, T data, int depth) {
            this.parent = parent;
            this.children = new ArrayList<>();
            this.data = data;
            this.depth = depth;
        }

        /**
         * Depth refers to how many ancestor nodes are above us.
         *
         * @return the current depth we are at.
         */
        public int getDepth() {
            return depth;
        }

        /**
         * Fetch data contained within the node.
         *
         * @return data within this node.
         */
        public T getData() {
            return data;
        }

        /**
         * Grab the ancestral node, this will only be null if
         * we've reached the root element.
         *
         * @return parent of this node
         */
        public TreeNode<T> getParent() {
            return parent;
        }

        /**
         * Iterate over every child node within this node.
         *
         * @param consumer nodes contained by this one.
         */
        public void forEach(Consumer<T> consumer) {
            for (TreeNode<T> node : this.children) {
                consumer.accept(node.getData());
            }
        }

        /**
         * Search for a direct child node, whose value matches with
         * the predicate.
         *
         * @param predicate the predicate to check against
         * @return the found node, or nothing.
         */
        public TreeNode<T> find(Predicate<T> predicate) {
            TreeNode<T> found = null;
            for (TreeNode<T> child : this.children) {
                if (predicate.test(child.data)) {
                    found = child;
                }
            }
            return found;
        }

        /**
         * Adds another child to this node.
         *
         * @param data the data we want to add.
         */
        public void add(T data) {
            this.children.add(new TreeNode<>(this, data, this.depth + 1));
        }

        /**
         * All children in the graph.
         *
         * @return a read-only view on children.
         */
        public List<TreeNode<T>> getChildren() {
            return Collections.unmodifiableList(children);
        }
    }
}
