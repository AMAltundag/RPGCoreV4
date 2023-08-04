package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.social.IGroupHandler;
import me.blutkrone.rpgcore.api.social.IPartySnapshot;
import me.blutkrone.rpgcore.chat.PlayerSnapshot;
import me.blutkrone.rpgcore.damage.DamageMetric;
import me.blutkrone.rpgcore.hud.menu.EquipMenu;
import me.blutkrone.rpgcore.item.styling.IDescriptionRequester;
import me.blutkrone.rpgcore.job.CoreJob;
import me.blutkrone.rpgcore.skill.CoreSkill;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * A menu meant to show information in a social context, this
 * refers to things such as equipment, skills, passive tree
 * and a couple interfaces to social structures.
 * <br>
 * This can also work with offline players.
 * <br>
 * This works off the latest data snapshot.
 */
public class SocialMenu extends AbstractCoreMenu {

    private me.blutkrone.rpgcore.hud.menu.SocialMenu template;
    private SocialInfoPlayer info_player;

    private ItemStack icon_invite_party;
    private ItemStack icon_kick_party;
    private ItemStack icon_join_party;

    private long cooldown = System.currentTimeMillis();

    public SocialMenu(UUID player, me.blutkrone.rpgcore.hud.menu.SocialMenu template) {
        super(6);
        this.template = template;
        this.info_player = null;
        // language info
        icon_invite_party = language().getAsItem("icon_invite_party").build();
        icon_kick_party = language().getAsItem("icon_kick_party").build();
        icon_join_party = language().getAsItem("icon_invite_party").build();
        // threaded effort to read target info
        Bukkit.getScheduler().runTaskAsynchronously(RPGCore.inst(), () -> {
            SocialInfoPlayer info_player = new SocialInfoPlayer(player);
            Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                this.info_player = info_player;
                // flush the info shown for that player
                for (ItemStack stack : this.info_player.getView().equipment.values()) {
                    RPGCore.inst().getItemManager().describe(stack, info_player);
                }
                for (ItemStack stack : this.info_player.getView().equipment.values()) {
                    RPGCore.inst().getItemManager().describe(stack, info_player);
                }

                rebuild();
            });
        });

        RPGCore.inst().getLogger().info("not implemented (passive tree from socials)");
    }

    @Override
    public void rebuild() {
        getMenu().clearItems();

        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-208);
        msb.append(resourcepack().texture("menu_player_social"), ChatColor.WHITE);

        if (this.info_player != null) {
            // show the equipment of target player
            this.info_player.getView().equipment.forEach((slot, item) -> {
                Integer where = this.template.getEquipmentSlots().get(slot);
                if (where != null) {
                    if (item == null || item.getType().isAir()) {
                        for (EquipMenu.Slot equip_slot : RPGCore.inst().getHUDManager().getEquipMenu().slots) {
                            if (equip_slot.id.equals(slot)) {
                                getMenu().setItemAt(where, equip_slot.empty);
                                break;
                            }
                        }
                    } else {
                        getMenu().setItemAt(where, item);
                    }
                }
            });
            // show the skills of target player
            for (int i = 0; i < template.getSkillSlots().size(); i++) {
                String skill = this.info_player.getView().skillbar[i];
                ItemStack icon;
                if (skill.equals("nothing")) {
                    icon = RPGCore.inst().getHUDManager().getSkillMenu().empty_icon;
                } else {
                    CoreSkill core_skill = RPGCore.inst().getSkillManager().getIndex().get(skill);
                    icon = core_skill.getItem(this.info_player);
                }

                Integer where = template.getSkillSlots().get(i);
                if (where != null) {
                    getMenu().setItemAt(where, icon);
                }
            }
            // show the emblem of the player class
            if (!this.info_player.getView().job.equals("nothing")) {
                CoreJob job = RPGCore.inst().getJobManager().getIndexJob().get(this.info_player.getView().job);
                getMenu().setItemAt(template.getJobSlot(), job.getEmblemIcon());
            }

            Iterator<Integer> where_next = template.getSpecialSlots().iterator();
            // party handling
            if (where_next.hasNext()) {
                Player viewer = getMenu().getViewer();
                IGroupHandler group_handler = RPGCore.inst().getSocialManager().getGroupHandler();
                IPartySnapshot party_of_viewer = group_handler.getPartySnapshot(viewer);
                IPartySnapshot party_of_target = group_handler.getPartySnapshot(this.info_player.getView().uuid);

                // target has no party and we can invite them
                if (where_next.hasNext() && party_of_target == null && (party_of_viewer == null || party_of_viewer.isLeader(viewer))) {
                    getMenu().setItemAt(where_next.next(), icon_invite_party);
                }
                // leader gets the option to kick from party
                if (where_next.hasNext() && party_of_viewer != null && party_of_viewer == party_of_target && party_of_viewer.isLeader(viewer)) {
                    getMenu().setItemAt(where_next.next(), icon_kick_party);
                }
                // request to join the party
                if (where_next.hasNext() && party_of_target != null && party_of_viewer == null) {
                    getMenu().setItemAt(where_next.next(), icon_join_party);
                }
            }
        }

        this.getMenu().setTitle(msb.compile());
    }

    @Override
    public void click(InventoryClickEvent event) {
        event.setCancelled(true);
        // enforce internal 3s cooldown to not spam click
        if (this.cooldown > System.currentTimeMillis()) {
            return;
        }
        this.cooldown = System.currentTimeMillis() + 3000L;

        if (this.info_player != null) {
            Player viewer = getMenu().getViewer();
            IGroupHandler group_handler = RPGCore.inst().getSocialManager().getGroupHandler();
            IPartySnapshot party_of_viewer = group_handler.getPartySnapshot(viewer);
            IPartySnapshot party_of_target = group_handler.getPartySnapshot(this.info_player.getView().uuid);

            if (this.icon_invite_party.isSimilar(event.getCurrentItem())) {
                if (party_of_target == null && (party_of_viewer == null || party_of_viewer.isLeader(viewer))) {
                    String name = Bukkit.getOfflinePlayer(this.info_player.getView().uuid).getName();
                    group_handler.inviteParty(viewer, name);
                    language().sendMessage(viewer, "group_feedback_invite_party", name);
                    Bukkit.getScheduler().runTask(RPGCore.inst(), viewer::closeInventory);
                } else {
                    event.setCurrentItem(new ItemStack(Material.AIR));
                }
            } else if (this.icon_join_party.isSimilar(event.getCurrentItem())) {
                if (party_of_viewer == null && party_of_target != null) {
                    String name = Bukkit.getOfflinePlayer(party_of_target.getLeaderUUID()).getName();
                    group_handler.joinParty(viewer, name);
                    language().sendMessage(viewer, "group_feedback_join_party", name);
                    Bukkit.getScheduler().runTask(RPGCore.inst(), viewer::closeInventory);
                } else {
                    event.setCurrentItem(new ItemStack(Material.AIR));
                }
            } else if (this.icon_kick_party.isSimilar(event.getCurrentItem())) {
                if (party_of_viewer != null && party_of_target == party_of_viewer && party_of_viewer.isLeader(viewer)) {
                    String name = Bukkit.getOfflinePlayer(this.info_player.getView().uuid).getName();
                    group_handler.kickParty(viewer, name);
                    language().sendMessage(viewer, "group_feedback_kick_party", name);
                    Bukkit.getScheduler().runTask(RPGCore.inst(), viewer::closeInventory);
                } else {
                    event.setCurrentItem(new ItemStack(Material.AIR));
                }
            }
        }
    }

    @Override
    public boolean isTrivial() {
        return true;
    }

    /*
     * A wrapper around the raw data of a player, this should be
     * the last character the player logged on to.
     */
    private class SocialInfoPlayer implements IDescriptionRequester {

        private PlayerSnapshot view;

        SocialInfoPlayer(UUID uuid) {
            this.view = new PlayerSnapshot(uuid);
        }

        public PlayerSnapshot getView() {
            return view;
        }

        @Override
        public Map<String, Map<Long, ItemStack>> getPassiveSocketed() {
            return getView().passive_socketed;
        }

        @Override
        public DamageMetric getMetric(String metric) {
            return null; // no metrics for info player
        }
    }

    private class SocialInfoView {

    }

    private class SocialTreeViewJob {

    }

    private class SocialTreeViewProfession {

    }

    private class SocialTreePreview {

    }
}
