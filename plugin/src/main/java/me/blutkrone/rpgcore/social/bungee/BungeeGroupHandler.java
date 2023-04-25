package me.blutkrone.rpgcore.social.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.bungee.IBungeeHandling;
import me.blutkrone.rpgcore.api.social.IGroupHandler;
import me.blutkrone.rpgcore.api.social.IPartySnapshot;
import me.blutkrone.rpgcore.dungeon.IDungeonInstance;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.language.LanguageManager;
import me.blutkrone.rpgcore.menu.AbstractYesNoMenu;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.social.BungeeTable;
import me.blutkrone.rpgcore.social.SocialManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.*;

public class BungeeGroupHandler implements IGroupHandler, IBungeeHandling {

    private final SocialManager social_manager;

    private final Map<String, PartySnapshot> parties_by_id = new HashMap<>();
    private final Map<UUID, PartySnapshot> parties_by_player = new HashMap<>();

    public BungeeGroupHandler(SocialManager social_manager) {
        this.social_manager = social_manager;
    }

    @Override
    public void onBungeeMessage(Player recipient, String channel, ByteArrayDataInput data) {
        if (BungeeTable.PROXY_MATCH_ASK.equals(channel)) {
            // ask player if they want to play the match
            UUID offer = UUID.fromString(data.readUTF());
            String content = data.readUTF();
            List<UUID> players = new ArrayList<>();
            int size = data.readInt();
            for (int i = 0; i < size; i++) {
                players.add(UUID.fromString(data.readUTF()));
            }

            doSyncTask(() -> {
                Player player = recipient.getPlayer();
                if (player != null) {
                    CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
                    if (core_player == null) {
                        // auto decline if not logged in
                        ByteArrayDataOutput response = ByteStreams.newDataOutput();
                        response.writeUTF(BungeeTable.SERVER_MATCH_DECLINE);
                        response.writeUTF(offer.toString());
                        player.sendPluginMessage(RPGCore.inst(), BungeeTable.CHANNEL_RPGCORE, response.toByteArray());
                    } else if (player.getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING) {
                        // auto decline while in any menu
                        ByteArrayDataOutput response = ByteStreams.newDataOutput();
                        response.writeUTF(BungeeTable.SERVER_MATCH_DECLINE);
                        response.writeUTF(offer.toString());
                        player.sendPluginMessage(RPGCore.inst(), BungeeTable.CHANNEL_RPGCORE, response.toByteArray());
                    } else {
                        // ask if player wants to participate
                        new PromptAskMatch(offer, content, players).finish(player);
                    }
                }
            });
        } else if (BungeeTable.PROXY_MATCH_REJECT.equals(channel)) {
            // one of the players asked refused the match
            UUID offer = UUID.fromString(data.readUTF());

            doSyncTask(() -> {
                Player player = recipient.getPlayer();
                if (player != null) {
                    // inform about having been rejected
                    String warning = RPGCore.inst().getLanguageManager().getTranslation("matchmaker_rejected");
                    player.sendMessage(ChatColor.RED + warning);
                    // attempt to close the menu to accept an invite
                    Inventory inventory = player.getOpenInventory().getTopInventory();
                    if (inventory instanceof IChestMenu) {
                        Object handle = ((IChestMenu) inventory).getLinkedHandle();
                        if (handle instanceof PromptAskMatch) {
                            if (offer.equals(((PromptAskMatch) handle).getOfferId())) {
                                player.closeInventory();
                            }
                        }
                    }
                }
            });
        } else if (BungeeTable.PROXY_MATCH_VERIFY.equals(channel)) {
            // ensure player can be transferred away
            doSyncTask(() -> {
                Player player = recipient.getPlayer();
                if (player != null) {
                    CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
                    if (core_player == null) {
                        // must be actively logged in
                        ByteArrayDataOutput response = ByteStreams.newDataOutput();
                        response.writeUTF(BungeeTable.SERVER_MATCH_VERIFY_FAILED);
                        player.sendPluginMessage(RPGCore.inst(), BungeeTable.CHANNEL_RPGCORE, response.toByteArray());
                    } else if (player.getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING) {
                        // must not have a menu open
                        ByteArrayDataOutput response = ByteStreams.newDataOutput();
                        response.writeUTF(BungeeTable.SERVER_MATCH_VERIFY_FAILED);
                        player.sendPluginMessage(RPGCore.inst(), BungeeTable.CHANNEL_RPGCORE, response.toByteArray());
                    } else {
                        // verification has been passed
                        ByteArrayDataOutput response = ByteStreams.newDataOutput();
                        response.writeUTF(BungeeTable.SERVER_MATCH_VERIFY_PASSED);
                        player.sendPluginMessage(RPGCore.inst(), BungeeTable.CHANNEL_RPGCORE, response.toByteArray());
                    }
                }
            });
        } else if (BungeeTable.PROXY_MATCH_TRANSFER_DEPART.equals(channel)) {
            // get ready to depart from the server
            doSyncTask(() -> {
                Player player = recipient.getPlayer();
                if (player != null) {
                    CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
                    if (core_player == null) {
                        // must be actively logged in
                        ByteArrayDataOutput response = ByteStreams.newDataOutput();
                        response.writeUTF(BungeeTable.SERVER_MATCH_TRANSFER_FAILED);
                        player.sendPluginMessage(RPGCore.inst(), BungeeTable.CHANNEL_RPGCORE, response.toByteArray());
                    } else if (player.getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING) {
                        // must not have a menu open
                        ByteArrayDataOutput response = ByteStreams.newDataOutput();
                        response.writeUTF(BungeeTable.SERVER_MATCH_TRANSFER_FAILED);
                        player.sendPluginMessage(RPGCore.inst(), BungeeTable.CHANNEL_RPGCORE, response.toByteArray());
                    } else {
                        // make sure we do not re-log into the menu
                        RPGCore.inst().getDataManager().suppressMenu(player, 60000L);
                        // skip roster menu next time we want to connect
                        core_player.quickJoinNextTime();
                        // save and unregister the player while we wait for a transfer
                        RPGCore.inst().getEntityManager().unregister(player.getUniqueId());
                        // verification has been passed
                        ByteArrayDataOutput response = ByteStreams.newDataOutput();
                        response.writeUTF(BungeeTable.SERVER_MATCH_TRANSFER_SUCCESS);
                        player.sendPluginMessage(RPGCore.inst(), BungeeTable.CHANNEL_RPGCORE, response.toByteArray());
                    }
                }
            });
        } else if (BungeeTable.PROXY_MATCH_TRANSFER_ARRIVE.equals(channel)) {
            // a party of players is arriving on this server
            String party = data.readUTF();
            int amount = data.readInt();
            List<String> players = new ArrayList<>();
            for (int i = 0; i < amount; i++) {
                players.add(data.readUTF());
            }

            String content = data.readUTF();
            doSyncTask(() -> {
                IDungeonInstance instance = RPGCore.inst().getDungeonManager().createInstance(content);
                instance.invite(players);
            });
        } else if (BungeeTable.PROXY_MATCH_TRANSFER_ERROR.equals(channel)) {
            // inform about the unexpected error
            String warning = RPGCore.inst().getLanguageManager().getTranslation("matchmaker_error");
            recipient.sendMessage(ChatColor.RED + warning);
            // unexpected transfer error, allow to log back on
            doSyncTask(() -> {
                Player player = recipient.getPlayer();
                RPGCore.inst().getDataManager().suppressMenu(player, -1L);
            });
        } else if (BungeeTable.PROXY_PARTY_UPDATE_ONE.equals(channel)) {
            // update of one specific party known to the proxy
            PartySnapshot party = new PartySnapshot(data);
            doSyncTask(() -> {
                PartySnapshot snapshot = this.parties_by_id.get(party.getId());
                if (snapshot != null) {
                    for (UUID member : snapshot.members) {
                        this.parties_by_player.remove(member);
                    }
                }
                if (party.getAllMembers().isEmpty()) {
                    this.parties_by_id.remove(party.getId());
                } else {
                    this.parties_by_id.put(party.getId(), party);
                }
            });
        } else if (BungeeTable.PROXY_PARTY_UPDATE_ALL.equals(channel)) {
            // update of every party known to the proxy
            Map<String, PartySnapshot> updated = new HashMap<>();
            int size = data.readInt();
            for (int i = 0; i < size; i++) {
                PartySnapshot party = new PartySnapshot(data);
                if (party.getAllMembers().size() > 0) {
                    updated.put(party.getId(), party);
                }
            }
            doSyncTask(() -> {
                this.parties_by_player.clear();
                this.parties_by_id.clear();
                this.parties_by_id.putAll(updated);
            });
        } else if (BungeeTable.PROXY_PARTY_ASK_STRANGER.equals(channel)) {
            // a stranger wants to join the party of the leader
            String who_asked = data.readUTF();
            String party = data.readUTF();
            doSyncTask(() -> {
                Player player = recipient.getPlayer();
                if (player != null && player.getOpenInventory().getType() == InventoryType.CRAFTING) {
                    new PromptAskStranger(who_asked, party).finish(player);
                }
            });
        } else if (BungeeTable.PROXY_PARTY_ASK_LEADER.equals(channel)) {
            // a leader has invited another player to their party
            String who_asked = data.readUTF();
            String party = data.readUTF();
            doSyncTask(() -> {
                Player player = recipient.getPlayer();
                if (player != null && player.getOpenInventory().getType() == InventoryType.CRAFTING) {
                    new PromptAskLeader(who_asked, party).finish(player);
                }
            });
        }
    }

