package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class EquipMenu extends AbstractCoreMenu {
    private ItemStack invisible;
    private List<me.blutkrone.rpgcore.hud.menu.EquipMenu.Slot> slots;

    public EquipMenu(me.blutkrone.rpgcore.hud.menu.EquipMenu origin) {
        super(6);

        this.slots = origin.slots;
        this.invisible = language().getAsItem("invisible").build();
    }

    @Override
    public void rebuild() {
        throw new UnsupportedOperationException("You cannot rebuild an equipment menu!");
    }

    @Override
    public void click(InventoryClickEvent event) {
        if (event.getClick() == ClickType.LEFT) {
            if (event.getClickedInventory() == event.getView().getTopInventory()) {
                me.blutkrone.rpgcore.hud.menu.EquipMenu.Slot slot = this.slots.stream().filter(s -> s.slot == event.getSlot()).findAny().orElse(null);
                if (slot != null) {
                    if (slot.empty.isSimilar(event.getCurrentItem())) {
                        if (slot.isAccepted(event.getCursor(), getMenu().getViewer())) {
                            // equip the item into an empty slot
                            event.setCurrentItem(new ItemStack(Material.AIR));
                            Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                                applyIntermittentChanges(((Player) event.getWhoClicked()), false);
                            });
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
                            Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                                applyIntermittentChanges(((Player) event.getWhoClicked()), false);
                            });
                        } else if (!slot.isAccepted(event.getCursor(), getMenu().getViewer())) {
                            // it is not compatible with slot
                            event.setCancelled(true);
                        } else {
                            // swap with the equipped item
                            event.setCancelled(false);
                            Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                                applyIntermittentChanges(((Player) event.getWhoClicked()), false);
                            });
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
    }

    /*
     * An intermittent change applies all changes, except for modifying
     * the bukkit inventory.
     */
    private void applyIntermittentChanges(Player bukkit_player, boolean reflected) {
        // update equipment and reflect it
        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(bukkit_player);
        if (core_player == null) {
            return;
        }

        for (me.blutkrone.rpgcore.hud.menu.EquipMenu.Slot slot : this.slots) {
            if (!slot.empty.isSimilar(getMenu().getItemAt(slot.slot))) {
                core_player.setEquipped(slot.id, getMenu().getItemAt(slot.slot));
            } else {
                core_player.setEquipped(slot.id, null);
            }
        }

        // apply the changes that were made
        RPGCore.inst().getHUDManager().getEquipMenu().applyEquipChange(core_player, reflected);

        // flush all items in the inventory
        CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(bukkit_player);
        Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
            // flush from cursor
            RPGCore.inst().getItemManager().describe(bukkit_player.getItemOnCursor(), player);
            // flush from inventory
            for (ItemStack item : bukkit_player.getInventory().getContents()) {
                if (item == null || RPGCore.inst().getHUDManager().getEquipMenu().isReflected(item)) {
                    continue;
                }
                RPGCore.inst().getItemManager().describe(item, player);
            }
            bukkit_player.updateInventory();
        });
    }

    @Override
    public void open(InventoryOpenEvent event) {
        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());
        // populate with the appropriate slot items
        for (me.blutkrone.rpgcore.hud.menu.EquipMenu.Slot slot : this.slots) {
            ItemStack item = core_player.getEquipped(slot.id);
            if (item.getType().isAir()) {
                item = slot.empty;
            }
            getMenu().setItemAt(slot.slot, item);
        }

        // populate remaining slots with invisible item
        for (int i = 0; i < 6 * 9; i++) {
            ItemStack previous = getMenu().getItemAt(i);
            if (previous == null || previous.getType().isAir()) {
                getMenu().setItemAt(i, this.invisible);
            }
        }

        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-208);
        msb.append(resourcepack().texture("menu_equipment"), ChatColor.WHITE);

        InstructionBuilder instructions = new InstructionBuilder();
        instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_equip"));
        instructions.apply(msb);

        getMenu().setTitle(msb.compile());
    }

    @Override
    public void close(InventoryCloseEvent event) {
        applyIntermittentChanges((Player) event.getPlayer(), true);
    }

    @Override
    public boolean isTrivial() {
        return true;
    }
}
