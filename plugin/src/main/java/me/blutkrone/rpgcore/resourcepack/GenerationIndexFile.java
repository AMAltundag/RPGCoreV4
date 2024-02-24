package me.blutkrone.rpgcore.resourcepack;

import me.blutkrone.rpgcore.resourcepack.generation.component.hud.AbstractTexture;
import me.blutkrone.rpgcore.resourcepack.generation.component.hud.ConfiguredTexture;
import me.blutkrone.rpgcore.bbmodel.io.deserialized.Model;
import org.apache.commons.io.FileUtils;
import org.bukkit.ChatColor;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The index file contains the bare minimum information
 * that is necessary to make use of the resourcepack.<br>
 * <br>
 * This file is <b>NOT</b> deterministic, having two identical
 * generation inputs does not guarantee the identical output.
 */
public class GenerationIndexFile {

    // bare minimum information from last generation
    final Map<String, AbstractTexture> hud_textures;
    final Map<Character, Integer> char_to_size;
    final Map<String, String> font_alias;
    final Map<String, Model> entities_generated;
    // cache for measured strings
    final Map<String, Integer> measuring_cache = new ConcurrentHashMap<>();

    /**
     * The index file contains the bare minimum information
     * that is necessary to make use of the resourcepack.<br>
     * <br>
     * An index file is only stable until the resourcepack is
     * generated again.<br>
     * <br>
     *
     * @param stream Serialize from byte stream.
     */
    public GenerationIndexFile(BukkitObjectInputStream stream) throws IOException {
        this.hud_textures = new HashMap<>();
        this.char_to_size = new HashMap<>();
        this.font_alias = new HashMap<>();
        this.entities_generated = new HashMap<>();

        int version = stream.readInt();
        if (version == 1) {
            int total = stream.readInt();
            for (int i = 0; i < total; i++) {
                String id = stream.readUTF();
                this.hud_textures.put(id, new ConfiguredTexture(stream));
            }

            total = stream.readInt();
            for (int i = 0; i < total; i++) {
                char character = stream.readChar();
                int value = stream.readInt();
                this.char_to_size.put(character, value);
            }

            total = stream.readInt();
            for (int i = 0; i < total; i++) {
                String alias_font = stream.readUTF();
                String real_font = stream.readUTF();
                this.font_alias.put(alias_font, real_font);
            }

            total = stream.readInt();
            for (int i = 0; i < total; i++) {
                String model_id = stream.readUTF().toLowerCase();
                Model rpg_model = new Model(model_id, stream);
                this.entities_generated.put(model_id, rpg_model);
            }
        } else {
            throw new IOException("Unsupported index version: " + version);
        }
    }

    /**
     * The index file contains the bare minimum information
     * that is necessary to make use of the resourcepack.<br>
     * <br>
     * An index file is only stable until the resourcepack is
     * generated again.<br>
     * <br>
     *
     * @param generation Extract from an on-going generation.
     */
    public GenerationIndexFile(OngoingGeneration generation) {
        this.hud_textures = generation.hud_textures;
        this.char_to_size = generation.char_to_size;
        this.font_alias = generation.font_alias;
        this.entities_generated = generation.entities_generated;
    }

    /**
     * Measure basic text, this will <b>NOT</b> accommodate
     *
     * @param text the base text to translate
     * @return the resulting length
     */
    public int measure(String text) {
        // fetch from cache if applicable
        text = ChatColor.translateAlternateColorCodes('&', text);
        return measuring_cache.computeIfAbsent(text, (string -> {
            // measure the text within our specifics
            int messagePxSize = 0;
            boolean previousCode = false;
            boolean isBold = false;
            for (char c : string.toCharArray()) {
                if (c == '§') {
                    previousCode = true;
                } else if (previousCode) {
                    previousCode = false;
                    isBold = c == 'l' || c == 'L';
                } else {
                    messagePxSize += this.char_to_size.getOrDefault(c, 0) + ((isBold && c == ' ') ? 1 : 0);
                    messagePxSize++;
                }
            }

            return messagePxSize;
        }));
    }

    /**
     * Save to the given index file..
     *
     * @param where
     */
    public void save(File where) throws IOException {
        // create index files if they do not exist
        where.getParentFile().mkdirs();
        where.createNewFile();
        // track relevant persistence information
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BukkitObjectOutputStream boos = new BukkitObjectOutputStream(stream);
        // track data of the individual symbols we got
        boos.writeInt(1);
        boos.writeInt(this.hud_textures.size());
        for (Map.Entry<String, AbstractTexture> entry : this.hud_textures.entrySet()) {
            boos.writeUTF(entry.getKey());
            ((ConfiguredTexture) entry.getValue()).dump(boos);
        }
        // track measurements of fonts
        boos.writeInt(this.char_to_size.size());
        for (Map.Entry<Character, Integer> mapEntry : this.char_to_size.entrySet()) {
            boos.writeChar(mapEntry.getKey());
            boos.writeInt(mapEntry.getValue());
        }
        // track font aliases
        boos.writeInt(this.font_alias.size());
        for (Map.Entry<String, String> entry : this.font_alias.entrySet()) {
            boos.writeUTF(entry.getKey());
            boos.writeUTF(entry.getValue());
        }
        // track model data
        boos.writeInt(this.entities_generated.size());
        for (Map.Entry<String, Model> entry : this.entities_generated.entrySet()) {
            boos.writeUTF(entry.getKey());
            entry.getValue().dump(boos);
        }
        // close the stream
        boos.close();
        // dump into the file
        FileUtils.writeByteArrayToFile(where, stream.toByteArray());
    }
}
