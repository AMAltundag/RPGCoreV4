package me.blutkrone.rpgcore.hud.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.item.crafting.CoreCraftingRecipe;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.npc.trait.impl.CoreCrafterTrait;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CrafterMenu {

    public CrafterMenu() {
    }

    public void present(Player player, CoreCrafterTrait trait) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();

        IChestMenu menu = RPGCore.inst().getVolatileManager().createMenu(6, player);
        menu.setData("preview", getPreview(player, trait));
        menu.setData("offset", 0);
        menu.setRebuilder(() -> {
            menu.clearItems();

            // base texture
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.retreat(8);
            msb.append(rpm.texture("menu_scroller_grid"), ChatColor.WHITE);

            // render viewport and place items
            List<ItemStack> preview = menu.getData("preview");
            int offset = menu.getData("offset");
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 8; j++) {
                    int k = (i + offset) * 8 + j;
                    if (k < preview.size()) {
                        // retrieve the item we are working with
                        ItemStack icon = preview.get(k);
                        // highlight item that is affordable
                        boolean affordable = IChestMenu.getBrand(icon, RPGCore.inst(), "recipe-affordable", "0")
                                .equalsIgnoreCase("1");
                        if (affordable) {
                            msb.shiftToExact(-2 + 18 * j);
                            msb.append(rpm.texture("scroller_highlight_" + i), ChatColor.WHITE);
                        }
                        // place clickable item we are using
                        menu.setItemAt(i * 9 + j, icon);
                    }
                }
            }

            // render scroll-bar for the viewport
            msb.shiftToExact(150);
            if (preview.size() <= 48) {
                msb.append(rpm.texture("pointer_huge_0"), ChatColor.WHITE);
            } else if (preview.size() <= 96) {
                double length = Math.ceil(preview.size() / 8d) - 6d;
                double ratio = offset / length;

                msb.append(rpm.texture("pointer_medium_" + (int) (100 * ratio)), ChatColor.WHITE);
            } else if (preview.size() <= 192) {
                double length = Math.ceil(preview.size() / 8d) - 6d;
                double ratio = offset / length;

                msb.append(rpm.texture("pointer_small_" + (int) (100 * ratio)), ChatColor.WHITE);
            } else {
                double length = Math.ceil(preview.size() / 8d) - 6d;
                double ratio = offset / length;

                msb.append(rpm.texture("pointer_tiny_" + (int) (100 * ratio)), ChatColor.WHITE);
            }

            InstructionBuilder instructions = new InstructionBuilder();
            instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_crafting"));
            instructions.apply(msb);

            menu.setTitle(msb.compile());
        });
        menu.setClickHandler((e) -> {
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
                    String recipe = IChestMenu.getBrand(e.getCurrentItem(), RPGCore.inst(), "recipe-id", null);
                    if (recipe != null) {
                        CoreCraftingRecipe core_recipe = RPGCore.inst().getItemManager().getCraftIndex().get(recipe);
                        showCrafter(core_recipe, ((Player) e.getWhoClicked()), menu, trait);
                    }
                }
            }
        });
        menu.open();
    }

    /*
     * Show a crafting recipe to a player.
     *
     * @param recipe what recipe are we showing
     * @param player who wants to see the crafting menu
     * @param previous head back to this menu after closing
     */
    private void showCrafter(CoreCraftingRecipe recipe, Player player, IChestMenu previous, CoreCrafterTrait trait) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();

        IChestMenu menu = RPGCore.inst().getVolatileManager().createMenu(1, player);
        menu.setRebuilder(() -> {
            menu.clearItems();

            // base texture
            MagicStringBuilder msb = new MagicStringBuilder();

            // first six slots show materials
            Iterator<Map.Entry<CoreItem, Integer>> ingredients = recipe.getIngredients().entrySet().iterator();
            for (int i = 0; i < 6; i++) {
                if (ingredients.hasNext()) {
                    Map.Entry<CoreItem, Integer> ingredient = ingredients.next();
                    ItemStack item = ingredient.getKey().unidentified();
                    IChestMenu.setBrand(item, RPGCore.inst(), "crafting-ingredient", ingredient.getKey().getId());

                    item.setAmount(ingredient.getValue());
                    menu.setItemAt(i, item);
                }
            }

            // crafter icons
            ItemStack invisible = RPGCore.inst().getLanguageManager().getAsItem("invisible").build();
            if (recipe.isMatched(menu.getViewer())) {
                IChestMenu.setBrand(invisible, RPGCore.inst(), "allow-crafting", "1");

                msb.retreat(8);
                msb.append(rpm.texture("menu_crafter_allowed"), ChatColor.WHITE);
                // last 3 slots are a crafting button
                for (int i = 6; i < 9; i++) {
                    menu.setItemAt(i, invisible);
                }
            } else {
                IChestMenu.setBrand(invisible, RPGCore.inst(), "allow-crafting", "0");

                msb.retreat(8);
                msb.append(rpm.texture("menu_crafter_disallowed"), ChatColor.WHITE);
                // last 3 slots are a crafting button
                for (int i = 6; i < 9; i++) {
                    menu.setItemAt(i, invisible);
                }
            }

            // crafting instructions
            InstructionBuilder instructions = new InstructionBuilder();
            instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_crafting"));
            instructions.apply(msb);

            menu.setTitle(msb.compile());
        });
        menu.setClickHandler((e) -> {
            e.setCancelled(true);

            // need a core player to actually craft
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(e.getWhoClicked());
            if (core_player == null) {
                return;
            }

            // do not craft at full inventory
            if (e.getWhoClicked().getInventory().firstEmpty() == -1) {
                return;
            }

            // check if we clicked the crafting button
            boolean crafting = IChestMenu.getBrand(e.getCurrentItem(), RPGCore.inst(), "allow-crafting", "0").equals("1");
            if (crafting) {
                CoreItem output = recipe.getOutput();
                ItemStack item = output.acquire(core_player, 0d);

                if (e.getClick() == ClickType.LEFT || output.isUnstackable()) {
                    // check how often it can be crafted
                    int receive = recipe.craftAndConsume(1, menu.getViewer());
                    // offer the item if possible
                    if (receive > 0) {
                        item.setAmount(receive);
                        menu.getViewer().getInventory().addItem(item);
                        menu.rebuild();
                    }
                } else if (e.getClick() == ClickType.SHIFT_LEFT) {
                    // check how often it can be crafted
                    int receive = recipe.craftAndConsume(item.getMaxStackSize(), menu.getViewer());
                    // offer the item if possible
                    if (receive > 0) {
                        item.setAmount(receive);
                        menu.getViewer().getInventory().addItem(item);
                        menu.rebuild();
                    }
                }

                return;
            }

            // check if we clicked the ingredient
            String jump_to_ingredient = IChestMenu.getBrand(e.getCurrentItem(), RPGCore.inst(), "crafting-ingredient", null);
            if (jump_to_ingredient != null) {
                List<CoreCraftingRecipe> recipes = trait.reverse.get().get(jump_to_ingredient);
                if (recipes != null && recipes.size() == 1) {
                    showCrafter(recipes.get(0), menu.getViewer(), menu, trait);
                }
                e.getWhoClicked().sendMessage("§cnot implemented (jump to ingredient recipe)");
                return;
            }
        });
        menu.setCloseHandler((e) -> {
            menu.stalled(() -> {
                if (menu.getViewer().getOpenInventory().getType() == InventoryType.CRAFTING) {
                    previous.open();
                }
            });
        });
        menu.open();
    }

    /*
     * Retrieve all recipes the user is qualified to see, the stacks
     * are flagged with appropriate information.
     *
     * @param bukkit_player who to build relative toward
     * @param core_player who to build relative toward
     * @return stacks hinting at craft-ability
     */
    private List<ItemStack> getPreview(Player bukkit_player, CoreCrafterTrait trait) {
        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(bukkit_player);

        List<ItemStack> previews_header = new ArrayList<>();
        List<ItemStack> previews_footer = new ArrayList<>();

        // get all items technically craft-able
        List<CoreCraftingRecipe> allowed = trait.recipes.get();

        // check if player got the ingredients
        for (CoreCraftingRecipe recipe : allowed) {
            ItemStack stack = recipe.getOutput().unidentified();
            IChestMenu.setBrand(stack, RPGCore.inst(), "recipe-id", recipe.getId());
            if (recipe.isMatched(bukkit_player)) {
                IChestMenu.setBrand(stack, RPGCore.inst(), "recipe-affordable", "1");
                previews_header.add(stack);
            } else {
                IChestMenu.setBrand(stack, RPGCore.inst(), "recipe-affordable", "0");
                previews_footer.add(stack);
            }
        }

        // pool into one collection (this keeps available recipes front-loaded.)
        previews_header.addAll(previews_footer);
        return previews_header;
    }
}
