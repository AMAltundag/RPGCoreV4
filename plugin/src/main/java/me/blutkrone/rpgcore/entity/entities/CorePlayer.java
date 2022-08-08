package me.blutkrone.rpgcore.entity.entities;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.data.IDataIdentity;
import me.blutkrone.rpgcore.api.entity.EntityProvider;
import me.blutkrone.rpgcore.attribute.IExpiringModifier;
import me.blutkrone.rpgcore.entity.IOfflineCorePlayer;
import me.blutkrone.rpgcore.entity.focus.FocusTracker;
import me.blutkrone.rpgcore.entity.tasks.PlayerFocusTask;
import me.blutkrone.rpgcore.hud.menu.SettingsMenu;
import me.blutkrone.rpgcore.item.ItemManager;
import me.blutkrone.rpgcore.item.data.ItemDataGeneric;
import me.blutkrone.rpgcore.item.data.ItemDataJewel;
import me.blutkrone.rpgcore.item.data.ItemDataModifier;
import me.blutkrone.rpgcore.job.CoreJob;
import me.blutkrone.rpgcore.minimap.MapMarker;
import me.blutkrone.rpgcore.skill.skillbar.OwnedSkillbar;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;

import java.io.IOException;
import java.util.*;

public class CorePlayer extends CoreEntity implements IOfflineCorePlayer, IDataIdentity {

    // character of the roster we represent
    private final int character;

    // a tracker for the focus ux component
    private FocusTracker focus_tracker = new FocusTracker();

    // a counter until the entity will die for real
    private int grave_counter = -1;

    // settings customizable by player
    private SettingsMenu.Settings settings = new SettingsMenu.Settings();

    // a listing of temporary map markers
    private Map<String, MapMarker> map_markers = new HashMap<>();

    // a skillbar to process skill inputs
    private OwnedSkillbar skillbar;
    private boolean skillbar_active;

    // evolution slots that were unlocked
    private Map<String, Set<Integer>> evolution_unlock = new HashMap<>();
    // one evolution per list element
    private Map<String, Map<Integer, String>> evolution_fitted = new HashMap<>();

    // default position to respawn, if nothing else specified
    private Location respawn_position;
    // location to go to once ready to play
    private Location login_position;

    // flag the entity for initiation having finished
    private boolean initiated;

    // display/cosmetic information to use
    private String alias = "nothing";
    private String portrait = "nothing";

    // the job of the player
    private String job = "nothing";

    // items in the equipment menu
    private Map<String, ItemStack> equipped = new HashMap<>();

    // an index of recently edited elements
    private List<String> editor_history = new ArrayList<>();

    // item persistence for relevant menus
    private Map<String, ItemStack> menu_persistence = new HashMap<>();

    // item ID mapped to banked quantity of it
    private Map<String, Integer> banked_items = new HashMap<>();
    // items stored as-is, can be a B64 string or an item list (up to 54)
    private Map<String, String> stored_items = new HashMap<>();
    private Map<String, Long> storage_unlocked = new HashMap<>();

    // refinement timestamps for afk refinement
    private Map<String, Long> refinement_timestamp = new HashMap<>();

    // tags used for quest processing
    private List<String> active_quests = new ArrayList<>();
    private Set<String> completed_quests = new HashSet<>();
    private Map<String, Integer> progress_quests = new HashMap<>();

    // persistent tags on the player
    private Set<String> persistent_tags = new HashSet<>();

    // a snapshot of carried items used by quests
    private Map<String, Integer> quest_items_snapshot = null;
    private long quest_items_timestamp = 0;

    public CorePlayer(LivingEntity entity, EntityProvider provider, int character) {
        super(entity, provider);

        Bukkit.getLogger().severe("not implemented (data persistence)");

        // initialize the relevant skillbar
        this.skillbar = new OwnedSkillbar(this);
        // task to invalidate the tracking when appropriate
        this.bukkit_tasks.add(new PlayerFocusTask(this).runTaskTimer(RPGCore.inst(), 1, 5));
        // the character which we represent
        this.character = character;
    }

    /**
     * A snapshot map which slowly updates, tracking the items that
     * may be demanded by quests.
     *
     * @return a (somewhat) up-to-date snapshot.
     */
    public Map<String, Integer> getSnapshotForQuestItems() {
        // update the cache if necessary
        if (System.currentTimeMillis() + 2000L > this.quest_items_timestamp || this.quest_items_snapshot == null) {
            Map<String, Integer> quantified = new HashMap<>();
            for (ItemStack item : this.getEntity().getInventory().getContents()) {
                ItemDataGeneric data = RPGCore.inst().getItemManager().getItemData(item, ItemDataGeneric.class);
                if (data != null) {
                    quantified.merge(data.getItem().getId(), item.getAmount(), (a, b) -> a + b);
                }
            }
            this.quest_items_snapshot = quantified;
            this.quest_items_timestamp = System.currentTimeMillis();
        }

        return this.quest_items_snapshot;
    }

