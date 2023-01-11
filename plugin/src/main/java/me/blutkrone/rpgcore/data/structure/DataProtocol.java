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
     * If flagged as roster data, we will save/load from a
     * pool of data shared across players.
     *
     * @return true if we are roster bound.
     */
    boolean isRosterData();

    /**
     * Data version for this specific protocol, always update
     * the data to the latest protocol when saving.
     *
     * @return the current data version we have.
     */
    default int getDataVersion() {
        return 0;
    }

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
     * @param player whose data is being read.
     * @param bundle where to read the data from.
     * @param version version of the data bundle.
     */
    void load(CorePlayer player, DataBundle bundle, int version);
}
