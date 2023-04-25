package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.social.IGroupHandler;
import me.blutkrone.rpgcore.api.social.IPartySnapshot;
import me.blutkrone.rpgcore.damage.DamageMetric;
import me.blutkrone.rpgcore.data.DataBundle;
import me.blutkrone.rpgcore.item.styling.IDescriptorReference;
import me.blutkrone.rpgcore.job.CoreJob;
import me.blutkrone.rpgcore.skill.CoreSkill;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import me.blutkrone.rpgcore.util.io.BukkitSerialization;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.*;

/**
 * A menu meant to show information in a social context, this
 * refers to things such as equipment, skills, passive tree
 * and a couple interfaces to social structures.
 * <p>
 * This can also work with offline players.
 * <p>
 * This works off the latest data snapshot.
 */
public class SocialMenu extends AbstractCoreMenu {

    private me.blutkrone.rpgcore.hud.menu.SocialMenu template;
    private SocialInfoPlayer info_player;

    private ItemStack icon_invite_party;
    private ItemStack icon_kick_party;
    private ItemStack icon_join_party;

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
            Map<String, DataBundle> data = RPGCore.inst().getDataManager().getLastData(player);
            SocialInfoPlayer info_player = new SocialInfoPlayer(player, data);
            Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                this.info_player = info_player;
                // flush the info shown for that player
                for (ItemStack stack : this.info_player.equipment.values()) {
                    RPGCore.inst().getItemManager().describe(stack, info_player);
                }
                for (ItemStack stack : this.info_player.equipment.values()) {
                    RPGCore.inst().getItemManager().describe(stack, info_player);
                }

