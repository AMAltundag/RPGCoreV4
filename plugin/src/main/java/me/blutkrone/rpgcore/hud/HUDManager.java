package me.blutkrone.rpgcore.hud;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.hud.ISidebarProvider;
import me.blutkrone.rpgcore.api.hud.IUXComponent;
import me.blutkrone.rpgcore.editor.EditorMenu;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.menu.*;
import me.blutkrone.rpgcore.hud.ux.*;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HUDManager implements Listener {
    // components drawn to the interface
    private List<IUXComponent<?>> components = new ArrayList<>();
    // cache for bossbar tracking
    private Map<UUID, BossBar> bossbar_tracker = new ConcurrentHashMap<>();
    // components which are referenced for certain purpose
    private SidebarComponent sidebar_component;
    private NotificationComponent notification_component;
    // menus used for specific purposes
    private RosterMenu roster_menu;
    private SettingsMenu settings_menu;
    private SkillMenu skill_menu;
    private EquipMenu equip_menu;
    private EditorMenu editor_menu;
    private JewelMenu jewel_menu;
    private RefinerMenu refiner_menu;
    private CrafterMenu crafter_menu;
    private VendorMenu vendor_menu;
    private DialogueMenu dialogue_menu;
    private QuestMenu quest_menu;
    private PlayerMenu player_menu;
    private SocialMenu social_menu;
    private StatusMenu status_menu;

    public HUDManager() {
        // load the appropriate UX components
        try {
            ConfigWrapper config = FileUtil.asConfigYML(FileUtil.file("menu", "ux.yml"));
            this.components.add(new NavigatorComponent(config));
            this.components.add(new FocusComponent(config));
            this.components.add(new MainPlateComponent(config));
            this.components.add(this.sidebar_component = new SidebarComponent(config));
            this.components.add(this.notification_component = new NotificationComponent(config));
            this.components.add(new StatusComponent(config));
            this.components.add(new PartyComponent(config));

            this.roster_menu = new RosterMenu();
            this.settings_menu = new SettingsMenu();
            this.skill_menu = new SkillMenu();
            this.equip_menu = new EquipMenu();
            this.editor_menu = new EditorMenu();
            this.jewel_menu = new JewelMenu();
            this.refiner_menu = new RefinerMenu();
            this.crafter_menu = new CrafterMenu();
            this.vendor_menu = new VendorMenu();
            this.dialogue_menu = new DialogueMenu();
            this.quest_menu = new QuestMenu();
            this.player_menu = new PlayerMenu();
            this.social_menu = new SocialMenu();
            this.status_menu = new StatusMenu();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // ticker to keep the interface contents updated
        Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
            for (Player bukkit : Bukkit.getOnlinePlayers()) {
                CorePlayer core = RPGCore.inst().getEntityManager().getPlayer(bukkit);
                if (core == null) continue;
                // retrieve or create the bossbar unit
                BossBar active_bossbar = bossbar_tracker.computeIfAbsent(bukkit.getUniqueId(), (k -> {
                    BossBar created = Bukkit.createBossBar("x", BarColor.YELLOW, BarStyle.SOLID);
                    created.setVisible(true);
                    created.addPlayer(bukkit);
                    created.setProgress(1d);
                    return created;
                }));
                // players in creative mode are unaffected by the core
                if (bukkit.getGameMode() == GameMode.CREATIVE || bukkit.getGameMode() == GameMode.SPECTATOR) {
                    RPGCore.inst().getVolatileManager().updateBossBar(active_bossbar, "You are unaffected by the core while in creative mode!");
                    continue;
                }
                // players about to die should only see that
                final int grave_timer = core.getGraveCounter();
                if (grave_timer > 0) {
                    // keep the bossbar inactive
                    RPGCore.inst().getVolatileManager().updateBossBar(active_bossbar, String.format("§fYou will die in %s seconds!", grave_timer / 20));
                    continue;
                }

                // prepare the info components we work with
                Queue<Object> prepared = new LinkedList<>();
                for (IUXComponent<?> component : this.components)
                    prepared.add(component.prepare(core, bukkit));

                // off-load heavy lifting tasks to another thread
                Bukkit.getScheduler().runTaskAsynchronously(RPGCore.inst(), () -> {
                    ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
                    // construct the data which is to be rendered
                    UXWorkspace workspace = new UXWorkspace();
                    workspace.bossbar().setFinalLength(core.getSettings().screen_width);
                    workspace.actionbar().setFinalLength(rpm.texture("static_plate_back").width);
                    for (IUXComponent component : this.components)
                        component.populate(core, bukkit, workspace, prepared.poll());
                    // have the client render the generated data
                    bukkit.spigot().sendMessage(ChatMessageType.ACTION_BAR, workspace.actionbar().compile());
                    RPGCore.inst().getVolatileManager().updateBossBar(active_bossbar, workspace.bossbar().compile());
                });
            }
        }, 1, 1);

        // events for UX, primarily maintaining bossbar logic
        Bukkit.getPluginManager().registerEvents(this, RPGCore.inst());
    }

    public void reload() {
        try {
            this.getStatusMenu().reload();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlayerMenu getPlayerMenu() {
        return player_menu;
    }

    public SkillMenu getSkillMenu() {
        return skill_menu;
    }

    public RosterMenu getRosterMenu() {
        return roster_menu;
    }

    public SettingsMenu getSettingsMenu() {
        return settings_menu;
    }

    public EquipMenu getEquipMenu() {
        return equip_menu;
    }

    public EditorMenu getEditorMenu() {
        return editor_menu;
    }

    public JewelMenu getJewelMenu() {
        return jewel_menu;
    }

    public RefinerMenu getRefinerMenu() {
        return refiner_menu;
    }

    public CrafterMenu getCrafterMenu() {
        return crafter_menu;
    }

    public VendorMenu getVendorMenu() {
        return vendor_menu;
    }

    public DialogueMenu getDialogueMenu() {
        return dialogue_menu;
    }

    public QuestMenu getQuestMenu() {
        return quest_menu;
    }

    public SocialMenu getSocialMenu() {
        return social_menu;
    }

    public StatusMenu getStatusMenu() {
        return status_menu;
    }

    /**
     * Register a sidebar provider, which will allow us to write
     * text to the sidebar.
     */
    public void addSidebar(ISidebarProvider provider) {
        this.sidebar_component.addProvider(provider);
    }

    /**
     * Show a pop-up notification to the player.
     *
     * @param player  who receives the notification
     * @param message the notification to see.
     */
    public void notify(Player player, String message) {
        this.notification_component.notify(player, message);
    }

    /**
     * Show a pop-up notification to the player.
     *
     * @param player  who receives the notification
     * @param message the notification to see.
     */
    public void notify(Player player, String message, net.md_5.bungee.api.ChatColor color) {
        this.notification_component.notify(player, message, color);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onBossbarCleanUpQuit(PlayerQuitEvent e) {
        BossBar bar = bossbar_tracker.remove(e.getPlayer().getUniqueId());
        if (bar != null) bar.removeAll();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onBossbarCleanUpDeath(PlayerDeathEvent e) {
        BossBar bar = bossbar_tracker.remove(e.getEntity().getUniqueId());
        if (bar != null) bar.removeAll();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onBossbarCleanUpWorld(PlayerChangedWorldEvent e) {
        BossBar bar = bossbar_tracker.remove(e.getPlayer().getUniqueId());
        if (bar != null) bar.removeAll();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onBossbarCleanUpWorld(PlayerGameModeChangeEvent e) {
        e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onWantOpenPlayerMenu(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) {
            return;
        }
        if (e.getClickedInventory().getType() != InventoryType.CRAFTING) {
            return;
        }
        e.setCancelled(true);
        getPlayerMenu().present((Player) e.getWhoClicked());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onWantOpenPlayerMenu(InventoryDragEvent e) {
        if (e.getView().getType() != InventoryType.CRAFTING) {
            return;
        }
        for (int i = 0; i < 5; i++) {
            if (e.getRawSlots().contains(i)) {
                e.setCancelled(true);
                getPlayerMenu().present((Player) e.getWhoClicked());
                return;
            }
        }
    }
}
