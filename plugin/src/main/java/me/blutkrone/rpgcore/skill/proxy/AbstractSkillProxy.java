package me.blutkrone.rpgcore.skill.proxy;

import me.blutkrone.rpgcore.api.IContext;

/**
 * A proxy which applies logic within appropriate
 * context, the finish is invoked instantly prior
 * to the entity being removed.
 */
public abstract class AbstractSkillProxy {
    // the context that created the object
    private final IContext context;

    public AbstractSkillProxy(IContext context) {
        this.context = context;
    }

    /**
     * The context with which the proxy was created.
     *
     * @return the context created within.
     */
    public IContext getContext() {
        return context;
    }

    /**
     * Called once per tick while still active, the finish
     * method is always called manually.
     *
     * @return true if completed.
     */
    public abstract boolean update();

    /**
     * Requests to terminate this proxy at the next best
     * opportunity, this is a request not a guarantee.
     */
    public abstract void pleaseCancelThis();
}
