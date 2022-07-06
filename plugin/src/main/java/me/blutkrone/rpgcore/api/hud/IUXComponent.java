package me.blutkrone.rpgcore.api.hud;

import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.UXWorkspace;
import org.bukkit.entity.Player;

public interface IUXComponent<K> {

    /**
     * The priority which we do have, with the lowest priority being
     * drawn first.
     *
     * @return the priority which we have.
     */
    int getPriority();

    /**
     * Prepare the rendering of this component, this method will
     * be called synchronously.
     *
     * @param core_player   core wrapped player
     * @param bukkit_player bukkit wrapped player
     * @return information to pass to asynchronously call
     */
    K prepare(CorePlayer core_player, Player bukkit_player);

    /**
     * Populate the workspace asynchronously with the information
     * which was prepared previously.
     *
     * @param core_player   core wrapped player
     * @param bukkit_player bukkit wrapped player
     * @param workspace     container for all UX info
     * @param prepared      data prepared during synchronous call
     */
    void populate(CorePlayer core_player, Player bukkit_player, UXWorkspace workspace, K prepared);
}
