package me.blutkrone.rpgcore.util.io;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class ConfigWrapper {

    private final ConfigurationSection handle;

    public ConfigWrapper(ConfigurationSection handle) {
        this.handle = handle;
    }

    public ConfigurationSection getHandle() {
        return handle;
    }

    public void forEachKey(Consumer<String> consumer) {
        for (String key : getKeys(false)) {
            consumer.accept(key);
        }
    }

    public void forEachWithSelf(BiConsumer<String, ConfigWrapper> consumer) {
        for (String key : getKeys(false)) {
            consumer.accept(key, this);
        }
    }

    public void forEachUnder(String path, BiConsumer<String, ConfigWrapper> consumer) {
        ConfigWrapper section = getSection(path);
        if (section == null) return;
        section.forEachWithSelf(consumer);
    }

    public Set<String> getKeys(boolean deep) {
        return getHandle().getKeys(deep);
    }

    public boolean contains(@NotNull String path) {
        return getHandle().contains(path);
    }

    public boolean isSet(@NotNull String path) {
        return getHandle().isSet(path);
    }

    public String getCurrentPath() {
        return getHandle().getCurrentPath();
    }

    public void set(@NotNull String path, @Nullable Object value) {
        getHandle().set(path, value);
    }

    public ConfigWrapper createSection(@NotNull String path) {
        return new ConfigWrapper(getHandle().createSection(path));
    }

    public ConfigWrapper createSection(@NotNull String path, @NotNull Map<?, ?> map) {
        return new ConfigWrapper(getHandle().createSection(path, map));
    }

    public String getString(@NotNull String path) {
        return getHandle().getString(path);
    }

    public String getString(@NotNull String path, @Nullable String def) {
        return getHandle().getString(path, def);
    }

    public boolean isString(@NotNull String path) {
        return getHandle().isString(path);
    }

    public int getInt(@NotNull String path) {
        return getHandle().getInt(path);
    }

    public int getInt(@NotNull String path, int def) {
        return getHandle().getInt(path, def);
    }

    public boolean isInt(@NotNull String path) {
        return getHandle().isInt(path);
    }

    public boolean getBoolean(@NotNull String path) {
        return getHandle().getBoolean(path);
    }

    public boolean getBoolean(@NotNull String path, boolean def) {
        return getHandle().getBoolean(path, def);
    }

    public boolean isBoolean(@NotNull String path) {
        return getHandle().isBoolean(path);
    }

    public double getDouble(@NotNull String path) {
        return getHandle().getDouble(path);
    }

    public double getDouble(@NotNull String path, double def) {
        return getHandle().getDouble(path, def);
    }

    public boolean isDouble(@NotNull String path) {
        return getHandle().isDouble(path);
    }

    public long getLong(@NotNull String path) {
        return getHandle().getLong(path);
    }

    public long getLong(@NotNull String path, long def) {
        return getHandle().getLong(path, def);
    }

    public boolean isLong(@NotNull String path) {
        return getHandle().isLong(path);
    }

    public boolean isList(@NotNull String path) {
        return getHandle().isList(path);
    }

    public List<String> getStringList(@NotNull String path) {
        return getHandle().getStringList(path);
    }

    public List<Integer> getIntegerList(@NotNull String path) {
        return getHandle().getIntegerList(path);
    }

    public List<Boolean> getBooleanList(@NotNull String path) {
        return getHandle().getBooleanList(path);
    }

    public List<Double> getDoubleList(@NotNull String path) {
        return getHandle().getDoubleList(path);
    }

    public List<Float> getFloatList(@NotNull String path) {
        return getHandle().getFloatList(path);
    }

    public List<Long> getLongList(@NotNull String path) {
        return getHandle().getLongList(path);
    }

    public List<Byte> getByteList(@NotNull String path) {
        return getHandle().getByteList(path);
    }

    public List<Character> getCharacterList(@NotNull String path) {
        return getHandle().getCharacterList(path);
    }

    public List<Short> getShortList(@NotNull String path) {
        return getHandle().getShortList(path);
    }

    @Deprecated
    public boolean isVector(@NotNull String path) {
        return getHandle().isVector(path);
    }

    @Deprecated
    public boolean isItemStack(@NotNull String path) {
        return getHandle().isItemStack(path);
    }

    @Deprecated
    public Color getColor(@NotNull String path) {
        return getHandle().getColor(path);
    }

    @Deprecated
    public Color getColor(@NotNull String path, @Nullable Color def) {
        return getHandle().getColor(path, def);
    }

    @Deprecated
    public boolean isColor(@NotNull String path) {
        return getHandle().isColor(path);
    }

    public Vector getVector(@NotNull String path) {
        if (!isSet(path))
            return null;
        double x = getDouble(path + ".x");
        double y = getDouble(path + ".y");
        double z = getDouble(path + ".z");
        return new Vector(x, y, z);
    }

    public Vector getVector(@NotNull String path, @Nullable Vector def) {
        Vector have = getVector(path);
        if (have == null) have = def;
        return have;
    }

    public Location getLocation(@NotNull String path) {
        if (!isSet(path))
            return null;
        World w = Bukkit.getWorld(getString(path + ".world", "undefined"));
        double x = getDouble(path + ".x");
        double y = getDouble(path + ".y");
        double z = getDouble(path + ".z");
        float pitch = (float) getDouble(path + ".pitch");
        float yaw = (float) getDouble(path + ".yaw");
        return new Location(w, x, y, z, yaw, pitch);
    }

    public Location getLocation(@NotNull String path, @Nullable Location def) {
        Location have = getLocation(path);
        if (have == null) have = def;
        return have;
    }

    @Deprecated
    public boolean isLocation(@NotNull String path) {
        return getHandle().isLocation(path);
    }

    public ConfigWrapper getSection(@NotNull String path) {
        ConfigurationSection section = getHandle().getConfigurationSection(path);
        return section == null ? null : new ConfigWrapper(section);
    }

    public boolean isSection(@NotNull String path) {
        return getHandle().isConfigurationSection(path);
    }
}
