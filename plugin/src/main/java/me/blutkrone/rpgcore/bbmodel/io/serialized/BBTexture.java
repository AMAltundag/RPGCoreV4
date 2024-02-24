package me.blutkrone.rpgcore.bbmodel.io.serialized;

import com.google.gson.JsonObject;
import me.blutkrone.rpgcore.bbmodel.util.BBUtil;

import java.awt.image.BufferedImage;
import java.util.UUID;

/**
 * Represents a texture in the model.
 */
public class BBTexture {
    // how geometry will access the texture
    public String id;
    // internal file name
    public UUID uuid;
    // buffered texture of the model
    public BufferedImage image;
    // optional *.png.mcmeta configuration
    public JsonObject mcmeta;

    /**
     * Represents a texture in the model.
     *
     * @param bb_texture Configuration of a texture object
     * @param bb_mcmeta Master container for mcmeta textures
     */
    public BBTexture(JsonObject bb_texture, JsonObject bb_mcmeta) {
        this.uuid = UUID.randomUUID();
        this.id = bb_texture.get("id").getAsString();
        this.image = BBUtil.toImage(bb_texture.get("source").getAsString());
        this.mcmeta = null; // todo: mcmeta handling
    }
}
