package me.blutkrone.rpgcore.data;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.data.IDataAdapter;
import me.blutkrone.rpgcore.api.data.IDataIdentity;
import me.blutkrone.rpgcore.api.entity.EntityProvider;
import me.blutkrone.rpgcore.api.event.CoreInitializationEvent;
import me.blutkrone.rpgcore.data.adapter.MongoAdapter;
import me.blutkrone.rpgcore.data.adapter.YamlAdapter;
import me.blutkrone.rpgcore.data.defaults.*;
import me.blutkrone.rpgcore.data.structure.CharacterProfile;
import me.blutkrone.rpgcore.data.structure.DataProtocol;
import me.blutkrone.rpgcore.data.structure.RosterProfile;
import me.blutkrone.rpgcore.entity.EntityManager;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.entity.providers.PlayerProvider;
import me.blutkrone.rpgcore.hud.HUDManager;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.util.Utility;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * All characters of a player belong to their roster, which provides
 * a shared set of data for all characters to write into.
 *
 * @see CharacterProfile a character within a roster
 * @see RosterProfile the roster of the player
 */
public class DataManager implements Listener {

    // factory which can provide us with vanilla players
    private EntityProvider player_provider = new PlayerProvider();
    // the approach on reading and writing player data
    private Map<String, DataProtocol> data_protocol = new HashMap<>();
    // position to use before player is fully initialized
    private Location pre_login_position;
    // which adapter to load data thorough
    private IDataAdapter data_adapter;
    // suppress roster menu for uuid until timestamp
    private Map<UUID, Long> suppress_time = new HashMap<>();

