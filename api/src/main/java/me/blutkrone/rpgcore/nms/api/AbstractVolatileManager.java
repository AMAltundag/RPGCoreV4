package me.blutkrone.rpgcore.nms.api;

import me.blutkrone.rpgcore.nms.api.entity.IEntityCollider;
import me.blutkrone.rpgcore.nms.api.entity.IEntityVisual;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.nms.api.menu.ITextInput;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * A uniform interface to access volatile code implementation, such as
 * nms code which isn't considered API or general functionality which
 * requires distinct implementations from other versions.
 * <p>
 * While minor deviations may exist across the implementation, there
 * should be a best-effort at maintaining consistent behaviour.
 */
public abstract class AbstractVolatileManager {

    // the plugin which requested the manager
    private final JavaPlugin plugin;

    /**
     * A manager dedicated to managing volatile code behaviour.
     *
     * @param plugin the plugin which requested the manager.
     */
    public AbstractVolatileManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates a volatile manager implementation for the current
     * server version, assuming that there is an available version
     * for us to retrieve.
     *
     * @return the manager for volatile code interaction
     */
    public static AbstractVolatileManager create(JavaPlugin plugin) {
        // retrieve the version the server is operating on
        String version = Bukkit.getServer().getClass().getPackage().getName();
        version = version.substring(version.lastIndexOf('.') + 1);
        // create a volatile manager for the requesting plugin
        try {
            Class<?> clazz = Class.forName("me.blutkrone.rpgcore.nms." + version + ".VolatileManager");
            Constructor<?> constructor = clazz.getConstructor(JavaPlugin.class);
            return (AbstractVolatileManager) constructor.newInstance(plugin);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * The plugin which created this volatile manager.
     *
     * @return plugin which created the volatile manager.
     */
    public JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * Create an entity for visual purposes.
     *
     * @param where where to spawn the entity at
     * @return the visual entity which was created
     */
    public abstract IEntityVisual createVisualEntity(Location where, boolean small);

    /**
     * Create an entity for collider purposes.
     *
     * @param owner initial sync target.
     * @return the collider entity which was created
     */
    public abstract IEntityCollider createCollider(Entity owner);

    /**
     * Update the title of a bossbar
     *
     * @param bar     the bar to update
     * @param message the message to write into the title
     */
    public abstract void updateBossBar(BossBar bar, BaseComponent[] message);

    /**
     * Update the title of a bossbar
     *
     * @param bar     the bar to update
     * @param message the message to write into the title
     */
    public void updateBossBar(BossBar bar, String message) {
        updateBossBar(bar, TextComponent.fromLegacyText(message));
    }

    /**
     * Create a chest menu, do not recycle.
     *
     * @param size the size of the menu (1-6)
     * @return the menu that was created
     */
    public abstract IChestMenu createMenu(int size, Player holder);

    /**
     * Create a text input, do not recycle.
     *
     * @param holder who holds the input.
     * @return the input that was created.
     */
    public abstract ITextInput createInput(Player holder);

    /**
     * Translate the given base component into a json-able string which
     * can be commonly used.
     *
     * @param raw the raw format to translate
     * @return a translated, usable, string.
     */
    public abstract String toChatString(BaseComponent[] raw);

    /**
     * Override the lore of the item with the given components.
     *
     * @param item the item to be updated.
     * @param lore the lines to write into the lore.
     */
    public abstract void setItemLore(ItemStack item, List<BaseComponent[]> lore);

    /**
     * Override the lore of the item with the given components.
     *
     * @param item the item to be updated.
     * @param name line to write into the name
     */
    public abstract void setItemName(ItemStack item, BaseComponent[] name);
}
