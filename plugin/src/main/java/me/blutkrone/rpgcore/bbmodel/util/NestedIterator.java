package me.blutkrone.rpgcore.bbmodel.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * An iterator that can traverse a nested hierarchy.
 *
 * @param <K>
 */
public abstract class NestedIterator<K> implements Iterator<K> {

    private final Queue<K> working;

    /**
     * An iterator that can traverse a nested hierarchy.
     *
     * @param root Root of the hierarchy.
     */
    public NestedIterator(K root) {
        this.working = new LinkedList<>();
        this.working.add(root);
    }

    /**
     * Extract the nested elements from the target object.
     *
     * @param current Current object
     * @return Elements directly nested into it.
     */
    public abstract Collection<K> getNestedFrom(K current);

    @Override
    public boolean hasNext() {
        return !this.working.isEmpty();
    }

    @Override
    public K next() {
        K next = this.working.poll();
        working.addAll(getNestedFrom(next));
        return next;
    }
}