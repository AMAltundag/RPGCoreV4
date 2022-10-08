package me.blutkrone.rpgcore.entity.entities;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.entity.EntityProvider;
import me.blutkrone.rpgcore.api.party.IActiveParty;
import me.blutkrone.rpgcore.entity.IOfflineCorePlayer;
import me.blutkrone.rpgcore.mob.CoreCreature;
import me.blutkrone.rpgcore.mob.loot.AbstractCoreLoot;
import me.blutkrone.rpgcore.nms.api.mob.IEntityBase;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.quest.task.AbstractQuestTask;
import me.blutkrone.rpgcore.quest.task.impl.CoreQuestTaskKill;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Rage is designed with points in mind, and has a soft-cap and a
 * hard cap.
 *
 * Damage is processed once per second, granting one point of rage
 * to the player who dealt the most amount of damage.
 *
 * Should the non-target have generated this point of rage, they
 * will instead reduce the rage. The "OBSESSION" attribute of the
 * rage holder will reduce this reduction.
 *
 * Rage has a hard cap, it will not accumulate beyond this.
 *
 * Rage has a soft-cap, which damage is affected by. You can use
 * skill logic to bypass the said logic and generate rage up to
 * the maximum value.
 *
 * Rage can be checked by the mob AI, allowing you to both manipulate
 * the rage as-well as enable certain behaviours only at given levels
 * of rage.
 *
 * RAGE_MAXIMUM: Mob will never increase rage past this.
 * RAGE_SOFT_LIMIT: Mob rage cannot exceed this naturally.
 * RAGE_RANGE: Mob will clear rage if target is out of range.
 * OBSESSION: If we are focused by a mob, other entities generate less rage.
 * RAGE_MAXIMUM
 */
public class CoreMob extends CoreEntity {

    private final CoreCreature template;
    private Set<UUID> killers = new HashSet<>();
    private boolean do_not_die;

    public CoreMob(LivingEntity entity, EntityProvider provider, CoreCreature template) {
        super(entity, provider);
        this.template = template;
    }

    /**
     * Prevents death, but not damage. Life should cap at 1 while this
     * is still available.
     *
     * @return true if we cannot die.
     */
    public boolean isDoNotDie() {
        return do_not_die;
    }

    /**
     * This prevents the creature from dying from damage, however
     * this does NOT cover for any non damaging sources. Damage is
     * still applied.
     *
     * @param do_not_die whether to allow death.
     */
    public void doNotDie(boolean do_not_die) {
        this.do_not_die = do_not_die;
    }

    /**
     * Retrieve the template which this mob is based off.
     *
     * @return related template.
     */
    public CoreCreature getTemplate() {
        return template;
    }

    /**
     * Retrieve the base accessor
     *
     * @return an accessor to low-level entity behaviour.
     */
    public IEntityBase getBase() {
        return RPGCore.inst().getVolatileManager().getEntity(getEntity());
    }

    /**
     * Offer award upon killing a creature, no rewards are offered
     * if this creature is summoned by another.
     */
    public void giveDeathReward() {
        // summons should never give rewards
        if (this.getParent() == null && !this.killers.isEmpty()) {
            // identify who will be rewarded
            Set<CorePlayer> rewarded = new HashSet<>();
            for (UUID killer : this.killers) {
                // extract latest player instance
                CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(killer);
                if (player == null) {
                    continue;
                }
                // assign player themselves
                rewarded.add(player);
                // assign every party member
                IActiveParty party = RPGCore.inst().getPartyManager().getPartyOf(player);
                if (party != null) {
                    for (IOfflineCorePlayer offline : party.getAllMembers()) {
                        CorePlayer other = RPGCore.inst().getEntityManager().getPlayer(offline.getUniqueId());
                        if (other != null) {
                            rewarded.add(other);
                        }
                    }
                }
            }
            // contribution not awarded beyond 128 blocks
            rewarded.removeIf(reward -> reward.distance(this) > 128d);
            if (!rewarded.isEmpty()) {
                // offer reward to everyone who contributed
                for (AbstractCoreLoot loot : getTemplate().loot) {
                    loot.offer(this, new ArrayList<>(rewarded));
                }
                // quest progress to everyone relevant
                for (CorePlayer player : rewarded) {
                    for (String id : player.getActiveQuestIds()) {
                        CoreQuest quest = RPGCore.inst().getQuestManager().getIndexQuest().get(id);
                        AbstractQuestTask task = quest.getCurrentTask(player);
                        if (task instanceof CoreQuestTaskKill) {
                            ((CoreQuestTaskKill) task).updateQuest(player, getTemplate().getId());
                        }
                    }
                }
            }
        }
    }

    /**
     * Considers the attacker, and their party members, to be a contributor
     * to the death of this entity.
     *
     * @param attacker
     */
    public void addContribution(CorePlayer attacker) {
        this.killers.add(attacker.getUniqueId());
    }
}
