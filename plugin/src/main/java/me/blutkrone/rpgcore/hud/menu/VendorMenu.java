package me.blutkrone.rpgcore.hud.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.index.IndexAttachment;
import me.blutkrone.rpgcore.hud.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.item.data.ItemDataGeneric;
import me.blutkrone.rpgcore.language.LanguageManager;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.npc.trait.impl.CoreVendorTrait;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class VendorMenu {
    // maps currency group to the physical items
    private IndexAttachment<CoreItem, Map<String, List<CoreItem>>> bank_to_item =
            RPGCore.inst().getItemManager().getItemIndex().createAttachment(index -> {
                Map<String, List<CoreItem>> cached = new HashMap<>();

                // re-index all items we cached
                for (CoreItem item : index.getAll()) {
                    String group = item.getBankGroup();
                    if (!group.equalsIgnoreCase("none")) {
                        cached.computeIfAbsent(group, (k -> new ArrayList<>())).add(item);
                    }
                }
                // sort the mappings by denomination
                cached.forEach((id, items) -> {
                    items.sort(Comparator.comparingInt(CoreItem::getBankQuantity));
                });

                return cached;
            });

    public VendorMenu() {
    }

    public void present(Player player, CoreVendorTrait trait) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        LanguageManager lpm = RPGCore.inst().getLanguageManager();

        if (RPGCore.inst().getEntityManager().getPlayer(player) == null) {
            return;
        }

        IChestMenu menu = RPGCore.inst().getVolatileManager().createMenu(6, player);
        menu.setData("preview", getPreview(trait));
        menu.setData("offset", 0);
        menu.setRebuilder(() -> {
            menu.clearItems();
            Map<String, Integer> carrying = new HashMap<>();
            for (ItemStack item : menu.getViewer().getInventory().getContents()) {
                ItemDataGeneric data = RPGCore.inst().getItemManager().getItemData(item, ItemDataGeneric.class);
                if (data != null) {
                    String group = data.getItem().getBankGroup();
                    int value = item.getAmount() * data.getItem().getBankQuantity();
                    carrying.merge(group, value, (a, b) -> a + b);
                }
            }

            // base texture
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.retreat(8);
            msb.append(rpm.texture("menu_scroller_dual"), ChatColor.WHITE);

            List<ItemStack> preview = menu.getData("preview");
            int offset = menu.getData("offset");
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

                    menu.setItemAt(i * 9, icon);
                    msb.shiftToExact(20);
                    msb.append(lpm.formatShortNumber(price), "scroller_text_" + (i + 1), (price < have) ? ChatColor.GREEN : ChatColor.RED);
                    msb.advance(3).append(rpm.texture("currency_" + symbol + "_menu_" + (i + 1)), ChatColor.WHITE);
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

                    menu.setItemAt(i * 9 + 4, icon);
                    msb.shiftToExact(92);
                    msb.append(lpm.formatShortNumber(price), "scroller_text_" + (i + 1), (price < have) ? ChatColor.GREEN : ChatColor.RED);
                    msb.advance(3).append(rpm.texture("currency_" + symbol + "_menu_" + (i + 1)), ChatColor.WHITE);
                }
            }

            // render scroll-bar for the viewport
            msb.shiftToExact(150);
            if (preview.size() <= 12) {
                msb.append(rpm.texture("pointer_huge_0"), ChatColor.WHITE);
            } else if (preview.size() <= 36) {
                double length = Math.ceil(preview.size() / 8d) - 6d;
                double ratio = offset / length;

                msb.append(rpm.texture("pointer_medium_" + (int) (100 * ratio)), ChatColor.WHITE);
            } else if (preview.size() <= 64) {
                double length = Math.ceil(preview.size() / 8d) - 6d;
                double ratio = offset / length;

                msb.append(rpm.texture("pointer_small_" + (int) (100 * ratio)), ChatColor.WHITE);
            } else {
                double length = Math.ceil(preview.size() / 8d) - 6d;
                double ratio = offset / length;

                msb.append(rpm.texture("pointer_tiny_" + (int) (100 * ratio)), ChatColor.WHITE);
            }

            InstructionBuilder instructions = new InstructionBuilder();
            instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_vendor"));
            instructions.apply(msb);

            menu.setTitle(msb.compile());
        });
        menu.setClickHandler((e -> {
            e.setCancelled(true);

            if (e.getView().getTopInventory() == e.getClickedInventory()) {
                List<ItemStack> preview = menu.getData("preview");

                if (e.getSlot() == 8) {
                    // scroll up by one
                    menu.setData("offset", Math.max(0, menu.getData("offset", 0) - 1));
                    menu.rebuild();
                } else if (e.getSlot() == 17) {
                    // scroll to top
                    menu.setData("offset", 0);
                    menu.rebuild();
                } else if (e.getSlot() == 26) {
                    // ignore other clicks
                } else if (e.getSlot() == 35) {
                    // ignore other clicks
                } else if (e.getSlot() == 44) {
                    // scroll to bottom
                    menu.setData("offset", (preview.size() / 8) - 6);
                    menu.rebuild();
                } else if (e.getSlot() == 53) {
                    // scroll down by one
                    int floor = Math.max(0, (preview.size() / 8) - 6);
                    menu.setData("offset", Math.min(floor, menu.getData("offset", 0) + 1));
                    menu.rebuild();
                } else {
                    CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);

                    String selling = IChestMenu.getBrand(e.getCurrentItem(), RPGCore.inst(), "vendor-id", null);
                    if (selling != null && e.getWhoClicked().getInventory().firstEmpty() != -1) {
                        String currency = IChestMenu.getBrand(e.getCurrentItem(), RPGCore.inst(), "vendor-currency", "");
                        int price = Integer.parseInt(IChestMenu.getBrand(e.getCurrentItem(), RPGCore.inst(), "vendor-price", "0"));

                        if (e.getClick() == ClickType.LEFT) {
                            purchase(player, core_player, false, selling, currency, price);
                            menu.rebuild();
                        } else if (e.getClick() == ClickType.SHIFT_LEFT) {
                            purchase(player, core_player, true, selling, currency, price);
                            menu.rebuild();
                        }
                    }
                }
            }
        }));
        menu.open();
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

    /*
     * Create a preview on the purchase options.
     *
     * @param player who wants to purchase
     * @param core_player who wants to purchase
     * @param trait vendor npc we are using
     * @return purchases available
     */
    private List<ItemStack> getPreview(CoreVendorTrait trait) {
        List<ItemStack> previews = new ArrayList<>();

        // create snapshots of purchases
        for (CoreVendorTrait.VendorOffer offer : trait.offers) {
            ItemStack stack = RPGCore.inst().getItemManager().getItemIndex().get(offer.item).unidentified();
            IChestMenu.setBrand(stack, RPGCore.inst(), "vendor-id", offer.item);
            IChestMenu.setBrand(stack, RPGCore.inst(), "vendor-currency", offer.currency);
            IChestMenu.setBrand(stack, RPGCore.inst(), "vendor-price", String.valueOf(offer.price));
            previews.add(stack);
        }

        return previews;
    }
}
