package me.blutkrone.rpgcore.resourcepack.bbmodel.editor;

import com.google.gson.JsonObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;

public class BBTexture {

    public static AtomicInteger TEXTURE_UID = new AtomicInteger(0);

    private String name;
    private int id; // this id is unique locally
    private BufferedImage texture;
    private int file_id; // this id is unique globally

    public BBTexture(JsonObject json) {
        // track the name of the texture
        if (json.has("name"))
            this.name = json.get("name").getAsString().replace(".png", "");
        // identify this specific texture
        this.id = json.get("id").getAsInt();
        // decode the b64 texture serialisation
        String b64 = json.get("source").getAsString();
        byte[] bytes = Base64.getDecoder().decode(b64.split(",")[1]);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            this.texture = ImageIO.read(bais);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // allocate a random unique identifier
        this.file_id = TEXTURE_UID.getAndIncrement();
    }

    public String getName() {
        return name;
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
