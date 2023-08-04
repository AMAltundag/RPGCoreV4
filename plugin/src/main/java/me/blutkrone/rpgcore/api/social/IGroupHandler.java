package me.blutkrone.rpgcore.api.social;

import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.menu.AbstractYesNoMenu;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface IGroupHandler {

    /**
     * Request to join an existing party.
     *
     * @param asking Who wants to join the party
     * @param target Player we want to join
     */
    void joinParty(Player asking, String target);

    /**
     * Invite another player to your party.
     *
     * @param asking Who is giving out the invitation to their party
     * @param target Player we want to invite
     */
    void inviteParty(Player asking, String target);

    /**
     * Attempt to kick another player out of own party.
     *
     * @param asking Who wants to kick the player out
     * @param kicked Who is being kicked out of the party
     */
    void kickParty(Player asking, String kicked);

    /**
     * Make the player quit their current party.
     *
     * @param player Who wants to quit.
     */
    void quitParty(Player player);

    /**
     * Make the player quit their current party.
     *
     * @param player Who wants to quit.
     */
    void quitParty(CorePlayer player);

    /**
     * A snapshot of the party, this is updated as the party changes in the
     * backend, however latency may stall the update.
     *
     * @param identifier unique identifier of the party
     * @return A read-only view of a party, or null
     */
    IPartySnapshot getPartySnapshot(String identifier);

    /**
     * A snapshot of the party, this is updated as the party changes in the
     * backend, however latency may stall the update.
     *
     * @param player player whose party we want
     * @return A read-only view of a party, or null
     */
    IPartySnapshot getPartySnapshot(OfflinePlayer player);

    /**
     * A snapshot of the party, this is updated as the party changes in the
     * backend, however latency may stall the update.
     *
     * @param player player whose party we want
     * @return A read-only view of a party, or null
     */
    IPartySnapshot getPartySnapshot(UUID player);

    /**
     * A snapshot of the party, this is updated as the party changes in the
     * backend, however latency may stall the update.
     *
     * @param player player whose party we want
     * @return A read-only view of a party, or null
     */
    IPartySnapshot getPartySnapshot(CorePlayer player);

    /**
     * Update the 'matchmaking' profile of a player, to seek for
     * the given content identifiers.
     *
     * @param player   who wants to update their matchmaking.
     * @param contents contents we want to engage in, empty to clear.
     */
    void queueForContent(Player player, String... contents);

    /**
     * Check if a player is queued, do note that there is only
     * one uniform queue that players may access.
     * <br>
     * This is a best effort and may not be accurate.
     *
     * @param player Who to check
     * @return Whether player is queued up
     */
    boolean isQueued(Player player);

    /**
     * A common ancestor for a match prompt, this will close the
     * prompt should anyone reject it
     */
    abstract class AbstractMatchPrompt extends AbstractYesNoMenu {

        /**
         * A unique identifier that identifies the match offer.
         *
         * @return identifier for the offer
         */
        public abstract UUID getOfferId();

        @Override
        public boolean isTrivial() {
            return true;
        }
    }
}