                rebuild();
            });
        });
    }

    @Override
    public void rebuild() {
        getMenu().clearItems();

        MagicStringBuilder msb = new MagicStringBuilder();
        msb.shiftToExact(-208);
        msb.append(resourcepack().texture("menu_player_social"), ChatColor.WHITE);

        if (this.info_player != null) {
            // show the equipment of target player
            this.info_player.equipment.forEach((slot, item) -> {
                Integer where = this.template.getEquipmentSlots().get(slot);
                if (where != null) {
                    getMenu().setItemAt(where, item);
                }
            });
            // show the skills of target player
            for (int i = 0; i < template.getSkillSlots().size(); i++) {
                String skill = this.info_player.skillbar[i];
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
            if (!this.info_player.job.equals("nothing")) {
                CoreJob job = RPGCore.inst().getJobManager().getIndexJob().get(this.info_player.job);
                getMenu().setItemAt(template.getJobSlot(), job.getEmblemIcon());
            }

            Iterator<Integer> where_next = template.getSpecialSlots().iterator();
            // party handling
            if (where_next.hasNext()) {
                Player viewer = getMenu().getViewer();
                IGroupHandler group_handler = RPGCore.inst().getSocialManager().getGroupHandler();
                IPartySnapshot party_of_viewer = group_handler.getPartySnapshot(viewer);
                IPartySnapshot party_of_target = group_handler.getPartySnapshot(info_player.uuid);

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
                    getMenu().setItemAt(where_next.next(), icon_invite_party);
                }
            }
        }

        this.getMenu().setTitle(msb.compile());
    }

    @Override
    public void click(InventoryClickEvent event) {
        event.setCancelled(true);

        if (this.info_player != null) {
            Player viewer = getMenu().getViewer();
            IGroupHandler group_handler = RPGCore.inst().getSocialManager().getGroupHandler();
            IPartySnapshot party_of_viewer = group_handler.getPartySnapshot(viewer);
            IPartySnapshot party_of_target = group_handler.getPartySnapshot(info_player.uuid);

            if (this.icon_invite_party.isSimilar(event.getCurrentItem())) {
                if (party_of_target == null && (party_of_viewer == null || party_of_viewer.isLeader(viewer))) {
                    group_handler.inviteParty(viewer, Bukkit.getOfflinePlayer(this.info_player.uuid).getName());
                }
            } else if (this.icon_join_party.isSimilar(event.getCurrentItem())) {
                if (party_of_viewer == null && party_of_target != null) {
                    group_handler.joinParty(viewer, Bukkit.getOfflinePlayer(party_of_target.getLeaderUUID()).getName());
                }
            } else if (this.icon_kick_party.isSimilar(event.getCurrentItem())) {
                if (party_of_viewer != null && party_of_target == party_of_viewer && party_of_viewer.isLeader(viewer)) {
                    group_handler.kickParty(viewer, Bukkit.getOfflinePlayer(this.info_player.uuid).getName());
                }
            }
        }
    }

    /*
     * A wrapper around the raw data of a player, this should be
     * the last character the player logged on to.
     */
    private class SocialInfoPlayer implements IDescriptorReference {
        UUID uuid;
        Map<String, ItemStack> equipment = new HashMap<>();
        String[] skillbar = new String[6];
        int level = 1;
        double experience = 0d;
        String job = "nothing";
        Map<String, Integer> passive_refunds = new HashMap<>();
        Map<String, Integer> passive_points = new HashMap<>();
        Map<String, Long> passive_viewport = new HashMap<>();
        Map<String, Long> passive_integrity = new HashMap<>();
        Map<String, Set<Long>> passive_allocated = new HashMap<>();
        Map<String, Map<Long, ItemStack>> passive_socketed = new HashMap<>();

        public SocialInfoPlayer(UUID uuid, Map<String, DataBundle> data) {
            this.uuid = uuid;
            loadSkillInfo(data.getOrDefault("skill", new DataBundle()));
            loadItemInfo(data.getOrDefault("item", new DataBundle()));
            loadLevelInfo(data.getOrDefault("level", new DataBundle()));
            loadJobInfo(data.getOrDefault("job", new DataBundle()));
            loadPassiveInfo(data.getOrDefault("passive", new DataBundle()));
        }

        private void loadSkillInfo(DataBundle bundle) {
            if (!bundle.isEmpty()) {
                for (int i = 0; i < 6; i++) {
                    this.skillbar[i] = bundle.getString(i);
                }
            }
        }

        private void loadLevelInfo(DataBundle bundle) {
            if (!bundle.isEmpty()) {
                this.level = bundle.getNumber(0).intValue();
                this.experience = bundle.getNumber(1).doubleValue();
            }
        }

        private void loadJobInfo(DataBundle bundle) {
            if (!bundle.isEmpty()) {
                this.job = bundle.getString(0);
            }
        }

        private void loadPassiveInfo(DataBundle bundle) {
            if (!bundle.isEmpty()) {
                int header = 0;
                int size;

                size = bundle.getNumber(header++).intValue();
                for (int i = 0; i < size; i++) {
                    String key = bundle.getString(header++);
                    int value = bundle.getNumber(header++).intValue();
                    passive_refunds.put(key, value);
                }

                size = bundle.getNumber(header++).intValue();
                for (int i = 0; i < size; i++) {
                    String key = bundle.getString(header++);
                    int value = bundle.getNumber(header++).intValue();
                    passive_points.put(key, value);
                }

                size = bundle.getNumber(header++).intValue();
                for (int i = 0; i < size; i++) {
                    String key = bundle.getString(header++);
                    long value = bundle.getNumber(header++).longValue();
                    passive_viewport.put(key, value);
                }

                size = bundle.getNumber(header++).intValue();
                for (int i = 0; i < size; i++) {
                    String key = bundle.getString(header++);
                    long value = bundle.getNumber(header++).longValue();
                    passive_integrity.put(key, value);
                }

                size = bundle.getNumber(header++).intValue();
                for (int i = 0; i < size; i++) {
                    String tree = bundle.getString(header++);
                    int size2 = bundle.getNumber(header++).intValue();
                    for (int j = 0; j < size2; j++) {
                        long where = bundle.getNumber(header++).longValue();
                        String socketedB64 = bundle.getString(header++);
                        try {
                            ItemStack socketed = BukkitSerialization.fromBase64(socketedB64)[0];
                            passive_socketed.computeIfAbsent(tree, (k -> new HashMap<>())).put(where, socketed);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                size = bundle.getNumber(header++).intValue();
                for (int i = 0; i < size; i++) {
                    String tree = bundle.getString(header++);
                    int size2 = bundle.getNumber(header++).intValue();
                    Set<Long> allocated = new HashSet<>();
                    for (int j = 0; j < size2; j++) {
                        long encoded_position = bundle.getNumber(header++).longValue();
                        allocated.add(encoded_position);
                    }
                    passive_allocated.put(tree, allocated);
                }
            }
        }

        private void loadItemInfo(DataBundle bundle) {
            if (!bundle.isEmpty()) {
                int length = bundle.getNumber(1).intValue();
                int header = 2;
                for (int i = 0; i < length; i++) {
                    String slot = bundle.getString(header++);
                    String item = bundle.getString(header++);
                    try {
                        ItemStack stack = BukkitSerialization.fromBase64(item)[0];
                        if (stack != null && RPGCore.inst().getHUDManager().getEquipMenu().isReflected(stack)) {
                            stack.setType(Material.AIR);
                        }
                        this.equipment.put(slot, stack);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public Map<String, Map<Long, ItemStack>> getPassiveSocketed() {
            return this.passive_socketed;
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
