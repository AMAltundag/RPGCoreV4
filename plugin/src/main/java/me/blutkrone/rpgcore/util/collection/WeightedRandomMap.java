package me.blutkrone.rpgcore.util.collection;

import java.util.*;

/**
 * Utility to help with random choices.
 *
 * @param <E>
 */
public class WeightedRandomMap<E> {
    private NavigableMap<Double, E> map = new TreeMap<>();
    private Map<E, Double> reverse = new HashMap<>();
    private Random random;
    private double total = 0;

    public WeightedRandomMap() {
        this.random = new Random();
    }

    /**
     * Retrieve the weight from the map.
     *
     * @param object object to check
     * @return weight of object
     */
    public double weight(E object) {
        return this.reverse.getOrDefault(object, 0d);
    }

    /**
     * Check if no elements are in the map.
     *
     * @return true if we have no elements.
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Adds another choice to the map.
     *
     * @param weight
     * @param object
     */
    public void add(double weight, E object) {
        if (weight <= 0) return;
        total += weight;
        map.put(total, object);
        reverse.put(object, weight);
    }

    /**
     * Adds another choice to the map.
     *
     * @param object
     * @param weight
     */
    public void add(E object, Double weight) {
        if (weight <= 0) return;
        total += weight;
        map.put(total, object);
        reverse.put(object, weight);
    }

    /**
     * Random choice, null if no elements are listed.
     *
     * @return random choice.
     */
    public E next() {
        try {
            double value = (random.nextDouble() * total);
            return map.ceilingEntry(value).getValue();
        } catch (Exception ignored) {
            return null;
        }
    }
}
