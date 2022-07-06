package me.blutkrone.rpgcore.data.structure;

import me.blutkrone.rpgcore.data.DataBundle;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;

/**
 * A protocol which defines how data is meant to be read and
 * written, these methods are not allowed to raise any error
 * under any circumstances.
 */
public interface DataProtocol {
    /**
     * Save the data from the player to the bundle.
     *
     * @param player whose data is being saved.
     * @param bundle where to write the data to.
     */
    void save(CorePlayer player, DataBundle bundle);

    /**
     * Read the data from the bundle and write it to the player.
     *
     * @param player
     * @param bundle
     */
    void load(CorePlayer player, DataBundle bundle);
}
