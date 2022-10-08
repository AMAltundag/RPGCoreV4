package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.index.IndexAttachment;
import me.blutkrone.rpgcore.hud.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.item.crafting.CoreCraftingRecipe;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.npc.trait.impl.CoreCrafterTrait;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CrafterMenu extends AbstractCoreMenu {

    private IndexAttachment<CoreCraftingRecipe, Map<String, List<CoreCraftingRecipe>>> reverse;
    private List<ItemStack> crafts;
    private int offset = 0;

    public CrafterMenu(CoreCrafterTrait trait, List<ItemStack> crafts) {
        super(6);
        this.crafts = crafts;
        this.reverse = trait.reverse;
    }

    @Override
    public void rebuild() {
        this.getMenu().clearItems();

        // base texture
        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-208);
        msb.append(resourcepack().texture("menu_scroller_grid"), ChatColor.WHITE);

        // render viewport and place items
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 8; j++) {
                int k = (i + offset) * 8 + j;
                if (k < crafts.size()) {
                    // retrieve the item we are working with
                    ItemStack icon = crafts.get(k);
                    // highlight item that is affordable
                    boolean affordable = IChestMenu.getBrand(icon, RPGCore.inst(), "recipe-affordable", "0")
                            .equalsIgnoreCase("1");
                    if (affordable) {
                        msb.shiftToExact(-2 + 18 * j);
                        msb.append(resourcepack().texture("scroller_highlight_" + i), ChatColor.WHITE);
                    }
                    // place clickable item we are using
                    this.getMenu().setItemAt(i * 9 + j, icon);
                }
            }
        }

        // render scroll-bar for the viewport
        msb.shiftToExact(150);
        if (crafts.size() <= 48) {
            msb.append(resourcepack().texture("pointer_huge_0"), ChatColor.WHITE);
        } else if (crafts.size() <= 96) {
            double length = Math.ceil(crafts.size() / 8d) - 6d;
            double ratio = offset / length;
            msb.append(resourcepack().texture("pointer_medium_" + (int) (100 * ratio)), ChatColor.WHITE);
        } else if (crafts.size() <= 192) {
            double length = Math.ceil(crafts.size() / 8d) - 6d;
            double ratio = offset / length;
            msb.append(resourcepack().texture("pointer_small_" + (int) (100 * ratio)), ChatColor.WHITE);
        } else {
            double length = Math.ceil(crafts.size() / 8d) - 6d;
            double ratio = offset / length;
            msb.append(resourcepack().texture("pointer_tiny_" + (int) (100 * ratio)), ChatColor.WHITE);
        }

        InstructionBuilder instructions = new InstructionBuilder();
        instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_crafting"));
        instructions.apply(msb);

        this.getMenu().setTitle(msb.compile());
    }

    @Override
    public void click(InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getView().getTopInventory() == event.getClickedInventory()) {
            if (event.getSlot() == 8) {
                // scroll up by one
                this.offset = Math.max(0, this.offset - 1);
                this.getMenu().queryRebuild();
            } else if (event.getSlot() == 17) {
                // scroll to top
                this.offset = 0;
                this.getMenu().queryRebuild();
            } else if (event.getSlot() == 26) {
                // ignore other clicks
            } else if (event.getSlot() == 35) {
                // ignore other clicks
            } else if (event.getSlot() == 44) {
                // scroll to bottom
                this.offset = (this.crafts.size() / 8) - 6;
                this.getMenu().queryRebuild();
            } else if (event.getSlot() == 53) {
                // scroll down by one
                int floor = Math.max(0, (this.crafts.size() / 8) - 6);
                this.offset = Math.min(floor, this.offset + 1);
                this.getMenu().queryRebuild();
            } else {
                String recipe = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "recipe-id", null);
                if (recipe != null) {
                    CoreCraftingRecipe core_recipe = RPGCore.inst().getItemManager().getCraftIndex().get(recipe);
                    this.getMenu().stalled(() -> {
                        new Confirm(this.getMenu(), core_recipe).finish(((Player) event.getWhoClicked()));
                    });
                }
            }
        }
    }

    /*
     * A sub-menu to confirm the crafting, also shows
     * ingredients used to craft the item.
     */
    class Confirm extends AbstractCoreMenu {

        private IChestMenu parent;
        private CoreCraftingRecipe recipe;

        Confirm(IChestMenu parent, CoreCraftingRecipe recipe) {
            super(1);
            this.parent = parent;
            this.recipe = recipe;
        }

        @Override
        public void rebuild() {
            this.getMenu().clearItems();

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
                    this.getMenu().setItemAt(i, item);
                }
            }

            // crafter icons
            ItemStack invisible = RPGCore.inst().getLanguageManager().getAsItem("invisible").build();
            if (recipe.isMatched(this.getMenu().getViewer())) {
                IChestMenu.setBrand(invisible, RPGCore.inst(), "allow-crafting", "1");

                msb.shiftToExact(-208);
                msb.append(resourcepack().texture("menu_crafter_allowed"), ChatColor.WHITE);
                // last 3 slots are a crafting button
                for (int i = 6; i < 9; i++) {
                    this.getMenu().setItemAt(i, invisible);
                }
            } else {
                IChestMenu.setBrand(invisible, RPGCore.inst(), "allow-crafting", "0");

                msb.shiftToExact(-208);
                msb.append(resourcepack().texture("menu_crafter_disallowed"), ChatColor.WHITE);
                // last 3 slots are a crafting button
                for (int i = 6; i < 9; i++) {
                    this.getMenu().setItemAt(i, invisible);
                }
            }

            // crafting instructions
            InstructionBuilder instructions = new InstructionBuilder();
            instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_crafting"));
            instructions.apply(msb);

            this.getMenu().setTitle(msb.compile());
        }

        @Override
        public void click(InventoryClickEvent event) {
            event.setCancelled(true);

            // need a core player to actually craft
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(event.getWhoClicked());
            if (core_player == null) {
                return;
            }

            // do not craft at full inventory
            if (event.getWhoClicked().getInventory().firstEmpty() == -1) {
                return;
            }

            // check if we clicked the crafting button
            boolean allow_to_craft = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "allow-crafting", "0").equals("1");
            if (allow_to_craft) {
                CoreItem output = recipe.getOutput();
                ItemStack item = output.acquire(core_player, 0d);

                if (event.getClick() == ClickType.LEFT || output.isUnstackable()) {
                    // check how often it can be crafted
                    int receive = recipe.craftAndConsume(1, this.getMenu().getViewer());
                    // offer the item if possible
                    if (receive > 0) {
                        item.setAmount(receive);
                        this.getMenu().getViewer().getInventory().addItem(item);
                        this.getMenu().queryRebuild();
                    }
                } else if (event.getClick() == ClickType.SHIFT_LEFT) {
                    // check how often it can be crafted
                    int receive = recipe.craftAndConsume(item.getMaxStackSize(), this.getMenu().getViewer());
                    // offer the item if possible
                    if (receive > 0) {
                        item.setAmount(receive);
                        this.getMenu().getViewer().getInventory().addItem(item);
                        this.getMenu().queryRebuild();
                    }
                }

                return;
            }

            // check if we clicked the ingredient
            String jump_to_ingredient = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "crafting-ingredient", null);
            if (jump_to_ingredient != null) {
                List<CoreCraftingRecipe> recipes = reverse.get().get(jump_to_ingredient);
                if (recipes != null && recipes.size() == 1) {
                    this.getMenu().stalled(() -> {
                        new Confirm(parent, recipes.get(0)).finish(this.getMenu().getViewer());
                    });
                }
            }
        }

        @Override
        public void close(InventoryCloseEvent event) {
            suggestOpen(this.parent);
        }
    }
}