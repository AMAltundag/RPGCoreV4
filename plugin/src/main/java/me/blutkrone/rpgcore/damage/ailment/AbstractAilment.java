package me.blutkrone.rpgcore.damage.ailment;

import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.world.ParticleUtility;
import org.bukkit.inventory.ItemStack;

/**
 * An ailment is a secondary effect caused by non-DOT damage.
 */
public abstract class AbstractAilment {

    // unique identifier of the ailment
    protected String identifier;

    // decor instruments to depict the ailment
    private String decor_ux;
    private ItemStack decor_model;
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
        if (!config.getString("model", "").isBlank()) {
            this.decor_model = ItemBuilder.of(config.getString("model")).build();
        }
        this.decor_particle = new WeightedRandomMap<>();
        config.forEachUnder("particles", (path, root) -> {
            ParticleUtility particle = new ParticleUtility(root.getSection(path));
            this.decor_particle.add(particle.getWeighting(), particle);
        });
    }

    /**
     * Should the entity have no tracker for the given ailment, this method
     * will create one for them. We may abandon a tracker once it no longer
     * has any active ailment.
     * <br>
     * This method is not allowed to fail.
     *
     * @param holder who will hold the tracker.
     * @return the newly created tracker.
     */
    public abstract AilmentTracker createTracker(CoreEntity holder);

    /**
     * Retrieve the model applied when we are afflicted by the ailment.
     *
     * @return Item model while afflicted, may be null.
     */
    public ItemStack getDecorModel() {
        return decor_model;
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
