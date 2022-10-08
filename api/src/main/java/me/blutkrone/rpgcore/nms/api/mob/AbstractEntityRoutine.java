package me.blutkrone.rpgcore.nms.api.mob;

/**
 * A routine refers to a singular task that the entity
 * has to complete, before being allowed to proceed in
 * their execution.
 */
public abstract class AbstractEntityRoutine {

    private final IEntityBase entity;
    private boolean please_finish_early = false;
    private boolean singleton;

    public AbstractEntityRoutine(IEntityBase entity) {
        this.entity = entity;
    }

    /**
     * A singleton means that once the routine is completed, it
     * will be removed from the entity.
     *
     * @param singleton whether to mark as a singleton.
     */
    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    /**
     * A singleton means that once the routine is completed, it
     * will be removed from the entity.
     *
     * @return whether we are a singleton.
     */
    public boolean isSingleton() {
        return singleton;
    }

    /**
     * The entity which this routine was instantiated for.
     *
     * @return which entity is backing up the routine.
     */
    public IEntityBase getEntity() {
        return entity;
    }

    /**
     * Called each tick when the routine has not started, to check
     * if the routine can start. Initialization handling should be
     * included within this method as-well.
     *
     * @return true if we want to start the routine.
     */
    public abstract boolean doStart();

    /**
     * Called each tick while the routine is active, once this method
     * returns false the routine is marked for completion.
     * <br>
     * Once we've been marked as completed, the {@link #doStart()} method
     * has to allow us restarting.
     *
     * @return true if the routine has been completed.
     */
    public abstract boolean doUpdate();

    /**
     * Attempt to make a barrier soak the damage, a barrier always
     * soaks any damage. The return value implies whether there is
     * a barrier available.
     *
     * @param damage the damage we want to soak.
     * @return whether the creature has any barrier.
     */
    public abstract boolean doBarrierDamageSoak(int damage);

    /**
     * Request for this routine to finish, there is no promise
     * on if or when the routine will be finished.
     */
    public void pleaseFinishEarly() {
        this.please_finish_early = true;
    }

    /**
     * Request for this routine to finish, there is no promise
     * on if or when the routine will be finished.
     *
     * @return if someone requested the early finishing.
     */
    public boolean isPleaseFinishEarly() {
        return please_finish_early;
    }

}
