package me.blutkrone.rpgcore.damage;


import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.damage.IDamageManager;
import me.blutkrone.rpgcore.api.damage.IDamageType;
import me.blutkrone.rpgcore.api.event.CoreEntityKilledEvent;
import me.blutkrone.rpgcore.api.party.IActiveParty;
import me.blutkrone.rpgcore.damage.ailment.AbstractAilment;
import me.blutkrone.rpgcore.damage.ailment.AilmentSnapshot;
import me.blutkrone.rpgcore.damage.ailment.ailments.AttributeAilment;
import me.blutkrone.rpgcore.damage.ailment.ailments.DamageAilment;
import me.blutkrone.rpgcore.damage.interaction.DamageElement;
import me.blutkrone.rpgcore.damage.interaction.DamageInteraction;
import me.blutkrone.rpgcore.damage.interaction.types.SpellDamageType;
import me.blutkrone.rpgcore.damage.interaction.types.TimeDamageType;
import me.blutkrone.rpgcore.damage.interaction.types.WeaponDamageType;
import me.blutkrone.rpgcore.entity.IOfflineCorePlayer;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.entities.CoreMob;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.entity.resource.EntityWard;
import me.blutkrone.rpgcore.nms.api.mob.IEntityBase;
import me.blutkrone.rpgcore.skill.trigger.CoreDealDamageTrigger;
import me.blutkrone.rpgcore.skill.trigger.CoreTakeDamageTrigger;
import me.blutkrone.rpgcore.skill.trigger.CoreWardBreakTrigger;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages all instances of damage between two entities.
 *
 * @see DamageInteraction contains information about damage dealt
 * @see CoreEntity only a core entity may be involved in damage
 */
public final class DamageManager implements IDamageManager, Listener {

    // a type defines how damage is calculated
    private Map<String, IDamageType> damage_types = new HashMap<>();
    // a single instance of damage has multiple elements
    private Map<String, DamageElement> damage_elements = new HashMap<>();
    // ailments are secondary effects of non-DOT damage
    private List<AbstractAilment> ailments = new ArrayList<>();

