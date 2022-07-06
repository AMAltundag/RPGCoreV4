package me.blutkrone.rpgcore.damage;


import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.damage.IDamageManager;
import me.blutkrone.rpgcore.api.damage.IDamageType;
import me.blutkrone.rpgcore.damage.ailment.AbstractAilment;
import me.blutkrone.rpgcore.damage.ailment.AilmentSnapshot;
import me.blutkrone.rpgcore.damage.ailment.ailments.AttributeAilment;
import me.blutkrone.rpgcore.damage.ailment.ailments.DamageAilment;
import me.blutkrone.rpgcore.damage.interaction.DamageElement;
import me.blutkrone.rpgcore.damage.interaction.DamageInteraction;
import me.blutkrone.rpgcore.damage.interaction.types.SpellDamageType;
import me.blutkrone.rpgcore.damage.interaction.types.TimeDamageType;
import me.blutkrone.rpgcore.damage.interaction.types.WeaponDamageType;
import me.blutkrone.rpgcore.entity.entities.CoreEntity;
import me.blutkrone.rpgcore.entity.resource.EntityWard;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    }

    @Override
    public void damage(DamageInteraction interaction) {
        // prepare the initial snapshot of the damage to inflict
        interaction.getType().process(interaction);

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
                Bukkit.getLogger().severe("not implemented (ward break skill trigger)");
            }
        }

        // allow mana to tank part of the damage
        double mana_as_life = Math.max(0d, interaction.evaluateAttribute("MANA_AS_HEALTH", interaction.getDefender()));
        if (interaction.getAttacker() != null)
            mana_as_life += Math.max(0d, interaction.evaluateAttribute("MANA_BURN", interaction.getAttacker()));
        mana_as_life = Math.min(1d, mana_as_life);
        for (DamageElement element : getElements()) {
            double remaining = interaction.getDamage(element);
            if (remaining <= 0d) continue;
            interaction.setDamage(element, interaction.getDefender().getMana().damageBy(remaining*mana_as_life));
        }

        // whatever remains should be tanked thorough life pool
        for (DamageElement element : getElements()) {
            double remaining = interaction.getDamage(element);
            if (remaining <= 0d) continue;
            interaction.setDamage(element, interaction.getDefender().getHealth().damageBy(remaining));
        }

        // if we dipped to 0 life, the defender should die
        if (interaction.getDefender().getHealth().getCurrentAmount() <= 0d) {
            interaction.getDefender().die(interaction);
            Bukkit.getLogger().severe("not implemented (kill trigger)");
            Bukkit.getLogger().severe("not implemented (died trigger)");
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

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    void onAutoAttackTransfer(EntityDamageByEntityEvent e) {
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    void onStopBukkitDamage(EntityDamageEvent e) {
        e.setCancelled(true);

        if (e instanceof EntityDamageByEntityEvent) {
            CoreEntity attacker = null;
            CoreEntity defender = null;

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
                damage(getType("WEAPON").create(defender, attacker));
            }
        }
    }
}