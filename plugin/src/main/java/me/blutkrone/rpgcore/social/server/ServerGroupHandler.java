package me.blutkrone.rpgcore.social.server;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.social.IGroupHandler;
import me.blutkrone.rpgcore.api.social.IPartySnapshot;
import me.blutkrone.rpgcore.dungeon.CoreDungeon;
import me.blutkrone.rpgcore.dungeon.IDungeonInstance;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.language.LanguageManager;
import me.blutkrone.rpgcore.menu.AbstractYesNoMenu;
import me.blutkrone.rpgcore.social.SocialManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.stream.Collectors;

public class ServerGroupHandler implements IGroupHandler, Listener {

    private static int RANDOM_PARTY_ID = 0;
    private final SocialManager social_manager;

    private Map<String, PartySnapshot> party_by_id = new HashMap<>();
    private Map<UUID, PartySnapshot> party_by_player = new HashMap<>();
    private Map<String, Set<UUID>> matchmaker_interests = new HashMap<>();
    private Map<UUID, MatchProcess> matchmaker_active = new HashMap<>();

    public ServerGroupHandler(SocialManager social_manager) {
        this.social_manager = social_manager;
        Bukkit.getPluginManager().registerEvents(this, RPGCore.inst());
    }

    private boolean verify(UUID who) {
        // must be online and registered
        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(who);
        if (core_player == null) {
            return false;
        }
        // cannot have a menu open
        if (core_player.getEntity().getOpenInventory().getType() != InventoryType.CRAFTING) {
            return false;
        }
        // cannot have a party already
        return getPartySnapshot(core_player) == null;
    }

    /**
     * Request a matchmaker termination for the given player, do
     * note that this will also kill the matchmaker process if it
     * started.
     *
     * @param player Who wants to terminate
     */
    public void terminateMatching(Player player) {
        // update queued contents to nothing
        this.matchmaker_interests.forEach((content, queued) -> queued.remove(player.getUniqueId()));
        // give negative feedback to matchmaker
        MatchProcess phase = this.matchmaker_active.remove(player.getUniqueId());
        if (phase != null) {
            phase.decline(player);
            for (UUID uuid : phase.accepted) {
                this.matchmaker_active.remove(uuid);
            }
        }
    }

    /**
     * Check if we are actively matching right now
     *
     * @return Whether player has an active matching process
     */
    public boolean isActivelyMatching(UUID player) {
        return this.matchmaker_active.containsKey(player);
    }

