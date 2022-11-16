package me.blutkrone.external.juliarn.npc.event;

import me.blutkrone.external.juliarn.npc.AbstractPlayerNPC;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * An event fired when a NPC is shown for a certain player.
 */
public class PlayerNPCShowEvent extends PlayerNPCEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * An event fired when a NPC is shown for a certain player.
     *
     * @param who The player who is now seeing the npc
     * @param npc The npc the player is now seeing
     */
    public PlayerNPCShowEvent(Player who, AbstractPlayerNPC npc) {
        super(who, npc);
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
