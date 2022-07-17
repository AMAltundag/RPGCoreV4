package me.blutkrone.rpgcore.item.data;

import me.blutkrone.rpgcore.item.CoreItem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Tracks durability specific information
 */
public class ItemDataDurability extends AbstractItemData {

    private int remaining_durability;

    public ItemDataDurability(ObjectInputStream buffer) throws IOException {
        super(buffer);

        this.remaining_durability = buffer.readInt();
    }

    public ItemDataDurability(CoreItem item, double quality) throws IOException {
        super(item, quality, 1);

        this.remaining_durability = Math.max(0, item.getMaximumDurability());
    }

    @Override
    public void save(ObjectOutputStream buffer) throws IOException {
        buffer.writeInt(this.remaining_durability);
    }

    /**
     * The amount of durability remaining on this item.
     *
     * @return current amount of durability.
     */
    public int getDurability() {
        return this.remaining_durability;
    }

    /**
     * Update the durability, provided this item can actually have
     * any durability.
     *
     * @param durability the new amount of durability, greater-equal 0
     */
    public void setDurability(int durability) {
        if (this.hasDurability() && durability >= 0) {
            this.remaining_durability = durability;
        }
    }

    /**
     * How much durability can this item have at most.
     *
     * @return maximum durability we can recover to.
     */
    public int getMaximumDurability() {
        return this.item.getMaximumDurability();
    }

    /**
     * Percentage of durability remaining.
     *
     * @return durability percentage between 0.0 to 1.0
     */
    public double getAsPercentage() {
        double maximum = this.getMaximumDurability();
        double current = this.getDurability();
        return maximum > 0 ? (current / maximum) : 1d;
    }

    /**
     * Whether this item uses any durability.
     *
     * @return true if we use durability.
     */
    public boolean hasDurability() {
        return this.item.getMaximumDurability() > 0;
    }
}
