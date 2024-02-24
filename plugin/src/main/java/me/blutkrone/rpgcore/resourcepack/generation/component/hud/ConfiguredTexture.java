package me.blutkrone.rpgcore.resourcepack.generation.component.hud;

import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.IOException;

/**
 * A texture serialized via configuration, these parameters are accurate
 * until the resourcepack is generated again.
 */
public class ConfiguredTexture extends AbstractTexture {
    public ConfiguredTexture(BukkitObjectInputStream stream) throws IOException {
        super(stream.readUTF(), stream.readUTF(), stream.readInt(), stream.readInt());
    }

    public ConfiguredTexture(AbstractTexture texture) {
        super(texture.symbol, texture.table, texture.width, texture.height);
    }

    public void dump(BukkitObjectOutputStream boos) throws IOException {
        boos.writeUTF(this.symbol);
        boos.writeUTF(this.table);
        boos.writeInt(this.width);
        boos.writeInt(this.height);
    }
}
