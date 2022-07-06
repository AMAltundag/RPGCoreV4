package me.blutkrone.rpgcore.entity.resource;

import me.blutkrone.rpgcore.api.entity.IEntityEffect;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Entities may have only one ward effect, ward is another
 * entity resource to absorb damage.
 */
public class EntityWard implements IEntityEffect {

    // whose effect is this ward
    private final CoreEntity entity;

    // recoup recovers based on damage taken in 4s
    public double recoup_rate = 0d;
    // % speed to recover all recouped damage
    public double recoup_speed = 0d;
    // regeneration is always active
    public double regeneration_percent = 0d;
    public double regeneration_flat = 0d;
    // recharge is inactive if hurt in past 4s
    public double recharge_percent = 0.1d;
    public double recharge_flat = 0d;
    // % rate to slow/accelerate recharge delay
    public double recharge_delay = 4d;
    // how much of damage is tanked by ward
    public double effectiveness = 1d;
    // rate at which damage is leeched as ward
    public double leech_rate;
    // maximum recover thorough leech in 1 second
    public double leech_maximum;

    // maximum amount ward can recover to
    private double maximum_amount;
    // current amount of ward that is left
    private double current_amount;
    // ward available in the leech buffer
    private double leech_buffer;
    // ward available in the recoup buffer
    private List<RecoupTicket> recoup_buffer = new ArrayList<>();
    // ticks before we can recharge again
    private int current_recharge_delay;

    // effect specific parameters
    private String id;
    private String icon;
    private long last_updated;
    private int duration;

    /**
     * A ward which can tank the damage taken.
     */
    public EntityWard(String id, CoreEntity entity, String icon, int duration) {
        this.id = id;
        this.entity = entity;
        this.icon = icon;
        this.duration = duration;
        this.last_updated = System.currentTimeMillis();
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

        // apply a delay on when we can recharge again
        this.current_recharge_delay = (int) (80d / Math.max(0.01d, 1d + this.recharge_delay));

        // adjust the damage taken by effectiveness
        double updated = this.current_amount - (amount * this.effectiveness);

        // allocate a recoup ticket
        this.recoup_buffer.add(new RecoupTicket(amount * this.recoup_rate));

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

    @Override
    public boolean tickEffect(int delta) {
        // consume the duration off the ward
        this.duration -= delta;
        // count down on recharge blocker
        this.current_recharge_delay -= delta;

        // recover if we have more then 0 ward left
        if (this.current_amount > 0d) {
            // ratio based off the ticking rate
            double time_ratio = delta / 20d;
            // compute how much ward we want to recover
            double ward_to_recover = 0d;
            // recovery thorough regeneration
            ward_to_recover += this.regeneration_flat * time_ratio;
            ward_to_recover += this.regeneration_percent * this.maximum_amount * time_ratio;
            // recovery thorough recharge
            if (this.current_recharge_delay <= 0) {
                ward_to_recover += this.recharge_flat * time_ratio;
                ward_to_recover += this.recharge_percent * this.maximum_amount * time_ratio;
            }
            // recovery via recoup tickets
            double recoup_rate = 0.25d * time_ratio * Math.max(0.1d, 1d + this.recoup_speed);
            for (RecoupTicket recoup : recoup_buffer) {
                double step = recoup.maximum * recoup_rate;
                if (recoup.remaining <= step) {
                    ward_to_recover += recoup.remaining;
                    recoup.remaining = 0d;
                } else {
                    ward_to_recover += step;
                    recoup.remaining -= step;
                }
            }
            this.recoup_buffer.removeIf((recoup -> recoup.remaining <= 0d));
            // recovery via leeched damage
            double available_leech = Math.max(0d, Math.min(this.leech_buffer, this.maximum_amount * this.leech_maximum * time_ratio));
            this.leech_buffer -= available_leech;
            ward_to_recover += available_leech;
            // acquire the total amount to recover
            if (recoverBy(ward_to_recover) > 0) {
                // leech resets if resource is capped.
                this.leech_buffer = 0d;
            }
        }

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

    private class RecoupTicket {
        double remaining;
        double maximum;

        private RecoupTicket(double maximum) {
            this.remaining = maximum;
            this.maximum = maximum;
        }
    }
}