    public DataManager() {
        Bukkit.getLogger().info("not implemented (data manager needs better threading!");

        try {
            this.data_adapter = CoreInitializationEvent.find(IDataAdapter.class);
            if (this.data_adapter == null) {
                String networkToken = Utility.getDatabaseToken();
                if (networkToken.startsWith("mongodb")) {
                    this.data_adapter = new MongoAdapter(networkToken);
                } else {
                    this.data_adapter = new YamlAdapter();
                }
            }

            ConfigWrapper config = FileUtil.asConfigYML(FileUtil.file("network.yml"));
            this.pre_login_position = config.getLocation("pre-login-position");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // ensure appropriate event handling
        Bukkit.getPluginManager().registerEvents(this, RPGCore.inst());

        // lock away player in (intended) dark box.
        Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
            ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
            EntityManager em = RPGCore.inst().getEntityManager();
            HUDManager hudm = RPGCore.inst().getHUDManager();

            for (Player player : Bukkit.getOnlinePlayers()) {
                // kick if a misconfiguration applies
                if (pre_login_position.getWorld() == null) {
                    player.kickPlayer("§cYou've been kicked by: §fRPGCore\n\n§cIllegal Config: 'pre-login-position'");
                    continue;
                }

                // solid block so we do not get fly kicked
                Block block = pre_login_position.getBlock();
                block = block.getRelative(BlockFace.DOWN);
                if (block.isPassable()) {
                    block.setType(Material.BEDROCK);
                }

                // suppression of login menu
                long suppressed = this.suppress_time.getOrDefault(player.getUniqueId(), 0L);
                if (suppressed > System.currentTimeMillis()) {
                    if (Utility.distanceSqOrWorld(player, pre_login_position) > 1d) {
                        try {
                            if (player.getGameMode() != GameMode.ADVENTURE) {
                                player.setGameMode(GameMode.ADVENTURE);
                            }
                            player.teleport(pre_login_position);
                        } catch (Exception e) {
                            player.kickPlayer("§cYou've been kicked by: §fRPGCore\n\n§cIllegal Config: 'pre-login-position'");
                        }
                    }
                    return;
                }
                this.suppress_time.remove(player.getUniqueId());

                if (!rpm.hasLoaded(player)) {
                    // re-teleport while waiting for resourcepack to load
                    try {
                        if (player.getGameMode() != GameMode.ADVENTURE) {
                            player.setGameMode(GameMode.ADVENTURE);
                        }
                        player.teleport(pre_login_position);
                    } catch (Exception e) {
                        player.kickPlayer("§cYou've been kicked by: §fRPGCore\n\n§cIllegal Config: 'pre-login-position'");
                    }
                } else if (em.getPlayer(player) == null) {
                    // present roster menu again if it was closed
                    if (player.getOpenInventory().getTopInventory().getType() == InventoryType.CRAFTING) {
                        // check if we can just perform a quick-join
                        try {
                            Map<String, DataBundle> data = getRawRosterData(player.getUniqueId());
                            DataBundle bundle = data.get("roster_quick_slot");
                            if (bundle != null) {
                                int quick_join = bundle.getNumber(1).intValue();
                                if (quick_join != -1) {
                                    CorePlayer core_player = RPGCore.inst().getDataManager().loadPlayer(player, IDataIdentity.of(player.getUniqueId(), quick_join));
                                    RPGCore.inst().getEntityManager().register(player.getUniqueId(), core_player);
                                    return;
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        hudm.getRosterMenu().open(player);
                    }

                    // re-teleport if moved before picking a character
                    if (Utility.distanceSqOrWorld(player, pre_login_position) > 1d) {
                        try {
                            if (player.getGameMode() != GameMode.ADVENTURE) {
                                player.setGameMode(GameMode.ADVENTURE);
                            }
                            player.teleport(pre_login_position);
                        } catch (Exception e) {
                            player.kickPlayer("§cYou've been kicked by: §fRPGCore\n\n§cIllegal Config: 'pre-login-position'");
                        }
                    }
                } else if (!hudm.getRosterMenu().hasInitiated(em.getPlayer(player))) {
                    // re-teleport if moved before picking a character
                    if (Utility.distanceSqOrWorld(player, pre_login_position) > 1d) {
                        try {
                            if (player.getGameMode() != GameMode.ADVENTURE) {
                                player.setGameMode(GameMode.ADVENTURE);
                            }
                            player.teleport(pre_login_position);
                        } catch (Exception e) {
                            player.kickPlayer("§cYou've been kicked by: §fRPGCore\n\n§cIllegal Config: 'pre-login-position'");
                        }
                    }
                }
            }
        }, 20, 20);

        // force-save once per minute
        Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getOpenInventory().getType() == InventoryType.CRAFTING) {
                    CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
                    if (core_player != null && core_player.canAutoSaveNow()) {
                        savePlayerAsync(core_player);
                    }
                }
            }
        }, 20, 1200);

        // default protocols to store data thorough
        data_protocol.put("position", new PositionProtocol());
        data_protocol.put("display", new DisplayProtocol());
        data_protocol.put("job", new JobProtocol());
        data_protocol.put("skill", new SkillProtocol());
        data_protocol.put("item", new ItemProtocol());
        data_protocol.put("editor", new EditorProtocol());
        data_protocol.put("menu", new MenuProtocol());
        data_protocol.put("quest", new QuestProtocol());
        data_protocol.put("tag", new TagProtocol());
        data_protocol.put("passive", new PassiveProtocol());
        data_protocol.put("level", new LevelProtocol());
        data_protocol.put("roster_bank", new RosterBankerProtocol());
        data_protocol.put("roster_storage", new RosterStorageProtocol());
        data_protocol.put("roster_refinement", new RosterRefinementProtocol());
        data_protocol.put("roster_quick_slot", new RosterQuickJoinProtocol());
        data_protocol.put("roster_chat", new RosterChatProtocol());
    }

    /**
     * Suppress the login interface until the given timestamp passed, this
     * is primarily for cross-server logout behaviour.
     *
     * @param player who should be suppressed
     * @param millis milliseconds to suppress
     */
    public void suppressMenu(Player player, long millis) {
        if (millis < 0) {
            this.suppress_time.remove(player.getUniqueId());
        } else {
            this.suppress_time.put(player.getUniqueId(), System.currentTimeMillis() + millis);
        }
    }

    /**
     * Suppress the login interface until the given timestamp passed, this
     * is primarily for cross-server logout behaviour.
     *
     * @param player who should be suppressed
     * @param millis milliseconds to suppress
     */
    public void suppressMenu(UUID player, long millis) {
        if (millis < 0) {
            this.suppress_time.remove(player);
        } else {
            this.suppress_time.put(player, System.currentTimeMillis() + millis);
        }
    }

    /**
     * Anchor where players are teleported while they haven't logged in
     * properly just-yet.
     *
     * @return pre-login anchor.
     */
    public Location getPreLoginPosition() {
        return pre_login_position;
    }

    /**
     * Adapter that handles the low-level IO operations.
     *
     * @return adapter for low level processing
     */
    public IDataAdapter getDataAdapter() {
        return data_adapter;
    }

    /**
     * Retrieve the raw data of a character.
     *
     * @param identity the identity to retrieve thorough.
     * @return the raw data we've retrieved
     * @throws IOException should the backing data adapter fail, it can raise an error.
     */
    public Map<String, DataBundle> getRawData(IDataIdentity identity) throws IOException {
        return this.data_adapter.loadCharacterData(identity.getUserId(), identity.getCharacter());
    }

    /**
     * Retrieve the raw data of the roster.
     *
     * @param identity the identity to retrieve thorough
     * @return the raw data we've retrieved
     * @throws IOException should the backing data adapter fail, it can raise an error.
     */
    public Map<String, DataBundle> getRawRosterData(UUID identity) throws IOException {
        return this.data_adapter.loadRosterData(identity);
    }

    /**
     * Create a core player with the given identity.
     *
     * @param bukkit_player bukkit entity we are wrapping
     * @param identity      the identity we've got.
     * @return the player that was created, but not initialized.
     * @throws IOException should the backing data adapter fail, it can raise an error.
     */
    public CorePlayer loadPlayer(Player bukkit_player, IDataIdentity identity) throws IOException {
        // create an empty player instance.
        CorePlayer core_player = new CorePlayer(bukkit_player, this.player_provider, identity.getCharacter());
        // read up the data stored for this player
        Map<String, DataBundle> loaded_character = data_adapter.loadCharacterData(identity.getUserId(), identity.getCharacter());
        Map<String, DataBundle> loaded_roster = data_adapter.loadRosterData(identity.getUserId());
        // load the data we got on this character
        data_protocol.forEach((id, protocol) -> {
            // load relevant data bundle
            DataBundle bundle;
            if (protocol.isRosterData()) {
                bundle = loaded_roster.getOrDefault(id, new DataBundle());
            } else {
                bundle = loaded_character.getOrDefault(id, new DataBundle());
            }
            // extract version number
            int version = 0;
            if (bundle.size() > 0) {
                version = Integer.parseInt(bundle.getHandle().remove(0));
            }
            // serialize into data bundle
            protocol.load(core_player, bundle, version);
        });
        // write back info on last loaded character
        data_adapter.saveInfo(identity.getUserId(), "last_character", new DataBundle(String.valueOf(identity.getCharacter())));
        // offer up the created player
        return core_player;
    }

    /**
     * Retrieve data of the last character connected to
     *
     * @param bukkit_player Whose last character info we want
     * @return Info on last character logged on
     */
    public Map<String, DataBundle> getLastData(UUID bukkit_player) {
        try {
            DataBundle bundle = data_adapter.loadInfo(bukkit_player, "last_character");
            if (!bundle.isEmpty()) {
                int character = bundle.getNumber(0).intValue();
                return getRawData(IDataIdentity.of(bukkit_player, character));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return new HashMap<>();
    }

    /**
     * Performs serialization synchronously, but performs an
     * asynchronous write operation.
     *
     * @param player Who to serialize
     */
    public void savePlayerAsync(CorePlayer player) {
        Map<String, DataBundle> raw_data_character = new HashMap<>();
        Map<String, DataBundle> raw_data_roster = new HashMap<>();
        data_protocol.forEach((id, protocol) -> {
            DataBundle bundle = new DataBundle();
            bundle.addNumber(protocol.getDataVersion());
            protocol.save(player, bundle);
            if (protocol.isRosterData()) {
                raw_data_roster.put(id, bundle);
            } else {
                raw_data_character.put(id, bundle);
            }
        });

        Bukkit.getScheduler().runTaskAsynchronously(RPGCore.inst(), () -> {
            try {
                data_adapter.saveCharacterData(player.getUserId(), player.getCharacter(), raw_data_character);
                data_adapter.saveRosterData(player.getUserId(), raw_data_roster);
            } catch (Exception e) {
                Bukkit.getLogger().severe("DataAdapter could not write " + player.getUniqueId() + ":" + player.getCharacter());
                e.printStackTrace();
            }
        });
    }

    /**
     * Save the data of a player.
     *
     * @param player whose data to save.
     */
    public void savePlayer(CorePlayer player) throws IOException {
        Map<String, DataBundle> raw_data_character = new HashMap<>();
        Map<String, DataBundle> raw_data_roster = new HashMap<>();
        data_protocol.forEach((id, protocol) -> {
            DataBundle bundle = new DataBundle();
            bundle.addNumber(protocol.getDataVersion());
            protocol.save(player, bundle);
            if (protocol.isRosterData()) {
                raw_data_roster.put(id, bundle);
            } else {
                raw_data_character.put(id, bundle);
            }
        });

        data_adapter.saveCharacterData(player.getUserId(), player.getCharacter(), raw_data_character);
        data_adapter.saveRosterData(player.getUserId(), raw_data_roster);

        // wipe items held by the player (anti dupe)
        player.getEntity().getInventory().clear();
        player.getEntity().getEquipment().clear();
    }

    /**
     * Block the thread and force all operations to complete instantly, do
     * note that this will block the thread we called from.
     */
    public void flush() {
        // todo: is a flush operation still necessary??
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    void onDebug(PlayerJoinEvent e) {
        // RPGCore.inst().getEntityManager().register(e.getPlayer().getUniqueId(), new CorePlayer(e.getPlayer(), player_provider));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    void onRemovePlayer(PlayerQuitEvent e) {
        // unregister a player, save their data
        RPGCore.inst().getEntityManager().unregister(e.getPlayer().getUniqueId());
        // do not retain menu suppression time
        this.suppress_time.remove(e.getPlayer().getUniqueId());
    }
}
