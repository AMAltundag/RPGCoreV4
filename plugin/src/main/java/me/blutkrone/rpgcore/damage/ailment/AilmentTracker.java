package me.blutkrone.rpgcore.damage.ailment;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.damage.interaction.DamageInteraction;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.util.world.ParticleUtility;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AilmentTracker {
    // the ailment which is being tracked
    private AbstractAilment ailment;
    // who is afflicted with the ailment
    private CoreEntity holder;
    // visual entity serving as an indicator
    private ItemDisplay visual;

    /**
     * A tracker who is tracking a specific ailment for one
     * single entity.
     *
     * @param ailment the ailment which we are tracking
     * @param holder  who was afflicted with the ailment
     */
    public AilmentTracker(AbstractAilment ailment, CoreEntity holder) {
        this.ailment = ailment;
        this.holder = holder;
    }

    /*
     * Generate random locations spread around the entity.
     *
     * @param affected who to base the points off.
     * @return random points scattered in close proximity.
     */
    private static List<Location> getRandomLocations(CoreEntity affected) {
        // basic modifiers we operate with
        LivingEntity entity = affected.getEntity();
        Location location = entity.getLocation();
        BoundingBox bounds = affected.getEntityProvider().getBounds(entity);
        // generate random locations within the given space
        List<Location> selected = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Location current = location.clone();
            // random location on the horizontal axis
            current.setX(current.getX() + ThreadLocalRandom.current().nextDouble(-bounds.getWidthX(), +bounds.getWidthX()));
            current.setY(current.getY() + ThreadLocalRandom.current().nextDouble(-bounds.getHeight(), +bounds.getHeight()));
            current.setZ(current.getZ() + ThreadLocalRandom.current().nextDouble(-bounds.getWidthZ(), +bounds.getWidthZ()));
            // the current position we are located at
            selected.add(current);
        }
        // provide the random locations we generated
        return selected;
    }

    /**
     * Make an effort to construct an ailment, the contract of this method
     * is to be specified by the implementing ailment.
     *
     * @param interaction the damage which was dealt
     * @return true, should we've acquired the ailment.
     */
    public abstract boolean acquireAilment(DamageInteraction interaction);

    /**
     * Tick the backing implementation of the tracker.
     *
     * @param interval how many ticks passed since the last invocation.
     * @return true should the instance be allowed to be discarded
     */
    public abstract boolean tick(int interval);

    /**
     * Update the decoration intended to assist with decor
     * hints of the ailment.
     * <br>
     * This method is automatically invoked after ticking.
     */
    public void decorate(int interval) {
        // the bukkit entity which we are decorating
        LivingEntity entity_handle = this.holder.getEntity();
        // present the particles to the surrounding players
        List<Player> observed_players = RPGCore.inst().getEntityManager().getObserving(entity_handle.getLocation());
        List<Location> particle_spots = AilmentTracker.getRandomLocations(this.holder);
        for (Location spot : particle_spots) {
            ParticleUtility particle = this.ailment.getParticle();
            if (particle == null) continue;
            particle.showAt(spot, observed_players);
        }
        // present the model used to the surrounding players
        ItemStack model = this.ailment.getDecorModel();
        if (model != null) {
            // create the visual entity
            if (this.visual == null) {
                this.visual = (ItemDisplay) entity_handle.getWorld().spawnEntity(entity_handle.getLocation(), EntityType.ITEM_DISPLAY);
                this.visual.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
                this.visual.setBillboard(Display.Billboard.FIXED);
                BoundingBox bounds = this.holder.getEntityProvider().getBounds(entity_handle);
                double scale = Math.max(bounds.getHeight(), Math.max(bounds.getWidthX(), bounds.getWidthZ())) / 2d;
                this.visual.setTransformation(new Transformation(new Vector3f(), new Quaternionf(), new Vector3f((float) scale), new Quaternionf()));
            }
            // maintain it as riding
            if (this.visual.getVehicle() != entity_handle) {
                entity_handle.addPassenger(this.visual);
            }
        }
    }

    /**
     * A method invoked when the tracker is removed, this method
     * is always called.
     */
    public void abandon() {
        if (this.visual != null) {
            this.visual.remove();
            this.visual = null;
        }
    }

    protected CoreEntity getHolder() {
        return holder;
    }

    protected AbstractAilment getAilment() {
        return ailment;
    }
}
