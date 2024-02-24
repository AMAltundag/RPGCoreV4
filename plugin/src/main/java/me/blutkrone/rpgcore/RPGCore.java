package me.blutkrone.rpgcore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.blutkrone.rpgcore.api.social.IGuildHandler;
import me.blutkrone.rpgcore.attribute.AttributeManager;
import me.blutkrone.rpgcore.bbmodel.BBModelManager;
import me.blutkrone.rpgcore.chat.ChatManager;
import me.blutkrone.rpgcore.command.AbstractCommand;
import me.blutkrone.rpgcore.command.CommandArgumentException;
import me.blutkrone.rpgcore.command.impl.*;
import me.blutkrone.rpgcore.damage.DamageManager;
import me.blutkrone.rpgcore.data.DataManager;
import me.blutkrone.rpgcore.dungeon.DungeonManager;
import me.blutkrone.rpgcore.editor.bundle.EditorBundleGsonAdapter;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.migration.MigrationHandler;
import me.blutkrone.rpgcore.effect.EffectManager;
import me.blutkrone.rpgcore.entity.EntityManager;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.exception.InitializationException;
import me.blutkrone.rpgcore.hologram.HologramManager;
import me.blutkrone.rpgcore.hud.HUDManager;
import me.blutkrone.rpgcore.hud.world.WorldIntegrationManager;
import me.blutkrone.rpgcore.item.ItemManager;
import me.blutkrone.rpgcore.job.JobManager;
import me.blutkrone.rpgcore.language.LanguageManager;
import me.blutkrone.rpgcore.level.LevelManager;
import me.blutkrone.rpgcore.mail.MailManager;
import me.blutkrone.rpgcore.minimap.v2.MinimapManagerV2;
import me.blutkrone.rpgcore.mob.MobManager;
import me.blutkrone.rpgcore.mount.MountManager;
import me.blutkrone.rpgcore.nms.api.AbstractVolatileManager;
import me.blutkrone.rpgcore.node.NodeManager;
import me.blutkrone.rpgcore.npc.NPCManager;
import me.blutkrone.rpgcore.passive.PassiveManager;
import me.blutkrone.rpgcore.quest.QuestManager;
import me.blutkrone.rpgcore.resourcepack.ResourcepackManager;
import me.blutkrone.rpgcore.resourcepack.utils.CompileClock;
import me.blutkrone.rpgcore.skill.SkillManager;
import me.blutkrone.rpgcore.skin.SkinPool;
import me.blutkrone.rpgcore.social.SocialManager;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class RPGCore extends JavaPlugin {

    static {
        Logger.getLogger("org.mongodb.driver").setLevel(Level.WARNING);
        Logger.getLogger("org.mongodb.driver.cluster").setFilter((log -> false));
        Logger.getLogger("org.mongodb.driver.protocol.event").setFilter((log -> false));
        Logger.getLogger("org.mongodb.driver.connection").setFilter((log -> false));
        Logger.getLogger("org.mongodb.driver.cluster").setFilter((log -> false));
        Logger.getLogger("org.mongodb.driver.connection.tls").setFilter((log -> false));
        Logger.getLogger("org.mongodb.driver.command").setFilter((log -> false));
        Logger.getLogger("org.mongodb.driver.management").setFilter((log -> false));
        Logger.getLogger("org.mongodb.driver.authenticator").setFilter((log -> false));
        Logger.getLogger("org.mongodb.driver.operation").setFilter((log -> false));
        Logger.getLogger("org.mongodb.driver.protocol.event").setFilter((log -> false));
        Logger.getLogger("org.mongodb.driver.client").setFilter((log -> false));
        Logger.getLogger("org.mongodb.driver.cluster.event").setFilter((log -> false));
    }

    // incremented by +1 each tick
    private int timestamp;
    // GSON implementation used by the core
    private Gson gson_pretty;
    private Gson gson_ugly;
    // delegate handlers for commands
    private Map<String, AbstractCommand> commands = new HashMap<>();
    // skin pool allowing us to use custom skins
    private SkinPool skin;

    // Managers providing low-level logic and functionality
    private MigrationHandler migration_manager;
    private AbstractVolatileManager volatile_manager;
    private AttributeManager attribute_manager;
    private EntityManager entity_manager;
    private HologramManager hologram_manager;
    private LanguageManager language_manager;
    private DataManager data_manager;
    private ResourcepackManager resourcepack_manager;
    private HUDManager hud_manager;
    private MinimapManagerV2 minimap_manager;
    private ItemManager item_manager;
    private DamageManager damage_manager;
    private JobManager job_manager;
    private EffectManager effect_manager;
    private NodeManager node_manager;
    private NPCManager npc_manager;
    private MailManager mail_manager;
    private SkillManager skill_manager;
    private QuestManager quest_manager;
    private MobManager mob_manager;
    private PassiveManager passive_manager;
    private SocialManager social_manager;
    private WorldIntegrationManager world_integration_manager;
    private ChatManager chat_manager;
    private BBModelManager bbmodel_manager;

    // Managers providing high-level functionality for the server
    private MountManager mount_manager;
    private IGuildHandler guild_manager;
    private LevelManager level_manager;
    private DungeonManager dungeon_manager;

    public static RPGCore inst() {
        return JavaPlugin.getPlugin(RPGCore.class);
    }

    @Override
    public void onEnable() {
        // perform migration scripts
        this.migration_manager = new MigrationHandler();

        // synchronize configuration
        ToolConfigPushCommand.pullConfig();
        // initialise demo world
        initDemoWorld();

        // provide the gson access for other managers to use
        this.gson_ugly = new Gson();
        this.gson_pretty = new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .registerTypeAdapter(IEditorBundle.class, new EditorBundleGsonAdapter<>())
                .create();
        // track the skin pool we work with
        this.skin = new SkinPool();

        // volatile code implementation handling non-api code
        this.volatile_manager = AbstractVolatileManager.create(this);
        if (this.volatile_manager == null)
            throw new InitializationException("Could not create 'Volatile Manager' for this version!");

        CompileClock clock = new CompileClock();
        
        // modules which make up the core
        this.attribute_manager = new AttributeManager();
        Bukkit.getLogger().info("Initialized 'rpgcore:%s' in %sms".formatted(AttributeManager.class.getSimpleName().toLowerCase(), clock.loop()));
        this.data_manager = new DataManager();
        Bukkit.getLogger().info("Initialized 'rpgcore:%s' in %sms".formatted(DataManager.class.getSimpleName().toLowerCase(), clock.loop()));
        this.entity_manager = new EntityManager();
        Bukkit.getLogger().info("Initialized 'rpgcore:%s' in %sms".formatted(EntityManager.class.getSimpleName().toLowerCase(), clock.loop()));
        this.language_manager = new LanguageManager();
        Bukkit.getLogger().info("Initialized 'rpgcore:%s' in %sms".formatted(LanguageManager.class.getSimpleName().toLowerCase(), clock.loop()));
        this.damage_manager = new DamageManager();
        Bukkit.getLogger().info("Initialized 'rpgcore:%s' in %sms".formatted(DamageManager.class.getSimpleName().toLowerCase(), clock.loop()));
        this.resourcepack_manager = new ResourcepackManager();
        Bukkit.getLogger().info("Initialized 'rpgcore:%s' in %sms".formatted(ResourcepackManager.class.getSimpleName().toLowerCase(), clock.loop()));
        this.job_manager = new JobManager();
        Bukkit.getLogger().info("Initialized 'rpgcore:%s' in %sms".formatted(JobManager.class.getSimpleName().toLowerCase(), clock.loop()));
        this.minimap_manager = new MinimapManagerV2();
        Bukkit.getLogger().info("Initialized 'rpgcore:%s' in %sms".formatted(MinimapManagerV2.class.getSimpleName().toLowerCase(), clock.loop()));
        this.item_manager = new ItemManager();
        Bukkit.getLogger().info("Initialized 'rpgcore:%s' in %sms".formatted(ItemManager.class.getSimpleName().toLowerCase(), clock.loop()));
        this.skill_manager = new SkillManager();
        Bukkit.getLogger().info("Initialized 'rpgcore:%s' in %sms".formatted(SkillManager.class.getSimpleName().toLowerCase(), clock.loop()));
        this.effect_manager = new EffectManager();
        Bukkit.getLogger().info("Initialized 'rpgcore:%s' in %sms".formatted(EffectManager.class.getSimpleName().toLowerCase(), clock.loop()));
        this.node_manager = new NodeManager();
        Bukkit.getLogger().info("Initialized 'rpgcore:%s' in %sms".formatted(NodeManager.class.getSimpleName().toLowerCase(), clock.loop()));
        this.npc_manager = new NPCManager();
        Bukkit.getLogger().info("Initialized 'rpgcore:%s' in %sms".formatted(NPCManager.class.getSimpleName().toLowerCase(), clock.loop()));
        this.mob_manager = new MobManager();
        Bukkit.getLogger().info("Initialized 'rpgcore:%s' in %sms".formatted(MobManager.class.getSimpleName().toLowerCase(), clock.loop()));
        this.quest_manager = new QuestManager();
        Bukkit.getLogger().info("Initialized 'rpgcore:%s' in %sms".formatted(QuestManager.class.getSimpleName().toLowerCase(), clock.loop()));
        this.passive_manager = new PassiveManager();
        Bukkit.getLogger().info("Initialized 'rpgcore:%s' in %sms".formatted(PassiveManager.class.getSimpleName().toLowerCase(), clock.loop()));
        this.level_manager = new LevelManager();
        Bukkit.getLogger().info("Initialized 'rpgcore:%s' in %sms".formatted(LevelManager.class.getSimpleName().toLowerCase(), clock.loop()));
        this.dungeon_manager = new DungeonManager();
        Bukkit.getLogger().info("Initialized 'rpgcore:%s' in %sms".formatted(DungeonManager.class.getSimpleName().toLowerCase(), clock.loop()));
        this.social_manager = new SocialManager();
        Bukkit.getLogger().info("Initialized 'rpgcore:%s' in %sms".formatted(SocialManager.class.getSimpleName().toLowerCase(), clock.loop()));
        this.mail_manager = new MailManager();
        Bukkit.getLogger().info("Initialized 'rpgcore:%s' in %sms".formatted(MailManager.class.getSimpleName().toLowerCase(), clock.loop()));
        this.hud_manager = new HUDManager();
        Bukkit.getLogger().info("Initialized 'rpgcore:%s' in %sms".formatted(HUDManager.class.getSimpleName().toLowerCase(), clock.loop()));
        this.hologram_manager = new HologramManager();
        Bukkit.getLogger().info("Initialized 'rpgcore:%s' in %sms".formatted(HologramManager.class.getSimpleName().toLowerCase(), clock.loop()));
        this.world_integration_manager = new WorldIntegrationManager();
        Bukkit.getLogger().info("Initialized 'rpgcore:%s' in %sms".formatted(WorldIntegrationManager.class.getSimpleName().toLowerCase(), clock.loop()));
        this.chat_manager = new ChatManager();
        Bukkit.getLogger().info("Initialized 'rpgcore:%s' in %sms".formatted(ChatManager.class.getSimpleName().toLowerCase(), clock.loop()));
        this.bbmodel_manager = new BBModelManager();
        Bukkit.getLogger().info("Initialized 'rpgcore:%s' in %sms".formatted(BBModelManager.class.getSimpleName().toLowerCase(), clock.loop()));

        // initialize relevant commands
        this.commands.put("mob", new SpawnMobCommand());
        this.commands.put("storage", new UnlockStorageCommand());
        this.commands.put("holoadd", new HologramAddCommand());
        this.commands.put("holodel", new HologramDeleteCommand());
        this.commands.put("edit", new EditorCommand());
        this.commands.put("box", new RandomItemBoxCommand());
        this.commands.put("compile", new ResourcepackCompileCommand());
        this.commands.put("url", new ResourcepackLinkCommand());
        this.commands.put("debug", new DebugCommand());
        this.commands.put("tool", new WorldToolCommand());
        this.commands.put("reload", new ReloadCommand());
        this.commands.put("help", new HelpCommand());
        this.commands.put("attribute", new TemporaryAttributeCommand());
        this.commands.put("refund", new RefundPointCommand());
        this.commands.put("passive", new PassivePointCommand());
        this.commands.put("tree", new ViewPassiveTreeCommand());
        this.commands.put("exp", new ExpCommand());
        this.commands.put("social", new SocialCommand());
        this.commands.put("dexit", new DungeonExitCommand());
        this.commands.put("internal", new InternalCommand());
        this.commands.put("dropbox", new DropboxCommand());

        this.commands.put("migrate", new ToolPlayerMigrateCommand());
        this.commands.put("push", new ToolConfigPushCommand());

        // task to update the timestamp
        Bukkit.getScheduler().runTaskTimer(this, () -> this.timestamp += 1, 1, 1);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (World world : Bukkit.getWorlds()) {
                world.setKeepSpawnInMemory(false);
            }
        }, 20, 20);
    }

    @Override
    public void onDisable() {
        // request all players to be saved
        for (Player player : Bukkit.getOnlinePlayers()) {
            CorePlayer core_player = getEntityManager().getPlayer(player);
            if (core_player != null) {
                RPGCore.inst().getEntityManager().unregister(core_player.getUniqueId());
            }
        }
        // make sure the data adapter finished
        getDataManager().flush();
        // close down message channel
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
    }

    /*
     * Initialisation for the demo world that RPGCore ships with.
     */
    private void initDemoWorld() {
        // copy our demo world if we got one
        File demo_want = FileUtil.directory("demo_world");
        File demo_have = new File(Bukkit.getWorldContainer().getAbsolutePath() + File.separator + "rpgcore_demo");
        if (demo_want.exists() && !demo_have.exists()) {
            try {
                FileUtils.copyDirectory(demo_want, demo_have);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // load demo world if it is registered
        if (demo_have.exists()) {
            World demo = WorldCreator.name("rpgcore_demo")
                    .type(WorldType.FLAT)
                    .generateStructures(false)
                    .createWorld();
            if (demo != null) {
                demo.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
                demo.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                demo.setGameRule(GameRule.DO_ENTITY_DROPS, false);
                demo.setGameRule(GameRule.DO_FIRE_TICK, false);
                demo.setGameRule(GameRule.DO_MOB_LOOT, false);
                demo.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                demo.setGameRule(GameRule.DO_TILE_DROPS, false);
                demo.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                demo.setGameRule(GameRule.KEEP_INVENTORY, true);
                demo.setGameRule(GameRule.MOB_GRIEFING, false);
                demo.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
                demo.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
                demo.setGameRule(GameRule.DISABLE_RAIDS, true);
                demo.setGameRule(GameRule.DO_INSOMNIA, false);
                demo.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, false);
                demo.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
                demo.setGameRule(GameRule.DO_TRADER_SPAWNING, false);

                demo.setKeepSpawnInMemory(true);
                demo.setSpawnLocation(0, 130, 0);

                RPGCore.inst().getLogger().info("RPGCore 'demo' world was created!");
            }
        }
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
     * <br>
     * Formats GSON in a pretty style, making it more readable.
     *
     * @return gson instance belonging to the plugin
     */
    public Gson getGsonPretty() {
        return gson_pretty;
    }

    /**
     * A gson implementation to provide an interface to json
     * logic.
     * <br>
     * Formats GSON in a ugly style, compressing file size.
     *
     * @return gson instance belonging to the plugin
     */
    public Gson getGsonUgly() {
        return gson_ugly;
    }

    /**
     * A mapping of all registered commands for the core.
     *
     * @return the mapping of registered commands.
     */
    public Map<String, AbstractCommand> getCommands() {
        return commands;
    }

    /**
     * Get a utility meant to handle skin processing.
     *
     * @return pool of all skins.
     */
    public SkinPool getSkinPool() {
        return skin;
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
        AbstractCommand cmd;
        if (args.length == 0) {
            cmd = commands.get("help");
        } else {
            cmd = commands.get(args[0]);
        }

        if (cmd != null) {
            if (cmd.canUseCommand(sender)) {
                try {
                    cmd.invoke(sender, args);
                } catch (CommandArgumentException e) {
                    sender.sendMessage("§c" + e.getMessage());
                }
            } else {
                String msg = RPGCore.inst().getLanguageManager().getTranslation("not_enough_permission");
                sender.sendMessage("§c" + msg);
            }
        } else {
            String msg = RPGCore.inst().getLanguageManager().getTranslation("unknown_command");
            sender.sendMessage("§c" + msg);
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

    public DamageManager getDamageManager() {
        return damage_manager;
    }

    public ResourcepackManager getResourcepackManager() {
        return resourcepack_manager;
    }

    public HUDManager getHUDManager() {
        return hud_manager;
    }

    public DataManager getDataManager() {
        return data_manager;
    }

    public MinimapManagerV2 getMinimapManager() {
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

    public QuestManager getQuestManager() {
        return quest_manager;
    }

    public MobManager getMobManager() {
        return mob_manager;
    }

    public PassiveManager getPassiveManager() {
        return passive_manager;
    }

    public MailManager getMailManager() {
        return mail_manager;
    }

    public LevelManager getLevelManager() {
        return level_manager;
    }

    public DungeonManager getDungeonManager() {
        return dungeon_manager;
    }

    public SocialManager getSocialManager() {
        return social_manager;
    }

    public WorldIntegrationManager getWorldIntegrationManager() {
        return world_integration_manager;
    }

    public ChatManager getChatManager() {
        return chat_manager;
    }

    public MigrationHandler getMigrationManager() {
        return migration_manager;
    }

    public BBModelManager getBBModelManager() {
        return bbmodel_manager;
    }
}