    @Override
    public void queueForContent(Player player, String... contents) {
        // update proxy information on our interests
        ByteArrayDataOutput data = ByteStreams.newDataOutput();
        data.writeUTF(BungeeTable.CHANNEL_RPGCORE);
        data.writeUTF(BungeeTable.SERVER_MATCH_UPDATE);
        data.writeInt(contents.length);
        for (String content : contents) {
            data.writeUTF(content);
        }
        player.sendPluginMessage(RPGCore.inst(), BungeeTable.CHANNEL_BUNGEE, data.toByteArray());
    }

    @Override
    public void joinParty(Player asking, String target) {
        ByteArrayDataOutput data = ByteStreams.newDataOutput();
        data.writeUTF(BungeeTable.CHANNEL_RPGCORE);
        data.writeUTF(BungeeTable.SERVER_PARTY_ASK_LEADER);
        data.writeUTF(target);
        asking.sendPluginMessage(RPGCore.inst(), BungeeTable.CHANNEL_BUNGEE, data.toByteArray());
    }

    @Override
    public void inviteParty(Player asking, String target) {
        ByteArrayDataOutput data = ByteStreams.newDataOutput();
        data.writeUTF(BungeeTable.CHANNEL_RPGCORE);
        data.writeUTF(BungeeTable.SERVER_PARTY_ASK_STRANGER);
        data.writeUTF(target);
        asking.sendPluginMessage(RPGCore.inst(), BungeeTable.CHANNEL_BUNGEE, data.toByteArray());
    }

