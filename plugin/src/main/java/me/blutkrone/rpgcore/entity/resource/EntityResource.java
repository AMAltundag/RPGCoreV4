package me.blutkrone.rpgcore.entity.resource;

import me.blutkrone.rpgcore.entity.entities.CoreEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * A resource can be consumed for certain things, the exact reason
 * it is consumed differing based on circumstances.
 */
public class EntityResource {

    // resource specific parameters
    private final String id;
    private final CoreEntity entity;
    private final boolean nullable;
    private final int minimum;
    // snapshot of latest maximum amount
    private double snapshot_maximum = 1d;
    // current amount of resource that is left
    private double current_amount = 1d;
    // resource available in the leech buffer
    private double leech_buffer;
    // resource available in the recoup buffer
    private List<RecoupTicket> recoup_buffer = new ArrayList<>();
    // ticks before we can recharge again
    private int current_recharge_delay;

    /**
     * A resource can be consumed for certain things, the exact reason
     * it is consumed differing based on circumstances.
     *
     * @param id       identifier which constructs relevant attributes
     * @param entity   which entity holds the resource
     * @param nullable if the resource can recover after reaching 0
     */
    public EntityResource(String id, CoreEntity entity, boolean nullable, int minimum) {
        this.id = id;
        this.entity = entity;
        this.nullable = nullable;
        this.minimum = minimum;
    }

    /**
     * Tank the given amount with the resource, reducing the remaining
     * resource and returning how much it could NOT tank.
     * <p>
     * If the amount is less-equal zero, nothing happens.
     *
     * @param amount damage to take from resource
     * @return how much damage couldn't be tanked
     */
    public double damageBy(double amount) {
        // ignore if amount is less-equal zero
        if (amount <= 0) {
            return 0d;
        }

        // compute attributes that matter for resource
        double recharge_faster = this.entity.evaluateAttribute(this.id + "_RECHARGE_FASTER");
        double recoup_rate = this.entity.evaluateAttribute(id + "_RECOUP_RATE");

        // apply a delay on when we can recharge again
        this.current_recharge_delay = (int) (80d / Math.max(0.01d, 1d + recharge_faster));

        // adjust the damage taken by effectiveness
        double updated = this.current_amount - amount;

        // allocate a recoup ticket
        this.recoup_buffer.add(new RecoupTicket(amount * recoup_rate));

        if (updated < 0) {
            // ensure we do not tank more then we have
            this.current_amount = 0;
            return (-1) * updated;
        } else {
            // we can tank everything
            this.current_amount = updated;
            return 0d;
        }
    }

    /**
     * Sets remaining resource to the given amount, this will bypass
     * any checks and validations - do NOT use this without absolute
     * certainty about the behaviour.
     *
     * @param amount the amount to update to.
     */
    public void setToExactUnsafe(double amount) {
        this.current_amount = amount;
    }

    /**
     * Recover the resource on the entity, do note that if the remaining resource
     * is zero it cannot be recovered at all and will be removed soon.
     * <p>
     * If the amount is less-equal zero, nothing happens.
     *
     * @param amount the amount to recover by
     * @return the excess which we couldn't recover
     */
    public double recoverBy(double amount) {
        // ignore if amount is less-equal zero
        if (amount <= 0) {
            return 0d;
        }
        // ignore if resource was already emptied
        if (!this.nullable && this.current_amount <= 0d) {
            return amount;
        }

        // recover damage suffered
        double updated = this.current_amount + amount;

        if (updated > this.snapshot_maximum) {
            // ensure we do not recover beyond our maximum
            this.current_amount = this.snapshot_maximum;
            return updated - this.snapshot_maximum;
        } else {
            // we can heal the whole amount
            this.current_amount = updated;
            return 0d;
        }
    }

    /**
     * Add a certain amount to the recoup buffer, this is a direct
     * addition independent of the recoup rate.
     *
     * @param amount the amount to recoup
     */
    public void addRecoup(double amount) {
        this.recoup_buffer.add(new RecoupTicket(amount));
    }

    /**
     * Add a certain amount to the leech buffer, this is a direct
     * addition independent of recoup rate.
     *
     * @param amount the amount which can be leeched.
     */
    public void addLeech(double amount) {
        this.leech_buffer += Math.max(0d, amount);
    }