    /**
     * Tags are permanently present on the player.
     *
     * @return the tags present on the player.
     */
    public Set<String> getPersistentTags() {
        return persistent_tags;
    }

    /**
     * Mapping a distinctive quest identifier to a decimal
     * number which is used to track quest progress.
     *
     * @return identifier mapped to task progress.
     */
    public Map<String, Integer> getProgressQuests() {
        return progress_quests;
    }

    /**
     * Ids of quests which the player is actively engaging
     * with, when adding to this list please ensure that it
     * is a distinct collection.
     *
     * @return active quest ids.
     */
    public List<String> getActiveQuestIds() {
        return this.active_quests;
    }

    /**
     * Ids of all quests the player completed.
     *
     * @return completed quest ids.
     */
    public Set<String> getCompletedQuests() {
        return completed_quests;
    }

    /**
     * Retrieve the storage unlocks.
     *
     * @return identifiers we've unlocked.
     */
    public Map<String, Long> getStorageUnlocked() {
        return storage_unlocked;
    }

    /**
     * Timestamps for refinement, this allows us to lazy-process refinement
     * while the menu is closed.
     *
     * @return inventory identifier mapped to timestamp menu closed at.
     */
    public Map<String, Long> getRefinementTimestamp() {
        return refinement_timestamp;
    }

    /**
     * Banked items are identified by a 'bank' keyword, allowing them
     * to be stored together as a quantitative value.
     *
     * @return items stored by quantity (via bank identifier.)
     */
    public Map<String, Integer> getBankedItems() {
        return banked_items;
    }

    /**
     * Items stored as actual items, do note that the items have to be
     * encoded into a B64 string when loaded and un-loaded.
     *
     * @return items stored as their individual item.
     */
    public Map<String, String> getStoredItems() {
        return stored_items;
    }

    /**
     * Persistent storage of items used by a menu, please use a prefix
     * to avoid namespace conflicts.
     *
     * @return items used by a menu.
     */
    public Map<String, ItemStack> getMenuPersistence() {
        return menu_persistence;
    }

    /**
     * A history of what was most recently edited by the user, do note that
     * this does not discriminate history of two elements sharing an ID.
     *
     * @return a list of what was most recently edited.
     */
    public List<String> getEditorHistory() {
        return editor_history;
    }

    /**
     * Abandon preceding modifiers from equipment
     */
    public void applyEquipment() {
        ItemManager manager = RPGCore.inst().getItemManager();

        // abandon previous equipment sourced modifiers
        List<IExpiringModifier> expiring = getExpiringModifiers("EQUIPMENT");
        expiring.forEach(IExpiringModifier::setExpired);
        expiring.clear();
        // apply the attributes gained from the items
        for (ItemStack item : this.equipped.values()) {
            // modifiers directly on the item
            ItemDataModifier modifiers = manager.getItemData(item, ItemDataModifier.class);
            if (modifiers == null) {
                continue;
            }
            expiring.addAll(modifiers.apply(this));
            // modifiers from socketed jewels
            ItemDataJewel jewels = manager.getItemData(item, ItemDataJewel.class);
            jewels.getAttributes().forEach((attribute, factor) -> {
                expiring.add(this.getAttribute(attribute).create(factor));
            });
        }
    }

    /**
     * Update the item equipped to a certain slot.
     *
     * @param slot the slot to update
     * @param item the item to replace with
     */
    public void setEquipped(String slot, ItemStack item) {
        this.equipped.put(slot, item == null ? new ItemStack(Material.AIR) : item);
    }

    /**
     * Retrieve the item equipped to a certain slot.
     *
     * @param slot equipment slot to check
     * @return the item on that slot, or an air item
     */
    public ItemStack getEquipped(String slot) {
        return this.equipped.getOrDefault(slot, new ItemStack(Material.AIR));
    }

    /**
     * Retrieve all items equipped by this player.
     *
     * @return the items that are equipped.
     */
    public Map<String, ItemStack> getEquipped() {
        return Collections.unmodifiableMap(equipped);
    }

    /**
     * Retrieve all evolutions of a skill
     *
     * @param skill the skill whose evolutions we fetch
     * @return a read-only view of the evolutions
     */
    public Map<Integer, String> getEvolution(String skill) {
        return Collections.unmodifiableMap(this.evolution_fitted.computeIfAbsent(skill, (k -> new HashMap<>())));
    }

