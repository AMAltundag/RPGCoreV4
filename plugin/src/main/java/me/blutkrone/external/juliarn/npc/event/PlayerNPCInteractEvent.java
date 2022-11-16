package me.blutkrone.external.juliarn.npc.event;

import me.blutkrone.external.juliarn.npc.AbstractPlayerNPC;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

/**
 * An event called when a player interacts with a npc.
 */
public class PlayerNPCInteractEvent extends PlayerNPCEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    // the action that was performed
    private final EntityUseAction action;
    // hand used to perform the action
    private final EquipmentSlot hand;

    /**
     * An event called when a player interacts with a npc.
     *
     * @param who    The player who interacted with the npc.
     * @param npc    The npc with whom the player has interacted.
     * @param action The action type of the interact.
     * @param hand   The player hand used for the interact.
     */
    public PlayerNPCInteractEvent(Player who, AbstractPlayerNPC npc, EntityUseAction action, EquipmentSlot hand) {
        super(who, npc);
        this.action = action;
        this.hand = hand;
    }

    /**
     * Get the handlers for this event.
     *
     * @return the handlers for this event.
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    /**
     * The action performed by the player.
     *
     * @return the action performed.
     */
    public EntityUseAction getAction() {
        return this.action;
    }

    /**
     * Hand used by the player.
     *
     * @return the hand used.
     */
    public EquipmentSlot getHand() {
        return this.hand;
    }

    /**
     * A wrapper describing the type of interaction performed.
     */
    public enum EntityUseAction {
        INTERACT,
        ATTACK,
        INTERACT_AT
    }
}