    /*
     * Generate snapshots of the attributes for read-only purposes
     */
    public void updateAttributeSnapshots() {
        // compute our updated maximum amount
        double maximum_amount = this.attribute("MAXIMUM");
        maximum_amount *= 1d + this.attribute("MAXIMUM_PERCENT");
        maximum_amount = Math.max(this.minimum, maximum_amount);
        this.entity.getAttribute(this.id + "_MAXIMUM_SNAPSHOT").setOverride(maximum_amount);

        // update recovery information
        this.entity.getAttribute(this.id + "_REGENERATION_SNAPSHOT").setOverride(this.attribute("REGENERATION_FLAT") + this.attribute("REGENERATION_PERCENT") * maximum_amount);
        this.entity.getAttribute(this.id + "_RECHARGE_SNAPSHOT").setOverride(this.attribute("RECHARGE_FLAT") + this.attribute("RECHARGE_PERCENT") * maximum_amount);
        
        // snapshot for recoup time
        if (this.attribute("RECOUP_PER_SECOND") <= 0d) {
            this.entity.getAttribute(this.id + "_RECOUP_TIME_SNAPSHOT").setOverride(0d);
        } else {
            this.entity.getAttribute(this.id + "_RECOUP_TIME_SNAPSHOT").setOverride(20d * (1d / this.attribute("RECOUP_PER_SECOND")));
        }

        // snapshot for recharge delay
        double recharge_faster = this.entity.evaluateAttribute(this.id + "_RECHARGE_FASTER");
        this.entity.getAttribute(this.id + "_RECHARGE_TIME_SNAPSHOT").setOverride((int) (80d / Math.max(0.01d, 1d + recharge_faster)));
    }

    /**
     * Tick the recovery logic associated with the resource.
     *
     * @param delta rate at which we are ticking.
     */
    public void tickRecovery(int delta) {
        // always count down recharge delay
        this.current_recharge_delay -= delta;
        // compute our updated maximum amount
        double maximum_amount = this.attribute("MAXIMUM");
        maximum_amount *= 1d + this.attribute("MAXIMUM_PERCENT");
        maximum_amount = Math.max(this.minimum, maximum_amount);
        // re-scale our resource to updated maximum amount
        if (Math.abs(maximum_amount - this.snapshot_maximum) > 0.01d) {
            double fraction = this.current_amount / this.snapshot_maximum;
            this.snapshot_maximum = maximum_amount;
            this.current_amount = maximum_amount * fraction;
        }
        // only recover if we are a nullable resource
        if (this.nullable || this.current_amount > 0d) {
            // ratio based off the ticking rate
            double time_ratio = delta / 20d;
            // compute how much resource we want to recover
            double resource_to_recover = 0d;
            // recovery thorough regeneration
            resource_to_recover += this.attribute("REGENERATION_FLAT");
            resource_to_recover += this.attribute("REGENERATION_PERCENT") * maximum_amount;
            // recovery thorough recharge
            if (this.current_recharge_delay <= 0) {
                resource_to_recover += this.attribute("RECHARGE_FLAT");
                resource_to_recover += this.attribute("RECHARGE_PERCENT") * maximum_amount;
            }
            // apply time ratio
            resource_to_recover = resource_to_recover * time_ratio;
            // recovery via recoup tickets
            double recoup_rate = Math.max(0d, this.attribute("RECOUP_PER_SECOND")) * time_ratio;
            for (RecoupTicket recoup : recoup_buffer) {
                double step = recoup.maximum * recoup_rate;
                if (recoup.remaining <= step) {
                    resource_to_recover += recoup.remaining;
                    recoup.remaining = 0d;
                } else {
                    resource_to_recover += step;
                    recoup.remaining -= step;
                }
            }
            this.recoup_buffer.removeIf((recoup -> recoup.remaining <= 0d));
            // recovery via leeched damage
            double available_leech = Math.max(0d, Math.min(this.leech_buffer, maximum_amount * this.attribute("LEECH_MAXIMUM") * time_ratio));
            this.leech_buffer -= available_leech;
            resource_to_recover += available_leech;
            // acquire the total amount to recover
            if (recoverBy(resource_to_recover) > 0) {
                // leech resets if resource is capped.
                this.leech_buffer = 0d;
            }
        }
    }

    /**
     * The last snapshot of the maximum amount of this resource, which does
     * respect additional attributes which affect it.
     *
     * @return the snapshot of the maximum amount.
     */
    public double getSnapshotMaximum() {
        return snapshot_maximum;
    }

    /**
     * Retrieve the remaining amount of this resource
     *
     * @return how much of this resource is left.
     */
    public double getCurrentAmount() {
        return current_amount;
    }

    /**
     * Create a snapshot of the current resource state.
     *
     * @return snapshot of current state
     */
    public ResourceSnapshot snapshot() {
        // 0-maximum should provide empty snapshot
        if (this.getSnapshotMaximum() <= 0d)
            return new ResourceSnapshot(0, 0, 0d);
        // otherwise compute snapshot value
        return new ResourceSnapshot((int) this.getCurrentAmount(), (int) this.getSnapshotMaximum(),
                this.getCurrentAmount() / this.getSnapshotMaximum());
    }

    private double attribute(String attribute) {
        return this.entity.evaluateAttribute(this.id + "_" + attribute);
    }

    private class RecoupTicket {
        double remaining;
        double maximum;

        private RecoupTicket(double maximum) {
            this.remaining = maximum;
            this.maximum = maximum;
        }
    }
}
