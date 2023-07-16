package me.blutkrone.rpgcore.skill;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.index.EditorIndex;
import me.blutkrone.rpgcore.editor.root.skill.EditorSkill;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.menu.EquipMenu;
import me.blutkrone.rpgcore.skill.activity.activities.FocusSkillActivity;
import me.blutkrone.rpgcore.skill.trigger.CoreAttackTrigger;
import me.blutkrone.rpgcore.skill.trigger.CoreMoveTrigger;
import me.blutkrone.rpgcore.skill.trigger.CoreTimerTrigger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages everything related to skills, do note that skill
 * components are also available to the mob logic.
 */
public class SkillManager implements Listener {

    // skills registered to the core
    private EditorIndex<CoreSkill, EditorSkill> skill_index;
    // snapshot of previous movement
    private Map<UUID, Location> snapshot = new HashMap<>();

    public SkillManager() {
        this.skill_index = new EditorIndex<>("skill", EditorSkill.class, EditorSkill::new);
        Bukkit.getPluginManager().registerEvents(this, RPGCore.inst());

        Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
            // grab all entities relevant for the skill pipeline
            Map<LivingEntity, CoreEntity> registered = new HashMap<>();
            for (World world : Bukkit.getWorlds()) {
                for (LivingEntity entity : world.getLivingEntities()) {
                    CoreEntity core_entity = RPGCore.inst().getEntityManager().getEntity(entity);
                    if (core_entity != null) {
                        registered.put(entity, core_entity);
                    }
                }
            }
            // update timer based triggers
            registered.forEach((bukkit, core) -> core.proliferateTrigger(CoreTimerTrigger.class, null));

            // prepare collections necessary to evaluate entity movement
            Map<UUID, Location> before = snapshot;
            Map<UUID, Location> after = new HashMap<>();
            registered.forEach((bukkit, core) -> after.put(bukkit.getUniqueId(), bukkit.getLocation()));
            snapshot = after;
            // async computing of distance moved
            Bukkit.getScheduler().runTaskAsynchronously(RPGCore.inst(), () -> {
                // identify motion of the entity
                Map<UUID, Double> motion = new HashMap<>();
                before.forEach((uuid, location1) -> {
                    Location location2 = after.get(uuid);
                    if (location2 != null) {
                        motion.put(uuid, location1.toVector().distance(location2.toVector()));
                    }
                });
                // synchronous task to invoke the motion trigger
                Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                    motion.forEach((uuid, distance) -> {
                        CoreEntity entity = RPGCore.inst().getEntityManager().getEntity(uuid);
                        if (entity != null) {
                            entity.proliferateTrigger(CoreMoveTrigger.class, distance);
                        }
                    });
                });
            });
        }, 1, 1);
    }

    /**
     * Index tracking all skills.
     *
     * @return skills on server.
     */
    public EditorIndex<CoreSkill, EditorSkill> getIndex() {
        return skill_index;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    void onPlayerToggleSkillbar(PlayerSwapHandItemsEvent e) {
        // ensure player can interact with the skillbar
        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(e.getPlayer());
        if (core_player == null || !core_player.isAllowTarget()) {
            return;
        }
        // ensure we are on our prime slot
        if (RPGCore.inst().getItemManager().isOnCombatSlot(e.getPlayer(), false)) {
            e.getPlayer().getInventory().setHeldItemSlot(0);
        }
        // handle the skillbar specific logic
        if (core_player.getActivity() instanceof FocusSkillActivity) {
            // mark the activity as ready to be triggered
            ((FocusSkillActivity) core_player.getActivity()).forceUsage();
        } else if (core_player.getActivity() == null) {
            // toggle the state of the skillbar selection
            core_player.setSkillbarActive(!core_player.isSkillbarActive());
        }
        // prevent the event from occurring
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    void onPlayerSelectSkillbar(PlayerItemHeldEvent e) {
        // ensure player can interact with the skillbar
        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(e.getPlayer());
        if (core_player == null || !core_player.isAllowTarget() || !core_player.isSkillbarActive() || e.getNewSlot() == 0) {
            return;
        }

        // cancel the event since its a skillbar click
        e.setCancelled(true);

        // cast the skill should it be on our skillbar
        if (e.getNewSlot() <= 6) {
            core_player.getSkillbar().castSkill(e.getNewSlot() - 1);
        }

        // disable the skillbar since we pressed a key
        core_player.setSkillbarActive(false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onSkillTrigger(PlayerInteractEvent event) {
        // prevents double firing
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        // ensure a player is registered
        CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(event.getPlayer());
        if (player == null) {
            return;
        }
        // proliferate an attack trigger
        ItemStack weapon = RPGCore.inst().getHUDManager().getEquipMenu()
                .getBukkitEquipment(player, EquipMenu.BukkitSlot.MAIN_HAND);
        player.proliferateTrigger(CoreAttackTrigger.class, weapon);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onSkillTrigger(PlayerInteractEntityEvent event) {
        // prevents double firing
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        // ensure a player is registered
        CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(event.getPlayer());
        if (player == null) {
            return;
        }
        // proliferate an attack trigger
        ItemStack weapon = RPGCore.inst().getHUDManager().getEquipMenu()
                .getBukkitEquipment(player, EquipMenu.BukkitSlot.MAIN_HAND);
        player.proliferateTrigger(CoreAttackTrigger.class, weapon);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onSkillTrigger(EntityShootBowEvent event) {
        // only process for player shooting
        if (event.getEntity() instanceof Player) {
            return;
        }
        // ensure a player is registered
        CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(event.getEntity());
        if (player == null) {
            return;
        }
        // proliferate an attack trigger
        ItemStack weapon = RPGCore.inst().getHUDManager().getEquipMenu()
                .getBukkitEquipment(player, EquipMenu.BukkitSlot.MAIN_HAND);
        player.proliferateTrigger(CoreAttackTrigger.class, weapon);
    }
}
