package me.blutkrone.rpgcore.hud.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.skill.CoreSkill;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A menu dedicated to allow players customize their skillbar
 * and the behaviour of their selected skills.
 */
public class SkillMenu {
    // control elements
    private ItemStack unbind_skill;
    private ItemStack viewport_left;
    private ItemStack viewport_right;
    private ItemStack evolution_available;
    private ItemStack evolution_unavailable;
    private ItemStack evolution_destroyer;
    private ItemStack empty_icon;
    // page designs for skill evolution
    private Map<String, EvolvePage> evolve_pages = new HashMap<>();
    // pages for customizing skills
    private List<SkillPage> skill_pages = new ArrayList<>();

    /**
     * A menu dedicated to allow players customize their skillbar
     * and the behaviour of their selected skills.
     */
    public SkillMenu() throws IOException {
        ConfigWrapper config = FileUtil.asConfigYML(FileUtil.file("menu", "skill.yml"));

        config.forEachUnder("evolution-pages", (path, root) -> this.evolve_pages.put(path, new EvolvePage(root.getSection(path))));
        config.forEachUnder("skill-pages", (path, root) -> this.skill_pages.add(new SkillPage(root.getSection(path))));

        this.unbind_skill = RPGCore.inst().getLanguageManager().getAsItem("unbind_skill").build();
        this.viewport_left = RPGCore.inst().getLanguageManager().getAsItem("viewport_left").build();
        this.viewport_right = RPGCore.inst().getLanguageManager().getAsItem("viewport_right").build();
        this.evolution_available = RPGCore.inst().getLanguageManager().getAsItem("evolution_available").build();
        this.evolution_unavailable = RPGCore.inst().getLanguageManager().getAsItem("evolution_unavailable").build();
        this.evolution_destroyer = RPGCore.inst().getLanguageManager().getAsItem("evolution_destroyer").build();
        this.empty_icon = RPGCore.inst().getLanguageManager().getAsItem("skillbar_empty").build();
    }

