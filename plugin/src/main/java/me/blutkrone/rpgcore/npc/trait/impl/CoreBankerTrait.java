package me.blutkrone.rpgcore.npc.trait.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.index.IndexAttachment;
import me.blutkrone.rpgcore.hud.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.hud.editor.root.npc.EditorBankerTrait;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.item.data.ItemDataGeneric;
import me.blutkrone.rpgcore.language.LanguageManager;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Storage primarily intended for "bulk" type items, which do not
 * care about their localized data.
 */
public class CoreBankerTrait extends AbstractCoreTrait {

    // what type of items can be banked away
    private List<String> banked;

    // banking tag mapped to items
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


    public CoreBankerTrait(EditorBankerTrait editor) {
        super(editor);
        this.banked = new ArrayList<>(editor.banked);
    }

    @Override
    public void engage(Player _player) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        LanguageManager lam = RPGCore.inst().getLanguageManager();

        // create a menu for our player
        IChestMenu menu = RPGCore.inst().getVolatileManager().createMenu(6, _player);
        menu.setRebuilder(() -> {
            menu.clearItems();

            MagicStringBuilder msb = new MagicStringBuilder();
            msb.retreat(8);
            msb.append(rpm.texture("menu_banker"), ChatColor.WHITE);

            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(menu.getViewer());
            for (int i = 0; i < this.banked.size() && i < 18; i++) {
                // identify which group was banked
                String group = this.banked.get(i);
                List<CoreItem> banked = this.bank_to_item.get().get(group);
                // list which items are on this bank
                if (!banked.isEmpty()) {
                    // how much of this item is stored
                    int quantity = core_player.getBankedItems().getOrDefault(group, 0);
                    // use most value denomination as an icon
                    CoreItem icon = getMostValued(group, quantity);
                    if (icon != null) {
                        ItemStack itemized = icon.acquire(core_player, 0d);
                        itemized = RPGCore.inst().getHUDManager().getEquipMenu().reflect(itemized);
                        // hide item description if we are not in use
                        if (quantity <= 0) {
                            itemized = ItemBuilder.of(itemized).name(" ").lore(new ArrayList<>()).build();
                        }
                        // track what sort of currency we are dealing with
                        IChestMenu.setBrand(itemized, RPGCore.inst(), "banked-id", group);
                        menu.setItemAt((i / 3) * 9 + (i % 3) * 3, itemized);
                        msb.shiftToExact(-8 + 28 + (54 * (i % 3)));
                        msb.append(lam.formatShortNumber(quantity), "banked_text_" + (i / 3), ChatColor.BLACK);
                    }
                }
            }

            // empty fields are populated
            ItemStack invisible = RPGCore.inst().getLanguageManager().getAsItem("invisible").build();
            for (int i = 0; i < 54; i++) {
                ItemStack checking = menu.getItemAt(i);
                if (checking == null || checking.getType().isAir()) {
                    menu.setItemAt(i, invisible);
                }
            }

            InstructionBuilder instructions = new InstructionBuilder();
            instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_npc_banker"));
            instructions.apply(msb);

            menu.setTitle(msb.compile());
        });
        menu.setClickHandler(e -> {
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(menu.getViewer());
            Player player = menu.getViewer();
            e.setCancelled(true);

            // make sure the clicked item actually exists
            ItemStack clicked_item = e.getCurrentItem();
            if (clicked_item == null || clicked_item.getType().isAir()) {
                return;
            }

            if (e.getClickedInventory() == e.getView().getTopInventory()) {
                // identify what item we want to fetch
                String brand = IChestMenu.getBrand(e.getCurrentItem(), RPGCore.inst(), "banked-id", null);

                if (menu.getViewer().getInventory().firstEmpty() == -1) {
                    // warn about inventory being full
                    String warning = RPGCore.inst().getLanguageManager().getTranslation("not_enough_space");
                    e.getWhoClicked().sendMessage(warning);
                } else if (brand != null) {
                    int remaining = core_player.getBankedItems().getOrDefault(brand, 0);
                    if (remaining > 0) {
                        if (e.getClick() == ClickType.LEFT) {
                            // search for the highest denomination we can grab
                            CoreItem highest_value = getMostValued(brand, remaining);
                            if (highest_value != null) {
                                // fetch a single stack of the highest denomination we can
                                core_player.getBankedItems().put(brand, remaining - highest_value.getBankQuantity());
                                player.getInventory().addItem(highest_value.acquire(core_player, 0d));
                                menu.rebuild();
                            }
                        } else if (e.getClick() == ClickType.SHIFT_LEFT) {
                            // grab a customized amount instead
                            pickCustom(menu, brand);
                        }
                    }
                }
            } else {
                ItemDataGeneric data = RPGCore.inst().getItemManager().getItemData(e.getCurrentItem(), ItemDataGeneric.class);
                if (data == null || !this.banked.contains(data.getItem().getBankGroup())) {
                    // warn about not being able to store this type of item
                    String warning = RPGCore.inst().getLanguageManager().getTranslation("cannot_be_banked_here");
                    e.getWhoClicked().sendMessage(warning);
                } else if (e.getClick() == ClickType.LEFT) {
                    // gain the value of 1 item from the stack
                    int before = core_player.getBankedItems().getOrDefault(data.getItem().getBankGroup(), 0);
                    core_player.getBankedItems().put(data.getItem().getBankGroup(), before + data.getItem().getBankQuantity());
                    // consume one item that was banked away
                    clicked_item.setAmount(clicked_item.getAmount() - 1);
                    // rebuild to reflect the updated balance
                    menu.rebuild();
                } else if (e.getClick() == ClickType.SHIFT_LEFT) {
                    // store all of the stack
                    int before = core_player.getBankedItems().getOrDefault(data.getItem().getBankGroup(), 0);
                    int gained = clicked_item.getAmount() * data.getItem().getBankQuantity();
                    core_player.getBankedItems().put(data.getItem().getBankGroup(), before + gained);
                    // consume all items that are now banked
                    clicked_item.setAmount(0);
                    // rebuild to reflect the updated balance
                    menu.rebuild();
                }
            }
        });
        menu.open();
    }

    /*
     * Opens a 1x9 menu allowing to pick from denominations
     *
     * @param origin
     */
    private void pickCustom(IChestMenu origin, String group) {
        Bukkit.getLogger().severe("CUSTOM PICKER!");
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        LanguageManager lam = RPGCore.inst().getLanguageManager();

        origin.stalled(() -> {
            Bukkit.getLogger().severe("CLOSED PREVIOUS!");
            origin.getViewer().closeInventory();

            IChestMenu menu = RPGCore.inst().getVolatileManager().createMenu(1, origin.getViewer());
            menu.setData("bank-group", group);
            menu.setRebuilder(() -> {
                menu.clearItems();

                String banked = menu.getData("bank-group");
                CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(menu.getViewer());
                Integer quantity = core_player.getBankedItems().getOrDefault(banked, 0);

                MagicStringBuilder msb = new MagicStringBuilder();
                msb.retreat(8);
                msb.append(rpm.texture("menu_denominator"), ChatColor.WHITE);

                // only render denominations small enough
                List<CoreItem> denominations = this.bank_to_item.get().get(banked);
                for (int i = 0; i < denominations.size() && i < 9; i++) {
                    CoreItem item = denominations.get(i);
                    if (item.getBankQuantity() <= quantity) {
                        ItemStack acquire = item.acquire(core_player, 0d);
                        IChestMenu.setBrand(acquire, RPGCore.inst(), "denominator", item.getId());
                        menu.setItemAt(i, acquire);
                    }
                }

                InstructionBuilder instructions = new InstructionBuilder();
                instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_npc_banker"));
                instructions.apply(msb);

                menu.setTitle(msb.compile());
            });
            menu.setClickHandler(e -> {
                String banked = menu.getData("bank-group");
                e.setCancelled(true);

                if (menu.getViewer().getInventory().firstEmpty() == -1) {
                    // warn about inventory being full
                    String warning = RPGCore.inst().getLanguageManager().getTranslation("not_enough_space");
                    e.getWhoClicked().sendMessage(warning);
                } else {
                    // grant one instance of our denominated value
                    CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(menu.getViewer());
                    String denominated = IChestMenu.getBrand(e.getCurrentItem(), RPGCore.inst(), "denominator", null);
                    if (denominated != null) {
                        CoreItem core_item = RPGCore.inst().getItemManager().getItemIndex().get(denominated);
                        int owned_amount = core_player.getBankedItems().getOrDefault(banked, 0);
                        if (owned_amount >= core_item.getBankQuantity()) {
                            ItemStack acquired = core_item.acquire(core_player, 0d);
                            // increment into the highest denomination
                            if (e.getClick() == ClickType.SHIFT_LEFT) {
                                for (int i = 1; i <= acquired.getMaxStackSize(); i++) {
                                    int value = i * core_item.getBankQuantity();
                                    if (value <= owned_amount) {
                                        acquired.setAmount(i);
                                    } else {
                                        break;
                                    }
                                }
                            }
                            // acquire the denomination and subtract from the bank
                            core_player.getBankedItems().put(banked, owned_amount - (acquired.getAmount() * core_item.getBankQuantity()));
                            menu.getViewer().getInventory().addItem(acquired);
                        }
                    }
                    // rebuild menu layout after consuming
                    menu.rebuild();
                }
            });
            menu.setCloseHandler(e -> menu.stalled(origin::open));
            menu.open();
            Bukkit.getLogger().severe("OPENED NEW MENU!");
        });
    }

    /*
     * Retrieve the denomination which is the most valuable.
     *
     * @param group the banked group to check
     * @param amount the amount we got
     * @return most valuable denomination
     */
    private CoreItem getMostValued(String group, int amount) {
        // grab all items in this banked group
        List<CoreItem> items = this.bank_to_item.get().get(group);
        if (items == null || items.isEmpty()) {
            return null;
        }
        // list is sorted, pick least valued as base
        CoreItem valuable = items.get(0);
        // search for the highest denomination within reason
        for (CoreItem item : items) {
            if (item.getBankQuantity() <= amount) {
                if (item.getBankQuantity() > valuable.getBankQuantity()) {
                    valuable = item;
                }
            }
        }

        // offer up the most valuable item
        return valuable;
    }
}
