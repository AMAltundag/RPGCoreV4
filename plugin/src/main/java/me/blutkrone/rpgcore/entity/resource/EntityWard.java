package me.blutkrone.rpgcore.entity.resource;

import me.blutkrone.rpgcore.api.entity.IEntityEffect;

/**
 * Entities may have only one ward effect, ward is another
 * entity resource to absorb damage.
 */
public class EntityWard implements IEntityEffect {

    // how much of damage is tanked by ward
    public double effectiveness;
    // maximum amount ward can recover to
    public double maximum_amount;
    // ticks for restoration to kick in
    public int restoration_delay;

    // current amount of ward that is left
    private double current_amount;
    // ticks before we can recharge again
    private int current_restoration_delay;

    // effect specific parameters
    private String icon;
    private long last_updated;
    private int duration;

    // flat to force expire the ward
    private boolean expired;

    /**
     * A ward which can tank the damage taken.
     */
    public EntityWard(String icon, int duration) {
        this.icon = icon;
        this.duration = duration;
        this.last_updated = System.currentTimeMillis();
        this.current_amount = 1;
    }

    /**
     * Tank the given amount with the ward, reducing the remaining
     * ward and returning how much it could NOT tank.
     * <p>
     * If the amount is less-equal zero, nothing happens.
     *
     * @param amount damage to take from ward
     * @return how much damage couldn't be tanked
     */
    public double damageBy(double amount) {
        // ignore if amount is less-equal zero
        if (amount <= 0) {
            return 0d;
        }

        // apply a delay on when we restore the ward
        this.current_restoration_delay = this.restoration_delay;
        // adjust the damage taken by effectiveness
        double updated = this.current_amount - (amount * this.effectiveness);

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
     * Recover the ward on the entity, do note that if the remaining ward
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
        // ignore if ward was already emptied
        if (this.current_amount <= 0d) {
            return amount;
        }
        // recover damage suffered
        double updated = this.current_amount + amount;

        if (updated > this.maximum_amount) {
            // ensure we do not recover beyond our maximum
            this.current_amount = this.maximum_amount;
            return updated - this.maximum_amount;
        } else {
            // we can heal the whole amount
            this.current_amount = updated;
            return 0d;
        }
    }

    /**
     * Maximum amount of damage which this ward can tank.
     *
     * @return maximum capacity of ward
     */
    public double getMaximumAmount() {
        return maximum_amount;
    }

    /**
     * Current amount of ward remaining.
     *
     * @return remaining ward.
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
        if (this.getMaximumAmount() <= 0d)
            return new ResourceSnapshot(0, 0, 0d);
        // otherwise compute snapshot value
        return new ResourceSnapshot((int) this.getCurrentAmount(), (int) this.getMaximumAmount(),
                this.getCurrentAmount() / this.getMaximumAmount());
    }

    /**
     * Expire the ward, it will not be used and removed at the
     * first opportunity given.
     */
    public void setExpired() {
        this.expired = true;
    }

    /**
     * Attempt to restore the ward (which triggers the restoration
     * cooldown), restoration will max out the ward. Doesn't trigger
     * if ward is maxed out already.
     */
    public void restore() {
        // restoration applies can kick in at non-maxed ward
        if (this.current_amount > 0d && this.current_amount < this.maximum_amount && this.current_restoration_delay <= 0) {
            this.current_amount = this.maximum_amount;
            this.current_restoration_delay = restoration_delay;
        }
    }

    @Override
    public boolean tickEffect(int delta) {
        // consume the duration off the ward
        this.duration -= delta;
        // count down on recharge blocker
        this.current_restoration_delay -= delta;

        // try to run a restoration
        restore();

        // remove if out of duration or out of ward
        return this.current_amount <= 0d || this.duration <= 0;
    }

    @Override
    public int getStacks() {
        return (int) this.current_amount;
    }

    @Override
    public int getDuration() {
        return this.duration;
    }

    @Override
    public String getIcon() {
        return this.icon;
    }

    @Override
    public long getLastUpdated() {
        return this.last_updated;
    }

    @Override
    public boolean isDebuff() {
        return false;
    }

    @Override
    public boolean isValid() {
        return this.current_amount > 0d && !this.expired;
    }

    @Override
    public void manipulate(int stack, int duration, boolean override) {
        if (override) {
            this.current_amount = Math.min(this.maximum_amount, stack);
            this.duration = duration;
        } else {
            this.current_amount = Math.min(this.maximum_amount, this.current_amount + stack);
            this.duration = this.duration + duration;
        }

        this.last_updated = System.currentTimeMillis();
    }

}
