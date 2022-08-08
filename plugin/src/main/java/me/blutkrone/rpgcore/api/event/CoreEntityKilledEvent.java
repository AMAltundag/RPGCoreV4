package me.blutkrone.rpgcore.api.event;

import me.blutkrone.rpgcore.damage.interaction.DamageInteraction;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Invoked when a core entity is slain.
 */
public class CoreEntityKilledEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    // the interaction associated with death
    private DamageInteraction cause;

    public CoreEntityKilledEvent(DamageInteraction cause) {
        this.cause = cause;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Who is the victim.
     *
     * @return entity that was killed
     */
    public CoreEntity getKilled() {
        return cause.getDefender();
    }

    /**
     * Who is the killer.
     *
     * @return entity that has killed
     */
    public CoreEntity getKiller() {
        return cause.getAttacker();
    }

    /**
     * What damage interaction caused the victim their health
     * to drop below zero.
     *
     * @return cause of death
     */
    public DamageInteraction getCause() {
        return cause;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
