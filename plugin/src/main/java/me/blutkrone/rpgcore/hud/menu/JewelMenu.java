package me.blutkrone.rpgcore.hud.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.item.ItemManager;
import me.blutkrone.rpgcore.item.data.ItemDataJewel;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.util.MenuAnimator;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class JewelMenu {
    private ItemStack invisible;
    private List<Integer> jewel_position;
    private ItemStack locked;
    private ItemStack unlocked;
    private ItemStack await;

    public JewelMenu() throws IOException {
        ConfigWrapper config = FileUtil.asConfigYML(FileUtil.file("menu", "jewel.yml"));
        this.jewel_position = config.getIntegerList("jewel-position");
        this.locked = RPGCore.inst().getLanguageManager().getAsItem("jewel_socket_locked").build();
        this.unlocked = RPGCore.inst().getLanguageManager().getAsItem("jewel_socket_unlocked").build();
        this.await = RPGCore.inst().getLanguageManager().getAsItem("jewel_socket_await_item").build();
        this.invisible = RPGCore.inst().getLanguageManager().getAsItem("invisible").build();
    }

    /**
     * When this method is called, we assume that the associated item has
     * been written to 'jewel_inspection' - when the menu closes the item
     * will simply be added to the player inventory. If there is no space
     * to do so, we will instead drop it at the player their feet.
     *
     * @param _player who are we inspecting.
     */
    public void open(Player _player) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        ItemManager item_manager = RPGCore.inst().getItemManager();

        IChestMenu menu = RPGCore.inst().getVolatileManager().createMenu(6, _player);
        menu.setData("animation", new MenuAnimator());

        menu.setRebuilder(() -> {
            // clear out all items on the menu
            menu.clearItems();
            // updated msb title for the menu
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.retreat(8);
            msb.append(rpm.texture("menu_jewel"), ChatColor.WHITE);
            // extract the item we are working with
            CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(menu.getViewer());
            ItemStack jewel_inspection = player.getMenuPersistence().get("jewel_inspection");
            // generate jewel information from the item
            ItemDataJewel jewel_data = item_manager.getItemData(jewel_inspection, ItemDataJewel.class);
            if (jewel_data == null) {
                throw new IllegalArgumentException("Cannot inspect item without jewel data!");
            }
            // populate the item with the relevant jewels
            Map<Integer, ItemStack> itemized = jewel_data.getItems();
            int maximum = jewel_data.getMaximumSockets();
            int unlocked = jewel_data.getAvailableSockets();
            for (int i = 0; i < this.jewel_position.size(); i++) {
                Integer position = this.jewel_position.get(i);

                ItemStack stack = itemized.get(position);
                if (stack != null) {
                    menu.setItemAt(position, stack);
                } else if (i < unlocked) {
                    menu.setItemAt(position, this.unlocked);
                } else if (i < maximum) {
                    menu.setItemAt(position, this.locked);
                }
            }

            // populate remaining slots with invisible item
            for (int i = 0; i < 6 * 9; i++) {
                ItemStack previous = menu.getItemAt(i);
                if (previous == null || previous.getType().isAir()) {
                    menu.setItemAt(i, this.invisible);
                }
            }

            // provide instructions on usage
            InstructionBuilder instructions = new InstructionBuilder();
            instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_jewel_socketing"));
            instructions.apply(msb);

            // supply the title to the player
            menu.setTitle(msb.compile());
        });
        menu.setTickingHandler(() -> {
            // jewel animation handling
            MenuAnimator animation = menu.getData("animation");

            // create basic jewel menu layout
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.retreat(8);
            msb.append(rpm.texture("menu_jewel"), ChatColor.WHITE);

            // provide instructions on usage
            InstructionBuilder instructions = new InstructionBuilder();
            instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_jewel_socketing"));
            instructions.apply(msb);

            // merge with the animation (if applicable)
            if (animation.merge(msb)) {
                // supply the title to the player
                menu.setTitle(msb.compile());
            }
        });
        menu.setClickHandler((event) -> {
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
            for (Integer position : this.jewel_position) {
                if (this.unlocked.isSimilar(menu.getItemAt(position))) {
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
            CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(menu.getViewer());
            ItemStack working = player.getMenuPersistence().get("jewel_inspection");
            ItemDataJewel working_data = item_manager.getItemData(working, ItemDataJewel.class);
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
            sacrifice.setAmount(sacrifice.getAmount()-1);
            // check if we need to shatter anything
            ItemDataJewel sacrifice_data = item_manager.getItemData(single, ItemDataJewel.class);
            double shatter_chance = sacrifice_data.getChanceToShatterOther();
            Map<Integer, ItemStack> shatter = new HashMap<>();
            if (shatter_chance > 0d) {
                shatter = working_data.shatter(shatter_chance);
            }
            Bukkit.getLogger().severe("SHATTER CHANCE: " + shatter_chance + " BROKE " + shatter.size());
            // update the original item
            int slot = slots.get(ThreadLocalRandom.current().nextInt(slots.size()));
            working_data.socket(slot, single);
            working_data.save(working);
            // show the jewel on the menu
            menu.setItemAt(slot, single);
            // query an effect for shattering a jewel
            if (!shatter.isEmpty()) {
                RPGCore.inst().getItemManager().playShatterEffect(menu.getViewer());
            }
            // clear slots which have shattered
            shatter.forEach((pos, item) -> {
                menu.setItemAt(pos, this.unlocked);
            });
            // play shattering animation as applicable
            MenuAnimator animation = menu.getData("animation");
            shatter.forEach((pos, item) -> {
                int v = pos / 9;
                int h = pos % 9;

                ItemDataJewel data = RPGCore.inst().getItemManager().getItemData(item, ItemDataJewel.class);
                if (data != null) {
                    String anim = data.getItem().getAnimationShatter();
                    animation.queue(anim + "_" + v, -24+18*h);
                }
            });
            // query an effect for embedding a jewel
            RPGCore.inst().getItemManager().playEmbedEffect(menu.getViewer());
        });
        menu.setCloseHandler((event) -> {
            Player bukkit_player = menu.getViewer();
            CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(bukkit_player);
            ItemStack working = player.getMenuPersistence().remove("jewel_inspection");
            item_manager.describe(working);
            if (bukkit_player.getInventory().firstEmpty() != -1) {
                // add to inventory if we got the space for that
                bukkit_player.getInventory().addItem(working);
            } else {
                // otherwise drop item near the player
                bukkit_player.getWorld().dropItem(bukkit_player.getLocation(), working);
            }
        });

        menu.open();
    }
}
