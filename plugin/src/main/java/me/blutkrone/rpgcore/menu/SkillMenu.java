package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.skill.CoreSkill;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class SkillMenu extends AbstractCoreMenu {

    private me.blutkrone.rpgcore.hud.menu.SkillMenu origin;

    public SkillMenu(me.blutkrone.rpgcore.hud.menu.SkillMenu origin) {
        super(1);
        this.origin = origin;
    }

    @Override
    public void rebuild() {
        // clear out all items on the menu
        getMenu().clearItems();

        // updated msb title for the menu
        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-208);
        msb.append(resourcepack().texture("menu_skill_hotbar"), ChatColor.WHITE);

        // fetch the current skillbar config
        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());
        for (int i = 0; i < 6; i++) {
            CoreSkill skill = core_player.getSkillbar().getSkill(i);
            ItemStack icon = skill == null ? origin.empty_icon : skill.getItem();
            icon = icon.clone();
            IChestMenu.setBrand(icon, RPGCore.inst(), "hotbar-slot", String.valueOf(i));
            getMenu().setItemAt(2 + i, icon);
        }

        InstructionBuilder instructions = new InstructionBuilder();
        instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_skill"));
        instructions.apply(msb);

        // supply the title to the player
        getMenu().setTitle(msb.compile());
    }

    @Override
    public void click(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!isRelevant(event.getCurrentItem())) {
            return;
        }

        // ensure that we clicked a hotbar slot
        int slot = Integer.parseInt(IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "hotbar-slot", "-1"));
        if (slot == -1) {
            return;
        }

        if (event.getClick() == ClickType.LEFT) {
            getMenu().stalled(() -> {
                event.getWhoClicked().closeInventory();
                new Rebind(getMenu(), slot).finish(getMenu().getViewer());
            });
        } else if (event.getClick() == ClickType.SHIFT_LEFT) {
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(event.getWhoClicked());
            CoreSkill skill = core_player.getSkillbar().getSkill(slot);
            if (skill != null) {
                me.blutkrone.rpgcore.hud.menu.SkillMenu.EvolvePage page = origin.evolve_pages.get(skill.getEvolutionType());
                if (page != null) {
                    getMenu().stalled(() -> {
                        event.getWhoClicked().closeInventory();
                        new Evolve(getMenu(), skill, page).finish(getMenu().getViewer());
                    });
                } else {
                    event.getWhoClicked().sendMessage("§cSkill has no valid evolution page!");
                }
            } else {
                event.getWhoClicked().sendMessage(RPGCore.inst().getLanguageManager().getTranslation("evolution_without_skill"));
            }
        }
    }

    private class Rebind extends AbstractCoreMenu {

        private IChestMenu parent;
        private int slot;
        private int viewport;

        public Rebind(IChestMenu parent, int slot) {
            super(6);
            this.parent = parent;
            this.slot = slot;
        }

        @Override
        public void rebuild() {
            // clear out all items on the menu
            getMenu().clearItems();

            // updated msb title for the menu
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.shiftToExact(-208);
            msb.append(resourcepack().texture("menu_skill_rebind"), ChatColor.WHITE);

            // create icons to swap between pages
            if (origin.skill_pages.size() <= 9) {
                // we can just view everything
                for (int i = 0; i < origin.skill_pages.size(); i++) {
                    getMenu().setItemAt(45 + i, origin.skill_pages.get(i).icon);
                }
            } else {
                // create viewport for scrolling
                getMenu().setItemAt(45, origin.viewport_left);
                getMenu().setItemAt(53, origin.viewport_right);
                for (int i = viewport; i < viewport + 7; i++) {
                    getMenu().setItemAt(46 + i, origin.skill_pages.get(i).icon);
                }
            }

            // note down the icons
            getMenu().setItemAt(0, origin.empty_icon);
            List<String> skills = origin.skill_pages.get(0).skills;
            for (int i = 0; i < skills.size() && i < 35; i++) {
                String id = skills.get(i);
                CoreSkill skill = RPGCore.inst().getSkillManager().getIndex().get(id);
                getMenu().setItemAt(1 + i, skill.getItem());
            }

            InstructionBuilder instructions = new InstructionBuilder();
            instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_skill"));
            instructions.apply(msb);

            getMenu().setTitle(msb.compile());
        }

        @Override
        public void click(InventoryClickEvent event) {
            event.setCancelled(true);
            if (!isRelevant(event.getCurrentItem())) {
                return;
            }

            if (origin.unbind_skill.isSimilar(event.getCurrentItem())) {
                CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(event.getWhoClicked());
                player.getSkillbar().setSkill(slot, null);
                Bukkit.getScheduler().runTask(RPGCore.inst(), () -> event.getWhoClicked().closeInventory());
            } else if (origin.viewport_left.isSimilar(event.getCurrentItem())) {
                // shift viewport to the left
                viewport = Math.max(0, Math.min(viewport - 1, origin.skill_pages.size() - 7));
            } else if (origin.viewport_right.isSimilar(event.getCurrentItem())) {
                // shift viewport to the right
                viewport = Math.max(0, Math.min(viewport + 1, origin.skill_pages.size() - 7));
            } else {
                // retrieve the skill ID, apply it, return back
                String id = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "skill-id", null);
                if (id != null) {
                    CoreSkill skill = RPGCore.inst().getSkillManager().getIndex().get(id);
                    CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(event.getWhoClicked());
                    player.getSkillbar().setSkill(slot, skill);
                    Bukkit.getScheduler().runTask(RPGCore.inst(), () -> event.getWhoClicked().closeInventory());
                }
            }
        }

        @Override
        public void close(InventoryCloseEvent event) {
            suggestOpen(parent);
        }
    }

    private class Evolve extends AbstractCoreMenu {

        private IChestMenu parent;
        private CoreSkill skill;
        private me.blutkrone.rpgcore.hud.menu.SkillMenu.EvolvePage page;

        public Evolve(IChestMenu parent, CoreSkill skill, me.blutkrone.rpgcore.hud.menu.SkillMenu.EvolvePage page) {
            super(6);
            this.parent = parent;
            this.skill = skill;
            this.page = page;

        }

        @Override
        public void rebuild() {
            // clear out all items on the menu
            getMenu().clearItems();

            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());

            // updated msb title for the menu
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.shiftToExact(-208);
            msb.append(resourcepack().texture("menu_" + page.menu_prefix + "base"), ChatColor.WHITE);

            // verify the data integrity of the user
            if (core_player.fixEvolution(skill.getId(), page.position)) {
                core_player.getEntity().sendMessage("§cBad config, some data was lost.");
            }

            // render the unlocked slots
            for (int slot : page.position) {
                if (core_player.hasUnlockedEvolutionSlot(skill.getId(), slot)) {
                    String fitted = core_player.getEvolution(skill.getId()).get(slot);
                    if (fitted != null) {
                        getMenu().setItemAt(slot, ItemBuilder.of(Material.DIAMOND)
                                .name("§fEvolution:" + fitted)
                                .persist("skill-evolution-stone", 1)
                                .build());
                    } else {
                        getMenu().setItemAt(slot, origin.evolution_available);
                    }

                    msb.shiftToExact(-208);
                    msb.append(resourcepack().texture("menu_" + page.menu_prefix + "" + slot), ChatColor.WHITE);
                } else {
                    getMenu().setItemAt(slot, origin.evolution_unavailable);
                }
            }

            // info item of the skill
            getMenu().setItemAt(page.skill_position, skill.getItem());

            InstructionBuilder instructions = new InstructionBuilder();
            instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_skill_evolve"));
            instructions.apply(msb);

            // send the title to the player
            getMenu().setTitle(msb.compile());
        }

        @Override
        public void click(InventoryClickEvent event) {
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(event.getWhoClicked());

            if (event.getClick() != ClickType.LEFT) {
                // reject  anything except simple left clicks
                event.setCancelled(true);
            } else if (event.getClickedInventory() == event.getView().getTopInventory()) {
                event.setCancelled(true);

                // process interaction with the actual menu
                ItemStack cursor = new ItemStack(Material.AIR);
                if (event.getCursor() != null)
                    cursor = event.getCursor();
                ItemStack clicked = new ItemStack(Material.AIR);
                if (event.getCurrentItem() != null)
                    clicked = event.getCurrentItem();

                if (origin.evolution_unavailable.isSimilar(clicked)) {
                    // check for slot unlocking
                    ItemMeta meta = cursor.getItemMeta();
                    if (meta != null) {
                        PersistentDataContainer data = meta.getPersistentDataContainer();
                        int key_grade = data.getOrDefault(new NamespacedKey(RPGCore.inst(), "skill-evolution-key"), PersistentDataType.INTEGER, 0);
                        if (key_grade > 0) {
                            // check if the grade is enough
                            if (core_player.tryToUnlockEvolveWithKey(skill.getId(), event.getSlot(), key_grade)) {
                                // do a full rebuild to cover changes
                                getMenu().queryRebuild();
                                // consume 1 of the evolution keys
                                cursor.setAmount(cursor.getAmount() - 1);
                            } else {
                                event.getWhoClicked().sendMessage(RPGCore.inst().getLanguageManager().getTranslation("evolution_key_too_low"));
                            }
                        }
                    }
                } else if (origin.evolution_destroyer.isSimilar(cursor)) {
                    // destroy the current fitted evolution
                    ItemMeta meta = cursor.getItemMeta();
                    if (meta != null) {
                        PersistentDataContainer data = meta.getPersistentDataContainer();
                        int evolution_item = data.getOrDefault(new NamespacedKey(RPGCore.inst(), "skill-evolution-stone"), PersistentDataType.INTEGER, 0);
                        if (evolution_item == 1) {
                            // clear the current fitting
                            event.setCurrentItem(origin.evolution_available);
                            // consume 1 of the cursor units
                            cursor.setAmount(cursor.getAmount() - 1);
                            // update the skill configuration
                            core_player.setEvolution(skill.getId(), event.getSlot(), null);
                        }
                    }
                } else if (!cursor.isSimilar(clicked)) {
                    // check if trying to replace fitting
                    ItemMeta meta = cursor.getItemMeta();
                    if (meta != null) {
                        PersistentDataContainer data = meta.getPersistentDataContainer();
                        String evolution_item = data.getOrDefault(new NamespacedKey(RPGCore.inst(), "skill-evolution-stone"), PersistentDataType.STRING, "");
                        if (!"".equals(evolution_item)) {
                            // override previous fitting with 1 of the cursor
                            clicked = cursor.clone();
                            clicked.setAmount(1);
                            event.setCurrentItem(clicked);
                            // consume 1 from the cursor
                            cursor.setAmount(cursor.getAmount() - 1);
                            // update the entity configuration
                            core_player.setEvolution(skill.getId(), event.getSlot(), evolution_item);
                        }
                    }
                }
            }
        }

        @Override
        public void close(InventoryCloseEvent event) {
            suggestOpen(parent);
        }
    }
}
