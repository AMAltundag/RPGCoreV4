package me.blutkrone.external.juliarn.npc.modifier;

import me.blutkrone.external.juliarn.npc.AbstractPlayerNPC;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A modifier for modifying the equipment of a player.
 */
public class EquipmentModifier extends AbstractNPCModifier {

    private final AbstractPlayerNPC npc;

    /**
     * A modifier for modifying the equipment of a player.
     *
     * @param npc The npc this modifier is for.
     */
    public EquipmentModifier(@NotNull AbstractPlayerNPC npc) {
        this.npc = npc;
    }

    /**
     * Equip entity with the given items.
     *
     * @param slot the slot to put the item in.
     * @param item the item to be equipped.
     * @return this instance, for chaining.
     */
    public EquipmentModifier queue(EquipmentSlot slot, ItemStack item) {
        return (EquipmentModifier) super.queue(this.npc.packet().equipment(slot, item));
    }
}
