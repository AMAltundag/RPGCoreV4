package me.blutkrone.rpgcore.api.data;

import me.blutkrone.rpgcore.data.DataBundle;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public interface IDataAdapter {

    /**
     * Load the roster information.
     *
     * @param uuid
     * @return
     */
    Map<String, DataBundle> loadRosterData(UUID uuid) throws IOException;

    /**
     * Load the character information.
     *
     * @param uuid
     * @param character
     * @return
     */
    Map<String, DataBundle> loadCharacterData(UUID uuid, int character) throws IOException;

    /**
     * Save roster specific information
     *
     * @param uuid
     * @param data
     */
    void saveRosterData(UUID uuid, Map<String, DataBundle> data) throws IOException;

    /**
     * Save character specific information
     *
     * @param uuid
     * @param character
     * @param data
     */
    void saveCharacterData(UUID uuid, int character, Map<String, DataBundle> data) throws IOException;

    /**
     * Block the thread and force all operations to complete instantly, do
     * note that this will block the thread we called from.
     */
    void flush();
}
