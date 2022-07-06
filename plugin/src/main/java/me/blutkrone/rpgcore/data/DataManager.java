package me.blutkrone.rpgcore.data;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.data.IDataAdapter;
import me.blutkrone.rpgcore.api.data.IDataIdentity;
import me.blutkrone.rpgcore.api.entity.EntityProvider;
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

    public DataManager() {
        try {
            ConfigWrapper config = FileUtil.asConfigYML(FileUtil.file("database.yml"));
            this.pre_login_position = config.getLocation("pre-login-position");
            this.data_adapter = new YamlAdapter(config);
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
                Block block = pre_login_position.getBlock();
                block = block.getRelative(BlockFace.DOWN);
                if (block.isPassable()) {
                    block.setType(Material.BEDROCK);
                }

                if (!rpm.hasLoaded(player)) {
                    // re-teleport while waiting for resourcepack to load
                    player.teleport(pre_login_position);
                } else if (em.getPlayer(player) == null) {
                    // re-teleport if moved before picking a character
                    if (Utility.distanceSqOrWorld(player, pre_login_position) > 1d) {
                        player.teleport(pre_login_position);
                    }
                    // present roster menu again if it was closed
                    if (player.getOpenInventory().getTopInventory().getType() == InventoryType.CRAFTING) {
                        hudm.getRosterMenu().open(player);
                    }
                } else if (!hudm.getRosterMenu().hasInitiated(em.getPlayer(player))) {
                    // re-teleport if moved before picking a character
                    if (Utility.distanceSqOrWorld(player, pre_login_position) > 1d) {
                        player.teleport(pre_login_position);
                    }
                }
            }
        }, 20, 20);

        // default protocols to store data thorough
        data_protocol.put("position", new PositionProtocol());
        data_protocol.put("display", new DisplayProtocol());
        data_protocol.put("job", new JobProtocol());
        data_protocol.put("skill", new SkillProtocol());
        data_protocol.put("item", new ItemProtocol());
        data_protocol.put("editor", new EditorProtocol());
        data_protocol.put("menu", new MenuProtocol());
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
        Map<String, DataBundle> loaded = data_adapter.loadCharacterData(identity.getUserId(), identity.getCharacter());
        // load the data we got on this character
        data_protocol.forEach((id, protocol) -> protocol.load(core_player, loaded.getOrDefault(id, new DataBundle())));
        // offer up the created player
        return core_player;
    }

    /**
     * Save the data of a player.
     *
     * @param player whose data to save.
     */
    public void savePlayer(CorePlayer player) throws IOException {
        Map<String, DataBundle> raw_data = new HashMap<>();
        data_protocol.forEach((id, protocol) -> {
            DataBundle bundle = new DataBundle();
            protocol.save(player, bundle);
            raw_data.put(id, bundle);
        });
        data_adapter.saveCharacterData(player.getUserId(), player.getCharacter(), raw_data);
    }

    /**
     * Block the thread and force all operations to complete instantly, do
     * note that this will block the thread we called from.
     */
    public void flush() {
        this.data_adapter.flush();
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    void onDebug(PlayerJoinEvent e) {
        // RPGCore.inst().getEntityManager().register(e.getPlayer().getUniqueId(), new CorePlayer(e.getPlayer(), player_provider));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    void onRemovePlayer(PlayerQuitEvent e) {
        // unregister a player, and save their data.
        RPGCore.inst().getEntityManager().unregister(e.getPlayer().getUniqueId());
    }
}
