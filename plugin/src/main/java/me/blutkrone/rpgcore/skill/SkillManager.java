package me.blutkrone.rpgcore.skill;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.EditorIndex;
import me.blutkrone.rpgcore.hud.editor.root.EditorSkill;
import me.blutkrone.rpgcore.skill.activity.activities.FocusSkillActivity;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

/**
 * Manages everything related to skills, do note that skill
 * components are also available to the mob logic.
 */
public class SkillManager implements Listener {

    // skills registered to the core
    private EditorIndex<CoreSkill, EditorSkill> skill_index;

    public SkillManager() {
        this.skill_index = new EditorIndex<>("skill", EditorSkill.class, EditorSkill::new);
        Bukkit.getPluginManager().registerEvents(this, RPGCore.inst());
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
}
