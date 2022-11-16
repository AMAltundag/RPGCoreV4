package me.blutkrone.external.juliarn.npc.modifier;

import me.blutkrone.rpgcore.nms.api.packet.IDispatchPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractNPCModifier {

    // queried packets describing the modification done
    private final List<IDispatchPacket> packets = new CopyOnWriteArrayList<>();

    /**
     * Query a packet to be dispatched.
     *
     * @param packet the packet to queue.
     * @return this instance, for chaining.
     */
    protected AbstractNPCModifier queue(IDispatchPacket packet) {
        this.packets.add(packet);
        return this;
    }

    /**
     * Sends the queued modifications to all players
     */
    public void flush() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (IDispatchPacket packet : this.packets) {
                packet.sendTo(player);
            }
        }

        this.packets.clear();
    }

    /**
     * Dispatch packets, will clear the queried packets.
     *
     * @param players The receivers of the packet.
     */
    public void flush(Collection<Player> players) {
        for (Player player : players) {
            for (IDispatchPacket packet : this.packets) {
                packet.sendTo(player);
            }
        }

        this.packets.clear();
    }

    /**
     * Dispatch packets, will clear the queried packets.
     *
     * @param players who wants to receive the updates.
     */
    public void flush(Player... players) {
        for (Player player : players) {
            for (IDispatchPacket packet : this.packets) {
                packet.sendTo(player);
            }
        }

        this.packets.clear();
    }
}
