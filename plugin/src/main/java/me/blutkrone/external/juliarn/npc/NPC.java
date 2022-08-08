package me.blutkrone.external.juliarn.npc;

import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.google.common.base.Preconditions;
import me.blutkrone.external.juliarn.npc.event.PlayerNPCHideEvent;
import me.blutkrone.external.juliarn.npc.event.PlayerNPCShowEvent;
import me.blutkrone.external.juliarn.npc.modifier.*;
import me.blutkrone.external.juliarn.npc.profile.Profile;
import me.blutkrone.external.juliarn.npc.profile.ProfileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;

/**
 * Represents a non-person player which can be spawned and is managed by a {@link NPCPool}.
 */
public class NPC {

    private final Collection<Player> seeingPlayers = new CopyOnWriteArraySet<>();

    private final int entityId;
    private final boolean usePlayerProfiles;

    private final Profile profile;
    private final WrappedGameProfile gameProfile;
    private final SpawnCustomizer spawnCustomizer;
    private Location location;
    private boolean lookAtPlayer;
    private Predicate<Player> visible;

    /**
     * Creates a new npc instance.
     *
     * @param profile           The profile of the npc.
     * @param spawnCustomizer   The spawn customizer of the npc.
     * @param location          The location of the npc.
     * @param entityId          The entity id of the npc.
     * @param lookAtPlayer      If the npc should always look in the direction of the player.
     * @param usePlayerProfiles If the npc should use the profile of the player being spawned to.
     * @param visible           Whether to render for a player
     */
    private NPC(
            @Nullable Profile profile,
            @NotNull Location location,
            @NotNull SpawnCustomizer spawnCustomizer,
            int entityId,
            boolean lookAtPlayer,
            boolean usePlayerProfiles,
            Predicate<Player> visible
    ) {
        this.entityId = entityId;

        this.location = location;
        this.spawnCustomizer = spawnCustomizer;

        this.lookAtPlayer = lookAtPlayer;
        this.usePlayerProfiles = usePlayerProfiles;

        this.visible = visible;

        // no profile -> create a random one
        if (profile == null) {
            this.profile = new Profile(
                    UUID.randomUUID(),
                    ProfileUtils.randomName(),
                    Collections.emptyList());
        } else if (profile.getName() == null || profile.getUniqueId() == null) {
            this.profile = new Profile(
                    profile.getUniqueId() == null ? UUID.randomUUID() : profile.getUniqueId(),
                    profile.getName() == null ? ProfileUtils.randomName() : profile.getName(),
                    Collections.emptyList());
        } else {
            this.profile = profile;
        }
        // wrap the profile to into a ProtocolLib wrapper
        this.gameProfile = ProfileUtils.profileToWrapper(this.profile);
    }

    /**
     * Creates a new builder instance for a npc.
     *
     * @return a new builder instance for a npc.
     * @since 2.5-SNAPSHOT
     */
    @NotNull
    public static NPC.Builder builder() {
        return new NPC.Builder();
    }

