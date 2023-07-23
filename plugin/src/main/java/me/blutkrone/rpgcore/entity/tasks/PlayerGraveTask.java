package me.blutkrone.rpgcore.entity.tasks;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.event.CoreEntityKilledEvent;
import me.blutkrone.rpgcore.damage.interaction.DamageInteraction;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.nms.api.packet.handle.IItemDisplay;
import me.blutkrone.rpgcore.nms.api.packet.handle.ITextDisplay;
import me.blutkrone.rpgcore.nms.api.packet.wrapper.VolatileBillboard;
import me.blutkrone.rpgcore.nms.api.packet.wrapper.VolatileDisplay;
import me.blutkrone.rpgcore.skill.mechanic.StatusMechanic;
import me.blutkrone.rpgcore.util.Utility;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;

public class PlayerGraveTask extends BukkitRunnable {

    private final CorePlayer player;

    // how many ticks before player actually dies
    private int grave_counter = -1;
    // interaction to blame for the player death
    private DamageInteraction grave_interaction;
    // entity to mark the grave of the player
    private IItemDisplay hologram_grave;
    private ITextDisplay hologram_text;
    // where did the player die
    private Location grave_anchor;
    // offer to revive the player
    private boolean revive_allow;
    private double revive_health;
    private double revive_mana;
    private double revive_stamina;
    private int revive_attribute_time;
    private Map<String, Double> revive_attributes;

    public PlayerGraveTask(CorePlayer player) {
        this.player = player;
    }

    /**
     * Whether player is allowed to be revived.
     *
     * @return Allowed to revive
     */
    public boolean isReviveAllowed() {
        return revive_allow;
    }

