package me.blutkrone.rpgcore.nms.api.packet.handle;

import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Packet handling for block modification.
 */
public interface IBlockMutator {

    /**
     * Mutate a block within this segment, the XYZ values refer
     * to the placement within the 16x16x16 slice. You can use
     * null as a material to grab the native value.
     *
     * @param x        chunk segment position
     * @param y        chunk segment position
     * @param z        chunk segment position
     * @param material updated material
     * @return what material is applied
     */
    Material mutate(int x, int y, int z, Material material);

    /**
     * Dispatch the changes to our target.
     *
     * @param players who will receive the change.
     */
    void dispatch(Player... players);
}
