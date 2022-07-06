package me.blutkrone.rpgcore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.blutkrone.rpgcore.api.damage.IDamageManager;
import me.blutkrone.rpgcore.api.guild.IGuildManager;
import me.blutkrone.rpgcore.api.party.IPartyManager;
import me.blutkrone.rpgcore.api.social.ISocialManager;
import me.blutkrone.rpgcore.attribute.AttributeManager;
import me.blutkrone.rpgcore.command.AbstractCommand;
import me.blutkrone.rpgcore.command.CommandArgumentException;
import me.blutkrone.rpgcore.command.impl.*;
import me.blutkrone.rpgcore.damage.DamageManager;
import me.blutkrone.rpgcore.data.DataManager;
import me.blutkrone.rpgcore.dungeon.DungeonManager;
import me.blutkrone.rpgcore.effect.EffectManager;
import me.blutkrone.rpgcore.entity.EntityManager;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.exception.InitializationException;
import me.blutkrone.rpgcore.hologram.HologramManager;
import me.blutkrone.rpgcore.hud.HUDManager;
import me.blutkrone.rpgcore.hud.editor.bundle.EditorBundleGsonAdapter;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.item.ItemManager;
import me.blutkrone.rpgcore.job.JobManager;
import me.blutkrone.rpgcore.language.LanguageManager;
import me.blutkrone.rpgcore.level.LevelManager;
import me.blutkrone.rpgcore.minimap.MinimapManager;
import me.blutkrone.rpgcore.mob.MobManager;
import me.blutkrone.rpgcore.mount.MountManager;
import me.blutkrone.rpgcore.npc.NPCManager;
import me.blutkrone.rpgcore.nms.api.AbstractVolatileManager;
import me.blutkrone.rpgcore.node.NodeManager;
import me.blutkrone.rpgcore.party.PartyManager;
import me.blutkrone.rpgcore.passive.PassiveManager;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.skill.SkillManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class RPGCore extends JavaPlugin {

    // incremented by +1 each tick
    private int timestamp;
    // GSON implementation used by the core
    private Gson gson;
    // delegate handlers for commands
    private Map<String, AbstractCommand> commands = new HashMap<>();

    // Managers providing low-level logic and functionality
    private AbstractVolatileManager volatile_manager;
    private AttributeManager attribute_manager;
    private EntityManager entity_manager;
    private HologramManager hologram_manager;
    private LanguageManager language_manager;
    private DataManager data_manager;
    private ResourcePackManager resourcepack_manager;
    private HUDManager hud_manager;
    private MinimapManager minimap_manager;
    private ItemManager item_manager;
    private IDamageManager damage_manager;
    private JobManager job_manager;
    private EffectManager effect_manager;
    private NodeManager node_manager;
    private NPCManager npc_manager;
    // Managers providing high-level functionality for the server
    private SkillManager skill_manager;
    private MobManager monster_manager;
    private MountManager mount_manager;
    private IGuildManager guild_manager;
    private IPartyManager party_manager;
    private ISocialManager social_manager;
    private LevelManager level_manager;
    private DungeonManager dungeon_manager;
    private PassiveManager passive_manager;

    public static RPGCore inst() {
        return JavaPlugin.getPlugin(RPGCore.class);
    }

    @Override
    public void onEnable() {
        // provide the gson access for other managers to use
        this.gson = new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .registerTypeAdapter(IEditorBundle.class, new EditorBundleGsonAdapter<>())
                .create();

        // volatile code implementation handling non-api code
        this.volatile_manager = AbstractVolatileManager.create(this);
        if (this.volatile_manager == null)
            throw new InitializationException("Could not create 'Volatile Manager' for this version!");

        // modules which make up the core
        this.language_manager = new LanguageManager();
        this.attribute_manager = new AttributeManager();
        this.damage_manager = new DamageManager();
        this.entity_manager = new EntityManager();
        this.hologram_manager = new HologramManager();
        this.data_manager = new DataManager();
        this.resourcepack_manager = new ResourcePackManager();
        this.hud_manager = new HUDManager();
        this.minimap_manager = new MinimapManager();
        this.item_manager = new ItemManager();
        this.skill_manager = new SkillManager();
        this.party_manager = new PartyManager();
        this.job_manager = new JobManager();
        this.effect_manager = new EffectManager();
        this.node_manager = new NodeManager();
        this.npc_manager = new NPCManager();

        // initialize relevant commands
        this.commands.put("edit", new EditorCommand());
        this.commands.put("box", new AdminBoxCommand());
        this.commands.put("skill", new SkillCommand());
        this.commands.put("compile", new ResourcepackCompileCommand());
        this.commands.put("url", new ResourcepackLinkCommand());
        this.commands.put("equip", new EquipCommand());
        this.commands.put("logout", new LogoutCommand());
        this.commands.put("debug", new DebugCommand());
        this.commands.put("tool", new ToolCommand());
        this.commands.put("help", new HelpCommand());

        // task to update the timestamp
        Bukkit.getScheduler().runTaskTimer(this, () -> this.timestamp += 1, 1, 1);

        // ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, PacketType.Play.Server.WINDOW_ITEMS) {
        //     @Override
        //     public void onPacketSending(PacketEvent event) {
        //         new Exception("window items").printStackTrace();
        //     }
        // });
        // ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, PacketType.Play.Server.SET_SLOT) {
        //     @Override
        //     public void onPacketSending(PacketEvent event) {
        //         new Exception("set slot").printStackTrace();
        //     }
        // });
    }

    @Override
    public void onDisable() {
        // request all players to be saved
        for (Player player : Bukkit.getOnlinePlayers()) {
            CorePlayer core_player = getEntityManager().getPlayer(player);
            if (core_player != null) {
                core_player.remove();
            }
        }
        // make sure the data adapter finished
        getDataManager().flush();
    }

    /**
     * Retrieve the random instance shared across the core.
     *
     * @return the random instance of the core.
     */
    public Random getRandom() {
        return ThreadLocalRandom.current();
    }

    /**
     * How many ticks passed since the core was activated, do note that
     * this value will NOT persist across updates.
     *
     * @return ticks passed since core was activated.
     */
    public int getTimestamp() {
        return timestamp;
    }

    /**
     * A gson implementation to provide an interface to json
     * logic.
     *
     * @return gson instance belonging to the plugin
     */
    public Gson getGson() {
        return gson;
    }

    /**
     * A mapping of all registered commands for the core.
     *
     * @return the mapping of registered commands.
     */
    public Map<String, AbstractCommand> getCommands() {
        return commands;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return AbstractCommand.suggestLimitBy(args[0], commands.keySet());
        } else if (args.length > 1) {
            AbstractCommand cmd = commands.get(args[0]);
            if (cmd == null) return null;
            return cmd.suggest(sender, args);
        } else {
            return null;
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        AbstractCommand cmd = commands.get(args[0]);
        if (cmd != null && cmd.canUseCommand(sender)) {
            try {
                cmd.invoke(sender, args);
            } catch (CommandArgumentException e) {
                sender.sendMessage("§c" + e.getMessage());
            }
        }
        return true;
    }

    public AbstractVolatileManager getVolatileManager() {
        return volatile_manager;
    }

    public AttributeManager getAttributeManager() {
        return attribute_manager;
    }

    public EntityManager getEntityManager() {
        return entity_manager;
    }

    public IDamageManager getDamageManager() {
        return damage_manager;
    }

    public ResourcePackManager getResourcePackManager() {
        return resourcepack_manager;
    }

    public HUDManager getHUDManager() {
        return hud_manager;
    }

    public DataManager getDataManager() {
        return data_manager;
    }

    public MinimapManager getMinimapManager() {
        return minimap_manager;
    }

    public LanguageManager getLanguageManager() {
        return language_manager;
    }

    public ItemManager getItemManager() {
        return item_manager;
    }

    public HologramManager getHologramManager() {
        return hologram_manager;
    }

    public SkillManager getSkillManager() {
        return skill_manager;
    }

    public IPartyManager getPartyManager() {
        return party_manager;
    }

    public JobManager getJobManager() {
        return job_manager;
    }

    public EffectManager getEffectManager() {
        return effect_manager;
    }

    public NodeManager getNodeManager() {
        return node_manager;
    }

    public NPCManager getNPCManager() {
        return npc_manager;
    }
}
