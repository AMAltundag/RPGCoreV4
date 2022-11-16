package me.blutkrone.rpgcore.util;

import me.blutkrone.rpgcore.RPGCore;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ItemBuilder {

    private ItemStack stack;
    private ItemMeta meta;

    private ItemBuilder(ItemStack stack) {
        this.stack = stack;
        this.meta = stack.getItemMeta();
        this.flag(ItemFlag.values());
    }

    public static ItemBuilder of(ItemStack stack) {
        return new ItemBuilder(stack);
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(new ItemStack(material));
    }

    public static ItemBuilder of(String serialized) {
        String[] split = serialized.split(":");
        Material material = Material.valueOf(split[0].toUpperCase());
        if (split.length == 1) {
            return new ItemBuilder(new ItemStack(material));
        } else {
            return new ItemBuilder(new ItemStack(material))
                    .model(Integer.parseInt(split[1]));
        }
    }

    public static ItemBuilder material(Material material) {
        return new ItemBuilder(new ItemStack(material));
    }

    private static String translate(String ln) {
        return ChatColor.translateAlternateColorCodes('&', ln);
    }

    private static List<String> translate(List<String> lns) {
        if (lns == null) lns = new ArrayList<>();
        List<String> out = new ArrayList<>();
        lns.forEach(ln -> out.add(translate(ln)));
        return out;
    }

    public ItemBuilder inheritIcon(ItemStack original) {
        return type(original.getType())
                .model(original)
                .meta(m -> m.setUnbreakable(original.getItemMeta().isUnbreakable()));
    }

    public ItemBuilder type(Material material) {
        stack.setItemMeta(meta);
        this.stack.setType(material);
        meta = this.stack.getItemMeta();
        return this;
    }

    public ItemBuilder model(int model) {
        return meta((m -> m.setCustomModelData(model)));
    }

    public ItemBuilder model(ItemStack model) {
        if (model == null || !model.hasItemMeta()) return this;
        ItemMeta meta = model.getItemMeta();
        if (meta == null) return this;
        if (!meta.hasCustomModelData()) return this;
        return this.meta(m -> m.setCustomModelData(meta.getCustomModelData()));
    }

    public ItemBuilder persist(String path, String value) {
        if (path.contains(".")) {
            throw new IllegalArgumentException("Persistent data cannot resolve '" + path + "' effectively!");
        }
        return meta((meta -> meta.getPersistentDataContainer()
                .set(new NamespacedKey(RPGCore.inst(), path), PersistentDataType.STRING, value)));
    }

    public ItemBuilder persist(String path, Double value) {
        if (path.contains(".")) {
            throw new IllegalArgumentException("Persistent data cannot resolve '" + path + "' effectively!");
        }
        return meta((meta -> meta.getPersistentDataContainer()
                .set(new NamespacedKey(RPGCore.inst(), path), PersistentDataType.DOUBLE, value)));
    }

    public ItemBuilder persist(String path, Integer value) {
        if (path.contains(".")) {
            throw new IllegalArgumentException("Persistent data cannot resolve '" + path + "' effectively!");
        }

        return meta((meta -> meta.getPersistentDataContainer()
                .set(new NamespacedKey(RPGCore.inst(), path), PersistentDataType.INTEGER, value)));
    }

    public ItemBuilder name(String arg0) {
        return meta(meta -> meta.setDisplayName(ItemBuilder.translate(arg0)));
    }

    public ItemBuilder leatherColor(Color arg0) {
        return meta(meta -> ((LeatherArmorMeta) meta).setColor(arg0));
    }

    public ItemBuilder potionColor(Color arg0) {
        return meta(meta -> ((PotionMeta) meta).setColor(arg0));
    }

    public ItemBuilder lore(String... arg0) {
        for (int i = 0; i < arg0.length; i++)
            arg0[i] = ItemBuilder.translate(arg0[i]);
        return meta(meta -> meta.setLore(Arrays.asList(arg0)));
    }

    public ItemBuilder lore(List<String> arg0) {
        return meta(meta -> meta.setLore(ItemBuilder.translate(arg0)));
    }

    public ItemBuilder prependLore(List<String> arg0) {
        return meta(meta -> {
            List<String> previous = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            List<String> updated = new ArrayList<>();
            updated.addAll(ItemBuilder.translate(arg0));
            updated.addAll(previous);
            meta.setLore(updated);
        });
    }

    public ItemBuilder prependLore(String... arg0) {
        return meta(meta -> {
            List<String> previous = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            List<String> updated = new ArrayList<>();
            updated.addAll(ItemBuilder.translate(Arrays.asList(arg0)));
            updated.addAll(previous);
            meta.setLore(updated);
        });
    }

    public ItemBuilder appendLore(List<String> arg0) {
        return meta(meta -> {
            List<String> previous = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            List<String> updated = new ArrayList<>();
            updated.addAll(previous);
            updated.addAll(ItemBuilder.translate(arg0));
            meta.setLore(updated);
        });
    }

    public ItemBuilder appendLore(String... arg0) {
        for (int i = 0; i < arg0.length; i++)
            arg0[i] = ItemBuilder.translate(arg0[i]);
        return meta(meta -> {
            List<String> previous = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            List<String> updated = new ArrayList<>();
            updated.addAll(previous);
            updated.addAll(Arrays.asList(arg0));
            meta.setLore(updated);
        });
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        return item(item -> item.addUnsafeEnchantment(enchantment, level));
    }

    public ItemBuilder flag(ItemFlag... flags) {
        return meta(meta -> meta.addItemFlags(flags));
    }

    public ItemBuilder meta(Consumer<ItemMeta> operation) {
        operation.accept(this.meta);
        return this;
    }

    public ItemBuilder item(Consumer<ItemStack> operation) {
        this.stack.setItemMeta(this.meta);
        operation.accept(this.stack);
        this.meta = this.stack.getItemMeta();
        return this;
    }

    public ItemBuilder color(int rgb) {
        try {
            Color color = Color.fromRGB(rgb);

            meta((meta -> {
                if (meta instanceof LeatherArmorMeta) {
                    ((LeatherArmorMeta) meta).setColor(color);
                } else if (meta instanceof PotionMeta) {
                    ((PotionMeta) meta).setColor(color);
                }
            }));
        } catch (Exception ignored) {
        }

        return this;
    }

    public ItemStack build() {
        this.stack.setItemMeta(this.meta);
        return this.stack;
    }

    public void inventory(Inventory inventory, int slot) {
        this.stack.setItemMeta(this.meta);
        inventory.setItem(slot, this.stack);
    }
}
