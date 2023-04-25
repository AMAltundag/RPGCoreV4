package me.blutkrone.rpgcore.api.social;

import java.util.List;

public interface IPlayerHandler {

    /**
     * A list of all players connected, this may offer players that
     * are connected on other servers on a network.
     *
     * @return connected players
     */
    List<String> getConnectedPlayers();
}
