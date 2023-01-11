package me.blutkrone.rpgcore.entity.entities;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.data.IDataIdentity;
import me.blutkrone.rpgcore.api.entity.EntityProvider;
import me.blutkrone.rpgcore.attribute.IExpiringModifier;
import me.blutkrone.rpgcore.damage.DamageMetric;
import me.blutkrone.rpgcore.entity.IOfflineCorePlayer;
import me.blutkrone.rpgcore.entity.focus.FocusTracker;
import me.blutkrone.rpgcore.entity.tasks.PlayerFocusTask;
import me.blutkrone.rpgcore.hud.menu.SettingsMenu;
import me.blutkrone.rpgcore.item.ItemManager;
import me.blutkrone.rpgcore.item.data.ItemDataGeneric;
import me.blutkrone.rpgcore.item.data.ItemDataJewel;
import me.blutkrone.rpgcore.item.data.ItemDataModifier;
import me.blutkrone.rpgcore.item.modifier.CoreModifier;
import me.blutkrone.rpgcore.job.CoreJob;
import me.blutkrone.rpgcore.level.LevelManager;
import me.blutkrone.rpgcore.minimap.MapMarker;
import me.blutkrone.rpgcore.passive.CorePassiveNode;
import me.blutkrone.rpgcore.passive.CorePassiveTree;
import me.blutkrone.rpgcore.passive.node.*;
import me.blutkrone.rpgcore.skill.CoreSkill;
import me.blutkrone.rpgcore.skill.SkillContext;
import me.blutkrone.rpgcore.skill.behaviour.BehaviourEffect;
import me.blutkrone.rpgcore.skill.behaviour.CoreBehaviour;
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

    // snapshot of passive behaviours from skills
    private Map<CoreBehaviour, BehaviourEffect> passive_behaviour = new HashMap<>();

    // level of professions we currently have
    private Map<String, Integer> profession_level = new HashMap<>();
    private Map<String, Double> profession_exp = new HashMap<>();

    // information and caching regarding passive tree
    private Map<String, Long> passive_integrity = new HashMap<>();
    private Map<String, Long> passive_viewport = new HashMap<>();
    private Map<String, Integer> passive_points = new HashMap<>();
    private Map<String, Integer> passive_refunds = new HashMap<>();
    private Map<String, Set<Long>> passive_allocated = new HashMap<>();
    private Map<String, Map<Long, ItemStack>> passive_socketed = new HashMap<>();
    private Map<String, Set<String>> passive_cache_skill_tag = new HashMap<>();
    private Map<String, Map<String, Double>> passive_cache_skill_attribute = new HashMap<>();
    private Map<String, List<String>> passive_cache_skill_reference = new HashMap<>();

    // metrics about damage inflicted
    private Map<String, DamageMetric> metrics = new HashMap<>();

    // levelling parameters
    private double current_exp;

    public CorePlayer(LivingEntity entity, EntityProvider provider, int character) {
        super(entity, provider);
        this.getMyTags().add("PLAYER");

        // initialize the relevant skillbar
        this.skillbar = new OwnedSkillbar(this);
        // task to invalidate the tracking when appropriate
        this.bukkit_tasks.add(new PlayerFocusTask(this).runTaskTimer(RPGCore.inst(), 1, 5));
        // the character which we represent
        this.character = character;
    }

    @Override
    public void setCurrentLevel(int current_level) {
        // update the level
        super.setCurrentLevel(current_level);
        // abandon previous attributes
        List<IExpiringModifier> modifiers = getExpiringModifiers("STAT_FROM_LEVEL");
        modifiers.forEach(IExpiringModifier::setExpired);
        modifiers.clear();
        // re-compute temporary attributes
        Map<String, Double> attributes = RPGCore.inst().getLevelManager().getAttributesFromLevels(this);
        attributes.forEach((id, factor) -> {
            modifiers.add(getAttribute(id).create(factor));
        });
    }

    /**
     * Exp for the current level of profession.
     *
     * @return profession mapped to exp gathered
     */
    public Map<String, Double> getProfessionExp() {
        return profession_exp;
    }

    /**
     * Level of the current professions.
     *
     * @return profession mapped to level
     */
    public Map<String, Integer> getProfessionLevel() {
        return profession_level;
    }

    /**
     * Current experience level, intended to be handled via level
     * manager. The exp resets whenever we do level up.
     *
     * @return current experience.
     */
    public double getCurrentExp() {
        return current_exp;
    }

    /**
     * Current experience level, intended to be handled via level
     * manager. The exp resets whenever we do level up.
     *
     * @param current_exp updated experience.
     */
    public void setCurrentExp(double current_exp) {
        LevelManager manager = RPGCore.inst().getLevelManager();

        // cannot gain exp if the current level isn't segmented for
        if (!manager.isSegmentedFor(getCurrentLevel())) {
            this.current_exp = 0d;
            return;
        }
        // cannot gain exp if missing a tag required to level up
        for (String tag : manager.getRequiredTags(getCurrentLevel())) {
            if (!getPersistentTags().contains(tag)) {
                this.current_exp = 0d;
                return;
            }
        }
        // cannot gain exp if next level does not exist
        double exp_to_level_up = manager.getExpToLevelUp(this);
        if (exp_to_level_up < 0d) {
            this.current_exp = 0d;
            return;
        }
        // update experience we have accumulated
        this.current_exp = Math.max(0d, current_exp);
        // check if we have enough exp to level up
        if (this.current_exp < exp_to_level_up) {
            return;
        }
        // check if we have appropriate tags
        for (String tag : manager.getRequiredTags(getCurrentLevel() + 1)) {
            if (!getPersistentTags().contains(tag)) {
                this.current_exp = exp_to_level_up - 1;
                return;
            }
        }
        // update our current level
        this.current_exp = 0d;
        setCurrentLevel(this.getCurrentLevel() + 1);
    }

    /**
     * Metrics on damage that was inflicted.
     *
     * @param blame the blame identifies damage origin.
     * @return a metric for a certain damage.
     */
    public DamageMetric getMetric(String blame) {
        return this.metrics.computeIfAbsent(blame, (k -> new DamageMetric()));
    }

    /**
     * Request an update of every passive tree the player has
     * access to.
     */
    public void updatePassiveTree() {
        List<IExpiringModifier> passive_cache_attribute = getExpiringModifiers("passive");
        // reset the caching structure we have
        passive_cache_attribute.clear();
        passive_cache_skill_attribute.clear();
        passive_cache_skill_reference.clear();
        passive_cache_skill_tag.clear();

        // first pass should strip away any bad tree
        List<String> needIntegrityCheck = new ArrayList<>(this.passive_allocated.keySet());
        for (String treeId : needIntegrityCheck) {
            CorePassiveTree tree = RPGCore.inst().getPassiveManager().getTreeIndex().get(treeId);
            tree.ensureIntegrity(this);
        }
        // second pass to handle skills available to player
        for (CoreSkill skill : RPGCore.inst().getSkillManager().getIndex().getAll()) {
            CorePassiveTree tree = RPGCore.inst().getPassiveManager().getTreeIndex().get("skill_" + skill.getId());
            Map<String, Double> skill_attributes = passive_cache_skill_attribute.computeIfAbsent(skill.getId(), (k -> new HashMap<>()));
            List<String> skill_references = passive_cache_skill_reference.computeIfAbsent(skill.getId(), (k -> new ArrayList<>()));
            Set<String> skill_tags = passive_cache_skill_tag.computeIfAbsent(skill.getId(), (k -> new HashSet<>()));

            // process the relevant nodes on the tree
            Set<Long> allocated = getAllocated(tree.getId());
            for (long where : allocated) {
                // never process the entry node
                if (where == 0L) {
                    continue;
                }
                // ensure node is not illegal
                CorePassiveNode node = tree.getNode(where);
                if (node == null) {
                    continue;
                }
                // handle the node on the tree
                AbstractCorePassive effect = node.getEffect().orElse(null);
                if (effect instanceof CorePassiveEntityAttribute) {
                    // add attributes to the entity
                    Map<String, Double> attributes = ((CorePassiveEntityAttribute) effect).getAttributes();
                    attributes.forEach((attribute, factor) -> {
                        passive_cache_attribute.add(getAttribute(attribute).create(factor));
                    });
                } else if (effect instanceof CorePassiveSkillAttribute) {
                    // add attributes to the skill
                    Map<String, Double> attributes = ((CorePassiveSkillAttribute) effect).getAttributes();
                    attributes.forEach((attribute, factor) -> {
                        skill_attributes.merge(attribute, factor, (a, b) -> a + b);
                    });
                } else {
                    ItemStack item = getPassiveSocketed(tree.getId(), where);
                    if (item != null) {
                        ItemDataModifier data = RPGCore.inst().getItemManager().getItemData(item, ItemDataModifier.class);
                        if (data != null) {
                            if (effect instanceof CorePassiveSocketEntityAttribute) {
                                // add attributes to the entity, from socketed item
                                passive_cache_attribute.addAll(data.apply(this));
                            } else if (effect instanceof CorePassiveSocketSkillAttribute) {
                                // add attributes to the skill, from socketed item
                                for (CoreModifier modifier : data.getModifiers()) {
                                    skill_tags.addAll(modifier.getTagEffects());
                                    modifier.getAttributeEffects().forEach((attribute, factor) -> {
                                        skill_attributes.merge(attribute, factor, (a, b) -> a + b);
                                    });
                                }
                            } else if (effect instanceof CorePassiveSocketSkillReference) {
                                // add a reference to the skill, from an itemized skill
                                data.getItem().getHidden("skill").ifPresent(skill_references::add);
                            }
                        }
                    }
                }
            }
        }
        // third pass to handle jobs of the player
        CoreJob job = getJob();
        if (job != null) {
            for (String id : job.getPassiveTree()) {
                CorePassiveTree tree = RPGCore.inst().getPassiveManager().getTreeIndex().get(id);
                Set<Long> allocated = getAllocated(tree.getId());

                for (long where : allocated) {
                    // never process the entry node
                    if (where == 0L) {
                        continue;
                    }
                    // ensure node is not illegal
                    CorePassiveNode node = tree.getNode(where);
                    if (node == null) {
                        continue;
                    }

                    // handle the node on the tree
                    AbstractCorePassive effect = node.getEffect().orElse(null);
                    if (effect instanceof CorePassiveEntityAttribute) {
                        // add attributes to the entity
                        Map<String, Double> attributes = ((CorePassiveEntityAttribute) effect).getAttributes();
                        attributes.forEach((attribute, factor) -> {
                            passive_cache_attribute.add(getAttribute(attribute).create(factor));
                        });
                    } else {
                        ItemStack item = getPassiveSocketed(tree.getId(), where);
                        if (item != null) {
                            ItemDataModifier data = RPGCore.inst().getItemManager().getItemData(item, ItemDataModifier.class);
                            if (data != null) {
                                if (effect instanceof CorePassiveSocketEntityAttribute) {
                                    // add attributes to the entity, from socketed item
                                    passive_cache_attribute.addAll(data.apply(this));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Integrity ensures to not retain passive tree allocation
     * if the tree layout was modified by the server.
     *
     * @return tree mapped to integrity snapshot
     */
    public Map<String, Long> getPassiveIntegrity() {
        return passive_integrity;
    }

    /**
     * Latest "anchor" position of the player.
     *
     * @return tree mapped to encoded position
     */
    public Map<String, Long> getPassiveViewport() {
        return passive_viewport;
    }

    /**
     * Check passive points available on a tree.
     *
     * @return total allocable points per type
     */
    public Map<String, Integer> getPassivePoints() {
        return passive_points;
    }

    /**
     * Check points available for refunds on the tree.
     *
     * @return refund points available per type
     */
    public Map<String, Integer> getPassiveRefunds() {
        return passive_refunds;
    }

    /**
     * Passive tree mapped to the positions.
     *
     * @return the tree type mapped to encoded positions
     */
    public Map<String, Set<Long>> getPassiveAllocated() {
        return passive_allocated;
    }

    /**
     * Grab the passive points allocated, the position 0|0 can always
     * be assumed to be allocated. The positions are encoded X|Y
     *
     * @param tree the tree to check against
     * @return allocated nodes, mutable collection
     */
    public Set<Long> getAllocated(String tree) {
        Set<Long> allocated = passive_allocated.computeIfAbsent(tree, (k -> new HashSet<>()));
        allocated.add(0L);
        return allocated;
    }

    /**
     * Items that were "equipped" to the passive menu.
     *
     * @return tree type to position that holds an item
     */
    public Map<String, Map<Long, ItemStack>> getPassiveSocketed() {
        return passive_socketed;
    }

    /**
     * Items socketed to the passive tree are pseudo equipment, or
     * some special context driven things. Only RPGCore items should
     * be able to be socketed.
     *
     * @param tree     which tree holds the items
     * @param position encoded position in the tree
     * @param item     the item socketed, or null to remove
     */
    public void setPassiveSocketed(String tree, long position, ItemStack item) {
        Map<Long, ItemStack> socketed = this.passive_socketed.computeIfAbsent(tree, (k -> new HashMap<>()));
        if (item == null) {
            socketed.remove(position);
        } else {
            socketed.put(position, item);
        }
    }

    /**
     * Items socketed to the passive tree are pseudo equipment, or
     * some special context driven things. Only RPGCore items should
     * be able to be socketed.
     *
     * @param tree     which tree holds the items
     * @param position encoded position in the tree
     * @return the item that is socketed, or null.
     */
    public ItemStack getPassiveSocketed(String tree, long position) {
        Map<Long, ItemStack> socketed = this.passive_socketed.computeIfAbsent(tree, (k -> new HashMap<>()));
        return socketed.get(position);
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
            Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                Map<String, Integer> quantified = new HashMap<>();
                for (ItemStack item : this.getEntity().getInventory().getContents()) {
                    ItemDataGeneric data = RPGCore.inst().getItemManager().getItemData(item, ItemDataGeneric.class);
                    if (data != null) {
                        quantified.merge(data.getItem().getId(), item.getAmount(), (a, b) -> a + b);
                    }
                }
                this.quest_items_snapshot = quantified;
                this.quest_items_timestamp = System.currentTimeMillis();
            });
        }

        Map<String, Integer> snapshot = this.quest_items_snapshot;
        return snapshot == null ? new HashMap<>() : snapshot;
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
     * A job provides certain benefits, while also granting access
     * to a set of passive-tree options.
     *
     * @return the job of this player.
     */
    public CoreJob getJob() {
        if (this.job == null || this.job.isEmpty() || "nothing".equalsIgnoreCase(this.job))
            return null;
        return RPGCore.inst().getJobManager().getIndexJob().get(this.job);
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
     * The location intended for logging in, this method will not matter once
     * the player instance was teleported to the login position.
     *
     * @return where to login the player.
     */
    public Location getLoginPosition() {
        return login_position;
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
     * Generate a float-text notification on the interface.
     *
     * @param color   the color of the message
     * @param message the contents of the message
     */
    public void notify(ChatColor color, String message) {
        RPGCore.inst().getHUDManager().notify(getEntity(), message, color.asBungee());
    }

    /**
     * Generate a float-text notification on the interface.
     *
     * @param message the contents of the message
     */
    public void notify(String message) {
        RPGCore.inst().getHUDManager().notify(getEntity(), message);
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
    public void updateSkills() {
        // grab behaviours from skillbar
        Set<CoreBehaviour> acquired = new HashSet<>();
        for (int i = 0; i < 6; i++) {
            CoreSkill skill = skillbar.getSkill(i);
            if (skill != null) {
                acquired.addAll(skill.getBehaviours());
            }
        }
        // search for all passive skills accessible
        for (CoreSkill skill : RPGCore.inst().getSkillManager().getIndex().getAll()) {
            if (skill.hasPassiveAccess(this)) {
                acquired.addAll(skill.getBehaviours());
            }
        }
        // drop behaviours no longer active
        this.passive_behaviour.entrySet().removeIf(entry -> {
            if (!acquired.contains(entry.getKey())) {
                entry.getValue().setAbandoned();
                return true;
            } else {
                return false;
            }
        });
        // add behaviours newly acquired
        for (CoreBehaviour behaviour : acquired) {
            if (!this.passive_behaviour.containsKey(behaviour)) {
                SkillContext context = createSkillContext(behaviour.getSkill());
                BehaviourEffect effect = behaviour.createEffect(context);
                this.passive_behaviour.put(behaviour, effect);
                addEffect("passive_skill_" + UUID.randomUUID().toString(), effect);
            }
        }
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

    @Override
    public SkillContext createSkillContext(CoreSkill skill) {
        SkillContext context = super.createSkillContext(skill);
        // grab attributes from passive tree
        Map<String, Double> attributes = passive_cache_skill_attribute.get(skill.getId());
        if (attributes != null) {
            attributes.forEach((attribute, factor) -> {
                context.getAttributeLocal(attribute).create(factor);
            });
        }
        // grab tags from the tree
        Set<String> tags = passive_cache_skill_tag.get(skill.getId());
        if (tags != null) {
            for (String tag : tags) {
                context.addTag(tag);
            }
        }
        // grab skill references
        List<String> referenced = passive_cache_skill_reference.get(skill.getId());
        if (referenced != null) {
            context.getLinkedSkills().addAll(referenced);
        }

        return context;
    }
}