    @Override
    public void kickParty(Player asking, String kicked) {
        ByteArrayDataOutput data = ByteStreams.newDataOutput();
        data.writeUTF(BungeeTable.CHANNEL_RPGCORE);
        data.writeUTF(BungeeTable.SERVER_PARTY_WANT_KICK);
        data.writeUTF(kicked);
        asking.sendPluginMessage(RPGCore.inst(), BungeeTable.CHANNEL_BUNGEE, data.toByteArray());
    }

    @Override
    public void createParty(String identifier, List<UUID> players) {
        ByteArrayDataOutput data = ByteStreams.newDataOutput();
        data.writeUTF(BungeeTable.CHANNEL_RPGCORE);
        data.writeUTF(BungeeTable.SERVER_PARTY_CREATE);
        data.writeUTF(identifier);
        data.writeInt(players.size());
        for (UUID player : players) {
            data.writeUTF(player.toString());
        }
        Iterator<? extends Player> iterator = Bukkit.getOnlinePlayers().iterator();
        if (iterator.hasNext()) {
            iterator.next().sendPluginMessage(RPGCore.inst(), BungeeTable.CHANNEL_BUNGEE, data.toByteArray());
        } else {
            Bukkit.getLogger().severe("Party create request " + identifier + " has failed!");
        }
    }

