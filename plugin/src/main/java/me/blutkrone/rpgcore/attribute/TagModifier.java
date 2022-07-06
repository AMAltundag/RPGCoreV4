package me.blutkrone.rpgcore.attribute;

/**
 * A boolean flag which can grant access to a certain tag, do
 * note that we only need modifier to be active for the tag to
 * be considered active.
 */
public class TagModifier implements IExpiringModifier {
    private boolean expired = false;

    @Override
    public void setExpired() {
        this.expired = true;
    }

    @Override
    public boolean isExpired() {
        return this.expired;
    }
}
