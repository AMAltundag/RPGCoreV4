package me.blutkrone.rpgcore.api.roster;

import me.blutkrone.rpgcore.entity.entities.CorePlayer;

/**
 * The initiator is called every time the core player instance
 * is created by the server.
 */
public interface IRosterInitiator {
    /**
     * When to run the initiator, compared to others.
     *
     * @return the priority of the initiator.
     */
    int priority();

    /**
     * Initiate the player, ensure that the player wasn't
     * initiated by this initiator previously.
     *
     * @param player who to initiate.
     * @return true if the initiator didn't finish, no other initiator
     * will run.
     */
    boolean initiate(CorePlayer player);
}
