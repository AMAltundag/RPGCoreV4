package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.index.IndexAttachment;
import me.blutkrone.rpgcore.hud.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.item.data.ItemDataGeneric;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.npc.trait.impl.CoreBankerTrait;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BankerMenu extends AbstractCoreMenu {

    private List<String> banked;
    private IndexAttachment<CoreItem, Map<String, List<CoreItem>>> bank_to_item;

    public BankerMenu(CoreBankerTrait origin) {
        super(6);
        this.banked = origin.banked;
        this.bank_to_item = origin.bank_to_item;
    }

    @Override
    public void rebuild() {
        getMenu().clearItems();

        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-208);
        msb.append(resourcepack().texture("menu_banker"), ChatColor.WHITE);

        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());
        for (int i = 0; i < this.banked.size() && i < 18; i++) {
            // identify which group was banked
            String group = this.banked.get(i);
            List<CoreItem> banked = this.bank_to_item.get().get(group);
            // list which items are on this bank
            if (banked != null && !banked.isEmpty()) {
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
                    getMenu().setItemAt((i / 3) * 9 + (i % 3) * 3, itemized);
                    msb.shiftToExact(-8 + 28 + (54 * (i % 3)));
                    msb.append(language().formatShortNumber(quantity), "banked_text_" + (i / 3), ChatColor.BLACK);
                }
            }
        }

        // empty fields are populated
        ItemStack invisible = RPGCore.inst().getLanguageManager().getAsItem("invisible").build();
        for (int i = 0; i < 54; i++) {
            ItemStack checking = getMenu().getItemAt(i);
            if (checking == null || checking.getType().isAir()) {
                getMenu().setItemAt(i, invisible);
            }
        }

        InstructionBuilder instructions = new InstructionBuilder();
        instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_npc_banker"));
        instructions.apply(msb);

        getMenu().setTitle(msb.compile());
    }

    @Override
    public void click(InventoryClickEvent event) {
        event.setCancelled(true);

        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());
        Player bukkit_player = getMenu().getViewer();

        // make sure the clicked item actually exists
        if (!isRelevant(event.getCurrentItem())) {
            return;
        }

        if (isUpperClick(event)) {
            // identify what item we want to fetch
            String banked_id = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "banked-id", null);

            if (getMenu().getViewer().getInventory().firstEmpty() == -1) {
                // warn about inventory being full
                String warning = RPGCore.inst().getLanguageManager().getTranslation("not_enough_space");
                bukkit_player.sendMessage(warning);
            } else if (banked_id != null) {
                int remaining = core_player.getBankedItems().getOrDefault(banked_id, 0);
                if (remaining > 0) {
                    if (event.getClick() == ClickType.LEFT) {
                        // search for the highest denomination we can grab
                        CoreItem highest_value = getMostValued(banked_id, remaining);
                        if (highest_value != null) {
                            // fetch a single stack of the highest denomination we can
                            core_player.getBankedItems().put(banked_id, remaining - highest_value.getBankQuantity());
                            bukkit_player.getInventory().addItem(highest_value.acquire(core_player, 0d));
                            getMenu().queryRebuild();
                        }
                    } else if (event.getClick() == ClickType.SHIFT_LEFT) {
                        // grab a customized amount instead
                        getMenu().stalled(() -> {
                            // abandon previous menu
                            getMenu().getViewer().closeInventory();
                            // open withdrawal menu
                            new Withdrawal(getMenu(), banked_id).finish(getMenu().getViewer());
                        });
                    }
                }
            }
        } else {
            ItemDataGeneric data = RPGCore.inst().getItemManager().getItemData(event.getCurrentItem(), ItemDataGeneric.class);
            if (data == null || !this.banked.contains(data.getItem().getBankGroup())) {
                // warn about not being able to store this type of item
                String warning = RPGCore.inst().getLanguageManager().getTranslation("cannot_be_banked_here");
                event.getWhoClicked().sendMessage(warning);
            } else if (event.getClick() == ClickType.LEFT) {
                // gain the value of 1 item from the stack
                int before = core_player.getBankedItems().getOrDefault(data.getItem().getBankGroup(), 0);
                core_player.getBankedItems().put(data.getItem().getBankGroup(), before + data.getItem().getBankQuantity());
                // consume one item that was banked away
                event.getCurrentItem().setAmount(event.getCurrentItem().getAmount() - 1);
                // rebuild to reflect the updated balance
                getMenu().queryRebuild();
            } else if (event.getClick() == ClickType.SHIFT_LEFT) {
                // store all of the stack
                int before = core_player.getBankedItems().getOrDefault(data.getItem().getBankGroup(), 0);
                int gained = event.getCurrentItem().getAmount() * data.getItem().getBankQuantity();
                core_player.getBankedItems().put(data.getItem().getBankGroup(), before + gained);
                // consume all items that are now banked
                event.getCurrentItem().setAmount(0);
                // rebuild to reflect the updated balance
                getMenu().queryRebuild();
            }
        }
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

    @Override
    public boolean isTrivial() {
        return true; // trivial due to visualized items having no dupe risk
    }

    class Withdrawal extends AbstractCoreMenu {

        private IChestMenu parent;
        private String currency_group;

        Withdrawal(IChestMenu parent, String currency_group) {
            super(1);
            this.parent = parent;
            this.currency_group = currency_group;
        }

        @Override
        public void rebuild() {
            this.getMenu().clearItems();

            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(this.getMenu().getViewer());
            int quantity = core_player.getBankedItems().getOrDefault(currency_group, 0);

            MagicStringBuilder msb = new MagicStringBuilder();
            msb.shiftToExact(-208);
            msb.append(resourcepack().texture("menu_denominator"), ChatColor.WHITE);

            // only render denominations small enough
            List<CoreItem> denominations = bank_to_item.get().get(currency_group);
            if (denominations != null) {
                for (int i = 0; i < denominations.size() && i < 9; i++) {
                    CoreItem item = denominations.get(i);
                    if (item.getBankQuantity() <= quantity) {
                        ItemStack acquire = item.acquire(core_player, 0d);
                        IChestMenu.setBrand(acquire, RPGCore.inst(), "denominator", item.getId());
                        this.getMenu().setItemAt(i, acquire);
                    }
                }
            }

            InstructionBuilder instructions = new InstructionBuilder();
            instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_npc_banker"));
            instructions.apply(msb);

            this.getMenu().setTitle(msb.compile());
        }

        @Override
        public void click(InventoryClickEvent event) {
            event.setCancelled(true);

            if (this.getMenu().getViewer().getInventory().firstEmpty() == -1) {
                // warn about inventory being full
                String warning = RPGCore.inst().getLanguageManager().getTranslation("not_enough_space");
                event.getWhoClicked().sendMessage(warning);
            } else {
                // grant one instance of our denominated value
                CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(this.getMenu().getViewer());
                String denominated = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "denominator", null);
                if (denominated != null) {
                    CoreItem core_item = RPGCore.inst().getItemManager().getItemIndex().get(denominated);
                    int owned_amount = core_player.getBankedItems().getOrDefault(currency_group, 0);
                    if (owned_amount >= core_item.getBankQuantity()) {
                        ItemStack acquired = core_item.acquire(core_player, 0d);
                        // increment into the highest denomination
                        if (event.getClick() == ClickType.SHIFT_LEFT) {
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
                        core_player.getBankedItems().put(currency_group, owned_amount - (acquired.getAmount() * core_item.getBankQuantity()));
                        this.getMenu().getViewer().getInventory().addItem(acquired);
                    }
                }
                // rebuild menu layout after consuming
                this.getMenu().queryRebuild();
            }
        }

        @Override
        public void close(InventoryCloseEvent event) {
            suggestOpen(parent);
        }

        @Override
        public boolean isTrivial() {
            return true;
        }
    }
}
