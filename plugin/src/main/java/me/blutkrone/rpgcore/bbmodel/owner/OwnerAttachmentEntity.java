package me.blutkrone.rpgcore.bbmodel.owner;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.bbmodel.active.ActiveModel;
import me.blutkrone.rpgcore.bbmodel.active.component.LocationSnapshot;
import me.blutkrone.rpgcore.bbmodel.io.deserialized.Model;
import me.blutkrone.rpgcore.bbmodel.util.ObservationMap;
import me.blutkrone.rpgcore.bbmodel.util.exception.BBExceptionRecycled;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.nms.api.packet.grouping.IBundledPacket;
import me.blutkrone.rpgcore.nms.api.packet.handle.IItemDisplay;
import me.blutkrone.rpgcore.nms.api.packet.wrapper.VolatileBillboard;
import me.blutkrone.rpgcore.nms.api.packet.wrapper.VolatileDisplay;
import me.blutkrone.rpgcore.util.ItemBuilder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class OwnerAttachmentEntity implements IModelOwner {

    private String animation = null;
    private CoreEntity entity;
    private float size;
    private int duration;

    private ActiveModel blockbench_model = null;
    private ObservationMap itemized_model = null;

    private Location last_location = null;

    private boolean recycled;

    public OwnerAttachmentEntity(CoreEntity entity, String model, String animation, double speed, int duration, double size) {
        this.entity = entity;
        this.size = (float) size;
        this.duration = duration;
        this.last_location = entity.getLocation();
        if (!animation.isBlank() || !animation.contains("nothingness")) {
            this.animation = animation;
        }

        Model bbmodel = RPGCore.inst().getResourcepackManager().getModel(model);
        if (bbmodel != null) {
            // initialize a blockbench model
            this.blockbench_model = new ActiveModel(bbmodel);
            this.blockbench_model.play(animation, (float) speed);
        } else {
            try {
                Location location = this.entity.getLocation();
                int entityID = this.entity.getEntity().getEntityId();
                ItemStack itemstack = ItemBuilder.of(model).build();
                IItemDisplay item = RPGCore.inst().getVolatileManager().getPackets().item();

                // prepare "show" handling
                IBundledPacket itemized_show = RPGCore.inst().getVolatileManager().getPackets().bundle();
                itemized_show.takeFromOther(item.spawn(location));
                itemized_show.takeFromOther(item.item(itemstack, VolatileBillboard.FIXED, VolatileDisplay.FIXED));
                itemized_show.takeFromOther(item.scale(size, size, size));
                itemized_show.takeFromOther(item.mount(entityID));
                // prepare "hide" handling
                IBundledPacket itemized_hide = RPGCore.inst().getVolatileManager().getPackets().bundle();
                itemized_hide.takeFromOther(item.destroy());

                // use an observation mapping to handle logic
                this.itemized_model = new ObservationMap() {
                    @Override
                    public void whenStart(Player player) {
                        itemized_show.dispatch(player);
                    }

                    @Override
                    public void whenFinish(Player player) {
                        itemized_hide.dispatch(player);
                    }
                };
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Refresh remaining duration fo attachment
     *
     * @param duration Updated duration.
     */
    public void refreshDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public void use(Model model) throws BBExceptionRecycled {
        throw new UnsupportedOperationException("Attachments cannot do this!");
    }

    @Override
    public Location getLocation(String bone, boolean normalized) {
        throw new UnsupportedOperationException("Attachments cannot do this!");
    }

    @Override
    public void recycle() {
        this.recycled = true;
    }

    @Override
    public void sync(int delta) {
        // no priority operations needed
    }

    @Override
    public IModelOwner async(int delta) {
        Location last_location = RPGCore.inst().getEntityManager().getLastLocation(this.entity.getUniqueId());
        this.duration -= 1;

        if (this.blockbench_model != null) {
            if (this.recycled) {
                this.blockbench_model.dieNaturally();
                return new OwnerDying(this.blockbench_model, this.last_location, this.size);
            } else if (this.last_location == null) {
                this.blockbench_model.dieNaturally();
                return new OwnerDying(this.blockbench_model, this.last_location, this.size);
            } else {
                if (this.animation != null && !this.blockbench_model.playing(this.animation)) {
                    this.blockbench_model.dieNaturally();
                    return new OwnerDying(this.blockbench_model, this.last_location, this.size);
                } else if (this.animation == null && this.duration <= 0) {
                    this.blockbench_model.dieNaturally();
                    return new OwnerDying(this.blockbench_model, this.last_location, this.size);
                } else {
                    this.blockbench_model.async(delta, size, new LocationSnapshot(last_location));
                    return this;
                }
            }
        } else if (this.itemized_model != null) {
            if (this.last_location == null || this.duration <= 0 || this.recycled) {
                this.itemized_model.terminate();
                return null;
            } else {
                this.itemized_model.update(new LocationSnapshot(last_location), 64);
                return this;
            }
        } else {
            return null;
        }
    }
}
