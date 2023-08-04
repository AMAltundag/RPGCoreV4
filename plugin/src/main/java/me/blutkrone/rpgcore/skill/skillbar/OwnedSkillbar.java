package me.blutkrone.rpgcore.skill.skillbar;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.skill.CoreSkill;
import me.blutkrone.rpgcore.skill.SkillContext;

/**
 * The skillbar of a player may occupy up to six slots, and
 * each slot is associated with one skill.
 * <br>
 * The progress of casting a skill is an activity, hence it
 * will overlap with other activities - the skillbar should
 * not accept requests while there is a pending activity.
 */
public class OwnedSkillbar {

    // whose skillbar is this
    private final CorePlayer player;
    // the skill slots we have
    private final String[] bindings = new String[6];

    /**
     * A per-player manager for their skillbar.
     *
     * @param player whose skillbar we are managing.
     */
    public OwnedSkillbar(CorePlayer player) {
        this.player = player;
    }

    /**
     * Bind the given skill to the requested skill slot.
     *
     * @param position the position to update
     * @param skill    the skill to update to
     */
    public void setSkill(int position, CoreSkill skill) {
        if (skill == null) {
            this.bindings[position] = null;
        } else {
            this.bindings[position] = skill.getId();
        }
    }

    /**
     * Retrieve the skill bound to the given position.
     *
     * @param position the position we are checking
     * @return the skill that was fetched
     */
    public CoreSkill getSkill(int position) {
        // ensure something was bound
        if (this.bindings[position] == null) {
            return null;
        }
        // grab the skill backing the ID
        CoreSkill skill = RPGCore.inst().getSkillManager().getIndex().get(this.bindings[position]);
        // check if we can actually hold this skill
        if (skill.isHidden() && !player.checkForTag("skill_" + skill.getId())) {
            return null;
        }
        // offer up the skill since it isn't hidden to us
        return skill;
    }

    /**
     * Invoke the casting process, so long there isn't another
     * activity already operating.
     *
     * @param position the skill which we want invoked.
     */
    public void castSkill(int position) {
        // reject casting process if we have an activity
        if (this.player.getActivity() != null) {
            return;
        }
        // ensure we got a skill on the position
        CoreSkill skill = getSkill(position);
        if (skill == null) {
            return;
        }

        // create the context we operate within
        SkillContext context = this.player.createSkillContext(skill);

        // we cannot be on cooldown
        if (skill.getBinding().getCooldown(context) > 0) {
            return;
        }

        // we must be able to afford it
        if (!skill.getBinding().isAffordable(context)) {
            return;
        }

        // request to invoke the skill
        skill.getBinding().invoke(context);
    }
}
