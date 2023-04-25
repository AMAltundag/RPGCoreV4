package me.blutkrone.rpgcore.api.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired during initialisation, if not externally populated
 * will be populated by the default RPGCore implementation.
 */
public class CoreInitializationEvent<K> extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Class<K> clazz;
    private K custom;

    public CoreInitializationEvent(Class<K> clazz) {
        this.clazz = clazz;
    }

    /**
     * Fire this event, looking for an override of the given class.
     *
     * @param clazz what class are we looking for
     * @return the injected instance, may be null.
     */
    public static <V> V find(Class<V> clazz) {
        CoreInitializationEvent<V> event = new CoreInitializationEvent<>(clazz);
        Bukkit.getPluginManager().callEvent(event);
        return event.getCustom();
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * What are we looking for.
     *
     * @return what we are looking for.
     */
    public Class<K> getClazz() {
        return clazz;
    }

    /**
     * A custom implementation of the class we are looking
     * for, if nobody does this RPGCore will handle it.
     *
     * @param custom custom implementation
     */
    public void inject(K custom) {
        if (this.custom == null) {
            this.custom = custom;
        } else {
            Bukkit.getLogger().severe("Another plugin already injected '" + this.clazz.getSimpleName() + "' before you!");
        }
    }

    /**
     * A custom implementation to be used.
     *
     * @return the custom implementation to be used.
     */
    public K getCustom() {
        return custom;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
