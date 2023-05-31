package me.blutkrone.rpgcore.nms.v1_19_R1.mob;

import me.blutkrone.rpgcore.nms.api.mob.AbstractEntityRoutine;
import me.blutkrone.rpgcore.nms.api.mob.IEntityBase;
import net.minecraft.util.profiling.GameProfilerFiller;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.goal.PathfinderGoalSelector;
import net.minecraft.world.entity.ai.goal.PathfinderGoalWrapped;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class VolatileEntityBase implements IEntityBase {

    private LivingEntity bukkit_entity;

    public VolatileEntityBase(LivingEntity bukkit_entity) {
        this.bukkit_entity = bukkit_entity;
    }

    @Override
    public void setAggressive(boolean aggressive) {
        EntityInsentient insentient = getInsentient();
        insentient.u(aggressive);
    }

    @Override
    public void addRoutine(String namespace, AbstractEntityRoutine routine) {
        getAI().routines.computeIfAbsent(namespace, (k -> new ArrayList<>())).add(routine);
    }

    @Override
    public void addDeathRoutine(AbstractEntityRoutine routine) {
        getAI().death_routines.add(routine);
    }

    @Override
    public boolean walkTo(LivingEntity entity, double speed) {
        // ensure location is in the same world
        if (entity.getWorld() != this.getBukkitHandle().getWorld()) {
            return false;
        }
        // make sure we can walk towards target
        Location where = entity.getLocation();
        if (!this.getInsentient().D().a(where.getX(), where.getY(), where.getZ(), speed)) {
            return false;
        }
        // look at the target location
        this.look(entity.getEyeLocation());
        // we are done
        return true;
    }

    @Override
    public boolean walkTo(Location where, double speed) {
        // ensure location is in the same world
        if (where.getWorld() != this.getBukkitHandle().getWorld()) {
            return false;
        }
        // make sure we can walk towards target
        if (!this.getInsentient().D().a(where.getX(), where.getY(), where.getZ(), speed)) {
            return false;
        }
        // look at the target location
        this.look(where);
        // we are done
        return true;
    }

    @Override
    public void stopWalk() {
        this.getInsentient().D().n();
    }

    @Override
    public boolean stroll(int minimum, int maximum, double speed, Predicate<Location> predicate) {
        EntityInsentient insentient = getInsentient();
        if (insentient instanceof EntityCreature) {
            EntityCreature creature = (EntityCreature) insentient;
            org.bukkit.World world = insentient.getBukkitEntity().getWorld();

            // find a location to stroll towards
            Vec3D where = null;
            for (int i = 0; i < 6 && where == null; i++) {
                // pool a random location
                where = LandRandomPos.a(creature, maximum, minimum);
                if (where == null) {
                    where = DefaultRandomPos.a(creature, maximum, minimum);
                }
                // ensure it matches condition
                if (where != null && !predicate.test(new Location(world, where.c, where.d, where.e))) {
                    where = null;
                }
            }

            // if we got the target, stroll there
            if (where != null) {
                return insentient.D().a(where.c, where.d, where.e, speed);
            }
        }

        return false;
    }

    @Override
    public void look(Entity entity) {
        look(entity.getLocation());
    }

    @Override
    public void look(Location location) {
        EntityInsentient insentient = getInsentient();
        insentient.z().a(location.getX(), location.getY(), location.getZ());
    }

    @Override
    public boolean canSense(LivingEntity other) {
        try {
            // identify the entities which are to be checked
            CraftLivingEntity craft_asking = (CraftLivingEntity) this.getBukkitHandle();
            CraftLivingEntity craft_asked = (CraftLivingEntity) other;
            if (craft_asking == null || craft_asked == null) return false;
            // remap into the nms handle of the entities
            EntityInsentient handle_asking = (EntityInsentient) craft_asking.getHandle();
            EntityLiving handle_asked = craft_asked.getHandle();
            // check if the asking entity is able to sense the target
            return handle_asking.E().a(handle_asked);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean canSee(LivingEntity other) {
        if (getBukkitHandle().getWorld() != other.getWorld())
            return false;
        Location loc1 = getBukkitHandle().getEyeLocation();
        Location loc2 = other.getEyeLocation();

        World world = ((CraftWorld) other.getWorld()).getHandle();
        Vec3D p1 = new Vec3D(loc1.getX(), loc1.getY(), loc1.getZ());
        Vec3D p2 = new Vec3D(loc2.getX(), loc2.getY(), loc2.getZ());
        return world.a(new RayTrace(p1, p2, RayTrace.BlockCollisionOption.a, RayTrace.FluidCollisionOption.a, null))
                .c() == MovingObjectPosition.EnumMovingObjectType.a;
    }

    @Override
    public LivingEntity getBukkitHandle() {
        return this.bukkit_entity;
    }

    @Override
    public boolean isWalking() {
        return !this.getInsentient().D().l();
    }

    @Override
    public boolean isInDeathSequence() {
        return getAI().death_routine != null;
    }

    @Override
    public boolean doDeathSequence(Runnable callback) {
        // do not invoke multiple times
        if (getAI().death_routine != null) {
            return false;
        }
        // search for a routine to query
        for (AbstractEntityRoutine death_routine : getAI().death_routines) {
            if (death_routine.doStart()) {
                getAI().death_routine = death_routine;
                getAI().death_callback = callback;
                return false;
            }
        }
        // no routine found, we can just die
        callback.run();
        return true;
    }

    @Override
    public void resetRage() {
        // clear off rage related parameters
        getAI().rage_entity = null;
        getAI().rage_value = 0;
        getAI().rage_cooldown = 0;
        getAI().rage_focus = 0;
    }

    @Override
    public void enrage(LivingEntity source, double amount, double maximum, double focus, boolean forced) {
        // rage of less-equal zero is ignored
        if (amount <= 0d) {
            return;
        }

        // rage cannot change during death sequence
        if (getAI().death_routine != null) {
            return;
        }

        if (getAI().rage_entity == null) {
            // apply as the new rage holder, inherits prior rage
            getAI().rage_entity = source;
            getAI().rage_focus = focus;
            getAI().rage_value = Math.max(1d, getAI().rage_value);
            // 3 second cooldown before allowing to pull
            getAI().rage_cooldown = System.currentTimeMillis() + 5000L;
        } else if (getAI().rage_entity == source) {
            if (getAI().rage_value < maximum) {
                // accumulate rage, and respect limits
                double updated = getAI().rage_value + amount;
                updated = Math.min(updated, maximum);
                // update the rage we're holding
                getAI().rage_value = updated;
            }
        } else {
            // pull rage (reduced by focus from the rage holder)
            double updated = getAI().rage_value - (amount / (1d + Math.max(0d, getAI().rage_focus)));

            if (updated > 0d) {
                // reduced rage a little
                getAI().rage_value = updated;
            } else if (System.currentTimeMillis() < getAI().rage_cooldown) {
                // low rage allows to pull away after cooldown passed
                getAI().rage_value = 0.01d;
            } else {
                // apply as the new rage holder, rage overflow is maintained
                getAI().rage_entity = source;
                getAI().rage_focus = focus;
                getAI().rage_value = Math.max(1d, (-1d) * updated);
                // 3 second cooldown before allowing to pull
                getAI().rage_cooldown = System.currentTimeMillis() + 5000L;
            }
        }
    }

    @Override
    public LivingEntity getRageEntity() {
        // wipe the rage if we lost the holder
        LivingEntity rage_entity = getAI().rage_entity;
        if (rage_entity != null && !rage_entity.isValid()) {
            resetRage();
        }

        return getAI().rage_entity;
    }

    @Override
    public double getRageValue() {
        return getAI().rage_value;
    }

    @Override
    public boolean doBarrierDamageSoak(int damage) {
        // soak damage by all barrier phases
        boolean barrier = false;
        for (AbstractEntityRoutine routine : getAI().active.values()) {
            barrier = barrier || routine.doBarrierDamageSoak(damage);
        }
        return barrier;
    }

    @Override
    public void rageTransfer(LivingEntity target, double focus) {
        // apply as the new rage holder, rage overflow is maintained
        getAI().rage_entity = target;
        getAI().rage_focus = focus;
        getAI().rage_value = Math.max(1d, getAI().rage_value);
        // 3 second cooldown before allowing to pull
        getAI().rage_cooldown = System.currentTimeMillis() + 5000L;
    }

    /**
     * Wrapper for core AI implementation.
     *
     * @return a wrapper for Core based AI.
     */
    public CoreAI getAI() {
        if (!(getInsentient().bS instanceof CoreAI)) {
            getInsentient().bS = new CoreAI(getInsentient().s.ad());
            getInsentient().bT = new PathfinderGoalSelector(getInsentient().s.ad());
        }

        return (CoreAI) getInsentient().bS;
    }

    /**
     * Retrieve the insentient handle.
     *
     * @return insentient entity wrapper
     */
    public EntityInsentient getInsentient() {
        return ((CraftMob) this.getBukkitHandle()).getHandle();
    }

    class CoreAI extends PathfinderGoalSelector {
        Map<String, List<AbstractEntityRoutine>> routines = new HashMap<>();
        Map<String, AbstractEntityRoutine> active = new HashMap<>();
        List<AbstractEntityRoutine> death_routines = new ArrayList<>();
        LivingEntity rage_entity;
        double rage_value;
        double rage_focus;
        long rage_cooldown;
        AbstractEntityRoutine death_routine;
        Runnable death_callback;

        CoreAI(Supplier<GameProfilerFiller> var0) {
            super(var0);
            super.a(1);
        }

        @Override
        public void a() {
        }

        @Override
        public void a(boolean var0) {
            // a(boolean); and b(); are called in alteration
            this.b();
        }

        @Override
        public Set<PathfinderGoalWrapped> c() {
            return new HashSet<>();
        }

        @Override
        public void a(int var0) {
        }

        @Override
        public void b() {
            // conclude a death routine if we got one
            if (death_routine != null) {
                if (death_routine.doUpdate()) {
                    // invoke the callback
                    death_callback.run();
                    // if still active, remove
                    if (getBukkitHandle().isValid()) {
                        getBukkitHandle().setHealth(0d);
                        getBukkitHandle().remove();
                    }
                }
                // do not process basic AI
                return;
            }

            // tick the rage
            routines.forEach((key, routines) -> {
                // fetch which routine we are working with
                AbstractEntityRoutine routine = active.get(key);

                if (routine == null) {
                    // attempt to initialize a new routine
                    for (AbstractEntityRoutine candidate : routines) {
                        if (candidate.doStart()) {
                            active.put(key, candidate);
                            return;
                        }
                    }
                } else if (routine.doUpdate()) {
                    // check if we've finished
                    active.remove(key);
                    // remove if it was a singleton
                    if (routine.isSingleton()) {
                        routines.remove(routine);
                    }
                }
            });
        }

        @Deprecated
        @Override
        public void a(PathfinderGoal.Type var0) {
        }

        @Deprecated
        @Override
        public void a(PathfinderGoal var0) {
        }

        @Deprecated
        @Override
        public void a(PathfinderGoal.Type var0, boolean var1) {
        }

        @Deprecated
        @Override
        public void a(int var0, PathfinderGoal var1) {
        }

        @Deprecated
        @Override
        public Stream<PathfinderGoalWrapped> d() {
            return Stream.empty();
        }

        @Deprecated
        @Override
        public void b(PathfinderGoal.Type var0) {
        }
    }
}
