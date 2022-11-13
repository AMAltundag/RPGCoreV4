package me.blutkrone.rpgcore.resourcepack.bbmodel.editor;

import com.google.gson.JsonObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;

public class BBTexture {

    // global ID so we do not overlap model textures
    private static AtomicInteger TEXTURE_UID = new AtomicInteger(0);

    // ID of texture within model
    private int id;
    // unique ID for texture generation
    private int file_id;
    // texture encoded into the model
    private BufferedImage texture;
    // metadata file (optional texture)
    private String mcmeta_path;

    public BBTexture(JsonObject json) {
        // extract id of texture
        this.id = json.get("id").getAsInt();
        // allocate unique identifier
        this.file_id = TEXTURE_UID.getAndIncrement();
        // serialize the texture
        String b64 = json.get("source").getAsString();
        byte[] bytes = Base64.getDecoder().decode(b64.split(",")[1]);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            this.texture = ImageIO.read(bais);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // identify mcmeta texture
        if (json.has("relative_path")) {
            this.mcmeta_path = json.get("relative_path").getAsString().substring(3) + ".mcmeta";
        } else {
            this.mcmeta_path = null;
        }
    }

    public String getMetaPath() {
        return mcmeta_path;
    }

    public int getId() {
        return id;
    }

    public int getFileId() {
        return file_id;
    }

    public BufferedImage getTexture() {
        return texture;
    }
}
