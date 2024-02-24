package me.blutkrone.rpgcore.nms.v1_20_R1.packet;

import me.blutkrone.rpgcore.nms.api.packet.grouping.IBundledPacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public class VolatileBundledPacket implements IBundledPacket {

    private Queue<Packet<ClientGamePacketListener>> packets = new LinkedList<>();
    private Queue<Player> players = new LinkedList<>();

    public VolatileBundledPacket(Packet<ClientGamePacketListener> packet) {
        this.packets.add(packet);
    }

    public VolatileBundledPacket(Collection<Packet<ClientGamePacketListener>> packets) {
        this.packets.addAll(packets);
    }

    public VolatileBundledPacket() {
    }

    @Override
    public void addToOther(IBundledPacket other) {
        if (other instanceof VolatileBundledPacket o) {
            o.packets.addAll(this.packets);
            this.packets = null;
        } else {
            throw new IllegalArgumentException("Bad bundled packet!");
        }
    }

    @Override
    public void takeFromOther(IBundledPacket packet) {
        if (packet instanceof VolatileBundledPacket o) {
            this.packets.addAll(o.packets);
            o.packets = null;
        } else {
            throw new IllegalArgumentException("Bad bundled packet!");
        }
    }

    @Override
    public void dispatch(Player... players) {
        ClientboundBundlePacket bundle = new ClientboundBundlePacket(this.packets);
        for (Player player : players) {
            ((CraftPlayer) player).getHandle().connection.send(bundle);
        }
    }

    @Override
    public void dispatch(Player player) {
        ClientboundBundlePacket bundle = new ClientboundBundlePacket(this.packets);
        ((CraftPlayer) player).getHandle().connection.send(bundle);
    }

    @Override
    public void dispatch(Collection<? extends Player> players) {
        ClientboundBundlePacket bundle = new ClientboundBundlePacket(this.packets);
        for (Player player : players) {
            ((CraftPlayer) player).getHandle().connection.send(bundle);
        }
    }

    @Override
    public void queue(Player player) {
        this.players.add(player);
    }

    @Override
    public void flush() {
        dispatch(players);
        players.clear();
    }
}
