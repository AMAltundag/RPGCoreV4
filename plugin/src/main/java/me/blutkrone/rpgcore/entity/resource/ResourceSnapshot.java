package me.blutkrone.rpgcore.entity.resource;

/**
 * A snapshot of a resource.
 */
public class ResourceSnapshot {
    // current remaining amount
    public final int current;
    // maximum amount of resource
    public final int maximum;
    // % based resource
    public final double fraction;

    /**
     * A snapshot of a resource.
     *
     * @param current  the current amount left
     * @param maximum  the maximum amount available
     * @param fraction ratio described as percentage
     */
    public ResourceSnapshot(int current, int maximum, double fraction) {
        this.current = current;
        this.maximum = maximum;
        this.fraction = fraction;
    }
}