    public DamageManager() {
        damage_types.put("WEAPON", new WeaponDamageType());
        damage_types.put("SPELL", new SpellDamageType());
        damage_types.put("DOT", new TimeDamageType());

        // load elements which damage can belong to
        try {
            ConfigWrapper damage_config = FileUtil.asConfigYML(FileUtil.file("damage.yml"));
            damage_config.forEachUnder("element", ((path, root) ->
                    damage_elements.put(path, new DamageElement(path, root.getSection(path)))));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // load secondary ailments which can be inflicted
        try {
            ConfigWrapper damage_config = FileUtil.asConfigYML(FileUtil.file("ailment.yml"));
            damage_config.forEachUnder("ailment", ((path, root) -> {
                ConfigWrapper ailment_config = root.getSection(path);
                String ailment_type = ailment_config.getString("type", "NONE");
                if (ailment_type.equalsIgnoreCase("DOT")) {
                    ailments.add(new DamageAilment(path, root.getSection(path)));
                } else if (ailment_type.equalsIgnoreCase("ATTRIBUTE")) {
                    ailments.add(new AttributeAilment(path, root.getSection(path)));
                } else {
                    Bukkit.getLogger().severe(String.format("Unable to create ailment of type '%s'", ailment_type));
                }
            }));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Bukkit.getLogger().severe("not implemented (vanilla migration)");
        Bukkit.getPluginManager().registerEvents(this, RPGCore.inst());
    }

    @Override
    public void damage(DamageInteraction interaction) {
        // do not process damage against friendlies
        if (interaction.getAttacker() != null) {
            if (interaction.getAttacker().isFriendly(interaction.getDefender())) {
                return;
            }
        }

        // do not process damage depending on immortality
        if (interaction.getDefender() instanceof CoreMob) {
            IEntityBase base = ((CoreMob) interaction.getDefender()).getBase();
            // invoked final death sequence
            if (base.isInDeathSequence()) {
                return;
            }
        }

        // prepare the initial snapshot of the damage to inflict
        interaction.getType().process(interaction);

        // triggers for damage events
        interaction.getDefender().proliferateTrigger(CoreTakeDamageTrigger.class, interaction);
        if (interaction.getAttacker() != null) {
            interaction.getAttacker().proliferateTrigger(CoreDealDamageTrigger.class, interaction);
        }

        // players should track damage for metric purposes
        if (interaction.getAttacker() instanceof CorePlayer) {
            for (DamageElement element : getElements()) {
                double damage = interaction.getDamage(element);
                if (damage > 0d) {
                    ((CorePlayer) interaction.getAttacker()).getMetric(interaction.getDamageBlame() + "_" + element.getId())
                            .track(interaction.getDefender().getUniqueId(), damage);
                }
            }
        }

        // transform damage by the variance factor
        if (interaction.getAttacker() != null) {
            for (DamageElement element : getElements()) {
                double damage = interaction.getDamage(element);
                if (damage <= 0d) {
                    continue;
                }
                // ranges of damage that can be dealt
                double minimum = 1d;
                for (String attribute : element.getMinimumRange()) {
                    minimum += interaction.evaluateAttribute(attribute, interaction.getAttacker());
                }
                double maximum = 1d;
                for (String attribute : element.getMaximumRange()) {
                    maximum += interaction.evaluateAttribute(attribute, interaction.getAttacker());
                }
                double lucky = 0d;
                for (String attribute : element.getRangeLucky()) {
                    lucky += interaction.evaluateAttribute(attribute, interaction.getAttacker());
                }
                double unlucky = 0d;
                for (String attribute : element.getRangeUnlucky()) {
                    unlucky += interaction.evaluateAttribute(attribute, interaction.getAttacker());
                }
                // normalize minimum/maximum
                double real_minimum = Math.min(minimum, maximum);
                double real_maximum = Math.max(minimum, maximum);
                // roll for the damage range
                double range = real_minimum + (Math.random() * (real_maximum - real_minimum));
                // lucky will pick better range
                if (Math.random() <= lucky) {
                    double lucky_range = real_minimum + (Math.random() * (real_maximum - real_minimum));
                    range = Math.max(range, lucky_range);
                }
                // unlucky will pick worse range
                if (Math.random() <= unlucky) {
                    double unlucky_range = real_minimum + (Math.random() * (real_maximum - real_minimum));
                    range = Math.min(range, unlucky_range);
                }
                // multiply damage with the range
                interaction.setDamage(element, damage * range);
            }
        }

        // if mob got a barrier phase absorb that
        if (interaction.getDefender().soakForBarrier((int) interaction.getDamage())) {
            return;
        }

        // see if we got any ward to break
        EntityWard ward = interaction.getDefender().getWard();
        if (ward != null) {
            // tank damage with ward first
            for (DamageElement element : getElements()) {
                double remaining = interaction.getDamage(element);
                if (remaining > 0d) continue;
                interaction.setDamage(element, ward.damageBy(remaining));
            }
            // if ward broke, fire a trigger about that
            if (ward.getCurrentAmount() <= 0d) {
                interaction.getDefender().proliferateTrigger(CoreWardBreakTrigger.class, interaction);
            }
        }

        // allow mana to tank part of the damage
        double mana_as_life = Math.max(0d, interaction.evaluateAttribute("MANA_AS_HEALTH", interaction.getDefender()));
        if (interaction.getAttacker() != null)
            mana_as_life += Math.max(0d, interaction.evaluateAttribute("MANA_BURN", interaction.getAttacker()));
        if (mana_as_life > 0d) {
            mana_as_life = Math.min(1d, mana_as_life);
            for (DamageElement element : getElements()) {
                double remaining = interaction.getDamage(element);
                if (remaining <= 0d) continue;
                double excess = interaction.getDefender().getMana().damageBy(remaining * mana_as_life);
                interaction.setDamage(element, (remaining * (1d - mana_as_life)) + excess);
            }
        }

        // whatever remains should be tanked thorough life pool
        for (DamageElement element : getElements()) {
            double remaining = interaction.getDamage(element);
            if (remaining <= 0d) continue;
            interaction.setDamage(element, interaction.getDefender().getHealth().damageBy(remaining));
        }

        // the attacker and their party is considered a contributor
        if (interaction.getDefender() instanceof CoreMob) {
            CoreEntity attacker = interaction.getAttacker();
            while (attacker.getParent() != null) {
                UUID parent = attacker.getParent();
                CoreEntity candidate = RPGCore.inst().getEntityManager().getEntity(parent);
                if (candidate != null) {
                    attacker = candidate;
                } else {
                    break;
                }
            }

            if (attacker instanceof CorePlayer) {
                ((CoreMob) interaction.getDefender()).addContribution((CorePlayer) attacker);
                IActiveParty party = RPGCore.inst().getPartyManager().getPartyOf(((CorePlayer) attacker));
                if (party != null) {
                    for (IOfflineCorePlayer partied : party.getAllMembers()) {
                        if (partied instanceof CorePlayer) {
                            ((CoreMob) interaction.getDefender()).addContribution(((CorePlayer) partied));
                        }
                    }
                }
            }
        }

        // if we dipped to 0 life, the defender should die
        if (interaction.getDefender().getHealth().getCurrentAmount() <= 0d) {
            if (interaction.getDefender() instanceof CoreMob) {
                // immortality prevents death
                if (((CoreMob) interaction.getDefender()).isDoNotDie()) {
                    // fix at 1 health while not allowed to die
                    interaction.getDefender().getHealth().setToExactUnsafe(1d);
                } else {
                    // core mobs may have a death routine
                    ((CoreMob) interaction.getDefender()).getBase().doDeathSequence(() -> {
                        ((CoreMob) interaction.getDefender()).giveDeathReward();
                        interaction.getDefender().die(interaction);
                        Bukkit.getPluginManager().callEvent(new CoreEntityKilledEvent(interaction));

                        Bukkit.getLogger().severe("not implemented (kill trigger)");
                        Bukkit.getLogger().severe("not implemented (died trigger)");
                    });
                }
            } else {
                interaction.getDefender().die(interaction);
                Bukkit.getPluginManager().callEvent(new CoreEntityKilledEvent(interaction));
                Bukkit.getLogger().severe("not implemented (kill trigger)");
                Bukkit.getLogger().severe("not implemented (died trigger)");
            }
        }

        // zero damage inflicted for vanilla hurt effect
        interaction.getDefender().getEntity().damage(0d);
        // inform skills about the damage inflicted

        Bukkit.getLogger().severe("not implemented (dealt damage trigger)");
        Bukkit.getLogger().severe("not implemented (took damage trigger)");
    }

    @Override
    public void handleAilment(DamageInteraction interaction, AilmentSnapshot prepared_damage) {
        // only inflict ailment if there is an attacker
        if (interaction.getAttacker() == null)
            return;
        // attempt to inflict all ailments possible
        for (AbstractAilment ailment : this.ailments) {
            interaction.getDefender().getAilmentTracker()
                    .computeIfAbsent(ailment, (k -> k.createTracker(interaction.getDefender())))
                    .acquireAilment(interaction, prepared_damage);
        }
    }

    @Override
    public DamageElement getElement(String element) {
        return this.damage_elements.get(element);
    }

    @Override
    public List<DamageElement> getElements() {
        return new ArrayList<>(this.damage_elements.values());
    }

    @Override
    public List<DamageElement> getElements(List<String> elements) {
        return elements.stream().map(this::getElement).collect(Collectors.toList());
    }

    @Override
    public List<String> getElementIds() {
        return new ArrayList<>(this.damage_elements.keySet());
    }

    @Override
    public IDamageType getType(String type) {
        return this.damage_types.get(type);
    }

    @Override
    public List<IDamageType> getTypes() {
        return new ArrayList<>(this.damage_types.values());
    }

    @Override
    public List<IDamageType> getTypes(List<String> types) {
        return types.stream().map(this::getType).collect(Collectors.toList());
    }

    @Override
    public List<String> getTypeIds() {
        return new ArrayList<>(this.damage_types.keySet());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    void onAutoAttackTransfer(EntityDamageByEntityEvent e) {
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    void onStopBukkitDamage(EntityDamageEvent e) {
        e.setCancelled(true);

        if (e instanceof EntityDamageByEntityEvent) {
            CoreEntity attacker = null;
            CoreEntity defender = null;

            // non-weapon slot cannot deal any damage
            if (((EntityDamageByEntityEvent) e).getDamager() instanceof Player) {
                Player player = (Player) ((EntityDamageByEntityEvent) e).getDamager();
                if (player.getInventory().getHeldItemSlot() != 0) {
                    return;
                }
            }

            // retrieve relevant entities that we are using
            Entity vanilla_attacker = ((EntityDamageByEntityEvent) e).getDamager();
            Entity vanilla_defender = e.getEntity();
            if (vanilla_attacker instanceof LivingEntity) {
                attacker = RPGCore.inst().getEntityManager().getEntity(vanilla_attacker.getUniqueId());
                defender = RPGCore.inst().getEntityManager().getEntity(vanilla_defender.getUniqueId());
            } else if (vanilla_attacker instanceof Projectile) {
                ProjectileSource shooter = ((Projectile) vanilla_attacker).getShooter();
                if (shooter instanceof LivingEntity) {
                    attacker = RPGCore.inst().getEntityManager().getEntity(((LivingEntity) shooter).getUniqueId());
                    defender = RPGCore.inst().getEntityManager().getEntity(vanilla_defender.getUniqueId());
                }
            }

            // only inflict damage if both parties exist
            if (attacker != null && defender != null) {
                // inflict weapon type damage on the entity
                DamageInteraction interaction = getType("WEAPON").create(defender, attacker);
                interaction.setDamageBlame("autoattack");
                damage(interaction);
                // update the rage on the given mob
                if (defender instanceof CoreMob) {
                    IEntityBase base = ((CoreMob) defender).getBase();
                    double focus = attacker.evaluateAttribute("RAGE_FOCUS");
                    base.enrage(attacker.getEntity(), 1d, 1d, focus, false);
                }
            }
        }
    }
}