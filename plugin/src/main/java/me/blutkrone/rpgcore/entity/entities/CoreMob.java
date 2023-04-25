package me.blutkrone.rpgcore.entity.entities;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.entity.EntityProvider;
import me.blutkrone.rpgcore.api.social.IPartySnapshot;
import me.blutkrone.rpgcore.dungeon.structure.SpawnerStructure;
import me.blutkrone.rpgcore.mob.CoreCreature;
import me.blutkrone.rpgcore.mob.loot.AbstractCoreLoot;
import me.blutkrone.rpgcore.nms.api.mob.IEntityBase;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.quest.task.AbstractQuestTask;
import me.blutkrone.rpgcore.quest.task.impl.CoreQuestTaskKill;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Mobs refer to creatures with AI created off a template.
 */
public class CoreMob extends CoreEntity {

    // template we were created from
    private final CoreCreature template;
    // everyone who contributed to killing
    private Set<UUID> killers = new HashSet<>();
    // will prevent creature from dying
    private boolean do_not_die;
    // prevents strolling outside of leash
    private double leash_range;
    private Location leash_anchor;
    // in case we are spawned by a dungeon
    private SpawnerStructure.ImportantData tracked_by_dungeon;

    public CoreMob(LivingEntity entity, EntityProvider provider, CoreCreature template) {
        super(entity, provider);
        this.template = template;
    }

    /**
     * If spawned by a dungeon, this is used to track important
     * creatures that are essential to logical progress.
     *
     * @param tracked_by_dungeon Data container to track with
     */
    public void setAsImportantDungeonSpawn(SpawnerStructure.ImportantData tracked_by_dungeon) {
        this.tracked_by_dungeon = tracked_by_dungeon;
    }

    /**
     * Establish a leash for the stroll mechanic, this is to prevent
     * unnecessary leash triggers that may not look nice.
     *
     * @param range  range of the leash
     * @param anchor anchor of the leash
     */
    public void setStrollLeash(double range, Location anchor) {
        if (this.leash_anchor != null) {
            this.leash_range = range;
            this.leash_anchor = anchor;
        }
    }

    /**
     * Check if a location is within our leash bounds.
     *
     * @param location the location to check against.
     * @return true if reachable without pulling leash.
     */
    public boolean isValidStrollTarget(Location location) {
        // if we have no anchor, unlimited stroll
        if (this.leash_anchor == null) {
            return true;
        }
        // if we have an anchor, stroll within range
        double dist = this.leash_anchor.distance(location);
        return dist <= this.leash_range;
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
        LivingEntity handle = getEntity();
        if (handle == null) {
            return null;
        }
        return RPGCore.inst().getVolatileManager().getEntity(handle);
    }

    /**
     * Offer award upon killing a creature, no rewards are offered
     * if this creature is summoned by another.
     */
    public void giveDeathReward() {
        // accredit important dungeon kill
        if (this.tracked_by_dungeon != null) {
            this.tracked_by_dungeon.slain = true;
        }

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
                IPartySnapshot party = RPGCore.inst().getSocialManager().getGroupHandler().getPartySnapshot(player);
                if (party != null) {
                    rewarded.addAll(party.getAllOnlineMembers());
                }
            }
            // contribution not awarded beyond 128 blocks
            rewarded.removeIf(reward -> reward.distance(this) > 128d);
            if (!rewarded.isEmpty()) {
                // offer reward to everyone who contributed
                for (AbstractCoreLoot loot : getTemplate().loot) {
                    try {
                        loot.offer(this, new ArrayList<>(rewarded));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