    /**
     * Shows this npc to a player.
     *
     * @param player             The player to show this npc to.
     * @param plugin             The plugin requesting the change.
     * @param tabListRemoveTicks The ticks before removing the player from the player list after
     *                           spawning. A negative value indicates that this npc shouldn't get
     *                           removed from the player list.
     */
    protected void show(@NotNull Player player, @NotNull Plugin plugin, long tabListRemoveTicks) {
        this.seeingPlayers.add(player);

        VisibilityModifier modifier = new VisibilityModifier(this);
        modifier.queuePlayerListChange(PlayerInfoAction.ADD_PLAYER).send(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            modifier.queueSpawn().send(player);
            this.spawnCustomizer.handleSpawn(this, player);

            if (tabListRemoveTicks >= 0) {
                // keeping the NPC longer in the player list, otherwise the skin might not be shown sometimes.
                Bukkit.getScheduler().runTaskLater(
                        plugin,
                        () -> modifier.queuePlayerListChange(PlayerInfoAction.REMOVE_PLAYER).send(player),
                        tabListRemoveTicks);
            }

            Bukkit.getPluginManager().callEvent(new PlayerNPCShowEvent(player, this));
        }, 10L);
    }

    /**
     * Hides this npc from a player.
     *
     * @param player The player to hide the npc for.
     * @param plugin The plugin requesting the change.
     * @param reason The reason why the npc was hidden for the player.
     */
    protected void hide(
            @NotNull Player player,
            @NotNull Plugin plugin,
            @NotNull PlayerNPCHideEvent.Reason reason
    ) {
        this.visibility()
                .queuePlayerListChange(PlayerInfoAction.REMOVE_PLAYER)
                .queueDestroy()
                .send(player);
        this.removeSeeingPlayer(player);

        Bukkit.getScheduler().runTask(
                plugin,
                () -> Bukkit.getPluginManager().callEvent(new PlayerNPCHideEvent(player, this, reason)));
    }

    /**
     * Removes this player from the players that can see the npc.
     *
     * @param player The player to remove.
     */
    protected void removeSeeingPlayer(@NotNull Player player) {
        this.seeingPlayers.remove(player);
    }

    /**
     * Get an immutable copy of all players which can see this npc.
     *
     * @return a copy of all players seeing this npc.
     */
    @NotNull
    public Collection<Player> getSeeingPlayers() {
        return Collections.unmodifiableCollection(this.seeingPlayers);
    }

    /**
     * Get if this npc is shown for the given {@code player}.
     *
     * @param player The player to check.
     * @return If the npc is shown for the given {@code player}.
     */
    public boolean isShownFor(@NotNull Player player) {
        return this.seeingPlayers.contains(player);
    }

    /**
     * Get if the specified {@code player} is explicitly not allowed to see this npc.
     *
     * @param player The player to check.
     * @return if the specified {@code player} is explicitly not allowed to see this npc.
     */
    public boolean isExcluded(@NotNull Player player) {
        return !player.hasPermission("rpg.admin") && !this.visible.test(player);
    }

    /**
     * Creates a new animation modifier which serves methods to play animations on an NPC
     *
     * @return a animation modifier modifying this NPC
     */
    @NotNull
    public AnimationModifier animation() {
        return new AnimationModifier(this);
    }

    /**
     * Creates a new rotation modifier which serves methods related to entity rotation
     *
     * @return a rotation modifier modifying this NPC
     */
    @NotNull
    public RotationModifier rotation() {
        return new RotationModifier(this);
    }

    /**
     * Creates a new equipemt modifier which serves methods to change an NPCs equipment
     *
     * @return an equipment modifier modifying this NPC
     */
    @NotNull
    public EquipmentModifier equipment() {
        return new EquipmentModifier(this);
    }

    /**
     * Creates a new metadata modifier which serves methods to change an NPCs metadata, including
     * sneaking etc.
     *
     * @return a metadata modifier modifying this NPC
     */
    @NotNull
    public MetadataModifier metadata() {
        return new MetadataModifier(this);
    }

    /**
     * Creates a new visibility modifier which serves methods to change an NPCs visibility.
     *
     * @return a visibility modifier modifying this NPC
     * @since 2.5-SNAPSHOT
     */
    @NotNull
    public VisibilityModifier visibility() {
        return new VisibilityModifier(this);
    }

    /**
     * Creates a new teleport modifier which serves methods to teleport an NPC.
     *
     * @return a teleport modifier modifying this NPC
     * @since 2.7-SNAPSHOT
     */
    @NotNull
    public TeleportModifier teleport() {
        return new TeleportModifier(this);
    }

    /**
     * Get the protocol lib profile wrapper for this npc. To use this method {@code ProtocolLib} is
     * needed as a dependency of your project. If you don't want to do that, use {@link #getProfile()}
     * instead.
     *
     * @return the protocol lib profile wrapper for this npc
     */
    @NotNull
    public WrappedGameProfile getGameProfile() {
        return this.gameProfile;
    }

    /**
     * The profile of this npc. The returned profile is mutable, however this has no effect to this
     * npc.
     *
     * @return The profile of this npc.
     * @since 2.5-SNAPSHOT
     */
    @NotNull
    public Profile getProfile() {
        return this.profile;
    }

    /**
     * Get the entity id of this npc.
     *
     * @return the entity id of this npc.
     */
    public int getEntityId() {
        return this.entityId;
    }

    /**
     * Get the location where this npc is located.
     *
     * @return the location where this npc is located.
     */
    @NotNull
    public Location getLocation() {
        return this.location;
    }

    /**
     * Method used to update only the variable of the position.
     * No supported for public use, changing the location of the npc would only lead to problems.
     * If you want to teleport the NPC instead you can use {@link #teleport()}.
     *
     * @param location new location of npc.
     * @since 2.7-SNAPSHOT
     */
    public void setLocation(@NotNull Location location) {
        this.location = location;
    }

    /**
     * Gets if this npc should always look to the player.
     *
     * @return if this npc should always look to the player.
     */
    public boolean isLookAtPlayer() {
        return this.lookAtPlayer;
    }

    /**
     * Sets if this npc should always look to the player.
     *
     * @param lookAtPlayer if this npc should always look to the player.
     */
    public void setLookAtPlayer(boolean lookAtPlayer) {
        this.lookAtPlayer = lookAtPlayer;
    }

    /**
     * Gets if this npc should always use the profile of the player being spawned to when spawning.
     *
     * @return if this npc uses the profile of the player being spawned to.
     * @since 2.7-SNAPSHOT
     */
    public boolean isUsePlayerProfiles() {
        return this.usePlayerProfiles;
    }

    /**
     * A builder for a npc.
     */
    public static class Builder {

        private Profile profile;

        private boolean lookAtPlayer = true;
        private boolean usePlayerProfiles = false;

        private Location location = new Location(Bukkit.getWorlds().get(0), 0D, 0D, 0D);
        private SpawnCustomizer spawnCustomizer = (npc, player) -> {
        };

        private Predicate<Player> visible = (player) -> true;

        /**
         * Creates a new builder instance.
         */
        private Builder() {
        }

        /**
         * Creates a new instance of the NPC builder
         *
         * @param profile a player profile defining UUID, name and textures of the NPC
         * @deprecated Use {@link NPC#builder()} instead
         */
        @Deprecated
        public Builder(@NotNull Profile profile) {
            this.profile = profile;
        }

        /**
         * Control whether the NPC should be visible/interact-able to a given
         * player.
         *
         * @param predicate the predicate to apply.
         * @return this builder instance
         */
        public Builder visible(Predicate<Player> predicate) {
            this.visible = predicate;
            return this;
        }

        /**
         * Sets the profile of the npc, cannot be changed afterwards.
         * <p>
         * If {@link #usePlayerProfiles(boolean)} gets set to {@code true} and a profile gets supplied
         * the unique id and name of the profile will be used. If no profile is given a random unique id
         * and name will be used for the npc when spawning. Profile properties of the given {@code
         * profile} will get ignored.
         * </p>
         *
         * @param profile the profile of this npc.
         * @return this builder instance
         */
        public Builder profile(@Nullable Profile profile) {
            this.profile = profile;
            return this;
        }

        /**
         * Sets the location of the npc, cannot be changed afterwards
         *
         * @param location the location
         * @return this builder instance
         */
        public Builder location(@NotNull Location location) {
            this.location = Preconditions.checkNotNull(location, "location");
            return this;
        }

        /**
         * Enables/disables looking at the player, default is true
         *
         * @param lookAtPlayer if the NPC should look at the player
         * @return this builder instance
         */
        public Builder lookAtPlayer(boolean lookAtPlayer) {
            this.lookAtPlayer = lookAtPlayer;
            return this;
        }

        /**
         * Sets an executor which will be called every time the NPC is spawned for a certain player.
         * Permanent NPC modifications should be done in this method, otherwise they will be lost at the
         * next respawn of the NPC.
         *
         * @param spawnCustomizer the spawn customizer which will be called on every spawn
         * @return this builder instance
         */
        public Builder spawnCustomizer(@NotNull SpawnCustomizer spawnCustomizer) {
            this.spawnCustomizer = Preconditions.checkNotNull(spawnCustomizer, "spawnCustomizer");
            return this;
        }

        /**
         * Sets that the npc always uses the skin of the player being spawned to rather than a fixed
         * supplied skin during the build process. In other words, if this option is set to {@code true}
         * no skin must be supplied to {@link #profile(Profile)}.
         *
         * @param usePlayerProfiles if the npc should always use the profile properties of the player
         *                          being spawned to.
         * @return this builder instance.
         * @since 2.7-SNAPSHOT
         */
        public Builder usePlayerProfiles(boolean usePlayerProfiles) {
            this.usePlayerProfiles = usePlayerProfiles;
            return this;
        }

        /**
         * Passes the NPC to a pool which handles events, spawning and destruction of this NPC for
         * players
         *
         * @param pool the pool the NPC will be passed to
         * @return this builder instance
         */
        @NotNull
        public NPC build(@NotNull NPCPool pool) {
            if (!this.usePlayerProfiles && (this.profile == null || !this.profile.isComplete())) {
                throw new IllegalArgumentException("No profile given or not completed");
            }

            NPC npc = new NPC(
                    this.profile,
                    this.location,
                    this.spawnCustomizer,
                    pool.getFreeEntityId(),
                    this.lookAtPlayer,
                    this.usePlayerProfiles,
                    this.visible);
            pool.takeCareOf(npc);

            return npc;
        }
    }
}
