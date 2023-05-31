package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.item.data.ItemDataJewel;
import me.blutkrone.rpgcore.util.MenuAnimator;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class JewelMenu extends AbstractCoreMenu {

    private final me.blutkrone.rpgcore.hud.menu.JewelMenu origin;
    private MenuAnimator animator;

    public JewelMenu(me.blutkrone.rpgcore.hud.menu.JewelMenu origin) {
        super(6);
        this.animator = new MenuAnimator();
        this.origin = origin;
    }

    @Override
    public void rebuild() {
        throw new UnsupportedOperationException("You cannot rebuild a jewel menu!");
    }

    @Override
    public void click(InventoryClickEvent event) {
        event.setCancelled(true);

        // only a jewel from the lower menu can be socketed
        if (event.getClickedInventory() == event.getView().getTopInventory()) {
            return;
        }

        // only a shift-right click sockets a jewel
        if (event.getClick() != ClickType.SHIFT_RIGHT) {
            return;
        }

        // select slots which can take the jewel
        List<Integer> slots = new ArrayList<>();
        for (Integer position : origin.jewel_position) {
            if (origin.unlocked.isSimilar(getMenu().getItemAt(position))) {
                slots.add(position);
            }
        }

        // make sure we got at least one jewel accepting slot
        if (slots.isEmpty()) {
            String warning = RPGCore.inst().getLanguageManager().getTranslation("cannot_use_item");
            event.getWhoClicked().sendMessage(warning);
            return;
        }

        // check what jewel we are trying to socket
        ItemStack sacrifice = event.getCurrentItem();
        // check the item we are socketing the jewel upon
        CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());
        ItemStack working = player.getMenuPersistence().get("jewel_inspection");
        ItemDataJewel working_data = RPGCore.inst().getItemManager().getItemData(working, ItemDataJewel.class);
        // verify the jewel is compatible with the item
        if (!working_data.check(sacrifice)) {
            String warning = RPGCore.inst().getLanguageManager().getTranslation("cannot_use_item");
            event.getWhoClicked().sendMessage(warning);
            return;
        }
        // create a copy to be actually socketed
        ItemStack single = sacrifice.clone();
        single.setAmount(1);
        // pop one jewel off the stack that was clicked
        sacrifice.setAmount(sacrifice.getAmount() - 1);
        // check if we need to shatter anything
        ItemDataJewel sacrifice_data = RPGCore.inst().getItemManager().getItemData(single, ItemDataJewel.class);
        double shatter_chance = sacrifice_data.getChanceToShatterOther();
        Map<Integer, ItemStack> shatter = new HashMap<>();
        if (shatter_chance > 0d) {
            shatter = working_data.shatter(shatter_chance);
        }
        // update the original item
        int slot = slots.get(ThreadLocalRandom.current().nextInt(slots.size()));
        working_data.socket(slot, single);
        working_data.save(working);
        // show the jewel on the menu
        getMenu().setItemAt(slot, single);
        // query an effect for shattering a jewel
        if (!shatter.isEmpty()) {
            origin.playShatterEffect(getMenu().getViewer());
        }
        // clear slots which have shattered
        shatter.forEach((pos, item) -> {
            getMenu().setItemAt(pos, origin.unlocked);
        });
        // play shattering animation as applicable
        shatter.forEach((pos, item) -> {
            int v = pos / 9;
            int h = pos % 9;

            ItemDataJewel data = RPGCore.inst().getItemManager().getItemData(item, ItemDataJewel.class);
            if (data != null) {
                String anim = data.getItem().getAnimationShatter();
                animator.queue(anim + "_" + v, -24 + 18 * h);
            }
        });
        // query an effect for embedding a jewel
        origin.playEmbedEffect(getMenu().getViewer());
    }

    @Override
    public void tick() {
        // create basic jewel menu layout
        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-208);
        msb.append(resourcepack().texture("menu_jewel"), ChatColor.WHITE);

        // provide instructions on usage
        InstructionBuilder instructions = new InstructionBuilder();
        instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_jewel_socketing"));
        instructions.apply(msb);

        // merge with the animation (if applicable)
        if (animator.merge(msb)) {
            // supply the title to the player
            getMenu().setTitle(msb.compile());
        }
    }

    @Override
    public void close(InventoryCloseEvent event) {
        Player bukkit_player = getMenu().getViewer();
        CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(bukkit_player);
        ItemStack working = player.getMenuPersistence().remove("jewel_inspection");
        RPGCore.inst().getItemManager().describe(working, player);
        if (bukkit_player.getInventory().firstEmpty() != -1) {
            // add to inventory if we got the space for that
            bukkit_player.getInventory().addItem(working);
        } else {
            // otherwise drop item near the player
            bukkit_player.getWorld().dropItem(bukkit_player.getLocation(), working);
        }
    }

    @Override
    public void open(InventoryOpenEvent event) {
        // updated msb title for the menu
        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-208);
        msb.append(resourcepack().texture("menu_jewel"), ChatColor.WHITE);
        // extract the item we are working with
        CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());
        ItemStack jewel_inspection = player.getMenuPersistence().get("jewel_inspection");
        // generate jewel information from the item
        ItemDataJewel jewel_data = RPGCore.inst().getItemManager().getItemData(jewel_inspection, ItemDataJewel.class);
        if (jewel_data == null) {
            throw new IllegalArgumentException("Cannot inspect item without jewel data!");
        }
        // populate the item with the relevant jewels
        Map<Integer, ItemStack> itemized = jewel_data.getItems();
        int maximum = jewel_data.getMaximumSockets();
        int unlocked = jewel_data.getAvailableSockets();
        for (int i = 0; i < origin.jewel_position.size(); i++) {
            Integer position = origin.jewel_position.get(i);

            ItemStack stack = itemized.get(position);
            if (stack != null) {
                getMenu().setItemAt(position, stack);
            } else if (i < unlocked) {
                getMenu().setItemAt(position, origin.unlocked);
            } else if (i < maximum) {
                getMenu().setItemAt(position, origin.locked);
            }
        }

        // populate remaining slots with invisible item
        for (int i = 0; i < 6 * 9; i++) {
            ItemStack previous = getMenu().getItemAt(i);
            if (previous == null || previous.getType().isAir()) {
                getMenu().setItemAt(i, origin.invisible);
            }
        }

        // provide instructions on usage
        InstructionBuilder instructions = new InstructionBuilder();
        instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_jewel_socketing"));
        instructions.apply(msb);

        // supply the title to the player
        getMenu().setTitle(msb.compile());
    }


}
