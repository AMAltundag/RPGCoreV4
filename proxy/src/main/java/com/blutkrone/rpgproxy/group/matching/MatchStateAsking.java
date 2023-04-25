package com.blutkrone.rpgproxy.group.matching;

import com.blutkrone.rpgproxy.group.BungeeGroupHandler;
import com.blutkrone.rpgproxy.util.BungeeTable;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/*
 * Ask players if they want to match up.
 */
public class MatchStateAsking implements IMatchPhase {

    final BungeeGroupHandler handler;

    final UUID id;
    final String content;
    final Set<UUID> interested;
    final Set<UUID> accepted;

    public MatchStateAsking(BungeeGroupHandler handler, String content) {
        this.handler = handler;
        this.id = UUID.randomUUID();
        this.content = content;
        this.interested = new HashSet<>();
        this.accepted = new HashSet<>();
    }

    @Override
    public UUID getUniqueID() {
        return this.id;
    }

    @Override
    public Collection<UUID> getInvolved() {
        return this.interested;
    }

    @Override
    public IMatchPhase positive(ProxiedPlayer player) {
        // check if we now have everyone
        this.accepted.add(player.getUniqueId());
        if (this.accepted.size() < this.interested.size()) {
            return this;
        }

        // deploy verification packets to players
        ByteArrayDataOutput data = ByteStreams.newDataOutput();
        data.writeUTF(BungeeTable.CHANNEL_RPGCORE);
        data.writeUTF(BungeeTable.PROXY_MATCH_VERIFY);
        byte[] bytes = data.toByteArray();
        for (UUID uuid : getInvolved()) {
            this.handler.getPlugin().sendData(uuid, bytes);
        }

        // shift into next phase
        return new MatchStateVerify(this.handler, this);
    }

    @Override
    public IMatchPhase negative(ProxiedPlayer player) {
        this.interested.remove(player.getUniqueId());

        // create packet to inform about rejection
        ByteArrayDataOutput data = ByteStreams.newDataOutput();
        data.writeUTF(BungeeTable.CHANNEL_RPGCORE);
        data.writeUTF(BungeeTable.PROXY_MATCH_REJECT);
        data.writeUTF(this.id.toString());

        // deploy reject packet to everyone
        byte[] bytes = data.toByteArray();
        for (UUID uuid : getInvolved()) {
            this.handler.getPlugin().sendData(uuid, bytes);
        }

        // return null to terminate process
        return null;
    }

    /**
     * Deploy packets to players, which will ask them about
     * whether they want to participate in this content.
     */
    public void inform() {
        ByteArrayDataOutput data = ByteStreams.newDataOutput();
        data.writeUTF(BungeeTable.CHANNEL_RPGCORE);
        data.writeUTF(BungeeTable.PROXY_MATCH_ASK);
        data.writeUTF(this.content);
        data.writeInt(this.interested.size());
        for (UUID uuid : this.interested) {
            data.writeUTF(uuid.toString());
        }

        this.broadcast(this.handler.getPlugin(), data.toByteArray());
    }
}