    /**
     * If dying, will allow the player to resurrect instead.
     *
     * @param health  Percentage to revive with
     * @param mana    Percentage to revive with
     * @param stamina Percentage to revive with
     */
    public void offerToRevive(double health, double mana, double stamina, Map<String, Double> attributes, int duration) {
        if (this.grave_interaction == null || this.revive_allow) {
            return;
        }
        // allow to revive
        this.revive_allow = true;
        // effects if we do revive
        this.revive_health = health;
        this.revive_mana = mana;
        this.revive_stamina = stamina;
        this.revive_attribute_time = duration;
        this.revive_attributes = attributes;
        // recover counter to a minimum of 10 seconds
        this.grave_counter = Math.max(200, this.grave_counter);
        // deploy a clickable message to accept revive
        List<String> strings = RPGCore.inst().getLanguageManager().getTranslationList("offer_to_revive");
        try {
            String message = strings.remove(0);
            String confirm = strings.remove(0);
            String tooltip = strings.remove(0);

            BaseComponent[] components = new ComponentBuilder().append(message).append(" ")
                    .append(confirm)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(tooltip)))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rpg internal revive"))
                    .create();
            this.player.getEntity().spigot().sendMessage(components);
        } catch (Exception ex) {
            this.player.getEntity().sendMessage("§cMalformed LC: 'offer_to_revive' - cannot revive!");
        }
    }

    /**
     * Resurrect the entity, do note this works even if you
     * are not allowed to revive - however if you are allowed
     * it will instead consume that permission.
     */
    public void revive() {
        if (this.grave_interaction == null) {
            return;
        }

        // release hologram and vanish
        clear();
        // reset the grave parameters
        this.grave_interaction = null;
        this.grave_counter = -1;
        this.grave_anchor = null;
        this.revive_allow = false;
        // recover resources of player
        this.player.getHealth().setToExactUnsafe(this.player.getHealth().getSnapshotMaximum() * this.revive_health);
        this.player.getMana().setToExactUnsafe(this.player.getMana().getSnapshotMaximum() * this.revive_mana);
        this.player.getStamina().setToExactUnsafe(this.player.getStamina().getSnapshotMaximum() * this.revive_stamina);
        // temporary status effect on player
        if (!this.revive_attributes.isEmpty()) {
            this.player.addEffect("rpgcore_revive_effect", new StatusMechanic.StatusEffect(
                    this.player, this.revive_attributes, this.revive_attribute_time, false));
        }
    }

    @Override
    public void run() {
        if (this.grave_interaction == null) {
            // release hologram and vanish
            this.clear();
            // reset the grave parameters
            this.grave_interaction = null;
            this.grave_counter = -1;
            this.grave_anchor = null;
            this.revive_allow = false;
        } else if (this.hologram_grave == null) {
            // we are dying and need the hologram
            Player player_bukkit = this.player.getEntity();
            Block block = player_bukkit.getLocation().getBlock();
            int max = 128;
            while (block.getRelative(BlockFace.DOWN).isPassable() && max-- > 0) {
                block = block.getRelative(BlockFace.DOWN);
            }
            this.grave_anchor = block.getLocation();
            // show the hologram to everyone interested
            this.hologram_grave = RPGCore.inst().getVolatileManager().getPackets().item();
            this.hologram_text = RPGCore.inst().getVolatileManager().getPackets().text();
            String info = RPGCore.inst().getLanguageManager().getTranslation("grave_indicator")
                    .replace("{NAME}", this.player.getName())
                    .replace("{TIME}", String.valueOf(Math.max(this.grave_counter / 20, 1)));
            List<Player> observing = RPGCore.inst().getEntityManager().getObserving(this.grave_anchor);
            for (Player viewer : observing) {
                this.hologram_grave.spawn(viewer, this.grave_anchor.clone().add(0d, 1d, 0d));
                this.hologram_grave.item(viewer, RPGCore.inst().getEntityManager().getGraveDefault(), 1d, VolatileBillboard.FIXED, VolatileDisplay.FIXED);
                this.hologram_text.spawn(viewer, this.grave_anchor.clone().add(0d, 2.5d, 0d));
                this.hologram_text.message(viewer, TextComponent.fromLegacyText(info), true, false);
            }
            // be invisible during the death phase
            for (Player viewer : player_bukkit.getWorld().getPlayers()) {
                viewer.hidePlayer(RPGCore.inst(), player_bukkit);
            }
            // limit mobility of player
            player_bukkit.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 10, false, false, false));
            player_bukkit.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 30, 128, false, false, false));
            player_bukkit.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1, false, false, false));
            player_bukkit.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 40, 1, false, false, false));
        } else if (this.grave_counter-- <= 0) {
            // out of time, apply true death
            this.player.die(this.grave_interaction);
            Bukkit.getPluginManager().callEvent(new CoreEntityKilledEvent(this.grave_interaction));
        } else if (this.grave_counter % 20 == 0) {
            // dying, keep hologram count-down updated
            Player player_bukkit = this.player.getEntity();
            String info = RPGCore.inst().getLanguageManager().getTranslation("grave_indicator")
                    .replace("{NAME}", this.player.getName())
                    .replace("{TIME}", String.valueOf(Math.max(this.grave_counter / 20, 1)));
            List<Player> observing = RPGCore.inst().getEntityManager().getObserving(this.grave_anchor);
            for (Player viewer : observing) {
                if (viewer != player_bukkit || true) {
                    this.hologram_text.message(viewer, TextComponent.fromLegacyText(info), true, false);
                }
            }
            // just in case player somehow moved, snap back
            if (Utility.distanceSqOrWorld(player_bukkit, this.grave_anchor) > 1d) {
                player_bukkit.teleport(this.grave_anchor);
            }
            // limit mobility of player
            player_bukkit.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 10, false, false, false));
            player_bukkit.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 40, 128, false, false, false));
            player_bukkit.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1, false, false, false));
            player_bukkit.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 40, 1, false, false, false));
        }
    }

    /**
     * Remove the grave entity which we had.
     */
    public void clear() {
        // destroy the hologram for grave model
        if (this.hologram_grave != null) {
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                this.hologram_grave.destroy(viewer);
            }
        }
        this.hologram_grave = null;
        // destroy the holograms for death text
        if (this.hologram_text != null) {
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                this.hologram_text.destroy(viewer);
            }
        }
        this.hologram_text = null;
        // undo the grave hiding
        Player entity = this.player.getEntity();
        for (Player viewer : entity.getWorld().getPlayers()) {
            viewer.showPlayer(RPGCore.inst(), entity);
        }
    }

    /**
     * Update the grave of the player, use -1 and null if you want
     * to disable the grave task.
     * <p>
     * Player is invisible and cannot be engaged with.
     *
     * @param timer How long before true death
     * @param cause What caused the death
     */
    public void setAsGrave(int timer, DamageInteraction cause) {
        this.grave_counter = timer;
        this.grave_interaction = cause;
    }

    /**
     * The interaction which was the cause of death.
     *
     * @return The cause of death
     */
    public DamageInteraction getGraveInteraction() {
        return grave_interaction;
    }

    /**
     * How many ticks until the player dies in their grave.
     *
     * @return How many ticks before the player dies.
     */
    public int getGraveTimeLeft() {
        return grave_counter;
    }
}
