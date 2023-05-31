package me.blutkrone.rpgcore.hologram.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.nms.api.packet.handle.IHologram;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.resourcepack.utils.IndexedTexture;
import me.blutkrone.rpgcore.util.Utility;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collections;
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
    // text used by the hologram
    private String lc_text;
    // runtime entity reference
    private transient IHologram hologram;

    public StationaryHologram() {
    }

    public StationaryHologram(Location where, String text) {
        this.id = UUID.randomUUID();
        this.x = where.getX();
        this.y = where.getY();
        this.z = where.getZ();
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
        MagicStringBuilder msb = new MagicStringBuilder();

        // retrieve the description for the mob
        List<String> contents = RPGCore.inst().getLanguageManager().getTranslationList(lc_text);
        // draw the basic holographic textures
        for (String content : contents) {
            if (rpm.textures().containsKey("hologram_" + content)) {
                IndexedTexture texture = rpm.texture("hologram_" + content);
                msb.shiftCentered(0, texture.width);
                msb.append(texture);
            }
        }
        // write hologram contents in reverse to ascend upward
        Collections.reverse(contents);
        // first pass writes the shadows
        int j = 0;
        for (int i = 0; i < contents.size() && i < 24; i++) {
            String content = contents.get(i);
            boolean shadow = content.startsWith("&!") || content.startsWith("§!");
            if (shadow) {
                content = content.substring(2);
            }

            if (!rpm.textures().containsKey("hologram_" + content)) {
                if (shadow) {
                    msb.shiftCentered(+1, Utility.measure(content));
                    msb.shadow(content, "nameplate_" + j);
                }
                j += 1;
            }
        }

        j = 0;
        for (int i = 0; i < contents.size() && i < 24; i++) {
            String content = contents.get(i);
            boolean shadow = content.startsWith("&!") || content.startsWith("§!");
            if (shadow) {
                content = content.substring(2);
            }

            if (!rpm.textures().containsKey("hologram_" + content)) {
                msb.shiftCentered(0, Utility.measure(content));
                msb.append(content, "nameplate_" + j++);
            }
        }
        // finalize the constructed hologram
        BaseComponent[] result = msb.shiftToExact(-2).compile();

        // // warn about text being too long
        // if (msb.getSymbolCount() > 256) {
        //     Bukkit.getLogger().severe("Hologram text '" + this.lc_text + "' is too long, contents were trimmed!");
        // }

        // present the hologram
        if (this.hologram == null) {
            this.hologram = RPGCore.inst().getVolatileManager().getPackets().hologram();
        }
        this.hologram.spawn(player, this.x, this.y, this.z);
        this.hologram.name(player, result);
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
