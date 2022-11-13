package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.item.data.ItemDataGeneric;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.skill.CoreSkill;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SkillMenu extends AbstractCoreMenu {

    private me.blutkrone.rpgcore.hud.menu.SkillMenu origin;

    public SkillMenu(me.blutkrone.rpgcore.hud.menu.SkillMenu origin) {
        super(2);
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
            ItemStack icon = skill == null ? origin.empty_icon : skill.getItem(core_player);
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
                new PassiveMenu("skill_" + skill.getId()).finish(((Player) event.getWhoClicked()));
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
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());

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

            // extract skills
            List<String> skills = new ArrayList<>(origin.skill_pages.get(0).skills);
            skills.removeIf(id -> {
                // check if the skill is considered hidden
                CoreSkill skill = RPGCore.inst().getSkillManager().getIndex().get(id);
                if (!skill.isHidden()) {
                    return false;
                }
                // check if we hold a relevant tag to acquire the skill
                return !core_player.checkForTag("skill_" + id.toLowerCase());
            });

            // note down the icons
            getMenu().setItemAt(0, origin.empty_icon);
            for (int i = 0; i < skills.size() && i < 35; i++) {
                String id = skills.get(i);
                CoreSkill skill = RPGCore.inst().getSkillManager().getIndex().get(id);
                getMenu().setItemAt(1 + i, skill.getItem(core_player));
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
                ItemDataGeneric data = RPGCore.inst().getItemManager().getItemData(event.getCurrentItem(), ItemDataGeneric.class);
                if (data != null) {
                    String id = data.getItem().getHidden("skill").orElse(null);
                    if (id != null) {
                        CoreSkill skill = RPGCore.inst().getSkillManager().getIndex().get(id);
                        CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(event.getWhoClicked());
                        player.getSkillbar().setSkill(slot, skill);
                        Bukkit.getScheduler().runTask(RPGCore.inst(), () -> event.getWhoClicked().closeInventory());
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
