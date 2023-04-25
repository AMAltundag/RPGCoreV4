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
 * Ask server if players are ready
 */
public class MatchStateVerify implements IMatchPhase {

    final BungeeGroupHandler handler;

    final UUID id;
    final String content;
    final Set<UUID> interested;
    final Set<UUID> verified;

    public MatchStateVerify(BungeeGroupHandler handler, MatchStateAsking before) {
        this.handler = handler;
        this.id = before.id;
        this.content = before.content;
        this.interested = before.interested;
        this.verified = new HashSet<>();
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
        this.verified.add(player.getUniqueId());
        if (this.verified.size() < this.interested.size()) {
            return this;
        }

        // deploy server transfer pre-departure packets
        ByteArrayDataOutput data = ByteStreams.newDataOutput();
        data.writeUTF(BungeeTable.CHANNEL_RPGCORE);
        data.writeUTF(BungeeTable.PROXY_MATCH_TRANSFER_DEPART);
        byte[] bytes = data.toByteArray();
        for (UUID uuid : getInvolved()) {
            this.handler.getPlugin().sendData(uuid, bytes);
        }

        return new MatchStateTransfer(this.handler, this);
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
}