    /**
     * Update the given evolution, remove the evolution by updating
     * it to a null value.
     *
     * @param skill     which skill to update.
     * @param position  the position of the evolution.
     * @param evolution which evolution to use
     */
    public void setEvolution(String skill, int position, String evolution) {
        Map<Integer, String> current = this.evolution_fitted.computeIfAbsent(skill, (k -> new HashMap<>()));
        if (evolution == null) {
            current.remove(position);
        } else {
            current.put(position, evolution);
        }
    }

    /**
     * Purge all evolution options which do not match with the
     * given list of slots.
     *
     * @param skill which skill to validate
     * @param valid the slots that can be valid
     * @return true if evolutions were abandoned
     */
    public boolean fixEvolution(String skill, List<Integer> valid) {
        boolean fixed = false;
        Map<Integer, String> fitted = this.evolution_fitted.get(skill);
        fixed |= fitted != null && fitted.keySet().removeIf(slot -> !valid.contains(slot));
        Set<Integer> unlocked = this.evolution_unlock.get(skill);
        fixed |= unlocked != null && unlocked.removeIf(slot -> !valid.contains(slot));
        return fixed;
    }

    /**
     * Attempt to unlock an additional evolution slot, so long
     * the key grade is high enough.
     *
     * @param key_grade the grade of the key.
     * @return true if we unlocked a new slot.
     */
    public boolean tryToUnlockEvolveWithKey(String skill, int position, int key_grade) {
        Set<Integer> current = this.evolution_unlock.computeIfAbsent(skill, (k -> new HashSet<>()));
        if (current.size() < key_grade) {
            current.add(position);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check if the evolution slot was unlocked.
     *
     * @param skill    which skill to check
     * @param position which position to check
     * @return true if the slot is unlocked
     */
    public boolean hasUnlockedEvolutionSlot(String skill, int position) {
        return this.evolution_unlock.computeIfAbsent(skill, (k -> new HashSet<>())).contains(position);
    }

    /**
     * Direct access to the selected skill evolutions.
     *
     * @return skill evolution map.
     */
    public Map<String, Map<Integer, String>> getEvolutionFitted() {
        return evolution_fitted;
    }

    /**
     * Direct access to the unlocked evolution slots.
     *
     * @return which evolution slots were unlocked.
     */
    public Map<String, Set<Integer>> getEvolutionUnlock() {
        return evolution_unlock;
    }

    /**
     * A job provides certain benefits, while also granting access
     * to a set of passive-tree options.
     *
     * @return the job of this player.
     */
    public CoreJob getJob() {
        if (this.job == null || this.job.isEmpty() || "nothing".equalsIgnoreCase(this.job))
            return null;
        return RPGCore.inst().getJobManager().getIndex().get(this.job);
    }

    /**
     * A job provides certain benefits, while also granting access
     * to a set of passive-tree options.
     *
     * @param job the new job of the player.
     */
    public void setJob(String job) {
        this.job = job;
    }

    /**
     * The identifier of the job which we are backed up by.
     *
     * @return the job identifier.
     */
    public String getRawJob() {
        return this.job;
    }

    /**
     * The portrait of the player.
     *
     * @return the portrait to use.
     */
    public String getPortrait() {
        return portrait;
    }

    /**
     * The portrait used for various menus, including the roster
     * selection but not limited to it.
     *
     * @param portrait the portrait we are using.
     */
    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    /**
     * The alias to give individual characters a unique flair.
     *
     * @return character alias, might be empty.
     */
    public String getAlias() {
        return alias;
    }

    /**
     * The alias to give individual characters a unique flair.
     *
     * @param alias character alias to use.
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * The process is marked as finished only once.
     *
     * @return true if we are done with the initiation.
     */
    public boolean isInitiated() {
        return initiated;
    }

    /**
     * Mark the initiator process as being finished.
     */
    public void setInitiated() {
        this.initiated = true;
    }

    /**
     * The position to resurrect at after death, should there be an
     * external override on the respawn position it will be taking
     * precedence.
     *
     * @return default respawn position.
     */
    public Location getRespawnPosition() {
        return respawn_position;
    }

    /**
     * The position to resurrect at after death, should there be an
     * external override on the respawn position it will be taking
     * precedence.
     *
     * @param respawn_position default respawn position.
     */
    public void setRespawnPosition(Location respawn_position) {
        this.respawn_position = respawn_position;
    }

    /**
     * Move to the login position, if no login position was set we
     * move to the respawn location instead.
     */
    public void moveToLoginPosition() {
        try {
            // attempt to recover login position
            getEntity().teleport(this.login_position);
        } catch (Exception e1) {
            // attempt to recover to respawn position
            try {
                getEntity().teleport(this.respawn_position);
            } catch (Exception e2) {
                // kick from server (bad location)
                getEntity().kickPlayer("§cYou've been kicked by: §fRPGCore\n\n§cIllegal Location!");
            }
        }
    }

    /**
     * Define the login position, used to make sure that delayed
     * spawn position rules can be applied properly. Do note that
     * this value is only processed once.
     *
     * @param login_position where to spawn-in once available.
     */
    public void setLoginPosition(Location login_position) {
        this.login_position = login_position;
    }

    /**
     * Verify if the skillbar is still considered active.
     *
     * @return true if the skillbar is active.
     */
    public boolean isSkillbarActive() {
        // skillbar is de-activated if there is an activity
        if (this.getActivity() != null || this.checkForTag("SKILLBAR_DISABLED")) {
            this.skillbar_active = false;
        }
        // offer up the skillbar active flag
        return this.skillbar_active;
    }

    /**
     * Flag the skillbar as being active.
     *
     * @param skillbar_active true if the skillbar is active.
     */
    public void setSkillbarActive(boolean skillbar_active) {
        this.skillbar_active = skillbar_active;
    }

    /**
     * Generate a float-text notification on the interface.
     *
     * @param color   the color of the message
     * @param message the contents of the message
     */
    public void notify(ChatColor color, String message) {
        Bukkit.getLogger().severe("not implemented (notification message)");
    }

    /**
     * Fetch the skillbar which is associated with the given
     * player.
     *
     * @return the skillbar of the given player.
     */
    public OwnedSkillbar getSkillbar() {
        return skillbar;
    }

    /**
     * Retrieve all personal map markers on the entity.
     *
     * @return markers on the map.
     */
    public Map<String, MapMarker> getMapMarkers() {
        return map_markers;
    }

    /**
     * Settings which the client is allowed to customize.
     *
     * @return the settings for this specific player.
     */
    public SettingsMenu.Settings getSettings() {
        return settings;
    }

    /**
     * Upon death, a time-limited counter applies until the entity
     * will perish.
     *
     * @return the ticks before entity perishes, or -1 if inactive.
     */
    public int getGraveCounter() {
        return grave_counter;
    }

    /**
     * Retrieve the tracked focus.
     *
     * @return the tracker which we are focused on.
     */
    public FocusTracker getFocusTracker() {
        return focus_tracker;
    }

    /**
     * Draw a ray-cast to the next entity which we collide with.
     *
     * @param distance range to throw a ray-cast in
     * @return the entity we ray-casted
     */
    public CoreEntity rayCastTarget(double distance) {
        LivingEntity bukkit_entity = getEntity();
        World world = bukkit_entity.getWorld();
        // throw a ray-cast to look for a valid candidate
        RayTraceResult result = world.rayTraceEntities(bukkit_entity.getEyeLocation(), bukkit_entity.getLocation().getDirection(), distance, 0.1d, candidate -> {
            if (bukkit_entity == candidate) return false;
            CoreEntity target = RPGCore.inst().getEntityManager().getEntity(candidate.getUniqueId());
            return target != null && !target.isInvalid() && target.isAllowTarget();
        });
        // ensure we've collided with something
        if (result == null || result.getHitEntity() == null)
            return null;
        // offer up the entity we've collided with
        return RPGCore.inst().getEntityManager().getEntity(result.getHitEntity().getUniqueId());
    }

    @Override
    public void remove() {
        // this is a dupe preventing system, it should ensure that
        // no player can logout during a menu interaction which is
        // expecting to write to the player data.
        Player player_handle = getEntity();
        if (player_handle != null) {
            player_handle.closeInventory();
        }
        // basic entity handling
        super.remove();
        // request the data-handler to drop our data
        try {
            RPGCore.inst().getDataManager().savePlayer(this);
            Bukkit.getLogger().severe("not implemented (async data handling)");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Player getEntity() {
        return (Player) super.getEntity();
    }

    @Override
    public boolean isAllowTarget() {
        Player player = getEntity();
        // if (getGraveTimer() > 0) return false;
        if (player == null) return false;
        if (player.isDead()) return false;
        if (player.getGameMode() == GameMode.SPECTATOR) return false;
        if (player.getGameMode() == GameMode.CREATIVE) return false;
        return true;
    }

    @Override
    public String getName() {
        return getOfflinePlayer().getName();
    }

    @Override
    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(this.getUniqueId());
    }

    @Override
    public UUID getUserId() {
        return getUniqueId();
    }

    @Override
    public int getCharacter() {
        return this.character;
    }
}
