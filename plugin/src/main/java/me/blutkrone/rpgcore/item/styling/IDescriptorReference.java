package me.blutkrone.rpgcore.item.styling;

import me.blutkrone.rpgcore.damage.DamageMetric;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * A common ancestor intended for items which may need
 * tuning for the specific recipient.
 */
public interface IDescriptorReference {

    /**
     * Items socketed on the passive tree.
     *
     * @return Items socketed in passive tree.
     */
    Map<String, Map<Long, ItemStack>> getPassiveSocketed();

    /**
     * Metric tracking damage output for a cause.
     *
     * @param metric What metric to check
     * @return The metric for that type, if it exists
     */
    DamageMetric getMetric(String metric);
}
