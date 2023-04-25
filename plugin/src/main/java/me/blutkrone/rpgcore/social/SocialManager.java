package me.blutkrone.rpgcore.social;

import com.google.common.io.ByteArrayDataInput;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.bungee.IBungeeHandling;
import me.blutkrone.rpgcore.api.event.CoreInitializationEvent;
import me.blutkrone.rpgcore.api.social.IFriendHandler;
import me.blutkrone.rpgcore.api.social.IGroupHandler;
import me.blutkrone.rpgcore.api.social.IGuildHandler;
import me.blutkrone.rpgcore.api.social.IPlayerHandler;
import me.blutkrone.rpgcore.social.bungee.BungeeGroupHandler;
import me.blutkrone.rpgcore.social.bungee.BungeePlayerHandler;
import me.blutkrone.rpgcore.social.server.ServerGroupHandler;
import me.blutkrone.rpgcore.social.server.ServerPlayerHandler;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.io.IOException;

public class SocialManager implements Listener, IBungeeHandling {

    private IPlayerHandler player_handler;
    private IFriendHandler friend_handler;
    private IGuildHandler guild_handler;
    private IGroupHandler group_handler;

    public SocialManager() {
        Bukkit.getPluginManager().registerEvents(this, RPGCore.inst());

        try {
            ConfigWrapper config = FileUtil.asConfigYML(FileUtil.file("network.yml"));
            boolean network_handling = config.getBoolean("use-network-handling");

            // todo: Merge Match+Party into one handler
            this.player_handler = CoreInitializationEvent.find(IPlayerHandler.class);
            this.friend_handler = CoreInitializationEvent.find(IFriendHandler.class);
            this.guild_handler = CoreInitializationEvent.find(IGuildHandler.class);

            if (network_handling) {
                this.player_handler = new BungeePlayerHandler();
                this.group_handler = new BungeeGroupHandler(this);
                // this.friend_handler = ...;
                // this.guild_handler = ...;
            } else {
                this.player_handler = new ServerPlayerHandler();
                this.group_handler = new ServerGroupHandler(this);
                // this.friend_handler = ...;
                // this.guild_handler = ...;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public IFriendHandler getFriendHandler() {
        return friend_handler;
    }

    public IGuildHandler getGuildHandler() {
        return guild_handler;
    }

    public IGroupHandler getGroupHandler() {
        return group_handler;
    }

    public IPlayerHandler getPlayerHandler() {
        return player_handler;
    }

    @Override
    public void onBungeeMessage(Player recipient, String channel, ByteArrayDataInput data) {
        if (getGroupHandler() instanceof IBungeeHandling) {
            ((IBungeeHandling) getGroupHandler()).onBungeeMessage(recipient, channel, data);
        } else if (getGuildHandler() instanceof IBungeeHandling) {
            ((IBungeeHandling) getGuildHandler()).onBungeeMessage(recipient, channel, data);
        } else if (getPlayerHandler() instanceof IBungeeHandling) {
            ((IBungeeHandling) getPlayerHandler()).onBungeeMessage(recipient, channel, data);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onShowSocialMenuShiftClick(PlayerInteractEntityEvent event) {
        // right click while shifting will bring up the menu
        if (event.getPlayer().isSneaking() && event.getRightClicked() instanceof Player && event.getHand() == EquipmentSlot.HAND) {
            Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                if (event.getPlayer().getOpenInventory().getType() == InventoryType.CRAFTING) {
                    RPGCore.inst().getHUDManager().getSocialMenu().present(event.getPlayer(), event.getRightClicked().getUniqueId());
                }
            });
        }
    }
}
