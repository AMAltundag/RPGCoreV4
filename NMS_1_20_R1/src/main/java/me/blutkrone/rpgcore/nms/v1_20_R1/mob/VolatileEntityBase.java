package me.blutkrone.rpgcore.nms.v1_20_R1.mob;

import me.blutkrone.rpgcore.nms.api.mob.AbstractEntityRoutine;
import me.blutkrone.rpgcore.nms.api.mob.IEntityBase;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class VolatileEntityBase implements IEntityBase {

    private final LivingEntity bukkit_entity;

    public VolatileEntityBase(LivingEntity bukkit_entity) {
        this.bukkit_entity = bukkit_entity;
    }

    @Override
    public boolean doBarrierDamageSoak(int damage) {
        // soak damage by all barrier phases
        boolean barrier = false;
        for (WrappedRoutineGroup group : getAI().routines.values()) {
            if (group.active != null) {
                barrier = barrier || group.active.routine.doBarrierDamageSoak(damage);
            }
        }
        return barrier;
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
    public void rageTransfer(LivingEntity target, double focus) {
        // apply as the new rage holder, rage overflow is maintained
        getAI().rage_entity = target;
        getAI().rage_focus = focus;
        getAI().rage_value = Math.max(1d, getAI().rage_value);
        // 3 second cooldown before allowing to pull
        getAI().rage_cooldown = System.currentTimeMillis() + 5000L;
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
    public void setAggressive(boolean aggressive) {
        getInsentient().setAggressive(true);
    }

    @Override
    public void addRoutine(String namespace, AbstractEntityRoutine routine) {
        getAI().routines.computeIfAbsent(namespace, (k -> new WrappedRoutineGroup())).routines.add(new WrappedRoutine(routine));
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
        if (!this.getInsentient().getNavigation().moveTo(where.getX(), where.getY(), where.getZ(), speed)) {
            return false;
        }
        // look at the target location
        this.look(entity.getEyeLocation());
        // snapshot the walk speed to avoid reflection
        getAI().last_known_walk_speed = speed;
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
        if (!this.getInsentient().getNavigation().moveTo(where.getX(), where.getY(), where.getZ(), speed)) {
            return false;
        }
        // look at the target location
        this.look(where);
        // snapshot the walk speed to avoid reflection
        getAI().last_known_walk_speed = speed;
        // we are done
        return true;
    }

    @Override
    public boolean stroll(int minimum, int maximum, double speed, Predicate<Location> predicate) {
        Mob insentient = getInsentient();
        if (insentient instanceof PathfinderMob) {
            PathfinderMob creature = (PathfinderMob) insentient;
            org.bukkit.World world = insentient.getBukkitEntity().getWorld();

            // find a location to stroll towards
            Vec3 where = null;
            for (int i = 0; i < 3 && where == null; i++) {
                // pool a random location
                where = LandRandomPos.getPos(creature, maximum, minimum);
                if (where == null) {
                    where = DefaultRandomPos.getPos(creature, maximum, minimum);
                }
                // ensure it matches condition
                if (where != null && !predicate.test(new Location(world, where.x, where.y, where.z))) {
                    where = null;
                }
            }

            // if we got the target, stroll there
            if (where != null) {
                getAI().last_known_walk_speed = speed;
                return insentient.getNavigation().moveTo(where.x, where.y, where.z, speed);
            }
        }

        return false;
    }

    @Override
    public void stopWalk() {
        getInsentient().getNavigation().stop();
    }

    @Override
    public void look(Entity entity) {
        look(entity.getLocation());
    }

    @Override
    public void look(Location location) {
        Mob insentient = getInsentient();
        insentient.getLookControl().setLookAt(location.getX(), location.getY(), location.getZ());
    }

    @Override
    public boolean canSense(LivingEntity other) {
        try {
            // identify the entities which are to be checked
            CraftLivingEntity craft_asking = (CraftLivingEntity) this.getBukkitHandle();
            CraftLivingEntity craft_asked = (CraftLivingEntity) other;
            if (craft_asking == null || craft_asked == null) return false;
            // remap into the nms handle of the entities
            Mob handle_asking = (Mob) craft_asking.getHandle();
            net.minecraft.world.entity.LivingEntity handle_asked = craft_asked.getHandle();
            // check if the asking entity is able to sense the target
            return handle_asking.getSensing().hasLineOfSight(handle_asked);
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

        ServerLevel world = ((CraftWorld) other.getWorld()).getHandle();
        Vec3 from = new Vec3(loc1.getX(), loc1.getY(), loc1.getZ());
        Vec3 to = new Vec3(loc2.getX(), loc2.getY(), loc2.getZ());
        return world.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null))
                .getType() == HitResult.Type.MISS;
    }

    @Override
    public LivingEntity getBukkitHandle() {
        return this.bukkit_entity;
    }

    @Override
    public boolean isWalking() {
        return !this.getInsentient().getNavigation().isDone();
    }

    @Override
    public double getWalkingSpeed() {
        return getAI().last_known_walk_speed;
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

    /**
     * Wrapper for core AI implementation.
     *
     * @return a wrapper for Core based AI.
     */
    public RPGCoreGoalSelector getAI() {
        if (!(getInsentient().goalSelector instanceof RPGCoreGoalSelector)) {
            getInsentient().goalSelector = new RPGCoreGoalSelector(getInsentient().level().getProfilerSupplier());
            getInsentient().targetSelector = new GoalSelector(getInsentient().level().getProfilerSupplier());
        }

        return (RPGCoreGoalSelector) getInsentient().goalSelector;
    }

    /**
     * Retrieve the insentient handle.
     *
     * @return insentient entity wrapper
     */
    public Mob getInsentient() {
        return ((CraftMob) this.getBukkitHandle()).getHandle();
    }

    class WrappedRoutine {
        // the routine that is wrapped
        AbstractEntityRoutine routine;
        // cooldown before checking this routine again
        int cooldown;

        public WrappedRoutine(AbstractEntityRoutine routine) {
            this.routine = routine;
            this.cooldown = 0;
        }
    }

    class WrappedRoutineGroup {
        // all routines that are allowed
        List<WrappedRoutine> routines;
        // current active routine in group
        WrappedRoutine active;

        public WrappedRoutineGroup() {
            this.routines = new ArrayList<>();
        }
    }

    /*
     * Special override of native goal selection process, primary
     * handling is done by RPGCore so no logic is necessary.
     */
    class RPGCoreGoalSelector extends GoalSelector {

        private final Supplier<ProfilerFiller> profiler;

        // general routines available to the entity
        Map<String, WrappedRoutineGroup> routines = new HashMap<>();
        // special routines invoked upon death
        List<AbstractEntityRoutine> death_routines = new ArrayList<>();
        AbstractEntityRoutine death_routine;
        Runnable death_callback;
        // target for entity rage
        LivingEntity rage_entity;
        double rage_value;
        double rage_focus;
        long rage_cooldown;
        // snapshot of walk speed to avoid reflection
        double last_known_walk_speed = 1.0f;

        public RPGCoreGoalSelector(Supplier<ProfilerFiller> supplier) {
            super(supplier);
            profiler = supplier;
            super.setNewGoalRate(1);
        }

        @Override
        public void tick() {
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

                // exit profiler
                profiler.get().pop();
                // do not process basic AI while dying
                return;
            }

            // tick the rage
            routines.forEach((key, group) -> {
                // update the cooldown of a routine
                for (WrappedRoutine routine : group.routines) {
                    routine.cooldown -= 1;
                }

                // initialize a routine if we are missing one
                for (int i = 0; group.active == null && i < group.routines.size(); i++) {
                    WrappedRoutine candidate = group.routines.get(i);
                    if (candidate.cooldown <= 0) {
                        if (candidate.routine.doStart()) {
                            group.active = candidate;
                        } else {
                            candidate.cooldown = candidate.routine.getWaitTime();
                        }
                    }
                }

                // process the routine that we have active
                if (group.active != null && group.active.routine.doUpdate()) {
                    group.active.cooldown = group.active.routine.getCooldownTime();

                    if (group.active.routine.isSingleton()) {
                        group.routines.remove(group.active);
                    }

                    group.active = null;
                }
            });

            // exit profiler
            profiler.get().pop();
        }

        @Override
        public void tickRunningGoals(boolean var0) {
            this.tick();
        }

        @Override
        public void removeAllGoals(Predicate<Goal> var0) {
        }

        @Override
        public void removeGoal(Goal var0) {
        }

        @Override
        public Set<WrappedGoal> getAvailableGoals() {
            return new HashSet<>();
        }

        @Override
        public Stream<WrappedGoal> getRunningGoals() {
            return Stream.empty();
        }

        @Override
        public void disableControlFlag(Goal.Flag var0) {
        }

        @Override
        public void enableControlFlag(Goal.Flag var0) {
        }

        @Override
        public void setControlFlag(Goal.Flag var0, boolean var1) {
        }

        @Override
        public void setNewGoalRate(int var0) {
        }
    }
}
