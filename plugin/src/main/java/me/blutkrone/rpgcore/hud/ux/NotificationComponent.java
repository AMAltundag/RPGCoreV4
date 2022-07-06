package me.blutkrone.rpgcore.hud.ux;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.hud.IUXComponent;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.UXWorkspace;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.util.Utility;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class NotificationComponent implements IUXComponent<List<NotificationComponent.Message>> {
    // each player has six notification slots which are time-bumped
    private Map<Player, Messages> buffered = new WeakHashMap<>();

    public NotificationComponent(ConfigWrapper section) {
        Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
            if (Math.random() < 0.3d) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    notify(player, "§r§cte§ast no§3tifi§2cat§4ion");
                }
            }
        }, 1, 5);
    }

    /**
     * Show a pop-up notification to the player.
     *
     * @param player  who receives the notification
     * @param message the notification to see.
     */
    public void notify(Player player, String message) {
        // retrieve existing notifications
        Messages notification
                = buffered.computeIfAbsent(player, (k -> new Messages()));
        // bump up all notifications by one unit
        for (int i = 5; i >= 1; i--)
            notification.buffer[i] = notification.buffer[i - 1];
        // append our new notification
        notification.buffer[0] = new Message(message, null);
    }

    /**
     * Show a pop-up notification to the player.
     *
     * @param player  who receives the notification
     * @param message the notification to see.
     */
    public void notify(Player player, String message, net.md_5.bungee.api.ChatColor color) {
        // retrieve existing notifications
        Messages notification
                = buffered.computeIfAbsent(player, (k -> new Messages()));
        // bump up all notifications by one unit
        for (int i = 5; i >= 1; i--)
            notification.buffer[i] = notification.buffer[i - 1];
        // append our new notification
        notification.buffer[0] = new Message(message, color);
    }

    @Override
    public int getPriority() {
        return 999;
    }

    @Override
    public List<Message> prepare(CorePlayer core_player, Player bukkit_player) {
        // ensure we got notifications to work with
        Messages notifications = buffered.get(bukkit_player);
        if (notifications == null)
            return new ArrayList<>(6);
        // bump notifications up if necessary
        if (notifications.bump_time++ >= 20) {
            // bump up all notifications by one unit
            for (int i = 5; i >= 1; i--)
                notifications.buffer[i] = notifications.buffer[i - 1];
            // append our new notification
            notifications.buffer[0] = null;
            // reset our counter again
            notifications.bump_time = 0;
        }
        // present the relevant notifications
        return new ArrayList<>(Arrays.asList(notifications.buffer));
    }

    @Override
    public void populate(CorePlayer core_player, Player bukkit_player, UXWorkspace workspace, List<Message> prepared) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        // identify where the message should center at
        int center = rpm.texture("static_plate_back").width / 2;
        // render the messages we prepared
        for (int i = 0; i < 5 && i < prepared.size(); i++) {
            Message notification = prepared.get(i);
            if (notification == null)
                continue;
            if (notification.color == null) {
                workspace.actionbar().shiftCentered(center + 1, Utility.measureWidthExact(notification.text) + 1);
                workspace.actionbar().shadow(notification.text, "hud_notification_" + (i + 1));
                workspace.actionbar().shiftCentered(center, Utility.measureWidthExact(notification.text) + 1);
                workspace.actionbar().append(notification.text, "hud_notification_" + (i + 1));
            } else {
                workspace.actionbar().shiftCentered(center + 1, Utility.measureWidthExact(notification.text) + 1);
                workspace.actionbar().shadow(notification.text, "hud_notification_" + (i + 1));
                workspace.actionbar().shiftCentered(center, Utility.measureWidthExact(notification.text) + 1);
                workspace.actionbar().append(notification.text, "hud_notification_" + (i + 1), notification.color);
            }
        }
    }

    class Messages {
        // a buffer of 6 elements for messages
        private final Message[] buffer = new Message[6];
        // if bump time hits 20 all messages are bumped
        private int bump_time = 0;
    }

    class Message {
        // text to be shown
        public final String text;
        // what color will the text have
        public final net.md_5.bungee.api.ChatColor color;

        public Message(String text, net.md_5.bungee.api.ChatColor color) {
            this.text = text;
            this.color = color;
        }
    }
}
