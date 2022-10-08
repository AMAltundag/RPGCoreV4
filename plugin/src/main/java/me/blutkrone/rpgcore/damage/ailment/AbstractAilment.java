package me.blutkrone.rpgcore.damage.ailment;

import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.world.ParticleUtility;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * An ailment is a secondary effect caused by non-DOT damage.
 */
public abstract class AbstractAilment {

    // unique identifier of the ailment
    protected String identifier;

    // attribute defining non-crit chance to be set off
    protected List<String> attribute_chance;
    // separate roll to reject applying of an ailment
    protected List<String> attribute_avoid;

    // decor instruments to depict the ailment
    private String decor_ux;
    private NavigableMap<Double, ItemStack> decor_model;
    private WeightedRandomMap<ParticleUtility> decor_particle;

    /**
     * An ailment is a secondary effect caused by non-DOT damage.
     *
     * @param id     identifier of this ailment
     * @param config how to setup the ailment.
     */
    public AbstractAilment(String id, ConfigWrapper config) {
        this.identifier = id;
        this.decor_ux = config.getString("ux-symbol");
        this.decor_model = new TreeMap<>();
        this.decor_particle = new WeightedRandomMap<>();
        config.forEachUnder("model", (path, root) -> {
            ItemStack item = ItemBuilder.of(root.getString(path)).build();
            double size = Double.parseDouble(path);
            this.decor_model.put(size, item);
        });
        config.forEachUnder("particle", (path, root) -> {
            ParticleUtility particle = new ParticleUtility(root.getSection(path));
            this.decor_particle.add(particle.getWeighting(), particle);
        });
    }

    /**
     * Should the entity have no tracker for the given ailment, this method
     * will create one for them. We may abandon a tracker once it no longer
     * has any active ailment.
     * <p>
     * This method is not allowed to fail.
     *
     * @param holder who will hold the tracker.
     * @return the newly created tracker.
     */
    public abstract AilmentTracker createTracker(CoreEntity holder);

    /**
     * A model used to indicate the entity is affected by the
     * ailment, the size depending on the entity.
     *
     * @return which item model to attach to entity.
     */
    public ItemStack getModel(CoreEntity entity) {
        // no model to be used if no scale exists
        if (this.decor_model.isEmpty()) return null;
        // identify the model we are to present forward
        BoundingBox bounds = entity.getEntityProvider().getBounds(entity.getEntity());
        double scale = Math.max(bounds.getHeight(), Math.max(bounds.getWidthX(), bounds.getWidthZ()));
        Map.Entry<Double, ItemStack> entry = this.decor_model.ceilingEntry(scale);
        if (entry == null) entry = this.decor_model.lastEntry();
        return entry == null ? null : entry.getValue();
    }

    /**
     * Which symbol to showcase on the UX.
     *
     * @return the UX symbol representing the ailment.
     */
    public String getSymbolUX() {
        return decor_ux;
    }

    /**
     * Particles to spawn while affected by ailment.
     *
     * @return which particles can spawn.
     */
    public ParticleUtility getParticle() {
        return decor_particle.next();
    }
}
