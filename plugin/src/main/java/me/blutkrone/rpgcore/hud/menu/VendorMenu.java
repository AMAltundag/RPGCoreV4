package me.blutkrone.rpgcore.hud.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.index.IndexAttachment;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.language.LanguageManager;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.npc.trait.impl.CoreVendorTrait;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class VendorMenu {
    // maps currency group to the physical items
    public IndexAttachment<CoreItem, Map<String, List<CoreItem>>> bank_to_item =
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

        new me.blutkrone.rpgcore.menu.VendorMenu(this, getPreview(trait)).finish(player);
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
