package me.blutkrone.rpgcore.damage.splash;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.damage.interaction.DamageInteraction;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.nms.api.packet.grouping.IBundledPacket;
import me.blutkrone.rpgcore.nms.api.packet.handle.ITextDisplay;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Handling for when we deal damage
 */
public class DamageIndicatorHandling {

    private static final Transformation TRANSFORM_START
            = new Transformation(new Vector3f(0f, 0f, 0f), new Quaternionf(), new Vector3f(3f, 3f, 3f), new Quaternionf());
    private static final Transformation TRANSFORM_FINISH
            = new Transformation(new Vector3f(0f, 3f, 0f), new Quaternionf(), new Vector3f(1f, 1f, 1f), new Quaternionf());

    // player mapped to their active indicators
    private final Map<UUID, Collection<Indicator>> indicators = new ConcurrentHashMap<>();

    public DamageIndicatorHandling() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(RPGCore.inst(), () -> {
            this.indicators.forEach((uuid, indicators) -> {
                indicators.removeIf(indicator -> {
                    if (indicator.duration == 0) {
                        // initialize player connection
                        indicator.connection = Bukkit.getPlayer(uuid);
                        // prepare initial animation
                        float impact = (float) Math.sqrt(indicator.message.length());
                        Vector3f translation = new Vector3f(indicator.x, indicator.y, indicator.z);
                        Vector3f scale = new Vector3f(impact, impact, impact);
                        // attach a hologram to the indicator
                        ITextDisplay hologram = RPGCore.inst().getVolatileManager().getPackets().text();

                        IBundledPacket bundled = hologram.spawn(indicator.where.getX(), indicator.where.getY(), indicator.where.getZ(), 0f, 0f);
                        hologram.message(TextComponent.fromLegacyText(String.valueOf(indicator.message)), false).addToOther(bundled);
                        hologram.transform(0, new Transformation(translation, new Quaternionf(), scale, new Quaternionf())).addToOther(bundled);
                        bundled.dispatch(indicator.connection);

                        indicator.display = hologram;
                    } else if (indicator.duration == 1) {
                        // prepare the target animation
                        Vector3f translation = new Vector3f(indicator.x, indicator.y+6, indicator.z);
                        Vector3f scale = new Vector3f(0f, 0f, 0f);
                        // start the damage indicator animation
                        indicator.display.transform(40, new Transformation(translation, new Quaternionf(), scale, new Quaternionf())).dispatch(indicator.connection);
                    } else if (indicator.duration == 41) {
                        // hint where we are going
                        indicator.display.destroy().dispatch(indicator.connection);
                        return true;
                    }

                    indicator.duration += 1;
                    return false;
                });
            });
        }, 1, 1);
    }

    public void indicate(CorePlayer player, DamageInteraction interaction) {
        Location where = interaction.getDefender().getEntity().getEyeLocation();
        String message = String.valueOf((long) (interaction.getDamage()));
        if (interaction.getTags().contains("CRITICAL_HIT")) {
            message = "§c§l" + message;
        }

        Collection<Indicator> indicators = this.indicators.computeIfAbsent(player.getUniqueId(), (k -> new ConcurrentLinkedQueue<>()));
        indicators.add(new Indicator(message, where, Math.log10(interaction.getDamage())));
    }

    private class Indicator {
        // center indicator around here
        Vector where;
        // packet handle for the display entity
        ITextDisplay display;
        // message to be shown
        String message;
        // progression state
        int duration;
        // player reference
        Player connection;
        // impact of damage number
        double impact;
        // random offsets to be applied
        float x, y, z;

        public Indicator(String message, Location where, double impact) {
            this.display = null;
            this.message = message;
            this.where = where.toVector();
            this.duration = 0;
            this.impact = impact;
            this.x = (float) (Math.random()*2-1);
            this.y = (float) (Math.random()*2-1);
            this.z = (float) (Math.random()*2-1);
        }
    }
}