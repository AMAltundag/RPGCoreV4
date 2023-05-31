package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.index.IndexAttachment;
import me.blutkrone.rpgcore.hud.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.item.data.ItemDataGeneric;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VendorMenu extends AbstractCoreMenu {

    private IndexAttachment<CoreItem, Map<String, List<CoreItem>>> bank_to_item;
    private List<ItemStack> preview;
    private int offset;

    public VendorMenu(me.blutkrone.rpgcore.hud.menu.VendorMenu origin, List<ItemStack> preview) {
        super(6);

        this.bank_to_item = origin.bank_to_item;
        this.preview = preview;
        this.offset = 0;
    }

    @Override
    public void rebuild() {
        getMenu().clearItems();
        Map<String, Integer> carrying = new HashMap<>();
        for (ItemStack item : getMenu().getViewer().getInventory().getContents()) {
            ItemDataGeneric data = RPGCore.inst().getItemManager().getItemData(item, ItemDataGeneric.class);
            if (data != null) {
                String group = data.getItem().getBankGroup();
                int value = item.getAmount() * data.getItem().getBankQuantity();
                carrying.merge(group, value, (a, b) -> a + b);
            }
        }

        // base texture
        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-208);
        msb.append(resourcepack().texture("menu_scroller_dual"), ChatColor.WHITE);

        for (int i = 0; i < 6; i++) {
            ItemStack icon = preview.size() > (offset + i * 2) ? preview.get(offset + i * 2) : null;
            if (icon != null) {
                String currency = IChestMenu.getBrand(icon, RPGCore.inst(), "vendor-currency", "");
                int price = Integer.parseInt(IChestMenu.getBrand(icon, RPGCore.inst(), "vendor-price", "0"));
                int have = carrying.getOrDefault(currency, 0);

                String symbol = "default";
                List<CoreItem> denominations = bank_to_item.get().get(currency);
                if (denominations != null && !denominations.isEmpty()) {
                    CoreItem highest = null;
                    for (CoreItem denomination : denominations) {
                        if (denomination.getBankQuantity() <= price) {
                            if (highest == null || highest.getBankQuantity() < denomination.getBankQuantity()) {
                                highest = denomination;
                            }
                        }
                    }
                    if (highest != null) {
                        symbol = highest.getBankSymbol();
                    }
                }

                getMenu().setItemAt(i * 9, icon);
                msb.shiftToExact(20);
                msb.append(language().formatShortNumber(price), "scroller_text_" + (i + 1), (price < have) ? ChatColor.GREEN : ChatColor.RED);
                msb.advance(3).append(resourcepack().texture("currency_" + symbol + "_menu_" + (i + 1)), ChatColor.WHITE);
            }
        }
        for (int i = 0; i < 6; i++) {
            ItemStack icon = preview.size() > (offset + i * 2 + 1) ? preview.get(offset + i * 2 + 1) : null;
            if (icon != null) {
                String currency = IChestMenu.getBrand(icon, RPGCore.inst(), "vendor-currency", "");
                int price = Integer.parseInt(IChestMenu.getBrand(icon, RPGCore.inst(), "vendor-price", "0"));
                int have = carrying.getOrDefault(currency, 0);

                String symbol = "default";
                List<CoreItem> denominations = bank_to_item.get().get(currency);
                if (denominations != null && !denominations.isEmpty()) {
                    CoreItem highest = null;
                    for (CoreItem denomination : denominations) {
                        if (denomination.getBankQuantity() <= price) {
                            if (highest == null || highest.getBankQuantity() < denomination.getBankQuantity()) {
                                highest = denomination;
                            }
                        }
                    }
                    if (highest != null) {
                        symbol = highest.getBankSymbol();
                    }
                }

                getMenu().setItemAt(i * 9 + 4, icon);
                msb.shiftToExact(92);
                msb.append(language().formatShortNumber(price), "scroller_text_" + (i + 1), (price < have) ? ChatColor.GREEN : ChatColor.RED);
                msb.advance(3).append(resourcepack().texture("currency_" + symbol + "_menu_" + (i + 1)), ChatColor.WHITE);
            }
        }

        // render scroll-bar for the viewport
        msb.shiftToExact(150);
        if (preview.size() <= 12) {
            msb.append(resourcepack().texture("pointer_huge_0"), ChatColor.WHITE);
        } else if (preview.size() <= 36) {
            double length = Math.ceil(preview.size() / 8d) - 6d;
            double ratio = offset / length;

            msb.append(resourcepack().texture("pointer_medium_" + (int) (100 * ratio)), ChatColor.WHITE);
        } else if (preview.size() <= 64) {
            double length = Math.ceil(preview.size() / 8d) - 6d;
            double ratio = offset / length;

            msb.append(resourcepack().texture("pointer_small_" + (int) (100 * ratio)), ChatColor.WHITE);
        } else {
            double length = Math.ceil(preview.size() / 8d) - 6d;
            double ratio = offset / length;

            msb.append(resourcepack().texture("pointer_tiny_" + (int) (100 * ratio)), ChatColor.WHITE);
        }

        InstructionBuilder instructions = new InstructionBuilder();
        instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_vendor"));
        instructions.apply(msb);

        getMenu().setTitle(msb.compile());
    }

    @Override
    public void click(InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getView().getTopInventory() == event.getClickedInventory()) {
            if (event.getSlot() == 8) {
                // scroll up by one
                offset = Math.max(0, offset - 1);
                getMenu().queryRebuild();
            } else if (event.getSlot() == 17) {
                // scroll to top
                offset = 0;
                getMenu().queryRebuild();
            } else if (event.getSlot() == 26) {
                // ignore other clicks
            } else if (event.getSlot() == 35) {
                // ignore other clicks
            } else if (event.getSlot() == 44) {
                // scroll to bottom
                offset = (preview.size() / 8) - 6;
                getMenu().queryRebuild();
            } else if (event.getSlot() == 53) {
                // scroll down by one
                int floor = Math.max(0, (preview.size() / 8) - 6);
                offset = Math.min(floor, offset + 1);
                getMenu().queryRebuild();
            } else {
                CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());

                String selling = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "vendor-id", null);
                if (selling != null && event.getWhoClicked().getInventory().firstEmpty() != -1) {
                    String currency = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "vendor-currency", "");
                    int price = Integer.parseInt(IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "vendor-price", "0"));

                    if (event.getClick() == ClickType.LEFT) {
                        purchase(getMenu().getViewer(), core_player, false, selling, currency, price);
                        getMenu().queryRebuild();
                    } else if (event.getClick() == ClickType.SHIFT_LEFT) {
                        purchase(getMenu().getViewer(), core_player, true, selling, currency, price);
                        getMenu().queryRebuild();
                    }
                }
            }
        }
    }

    /*
     * Purchase this item, if maximum is enabled we buy up
     * to a full stack of the item. Unstackable items will
     * still only buy one copy.
     * <p>
     * This will directly consume the cost and give the item
     * to the player. Excess will be mailed.
     *
     * @param maximum buy as much of a stack as possible
     */
    private void purchase(Player player, CorePlayer core_player, boolean maximum, String selling, String currency, int price) {
        if (player.getInventory().firstEmpty() == -1) {
            return;
        }

        CoreItem selling_item = RPGCore.inst().getItemManager().getItemIndex().get(selling);
        ItemStack stack = selling_item.acquire(core_player, 0d);
        int amount = (selling_item.isUnstackable() || !maximum) ? 1 : stack.getMaxStackSize();

        // track valuation per-stack
        int total_value = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            ItemDataGeneric data = RPGCore.inst().getItemManager().getItemData(item, ItemDataGeneric.class);
            if (data != null) {
                String group = data.getItem().getBankGroup();
                if (currency.equals(group)) {
                    total_value += data.getItem().getBankQuantity() * item.getAmount();
                }
            }
        }

        // cap maximum purchase to available currency
        amount = Math.min(amount, total_value / price);
        if (amount > 0) {
            // acquire a copy of the item
            stack.setAmount(amount);
            player.getInventory().addItem(stack);

            // burn any existing currency, acquire new stacks
            for (ItemStack item : player.getInventory().getContents()) {
                ItemDataGeneric data = RPGCore.inst().getItemManager().getItemData(item, ItemDataGeneric.class);
                if (data != null) {
                    String group = data.getItem().getBankGroup();
                    if (currency.equals(group)) {
                        item.setAmount(0);
                    }
                }
            }

            // identify new amount player should be holding
            int remaining = total_value - (amount * price);

            // give player the updated amount of currency
            List<CoreItem> reverse = bank_to_item.get().get(currency);
            while (remaining > 0) {
                // search for the greatest denomination available
                CoreItem focus = null;
                for (CoreItem candidate : reverse) {
                    if (candidate.getBankQuantity() < remaining) {
                        if (focus == null || focus.getBankQuantity() < candidate.getBankQuantity()) {
                            focus = candidate;
                        }
                    }
                }

                // if no focus found, put it in the bank
                if (focus == null) {
                    player.sendMessage("§cUNEXPECTED INTERNAL ERROR - LEFT OVER WAS BANKED!");
                    core_player.getBankedItems().merge(currency, remaining, (a, b) -> a + b);
                    break;
                }

                // give the player their change
                int size = focus.isUnstackable() ? 1 : (remaining / focus.getBankQuantity());
                if (player.getInventory().firstEmpty() == -1) {
                    // generate the itemized currency
                    ItemStack acquire = focus.bounded(core_player, 0d);
                    size = Math.min(acquire.getMaxStackSize(), size);
                    acquire.setAmount(size);
                    // consume the debt based on our denomination
                    remaining -= size * focus.getBankQuantity();
                    // drop as bound so others cannot pick it up
                    player.getWorld().dropItem(player.getLocation(), acquire);
                } else {
                    // generate the itemized currency
                    ItemStack acquire = focus.acquire(core_player, 0d);
                    size = Math.min(acquire.getMaxStackSize(), size);
                    acquire.setAmount(size);
                    // consume the debt based on our denomination
                    remaining -= size * focus.getBankQuantity();
                    // add directly to player inventory
                    player.getInventory().addItem(acquire);
                }
            }
        }
    }

    @Override
    public boolean isTrivial() {
        return true;
    }
}