    /**
     * Open the skill menu for the given player.
     *
     * @param _player who to present the skill menu to.
     */
    public void open(Player _player) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        IChestMenu menu = RPGCore.inst().getVolatileManager().createMenu(1, _player);
        menu.setRebuilder(() -> {
            // clear out all items on the menu
            menu.clearItems();

            // updated msb title for the menu
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.retreat(8);
            msb.append(rpm.texture("menu_skill_hotbar"), ChatColor.WHITE);

            // fetch the current skillbar config
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(menu.getViewer());
            for (int i = 0; i < 6; i++) {
                CoreSkill skill = core_player.getSkillbar().getSkill(i);
                ItemStack icon = skill == null ? this.empty_icon : skill.getItem();
                menu.setItemAt(2 + i, ItemBuilder.of(icon.clone()).persist("hotbar-slot", i).build());
            }

            InstructionBuilder instructions = new InstructionBuilder();
            instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_skill"));
            instructions.apply(msb);

            // supply the title to the player
            menu.setTitle(msb.compile());
        });
        menu.setClickHandler(event -> {
            event.setCancelled(true);
            if (event.getCurrentItem() == null)
                return;
            if (event.getCurrentItem().getType() == Material.AIR)
                return;
            ItemMeta meta = event.getCurrentItem().getItemMeta();
            if (meta == null)
                return;
            PersistentDataContainer data = meta.getPersistentDataContainer();
            // ensure that we clicked a hotbar slot
            int slot = data.getOrDefault(new NamespacedKey(RPGCore.inst(), "hotbar-slot"), PersistentDataType.INTEGER, -1);
            if (slot == -1) {
                return;
            } else if (event.getClick() == ClickType.LEFT) {
                menu.stalled(() -> {
                    event.getWhoClicked().closeInventory();
                    rebind((Player) event.getWhoClicked(), slot, menu);
                });
            } else if (event.getClick() == ClickType.SHIFT_LEFT) {
                CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(event.getWhoClicked());
                CoreSkill skill = core_player.getSkillbar().getSkill(slot);
                if (skill != null) {
                    EvolvePage page = this.evolve_pages.get(skill.getEvolutionType());
                    if (page != null) {
                        menu.stalled(() -> {
                            event.getWhoClicked().closeInventory();
                            evolve(((Player) event.getWhoClicked()), skill, menu, page);
                        });
                    } else {
                        event.getWhoClicked().sendMessage("§cSkill has no valid evolution page!");
                    }
                } else {
                    event.getWhoClicked().sendMessage(RPGCore.inst().getLanguageManager().getTranslation("evolution_without_skill"));
                }
            }
        });
        menu.open();
    }

    /*
     * Rebind the given menu on the skillbar.
     *
     * @param _player whose skillbar to update.
     * @param slot the slot to be updated.
     */
    private void rebind(Player _player, int slot, IChestMenu _back) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        IChestMenu menu = RPGCore.inst().getVolatileManager().createMenu(6, _player);
        menu.setData("slot", slot);
        menu.setData("viewport", 0);
        menu.setData("back", _back);
        menu.setRebuilder(() -> {
            // clear out all items on the menu
            menu.clearItems();

            // updated msb title for the menu
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.retreat(8);
            msb.append(rpm.texture("menu_skill_rebind"), ChatColor.WHITE);

            // create icons to swap between pages
            if (this.skill_pages.size() <= 9) {
                // we can just view everything
                for (int i = 0; i < skill_pages.size(); i++) {
                    menu.setItemAt(45 + i, skill_pages.get(i).icon);
                }
            } else {
                int viewport = menu.getData("viewport", 0);

                // create viewport for scrolling
                menu.setItemAt(45, this.viewport_left);
                menu.setItemAt(53, this.viewport_right);
                for (int i = viewport; i < viewport + 7; i++) {
                    menu.setItemAt(46 + i, this.skill_pages.get(i).icon);
                }
            }

            // note down the icons
            menu.setItemAt(0, this.empty_icon);
            List<String> skills = this.skill_pages.get(0).skills;
            for (int i = 0; i < skills.size() && i < 35; i++) {
                String id = skills.get(i);
                CoreSkill skill = RPGCore.inst().getSkillManager().getIndex().get(id);
                menu.setItemAt(1 + i, skill.getItem());
            }

            InstructionBuilder instructions = new InstructionBuilder();
            instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_skill"));
            instructions.apply(msb);

            menu.setTitle(msb.compile());
        });
        menu.setClickHandler(event -> {
            event.setCancelled(true);
            if (event.getCurrentItem() == null)
                return;
            if (event.getCurrentItem().getType() == Material.AIR)
                return;
            if (event.getClick() != ClickType.LEFT)
                return;
            ItemMeta meta = event.getCurrentItem().getItemMeta();
            if (meta == null)
                return;

            int viewport = menu.getData("viewport", 0);

            if (this.unbind_skill.isSimilar(event.getCurrentItem())) {
                CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(event.getWhoClicked());
                player.getSkillbar().setSkill(menu.getData("slot", 0), null);
                Bukkit.getScheduler().runTask(RPGCore.inst(), () -> event.getWhoClicked().closeInventory());
            } else if (this.viewport_left.isSimilar(event.getCurrentItem())) {
                // shift viewport to the left
                menu.setData("viewport", Math.max(0, Math.min(viewport - 1, skill_pages.size() - 7)));
            } else if (this.viewport_right.isSimilar(event.getCurrentItem())) {
                // shift viewport to the right
                menu.setData("viewport", Math.max(0, Math.min(viewport + 1, skill_pages.size() - 7)));
            } else {
                // retrieve the skill ID, apply it, return back
                PersistentDataContainer data = meta.getPersistentDataContainer();
                String id = data.get(new NamespacedKey(RPGCore.inst(), "skill-id"), PersistentDataType.STRING);
                if (id != null) {
                    CoreSkill skill = RPGCore.inst().getSkillManager().getIndex().get(id);
                    CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(event.getWhoClicked());
                    player.getSkillbar().setSkill(menu.getData("slot", 0), skill);
                    Bukkit.getScheduler().runTask(RPGCore.inst(), () -> event.getWhoClicked().closeInventory());
                }
            }
        });

        menu.open();
    }

    /*
     * Update the evolution of the given skill.
     *
     * @param _player whose skill to evolve.
     * @param skill the skill to be evolved.
     */
    private void evolve(Player _player, CoreSkill _skill, IChestMenu _back, EvolvePage _page) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        IChestMenu menu = RPGCore.inst().getVolatileManager().createMenu(6, _player);
        menu.setData("skill", _skill);
        menu.setData("back", _back);
        menu.setData("page", _page);
        menu.setRebuilder(() -> {
            CoreSkill skill = menu.getData("skill");
            EvolvePage page = menu.getData("page");
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(menu.getViewer());

            // clear out all items on the menu
            menu.clearItems();

            // updated msb title for the menu
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.retreat(8);
            msb.append(rpm.texture("menu_" + page.menu_prefix + "base"), ChatColor.WHITE);

            // verify the data integrity of the user
            if (core_player.fixEvolution(skill.getId(), page.position)) {
                core_player.getEntity().sendMessage("§cBad config, some data was lost.");
            }

            // render the unlocked slots
            for (int slot : page.position) {
                if (core_player.hasUnlockedEvolutionSlot(skill.getId(), slot)) {
                    String fitted = core_player.getEvolution(skill.getId()).get(slot);
                    if (fitted != null) {
                        menu.setItemAt(slot, ItemBuilder.of(Material.DIAMOND)
                                .name("§fEvolution:" + fitted)
                                .persist("skill-evolution-stone", 1)
                                .build());
                    } else {
                        menu.setItemAt(slot, this.evolution_available);
                    }

                    msb.shiftToExact(-8);
                    msb.append(rpm.texture("menu_" + page.menu_prefix + "" + slot), ChatColor.WHITE);
                } else {
                    menu.setItemAt(slot, this.evolution_unavailable);
                }
            }

            // info item of the skill
            menu.setItemAt(page.skill_position, skill.getItem());

            InstructionBuilder instructions = new InstructionBuilder();
            instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_skill_evolve"));
            instructions.apply(msb);

            // send the title to the player
            menu.setTitle(msb.compile());
        });
        menu.setClickHandler(event -> {
            CoreSkill skill = menu.getData("skill");
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

                if (this.evolution_unavailable.isSimilar(clicked)) {
                    // check for slot unlocking
                    ItemMeta meta = cursor.getItemMeta();
                    if (meta != null) {
                        PersistentDataContainer data = meta.getPersistentDataContainer();
                        int key_grade = data.getOrDefault(new NamespacedKey(RPGCore.inst(), "skill-evolution-key"), PersistentDataType.INTEGER, 0);
                        if (key_grade > 0) {
                            // check if the grade is enough
                            if (core_player.tryToUnlockEvolveWithKey(skill.getId(), event.getSlot(), key_grade)) {
                                // do a full rebuild to cover changes
                                menu.stalled(menu::rebuild);
                                // consume 1 of the evolution keys
                                cursor.setAmount(cursor.getAmount() - 1);
                            } else {
                                event.getWhoClicked().sendMessage(RPGCore.inst().getLanguageManager().getTranslation("evolution_key_too_low"));
                            }
                        }
                    }
                } else if (this.evolution_destroyer.isSimilar(cursor)) {
                    // destroy the current fitted evolution
                    ItemMeta meta = cursor.getItemMeta();
                    if (meta != null) {
                        PersistentDataContainer data = meta.getPersistentDataContainer();
                        int evolution_item = data.getOrDefault(new NamespacedKey(RPGCore.inst(), "skill-evolution-stone"), PersistentDataType.INTEGER, 0);
                        if (evolution_item == 1) {
                            // clear the current fitting
                            event.setCurrentItem(this.evolution_available);
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
        });
        menu.open();
    }

    /*
     * A page design for the evolution of a certain skill,
     * this is merely the presentation approach.
     */
    class EvolvePage {
        // position of the skill icon
        int skill_position;
        // prefix for the menu design
        String menu_prefix;
        // the positions of the slots
        List<Integer> position;

        EvolvePage(ConfigWrapper config) {
            this.menu_prefix = config.getString("menu-prefix");
            this.skill_position = config.getInt("skill-position");
            this.position = config.getIntegerList("position");
        }
    }

    /*
     * A self-contained page which can be configured to
     * present a selection of 35 skills (the 36th would
     * always be 'unbind'
     */
    class SkillPage {
        ItemStack icon;
        List<String> skills;

        SkillPage(ConfigWrapper config) {
            this.icon = RPGCore.inst().getLanguageManager().getAsItem("icon").build();
            this.skills = config.getStringList("skills");
        }
    }
}
