package me.blutkrone.rpgcore.api.data;

import me.blutkrone.rpgcore.data.DataBundle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public interface IDataAdapter {

    /**
     * Whether the data adapter is currently working, do note that a
     * data adapter has to perform as synchronized otherwise we don't
     * have a guarantee on data integrity.
     *
     * @return Check if adapter is currently working.
     */
    boolean isWorking();

    /**
     * Load, process and save the customized data, despite using a consumer
     * we are instantly processed.
     *
     * @param uuid
     * @param keyword
     * @param process
     */
    void operateCustom(UUID uuid, String keyword, Consumer<Map<String, DataBundle>> process);

    /**
     * Load customized data
     *
     * @param uuid
     * @param keyword
     * @return
     */
    Map<String, DataBundle> loadCustom(UUID uuid, String keyword) throws IOException;

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
     * Save customized data
     *
     * @param uuid
     * @param keyword
     * @param data
     */
    void saveCustom(UUID uuid, String keyword, Map<String, DataBundle> data) throws IOException;

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
     * Save one data bundle
     *
     * @param uuid
     * @param info
     * @param bundle
     */
    default void saveInfo(UUID uuid, String info, DataBundle bundle) throws IOException {
        Map<String, DataBundle> wrapped = new HashMap<>();
        wrapped.put("info", bundle);
        saveCustom(uuid, "info_" + info, wrapped);
    }

    /**
     * Load one data bundle
     *
     * @param uuid
     * @param info
     * @return
     */
    default DataBundle loadInfo(UUID uuid, String info) throws IOException {
        Map<String, DataBundle> wrapped = loadCustom(uuid, "info_" + info);
        DataBundle bundle = wrapped.get("info");
        if (bundle == null) {
            bundle = new DataBundle();
        }
        return bundle;
    }
}
