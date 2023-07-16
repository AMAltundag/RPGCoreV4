package me.blutkrone.rpgcore.hologram.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.nms.api.packet.handle.ITextDisplay;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.resourcepack.utils.IndexedTexture;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

/**
 * A simple hologram implementation intended for stationary
 * processing.
 */
public class StationaryHologram {
    // identifier for this hologram
    private UUID id;
    // location of the hologram
    private double x;
    private double y;
    private double z;
    private float pitch;
    private float yaw;
    private boolean locked;
    // text used by the hologram
    private String lc_text;
    // runtime entity reference
    private transient ITextDisplay hologram;

    public StationaryHologram() {
    }

    public StationaryHologram(Location where, String text, boolean locked) {
        this.id = UUID.randomUUID();
        this.x = where.getX();
        this.y = where.getY();
        this.z = where.getZ();
        this.pitch = where.getPitch();
        this.yaw = where.getYaw();
        this.locked = locked;
        this.lc_text = text;
    }

    /**
     * A unique identifier used by this hologram.
     *
     * @return unique hologram identifier.
     */
    public UUID getId() {
        return id;
    }

    /**
     * The position of this hologram wrapper.
     *
     * @return hologram position.
     */
    public Vector getPosition() {
        return new Vector(x, y, z);
    }

    /**
     * Spawn and update the holograms.
     *
     * @param player who will receive the hologram.
     */
    public void update(Player player) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();

        // retrieve the description for the mob
        ComponentBuilder builder = new ComponentBuilder();
        List<String> contents = RPGCore.inst().getLanguageManager().getTranslationList(lc_text);
        for (String content : contents) {
            IndexedTexture.ConfigTexture texture = rpm.textures().get(content);
            if (texture == null) {
                // add as text
                builder.append(content).append("\n");
            } else {
                // add as message
                TextComponent component = new TextComponent();
                component.setText(texture.symbol);
                component.setFont(texture.table);
                builder.append(component).append("\n");
            }
        }

        // present the hologram
        if (this.hologram == null) {
            this.hologram = RPGCore.inst().getVolatileManager().getPackets().text();
        }
        this.hologram.spawn(player, this.x, this.y, this.z, this.pitch, this.yaw);
        this.hologram.rotate(player, this.yaw);
        this.hologram.message(player, builder.create(), true, this.locked);
    }

    /**
     * Destruct all holograms for the player.
     *
     * @param player whose holograms are we showing.
     */
    public void destroy(Player player) {
        if (this.hologram != null) {
            this.hologram.destroy(player);
        }
    }
}
