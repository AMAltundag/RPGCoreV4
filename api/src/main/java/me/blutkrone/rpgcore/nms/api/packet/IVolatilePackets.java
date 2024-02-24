package me.blutkrone.rpgcore.nms.api.packet;

import me.blutkrone.rpgcore.nms.api.packet.grouping.IBundledPacket;
import me.blutkrone.rpgcore.nms.api.packet.handle.*;
import org.bukkit.World;

import java.util.UUID;

public interface IVolatilePackets {

    /**
     * Packet provider for handling block manipulation.
     * Do not cache the return value!
     *
     * @param world which world we operate in
     * @param x     chunk segment position
     * @param y     chunk segment position
     * @param z     chunk segment position
     * @return packet handler
     */
    IBlockMutator blocks(World world, int x, int y, int z);

    /**
     * Packet provider for handling text.
     *
     * @return packet handler
     */
    ITextDisplay text();

    /**
     * Packet provider for handling items.
     *
     * @return packet handler
     */
    IItemDisplay item();

    /**
     * Packet provider for handling player NPCs.
     *
     * @return packet handler
     */
    IPlayerNPC npc(UUID uuid);

    /**
     * Highlight a certain block.
     *
     * @param x Location to highlight
     * @param y Location to highlight
     * @param z Location to highlight
     * @return Highlight wrapper
     */
    IHighlight highlight(int x, int y, int z);

    /**
     * Create an empty packet bundle.
     *
     * @return Empty packet bundle
     */
    IBundledPacket bundle();
}
