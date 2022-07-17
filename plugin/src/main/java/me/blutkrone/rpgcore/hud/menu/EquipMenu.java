package me.blutkrone.rpgcore.hud.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.item.data.ItemDataGeneric;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * A menu where the user can update their actual equipment, do note
 * that the core doesn't put "real" items on the vanilla slots.
 */
public class EquipMenu implements Listener {

    // dummy for invisible items
    private ItemStack invisible;
    // slots available on the menu
    private List<Slot> slots = new ArrayList<>();
    // placeholder while nothing equipped
    private ItemStack placeholder;

    /**
     * A menu where the user can update their actual equipment, do note
     * that the core doesn't put "real" items on the vanilla slots.
     */
    public EquipMenu() throws IOException {
        ConfigWrapper config = FileUtil.asConfigYML(FileUtil.file("menu", "equip.yml"));

        this.placeholder = RPGCore.inst().getLanguageManager().getAsItem("equip_placeholder")
                .persist("reflected-item", 1).build();
        this.invisible = RPGCore.inst().getLanguageManager().getAsItem("invisible").build();

        config.forEachUnder("slots", (path, root) -> {
            this.slots.add(new Slot(path, root.getSection(path)));
        });

        Bukkit.getPluginManager().registerEvents(this, RPGCore.inst());
    }

