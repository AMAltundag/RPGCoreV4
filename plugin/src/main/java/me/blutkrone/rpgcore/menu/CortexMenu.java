package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.npc.CoreNPC;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public abstract class CortexMenu extends AbstractCoreMenu {

    protected final CoreNPC npc;
    protected List<AbstractCoreTrait> traits;

    CortexMenu(List<AbstractCoreTrait> traits, CoreNPC npc) {
        super(6);
        this.traits = traits;
        this.npc = npc;
    }

    @Override
    public void click(InventoryClickEvent event) {
        event.setCancelled(true);
        AbstractCoreTrait trait = getTraitFromIcon(event.getCurrentItem());
        if (trait != null) {
            this.getMenu().stalled(() -> {
                this.getMenu().getViewer().closeInventory();
                trait.engage(this.getMenu().getViewer(), npc);
            });
        }
    }

    /*
     * Recover a trait instance from an icon.
     *
     * @param item
     * @return
     */
    private AbstractCoreTrait getTraitFromIcon(ItemStack item) {
        // check if there is a brand to fetch
        String brand = IChestMenu.getBrand(item, RPGCore.inst(), "cortex-id", null);
        if (brand == null) {
            return null;
        }
        // match identifier with a trait
        UUID uuid = UUID.fromString(brand);
        for (AbstractCoreTrait trait : this.traits) {
            if (trait.getUuid().equals(uuid)) {
                return trait;
            }
        }
        // no trait matched with us
        return null;
    }

    @Override
    public boolean isTrivial() {
        return true;
    }

    public static class Cortex2 extends CortexMenu {

        public Cortex2(List<AbstractCoreTrait> traits, CoreNPC npc) {
            super(traits, npc);
        }

        @Override
        public void rebuild() {
            this.getMenu().clearItems();

            // build basic background
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.shiftToExact(-208);
            msb.append(resourcepack().texture("menu_cortex_2"), ChatColor.WHITE);

            // extract traits we are working on
            AbstractCoreTrait trait1 = traits.get(0);
            AbstractCoreTrait trait2 = traits.get(1);

            // update menu design
            msb.shiftToExact(0).append(resourcepack().texture("cortex_large_" + trait1.getSymbol() + "_0", "cortex_large_default_0"), ChatColor.WHITE);
            msb.shiftToExact(0).append(resourcepack().texture("cortex_large_" + trait2.getSymbol() + "_1", "cortex_large_default_1"), ChatColor.WHITE);

            // place clickable items
            Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26)
                    .forEach(i -> this.getMenu().setItemAt(i, trait1.getIcon()));
            Arrays.asList(27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53)
                    .forEach(i -> this.getMenu().setItemAt(i, trait2.getIcon()));

            this.getMenu().setTitle(msb.compile());
        }
    }

    public static class Cortex3 extends CortexMenu {

        public Cortex3(List<AbstractCoreTrait> traits, CoreNPC npc) {
            super(traits, npc);
        }

        @Override
        public void rebuild() {
            this.getMenu().clearItems();

            // build basic background
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.shiftToExact(-208);
            msb.append(resourcepack().texture("menu_cortex_3"), ChatColor.WHITE);

            // extract traits we are working on
            AbstractCoreTrait trait1 = traits.get(0);
            AbstractCoreTrait trait2 = traits.get(1);
            AbstractCoreTrait trait3 = traits.get(2);

            // update menu design
            msb.shiftToExact(0).append(resourcepack().texture("cortex_large_" + trait1.getSymbol() + "_0", "cortex_large_default_0"), ChatColor.WHITE);
            msb.shiftToExact(0).append(resourcepack().texture("cortex_medium_" + trait2.getSymbol() + "_1", "cortex_medium_default_1"), ChatColor.WHITE);
            msb.shiftToExact(81).append(resourcepack().texture("cortex_medium_" + trait3.getSymbol() + "_1", "cortex_medium_default_1"), ChatColor.WHITE);

            // place clickable items
            Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26)
                    .forEach(i -> this.getMenu().setItemAt(i, trait1.getIcon()));
            Arrays.asList(27, 28, 29, 30, 36, 37, 38, 39, 45, 46, 47, 48)
                    .forEach(i -> this.getMenu().setItemAt(i, trait2.getIcon()));
            Arrays.asList(32, 33, 34, 35, 41, 42, 43, 44, 50, 51, 52, 53)
                    .forEach(i -> this.getMenu().setItemAt(i, trait3.getIcon()));

            this.getMenu().setTitle(msb.compile());
        }
    }

    public static class Cortex4 extends CortexMenu {

        public Cortex4(List<AbstractCoreTrait> traits, CoreNPC npc) {
            super(traits, npc);
        }

        @Override
        public void rebuild() {
            this.getMenu().clearItems();

            // build basic background
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.shiftToExact(-208);
            msb.append(resourcepack().texture("menu_cortex_4"), ChatColor.WHITE);

            // extract traits we are working on
            AbstractCoreTrait trait1 = traits.get(0);
            AbstractCoreTrait trait2 = traits.get(1);
            AbstractCoreTrait trait3 = traits.get(2);
            AbstractCoreTrait trait4 = traits.get(3);

            // update menu design
            msb.shiftToExact(0).append(resourcepack().texture("cortex_medium_" + trait1.getSymbol() + "_0", "cortex_medium_default_ß"), ChatColor.WHITE);
            msb.shiftToExact(81).append(resourcepack().texture("cortex_medium_" + trait2.getSymbol() + "_0", "cortex_medium_default_0"), ChatColor.WHITE);
            msb.shiftToExact(0).append(resourcepack().texture("cortex_medium_" + trait3.getSymbol() + "_1", "cortex_medium_default_1"), ChatColor.WHITE);
            msb.shiftToExact(81).append(resourcepack().texture("cortex_medium_" + trait4.getSymbol() + "_1", "cortex_medium_default_1"), ChatColor.WHITE);

            // place clickable items
            Arrays.asList(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21)
                    .forEach(i -> this.getMenu().setItemAt(i, trait1.getIcon()));
            Arrays.asList(5, 6, 7, 8, 14, 15, 16, 17, 23, 24, 25, 26)
                    .forEach(i -> this.getMenu().setItemAt(i, trait2.getIcon()));
            Arrays.asList(27, 28, 29, 30, 36, 37, 38, 39, 45, 46, 47, 48)
                    .forEach(i -> this.getMenu().setItemAt(i, trait3.getIcon()));
            Arrays.asList(32, 33, 34, 35, 41, 42, 43, 44, 50, 51, 52, 53)
                    .forEach(i -> this.getMenu().setItemAt(i, trait4.getIcon()));

            this.getMenu().setTitle(msb.compile());
        }
    }

    public static class Cortex5 extends CortexMenu {

        public Cortex5(List<AbstractCoreTrait> traits, CoreNPC npc) {
            super(traits, npc);
        }

        @Override
        public void rebuild() {
            this.getMenu().clearItems();

            // build basic background
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.shiftToExact(-208);
            msb.append(resourcepack().texture("menu_cortex_5"), ChatColor.WHITE);

            // extract traits we are working on
            AbstractCoreTrait trait1 = traits.get(0);
            AbstractCoreTrait trait2 = traits.get(1);
            AbstractCoreTrait trait3 = traits.get(2);
            AbstractCoreTrait trait4 = traits.get(3);
            AbstractCoreTrait trait5 = traits.get(4);

            // update menu design
            msb.shiftToExact(0).append(resourcepack().texture("cortex_medium_" + trait1.getSymbol() + "_0", "cortex_medium_default_0"), ChatColor.WHITE);
            msb.shiftToExact(0).append(resourcepack().texture("cortex_medium_" + trait2.getSymbol() + "_1", "cortex_medium_default_1"), ChatColor.WHITE);
            msb.shiftToExact(81).append(resourcepack().texture("cortex_small_" + trait3.getSymbol() + "_0", "cortex_small_default_0"), ChatColor.WHITE);
            msb.shiftToExact(81).append(resourcepack().texture("cortex_small_" + trait4.getSymbol() + "_1", "cortex_small_default_1"), ChatColor.WHITE);
            msb.shiftToExact(81).append(resourcepack().texture("cortex_small_" + trait5.getSymbol() + "_2", "cortex_small_default_2"), ChatColor.WHITE);

            // place clickable items
            Arrays.asList(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21)
                    .forEach(i -> this.getMenu().setItemAt(i, trait1.getIcon()));
            Arrays.asList(27, 28, 29, 30, 36, 37, 38, 39, 45, 46, 47, 48)
                    .forEach(i -> this.getMenu().setItemAt(i, trait2.getIcon()));
            Arrays.asList(5, 6, 7, 8, 14, 15, 16, 17)
                    .forEach(i -> this.getMenu().setItemAt(i, trait3.getIcon()));
            Arrays.asList(23, 24, 25, 26, 32, 33, 34, 35)
                    .forEach(i -> this.getMenu().setItemAt(i, trait4.getIcon()));
            Arrays.asList(41, 42, 43, 44, 50, 51, 52, 53)
                    .forEach(i -> this.getMenu().setItemAt(i, trait5.getIcon()));

            this.getMenu().setTitle(msb.compile());
        }
    }

    public static class Cortex6 extends CortexMenu {

        public Cortex6(List<AbstractCoreTrait> traits, CoreNPC npc) {
            super(traits, npc);
        }

        @Override
        public void rebuild() {
            this.getMenu().clearItems();

            // build basic background
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.shiftToExact(-208);
            msb.append(resourcepack().texture("menu_cortex_6"), ChatColor.WHITE);

            // extract traits we are working on
            AbstractCoreTrait trait1 = traits.get(0);
            AbstractCoreTrait trait2 = traits.get(1);
            AbstractCoreTrait trait3 = traits.get(2);
            AbstractCoreTrait trait4 = traits.get(3);
            AbstractCoreTrait trait5 = traits.get(4);
            AbstractCoreTrait trait6 = traits.get(5);

            // update menu design
            msb.shiftToExact(0).append(resourcepack().texture("cortex_small_" + trait1.getSymbol() + "_0", "cortex_small_default_0"), ChatColor.WHITE);
            msb.shiftToExact(0).append(resourcepack().texture("cortex_small_" + trait2.getSymbol() + "_1", "cortex_small_default_1"), ChatColor.WHITE);
            msb.shiftToExact(0).append(resourcepack().texture("cortex_small_" + trait3.getSymbol() + "_2", "cortex_small_default_2"), ChatColor.WHITE);
            msb.shiftToExact(81).append(resourcepack().texture("cortex_small_" + trait4.getSymbol() + "_0", "cortex_small_default_0"), ChatColor.WHITE);
            msb.shiftToExact(81).append(resourcepack().texture("cortex_small_" + trait5.getSymbol() + "_1", "cortex_small_default_1"), ChatColor.WHITE);
            msb.shiftToExact(81).append(resourcepack().texture("cortex_small_" + trait6.getSymbol() + "_2", "cortex_small_default_2"), ChatColor.WHITE);

            // place clickable items
            Arrays.asList(0, 1, 2, 3, 9, 10, 11, 12)
                    .forEach(i -> this.getMenu().setItemAt(i, trait1.getIcon()));
            Arrays.asList(18, 19, 20, 21, 27, 28, 29, 30)
                    .forEach(i -> this.getMenu().setItemAt(i, trait2.getIcon()));
            Arrays.asList(36, 37, 38, 39, 45, 46, 47, 48)
                    .forEach(i -> this.getMenu().setItemAt(i, trait2.getIcon()));
            Arrays.asList(5, 6, 7, 8, 14, 15, 16, 17)
                    .forEach(i -> this.getMenu().setItemAt(i, trait3.getIcon()));
            Arrays.asList(23, 24, 25, 26, 32, 33, 34, 35)
                    .forEach(i -> this.getMenu().setItemAt(i, trait4.getIcon()));
            Arrays.asList(41, 42, 43, 44, 50, 51, 52, 53)
                    .forEach(i -> this.getMenu().setItemAt(i, trait5.getIcon()));

            this.getMenu().setTitle(msb.compile());
        }
    }
}