    @Override
    public void quitParty(Player player) {
        ByteArrayDataOutput data = ByteStreams.newDataOutput();
        data.writeUTF(BungeeTable.CHANNEL_RPGCORE);
        data.writeUTF(BungeeTable.SERVER_PARTY_WANT_QUIT);
        player.sendPluginMessage(RPGCore.inst(), BungeeTable.CHANNEL_BUNGEE, data.toByteArray());
    }

    @Override
    public void quitParty(CorePlayer player) {
        ByteArrayDataOutput data = ByteStreams.newDataOutput();
        data.writeUTF(BungeeTable.CHANNEL_RPGCORE);
        data.writeUTF(BungeeTable.SERVER_PARTY_WANT_QUIT);
        player.getEntity().sendPluginMessage(RPGCore.inst(), BungeeTable.CHANNEL_BUNGEE, data.toByteArray());
    }

    @Override
    public IPartySnapshot getPartySnapshot(String identifier) {
        return this.parties_by_id.get(identifier);
    }

    @Override
    public IPartySnapshot getPartySnapshot(OfflinePlayer player) {
        return this.parties_by_player.computeIfAbsent(player.getUniqueId(), (uuid -> {
            for (PartySnapshot party : this.parties_by_id.values()) {
                if (party.isMember(uuid)) {
                    return party;
                }
            }

            return null;
        }));
    }

    @Override
    public IPartySnapshot getPartySnapshot(UUID player) {
        return this.parties_by_player.computeIfAbsent(player, (uuid -> {
            for (PartySnapshot party : this.parties_by_id.values()) {
                if (party.isMember(uuid)) {
                    return party;
                }
            }

            return null;
        }));
    }

    @Override
    public IPartySnapshot getPartySnapshot(CorePlayer player) {
        return this.parties_by_player.computeIfAbsent(player.getUniqueId(), (uuid -> {
            for (PartySnapshot party : this.parties_by_id.values()) {
                if (party.isMember(uuid)) {
                    return party;
                }
            }

            return null;
        }));
    }

    /*
     * A prompt that allows a player to accept or decline an invitation to
     * a piece of content.
     */
    private static class PromptAskMatch extends IGroupHandler.AbstractMatchPrompt {

        private final UUID offer;
        private final String content;
        private final List<UUID> player;

        PromptAskMatch(UUID offer, String content, List<UUID> player) {
            this.offer = offer;
            this.content = content;
            this.player = player;
        }

        @Override
        public List<String> getPrompt() {
            return RPGCore.inst().getLanguageManager().getTranslationList("matchmaker_" + this.content);
        }

        @Override
        public void handleResponse(boolean response) {
            Player player = getMenu().getViewer();

            if (response) {
                // inform proxy that player accepted a match
                ByteArrayDataOutput data = ByteStreams.newDataOutput();
                data.writeUTF(BungeeTable.CHANNEL_RPGCORE);
                data.writeUTF(BungeeTable.SERVER_MATCH_ACCEPT);
                data.writeUTF(offer.toString());
                player.sendPluginMessage(RPGCore.inst(), BungeeTable.CHANNEL_BUNGEE, data.toByteArray());
            } else {
                // inform proxy that player declined a match
                ByteArrayDataOutput data = ByteStreams.newDataOutput();
                data.writeUTF(BungeeTable.CHANNEL_RPGCORE);
                data.writeUTF(BungeeTable.SERVER_MATCH_DECLINE);
                data.writeUTF(offer.toString());
                player.sendPluginMessage(RPGCore.inst(), BungeeTable.CHANNEL_BUNGEE, data.toByteArray());
            }
        }

