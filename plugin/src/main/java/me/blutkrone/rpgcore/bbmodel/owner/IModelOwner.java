package me.blutkrone.rpgcore.bbmodel.owner;

import me.blutkrone.rpgcore.bbmodel.io.deserialized.Model;
import me.blutkrone.rpgcore.bbmodel.util.exception.BBExceptionRecycled;
import org.bukkit.Location;

public interface IModelOwner {

    /**
     * Update the model owner to use the given model, previous
     * model will be cleaned up.
     *
     * @param model    The model to use
     */
    void use(Model model) throws BBExceptionRecycled;

    /**
     * Get the world location of a bone, while the direction defaults to
     * being relative to the parent bone special bones may not respect a
     * rule like that.
     *
     * @param bone The bone we want
     * @param normalized Normalize rotation
     * @return The bone
     */
    Location getLocation(String bone, boolean normalized);

    /**
     * Invoke upon loss of the owner to clean up the mob, nothing
     * will happen if we called this method already.
     */
    void recycle();

    /**
     * Update method, invoked from the main thread.
     *
     * @param delta Ticks since last update
     */
    void sync(int delta);

    /**
     * Update method, invoked from non-main thread.
     *
     * @param delta Ticks since last update
     * @return Next state of the owner.
     */
    IModelOwner async(int delta);
}