    /*
     * Search for match-making content that fits the given
     * player.
     *
     * @param player
     * @param contents
     */
    private void findMatchFor(Player player, String[] contents) {
        for (String content : contents) {
            // check if we have enough players
            Set<UUID> candidates = this.matchmaker_interests.getOrDefault(content, Collections.emptySet());
            // check if have enough participants now
            CoreDungeon template = RPGCore.inst().getDungeonManager().getDungeonIndex().get(content);
            if (candidates.size() < template.getPlayerLimit()) {
                continue;
            }
            // ask players about participation
            MatchProcess process = new MatchProcess(content);
            process.interested.add(player.getUniqueId());
            for (UUID candidate : candidates) {
                if (!isActivelyMatching(candidate)) {
                    continue;
                }
                if (process.interested.size() >= template.getPlayerLimit()) {
                    break;
                }
                process.interested.add(candidate);
            }
            // register process and deploy prompt
            matchmaker_active.put(process.id, process);
            for (UUID uuid : process.interested) {
                this.matchmaker_active.put(uuid, process);
            }
            process.inform();
            // we are done
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onQuitServer(PlayerQuitEvent e) {
        // quit queue and leave party on disconnect
        quitParty(e.getPlayer());
        terminateMatching(e.getPlayer());
    }

    @Override
    public void queueForContent(Player player, String... contents) {
        // terminate prior matching
        terminateMatching(player);

        PartySnapshot party = getPartySnapshot(player);
        if (party == null) {
            for (String content : contents) {
                this.matchmaker_interests.computeIfAbsent(content, (k -> new HashSet<>())).add(player.getUniqueId());
            }
            findMatchFor(player, contents);
        } else if (party.isLeader(player) && contents.length != 0) {
            // ensure everyone is capable of participation
            for (UUID uuid : party.getMembers()) {
                // must be online and registered
                CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(uuid);
                if (core_player == null) {
                    RPGCore.inst().getLanguageManager().sendMessage(player, "group_internal_error");
                    return;
                }
                // cannot have a menu open
                if (core_player.getEntity().getOpenInventory().getType() != InventoryType.CRAFTING) {
                    RPGCore.inst().getLanguageManager().sendMessage(player, "group_internal_error");
                    return;
                }
            }
            // enter the dungeon area
            for (UUID uuid : party.getMembers()) {
                matchmaker_active.remove(uuid);
            }
            for (UUID uuid : party.getMembers()) {
                matchmaker_interests.values().forEach(uuids -> uuids.remove(uuid));
            }
            // handle transfer to the relevant area
            IDungeonInstance instance = RPGCore.inst().getDungeonManager().createInstance(contents[0]);
            instance.invite(party.getAllMembers().stream().map(OfflinePlayer::getName).collect(Collectors.toList()));
        } else {
            RPGCore.inst().getLanguageManager().sendMessage(player, "group_internal_error");
        }
    }

    @Override
    public void joinParty(Player asking, String target) {
        if (getPartySnapshot(asking) == null) {
            Player leader = Bukkit.getPlayer(target);
            if (leader != null && leader.getOpenInventory().getType() == InventoryType.CRAFTING) {
                PartySnapshot party = getPartySnapshot(leader);
                if (party != null && party.getAllOnlineMembers().size() < 6 && party.isLeader(leader)) {
                    new PromptAskLeader(asking.getName(), party.getId()).finish(leader);
                    return;
                }
            }
        }

        RPGCore.inst().getLanguageManager().sendMessage(asking, "group_internal_error");
    }

    @Override
    public void inviteParty(Player asking, String target) {
        PartySnapshot party = getPartySnapshot(asking);
        if (party == null) {
            Player stranger = Bukkit.getPlayer(target);
            if (stranger != null && getPartySnapshot(stranger) == null && stranger.getOpenInventory().getType() == InventoryType.CRAFTING) {
                party = new PartySnapshot("auto_generated_" + RANDOM_PARTY_ID++);
                party.leader = asking.getUniqueId();
                party.members.add(asking.getUniqueId());
                this.party_by_id.put(party.getId(), party);
                this.party_by_player.put(asking.getUniqueId(), party);

                new PromptAskStranger(asking.getName(), party.getId()).finish(stranger);
                return;
            }
        } else if (party.getAllOnlineMembers().size() < 6 && party.isLeader(asking)) {
            Player stranger = Bukkit.getPlayer(target);
            if (stranger != null && getPartySnapshot(stranger) == null && stranger.getOpenInventory().getType() == InventoryType.CRAFTING) {
                new PromptAskStranger(asking.getName(), party.getId()).finish(stranger);
                return;
            }
        }

        RPGCore.inst().getLanguageManager().sendMessage(asking, "group_internal_error");
    }

    @Override
    public void kickParty(Player asking, String kicked) {
        PartySnapshot party = getPartySnapshot(asking);
        if (party != null && party.isLeader(asking)) {
            OfflinePlayer stranger = Bukkit.getOfflinePlayer(kicked);
            if (party.isMember(stranger) && !party.isLeader(stranger)) {
                party.members.remove(stranger.getUniqueId());
                this.party_by_player.remove(stranger.getUniqueId());

                for (Player player : party.getOnlineBukkitPlayers()) {
                    RPGCore.inst().getLanguageManager().sendMessage(player, "group_member_quit", stranger.getName());
                }

                Player stranger_online = stranger.getPlayer();
                if (stranger_online != null) {
                    RPGCore.inst().getLanguageManager().sendMessage(stranger_online, "group_you_were_kicked");
                }

                return;
            }
        }

        RPGCore.inst().getLanguageManager().sendMessage(asking, "group_internal_error");
    }

    @Override
    public void createParty(String identifier, List<UUID> players) {
        Set<UUID> interested = new HashSet<>();
        for (UUID player : players) {
            Player online = Bukkit.getPlayer(player);
            if (online != null && getPartySnapshot(online) == null) {
                interested.add(player);
            }
        }

        if (!interested.isEmpty()) {
            PartySnapshot party = new PartySnapshot(identifier);
            party.leader = interested.iterator().next();
            party.members.addAll(interested);
            this.party_by_id.put(identifier, party);
            for (UUID uuid : interested) {
                this.party_by_player.put(uuid, party);
            }

            for (Player player : party.getOnlineBukkitPlayers()) {
                for (Player other : party.getOnlineBukkitPlayers()) {
                    RPGCore.inst().getLanguageManager().sendMessage(player, "group_member_join", other.getName());
                }
            }
        }
    }

    @Override
    public void quitParty(Player player) {
        PartySnapshot party = getPartySnapshot(player);
        if (party != null && party.isMember(player)) {
            party.members.remove(player.getUniqueId());
            this.party_by_player.remove(player.getUniqueId());
            if (party.members.isEmpty()) {
                this.party_by_id.remove(party.getId());
            } else {
                party.leader = party.members.iterator().next();
            }

            RPGCore.inst().getLanguageManager().sendMessage(player, "group_you_have_quit");
            for (Player party_player : party.getOnlineBukkitPlayers()) {
                RPGCore.inst().getLanguageManager().sendMessage(party_player, "group_member_quit", player.getName());
            }
            return;
        }

        RPGCore.inst().getLanguageManager().sendMessage(player, "group_internal_error");
    }

    @Override
    public void quitParty(CorePlayer player) {
        PartySnapshot party = getPartySnapshot(player);
        if (party != null && party.isMember(player)) {
            party.members.remove(player.getUniqueId());
            this.party_by_player.remove(player.getUniqueId());
            if (party.members.isEmpty()) {
                this.party_by_id.remove(party.getId());
            } else {
                party.leader = party.members.iterator().next();
            }

            RPGCore.inst().getLanguageManager().sendMessage(player.getEntity(), "group_you_have_quit");
            for (Player party_player : party.getOnlineBukkitPlayers()) {
                RPGCore.inst().getLanguageManager().sendMessage(party_player, "group_member_quit", player.getName());
            }
            return;
        }

        RPGCore.inst().getLanguageManager().sendMessage(player.getEntity(), "group_internal_error");
    }

    @Override
    public PartySnapshot getPartySnapshot(String identifier) {
        return this.party_by_id.get(identifier);
    }

    @Override
    public PartySnapshot getPartySnapshot(OfflinePlayer player) {
        return this.party_by_player.get(player.getUniqueId());
    }

    @Override
    public IPartySnapshot getPartySnapshot(UUID player) {
        return this.party_by_player.get(player);
    }

    @Override
    public PartySnapshot getPartySnapshot(CorePlayer player) {
        return this.party_by_player.get(player.getUniqueId());
    }

    /*
     * A prompt that allows a player to accept or decline an invitation to
     * a piece of content.
     */
    private class PromptAskMatch extends IGroupHandler.AbstractMatchPrompt {

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
            MatchProcess process = matchmaker_active.get(player.getUniqueId());
            if (process != null) {
                if (response) {
                    process.accept(player);
                } else {
                    process.decline(player);
                }
            }
        }

        @Override
        public UUID getOfferId() {
            return this.offer;
        }
    }

    private class MatchProcess {
        private final UUID id;
        private final String content;
        private final Set<UUID> interested = new HashSet<>();
        private Set<UUID> accepted = new HashSet<>();

        private MatchProcess(String content) {
            this.id = UUID.randomUUID();
            this.content = content;
        }

        public void accept(Player player) {
            this.accepted.add(player.getUniqueId());
            if (this.accepted.size() >= this.interested.size()) {
                boolean alright = true;
                for (UUID uuid : interested) {
                    if (!verify(uuid)) {
                        alright = false;
                    }
                }

                for (UUID uuid : interested) {
                    matchmaker_active.remove(uuid);
                }

                if (alright) {
                    // get ready to participate
                    for (UUID uuid : interested) {
                        matchmaker_interests.values().forEach(uuids -> uuids.remove(uuid));
                    }
                    // create a group with interested players
                    String id = "matchmaker_" + content + "_" + RANDOM_PARTY_ID++;
                    createParty(id, new ArrayList<>(interested));
                    // handle transfer to the relevant area
                    IDungeonInstance instance = RPGCore.inst().getDungeonManager().createInstance(content);
                    instance.invite(interested.stream().map(uuid -> Bukkit.getOfflinePlayer(uuid).getName()).collect(Collectors.toList()));
                } else {
                    // something went wrong, we cannot play this content
                    String warning = RPGCore.inst().getLanguageManager().getTranslation("matchmaker_rejected");
                    for (UUID uuid : interested) {
                        Player interested_player = Bukkit.getPlayer(uuid);
                        if (interested_player != null) {
                            interested_player.sendMessage(ChatColor.RED + warning);
                        }
                    }
                }
            }
        }

        public void decline(Player player) {
            // decline the offer
            for (UUID uuid : interested) {
                matchmaker_active.remove(uuid);
            }
            // something went wrong, we cannot play this content
            String warning = RPGCore.inst().getLanguageManager().getTranslation("matchmaker_rejected");
            for (UUID uuid : interested) {
                Player interested_player = Bukkit.getPlayer(uuid);
                if (interested_player != null) {
                    interested_player.sendMessage(ChatColor.RED + warning);
                }
            }
        }

        public void inform() {
            // offer prompts
            for (UUID uuid : this.interested) {
                Player player = Bukkit.getPlayer(uuid);
                new PromptAskMatch(id, content, new ArrayList<>(this.interested)).finish(player);
            }
        }
    }

    /*
     * Prompt shown to stranger, if leader invited them.
     */
    private class PromptAskLeader extends AbstractYesNoMenu {

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
                PartySnapshot party = getPartySnapshot(this.party);
                if (party != null && party.members.size() < 6) {
                    Player player = Bukkit.getPlayer(who_asked);
                    if (player != null) {
                        terminateMatching(player);
                        party.members.add(player.getUniqueId());
                        party_by_player.put(player.getUniqueId(), party);
                        for (Player party_player : party.getOnlineBukkitPlayers()) {
                            RPGCore.inst().getLanguageManager().sendMessage(party_player, "group_member_join", player.getName());
                        }
                    }
                }
            }
        }
    }

    /*
     * Prompt shown to leader, if a stranger requested to join.
     */
    private class PromptAskStranger extends AbstractYesNoMenu {

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
                PartySnapshot party = getPartySnapshot(this.party);
                if (party != null && party.members.size() < 6) {
                    Player player = getMenu().getViewer();
                    terminateMatching(player);
                    party.members.add(player.getUniqueId());
                    party_by_player.put(player.getUniqueId(), party);
                    for (Player party_player : party.getOnlineBukkitPlayers()) {
                        RPGCore.inst().getLanguageManager().sendMessage(party_player, "group_member_join", player.getName());
                    }
                }
            }
        }
    }

    /*
     * Active instance of a party, despite being called a snapshot
     * the backing implementation is mutable.
     */
    private class PartySnapshot implements IPartySnapshot {

        private String id;
        private UUID leader;
        private Set<UUID> members = new HashSet<>();

        PartySnapshot(String id) {
            this.id = id;
        }

        boolean isLeader(OfflinePlayer player) {
            return getLeaderUUID().equals(player.getUniqueId());
        }

        boolean isMember(OfflinePlayer player) {
            return this.members.contains(player.getUniqueId());
        }

        boolean isMember(UUID player) {
            return this.members.contains(player);
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

        public List<Player> getOnlineBukkitPlayers() {
            List<Player> players = new ArrayList<>();
            for (UUID member : this.members) {
                Player player = Bukkit.getPlayer(member);
                if (player != null) {
                    players.add(player);
                }
            }
            return players;
        }

        public Set<UUID> getMembers() {
            return members;
        }
    }
}