        @Override
        public UUID getOfferId() {
            return this.offer;
        }
    }

    /*
     * Prompt shown to stranger, if leader invited them.
     */
    private static class PromptAskLeader extends AbstractYesNoMenu {

        private final String party;
        private final String who_asked;
        private List<String> prompt;

        PromptAskLeader(String who_asked, String party) {
            LanguageManager language = RPGCore.inst().getLanguageManager();
            this.prompt = language.getTranslationList("group_prompt_ask_leader");
            this.prompt.replaceAll(string -> string.replace("%player%", who_asked));
            this.party = party;
            this.who_asked = who_asked;
        }

        @Override
        public List<String> getPrompt() {
            return this.prompt;
        }

        @Override
        public void handleResponse(boolean response) {
            if (response) {
                Player player = getMenu().getViewer();
                // inform proxy about our decision
                ByteArrayDataOutput data = ByteStreams.newDataOutput();
                data.writeUTF(BungeeTable.CHANNEL_RPGCORE);
                data.writeUTF(BungeeTable.SERVER_PARTY_ADD);
                data.writeUTF(party);
                data.writeUTF(who_asked);
                player.sendPluginMessage(RPGCore.inst(), BungeeTable.CHANNEL_BUNGEE, data.toByteArray());
            }
        }
    }

    /*
     * Prompt shown to leader, if a stranger requested to join.
     */
    private static class PromptAskStranger extends AbstractYesNoMenu {

        private final String party;
        private final String who_asked;
        private List<String> prompt;

        PromptAskStranger(String who_asked, String party) {
            LanguageManager language = RPGCore.inst().getLanguageManager();
            this.party = party;
            this.who_asked = who_asked;
            this.prompt = language.getTranslationList("group_prompt_ask_stranger");
            this.prompt.replaceAll(string -> string.replace("%player%", who_asked));
        }

        @Override
        public List<String> getPrompt() {
            return this.prompt;
        }

        @Override
        public void handleResponse(boolean response) {
            if (response) {
                Player player = getMenu().getViewer();
                // inform proxy about our decision
                ByteArrayDataOutput data = ByteStreams.newDataOutput();
                data.writeUTF(BungeeTable.CHANNEL_RPGCORE);
                data.writeUTF(BungeeTable.SERVER_PARTY_ADD);
                data.writeUTF(party);
                data.writeUTF(who_asked);
                player.sendPluginMessage(RPGCore.inst(), BungeeTable.CHANNEL_BUNGEE, data.toByteArray());
            }
        }
    }

    /*
     * A snapshot that provides basic information regarding
     * a party, the information may not be up-to-date.
     */
    private class PartySnapshot implements IPartySnapshot {

        private final String id;
        private final UUID leader;
        private final Set<UUID> members;

        PartySnapshot(ByteArrayDataInput data) {
            this.id = data.readUTF();
            this.leader = UUID.fromString(data.readUTF());
            this.members = new HashSet<>();
            int size = data.readInt();
            for (int i = 0; i < size; i++) {
                this.members.add(UUID.fromString(data.readUTF()));
            }
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public UUID getLeaderUUID() {
            return this.leader;
        }

        @Override
        public boolean isMember(CorePlayer player) {
            return this.members.contains(player.getUniqueId());
        }

        public boolean isMember(OfflinePlayer player) {
            return this.members.contains(player.getUniqueId());
        }

        public boolean isMember(UUID player) {
            return this.members.contains(player);
        }

        @Override
        public List<CorePlayer> getAllOnlineMembers() {
            List<CorePlayer> players = new ArrayList<>();
            for (UUID member : this.members) {
                CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(member);
                if (player != null) {
                    players.add(player);
                }
            }
            return players;
        }

        @Override
        public List<OfflinePlayer> getAllMembers() {
            List<OfflinePlayer> players = new ArrayList<>();
            for (UUID member : this.members) {
                players.add(Bukkit.getOfflinePlayer(member));
            }
            return players;
        }
    }
}