    /**
     * Open the menu for the given player.
     *
     * @param _player who to present the menu to.
     */
    public void open(Player _player) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        IChestMenu menu = RPGCore.inst().getVolatileManager().createMenu(6, _player);
        menu.setRebuilder((() -> {
            menu.clearItems();
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(menu.getViewer());
            // populate with the appropriate slot items
            for (Slot slot : this.slots) {
                ItemStack item = core_player.getEquipped(slot.id);
                if (item.getType().isAir()) {
                    item = slot.empty;
                }
                menu.setItemAt(slot.slot, item);
            }

            // populate remaining slots with invisible item
            for (int i = 0; i < 6 * 9; i++) {
                ItemStack previous = menu.getItemAt(i);
                if (previous == null || previous.getType().isAir()) {
                    menu.setItemAt(i, this.invisible);
                }
            }

            MagicStringBuilder msb = new MagicStringBuilder();
            msb.retreat(8);
            msb.append(rpm.texture("menu_equipment"), ChatColor.WHITE);

            InstructionBuilder instructions = new InstructionBuilder();
            instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_equip"));
            instructions.apply(msb);

            menu.setTitle(msb.compile());
        }));
        menu.setClickHandler((event -> {
            if (event.getClick() == ClickType.LEFT) {
                if (event.getClickedInventory() == event.getView().getTopInventory()) {
                    Slot slot = this.slots.stream().filter(s -> s.slot == event.getSlot()).findAny().orElse(null);
                    if (slot != null) {
                        if (slot.empty.isSimilar(event.getCurrentItem())) {
                            if (slot.isAccepted(event.getCursor(), menu.getViewer())) {
                                // equip the item into an empty slot
                                event.setCurrentItem(new ItemStack(Material.AIR));
                            } else {
                                // it is not compatible with slot
                                event.setCancelled(true);
                            }
                        } else {
                            if (event.getCursor() == null || event.getCursor().getType().isAir()) {
                                // we want to remove an equipped item
                                event.setCursor(event.getCurrentItem());
                                event.setCurrentItem(slot.empty);
                                event.setCancelled(true);
                            } else if (!slot.isAccepted(event.getCursor(), menu.getViewer())) {
                                // it is not compatible with slot
                                event.setCancelled(true);
                            } else {
                                // swap with the equipped item
                                event.setCancelled(false);
                            }
                        }
                    } else {
                        // prevent clicking on non-equip slots
                        event.setCancelled(true);
                    }
                } else {
                    // allow freely picking items to equip
                    event.setCancelled(false);
                }
            } else {
                event.setCancelled(true);
            }
        }));
        menu.setCloseHandler((event -> {
            // update equipment and reflect it
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(event.getPlayer());
            for (Slot slot : this.slots) {
                if (!slot.empty.isSimilar(menu.getItemAt(slot.slot))) {
                    core_player.setEquipped(slot.id, menu.getItemAt(slot.slot));
                } else {
                    core_player.setEquipped(slot.id, null);
                }
            }
            // recover the items on the player
            applyEquipChange(core_player);
        }));
        menu.open();
    }

    /**
     * Reflect the core equipment of the player.
     *
     * @param player whose items to reflect.
     */
    public void applyEquipChange(CorePlayer player) {
        // acquire the item as a vanilla variant
        Player entity = player.getEntity();
        for (Slot slot : this.slots) {
            // retrieve, non-null, item from slot
            ItemStack equipped = player.getEquipped(slot.id);
            // prepare the reflected equivalent
            if (equipped.getType().isAir()) {
                equipped = this.placeholder;
            } else {
                equipped = reflect(equipped);
            }
            // write item to player slots
            slot.target.setSlotMethod.accept(entity, equipped);
        }

        // make the entity recompute their stats
        player.applyEquipment();
    }

    /**
     * Create a reflected copy of the given item, the reflected
     * copy looks like the real thing but is just strictly used
     * for visual purposes.
     *
     * @param item the item to create a reflection of
     * @return a reflection item.
     */
    public ItemStack reflect(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return ItemBuilder.of(Material.BARRIER).name("CANNOT REFLECT ITEM").build();
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return ItemBuilder.of(Material.BARRIER).name("CANNOT REFLECT ITEM").build();
        }

        // exact copy, but without backing data
        ItemStack copy = item.clone();
        ItemMeta dupe = copy.getItemMeta();
        PersistentDataContainer data = dupe.getPersistentDataContainer();
        Set<NamespacedKey> keys = new HashSet<>(data.getKeys());
        keys.forEach(data::remove);
        data.set(new NamespacedKey(RPGCore.inst(), "reflected-item"), PersistentDataType.INTEGER, 1);
        dupe.setUnbreakable(true);
        copy.setItemMeta(dupe);

        return copy;
    }

    /**
     * Check if the clicked item is a reflected item, i.E: A snapshot
     * that is copied into the equipment slot of a player.
     *
     * @param item the item to verify
     * @return true if the item is reflected
     */
    public boolean isReflected(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
        int reflected = data.getOrDefault(new NamespacedKey(RPGCore.inst(), "reflected-item"), PersistentDataType.INTEGER, 0);
        return reflected == 1;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    void onEquipmentGameModeSwap(PlayerGameModeChangeEvent e) {
        Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
            GameMode mode = e.getPlayer().getGameMode();
            if (mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR) {
                // hide reflected items in creative mode
                for (BukkitSlot slot : BukkitSlot.values()) {
                    slot.setSlotMethod.accept(e.getPlayer(), new ItemStack(Material.AIR));
                }
            } else {
                // recover reflected items when exiting creative
                applyEquipChange(RPGCore.inst().getEntityManager().getPlayer(e.getPlayer()));
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    void onNeverSwapHeldItems(PlayerSwapHandItemsEvent e) {
        // reject interaction with protected HUD items
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    void onNeverClickReflectEquip(InventoryClickEvent e) {
        // reject interaction with protected HUD items
        ItemStack item = e.getCurrentItem();
        if (placeholder.isSimilar(item) || isReflected(item)) {
            e.setCancelled(true);
        }
        // reject number swapping
        if (e.getClick() == ClickType.NUMBER_KEY && e.getHotbarButton() == 0) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    void onNeverDragReflectEquip(InventoryDragEvent e) {
        // reject interaction with protected items
        for (ItemStack item : e.getNewItems().values()) {
            if (placeholder.isSimilar(item) || isReflected(item)) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    void onNeverDropReflectEquip(PlayerDropItemEvent e) {
        // reject interaction with protected items
        ItemStack item = e.getItemDrop().getItemStack();
        if (placeholder.isSimilar(item) || isReflected(item)) {
            e.setCancelled(true);
        }
    }

    /*
     * A convenient wrapper to easily place items into
     * whatever vanilla slot was given.
     */
    enum BukkitSlot {
        MAIN_HAND((p, i) -> p.getInventory().setItem(0, i)),
        OFF_HAND((p, i) -> p.getInventory().setItemInOffHand(i)),
        HELMET((p, i) -> p.getInventory().setHelmet(i)),
        CHEST((p, i) -> p.getInventory().setChestplate(i)),
        PANTS((p, i) -> p.getInventory().setLeggings(i)),
        BOOTS((p, i) -> p.getInventory().setBoots(i)),
        NONE((p, i) -> {
        });

        // functional interface to place an item on a player
        final BiConsumer<Player, ItemStack> setSlotMethod;

        /*
         * A convenient wrapper to easily place items into
         * whatever vanilla slot was given.
         *
         * @param setSlotMethod method to place item on a player
         */
        BukkitSlot(BiConsumer<Player, ItemStack> setSlotMethod) {
            this.setSlotMethod = setSlotMethod;
        }
    }

    /*
     * A slot where-in a player can equip an item.
     */
    class Slot {
        // identifier for the slot
        String id;
        // item used while slot is empty
        ItemStack empty;
        // position in the menu design
        int slot;
        // vanilla slot to put item into
        BukkitSlot target;
        // tags on the item to equip
        Set<String> accept;

        /*
         * A slot where-in a player can equip an item.
         *
         * @param config data to be placed on player.
         */
        Slot(String id, ConfigWrapper config) {
            this.id = id;
            this.empty = RPGCore.inst().getLanguageManager().getAsItem(config.getString("empty")).build();
            this.slot = config.getInt("slot");
            this.target = BukkitSlot.valueOf(config.getString("target", "NONE").toUpperCase());
            this.accept = new HashSet<>(config.getStringList("accept"));
        }

        /*
         * Check if the item is compatible with this slot.
         *
         * @param item which item to check
         * @return whether item is accepted by this slot
         */
        boolean isAccepted(ItemStack item, Player player) {
            // retrieve type and check for compatibility
            ItemDataGeneric item_data = RPGCore.inst().getItemManager().getItemData(item, ItemDataGeneric.class);
            // if this data does not exist, it is not a rpgcore item
            if (item_data == null) {
                return false;
            }
            // if we are bound to another player, do not use the item
            if (!item_data.canUseBound(player)) {
                return false;
            }
            // make sure the equipment slot is compatible
            for (String slot : item_data.getItem().getEquipmentSlot()) {
                if (this.accept.contains(slot)) {
                    return true;
                }
            }
            // if nothing fits, we cannot be equipped
            return false;
        }
    }
}
