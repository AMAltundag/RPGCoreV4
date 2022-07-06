package me.blutkrone.rpgcore.api.entity;

public interface IEntityEffect {

    /**
     * Tick the logic of this effect, do note that the rate at which
     * this effect is ticked depends on the entity which holds it.
     *
     * @param delta the ticks since the effect was ticked.
     * @return true if effect can be removed.
     */
    boolean tickEffect(int delta);

    /**
     * How many stacks of this effect are left.
     *
     * @return the current stacks we have.
     */
    int getStacks();

    /**
     * How many ticks this effect has left.
     *
     * @return the remaining duration of this effect.
     */
    int getDuration();

    /**
     * ID of the icon which this effect is using. If the icon is null, the
     * effect will not be rendered on the HUD.
     *
     * @return the symbol to use on the HUD.
     */
    String getIcon();

    /**
     * Timestamp when this effect was last updated, this is used to
     * prioritize which effects are shown when.
     *
     * @return timestamp of when this effect was updated.
     * @see System#currentTimeMillis() Use UNIX timestamp for effective ordering.
     */
    long getLastUpdated();

    /**
     * Whether to consider this effect as a debuff.
     *
     * @return true if we are a debuff.
     */
    boolean isDebuff();
}
