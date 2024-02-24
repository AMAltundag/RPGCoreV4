package me.blutkrone.rpgcore.npc;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.editor.bundle.npc.AbstractEditorNPCTrait;
import me.blutkrone.rpgcore.editor.root.npc.EditorNPC;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.menu.CortexMenu;
import me.blutkrone.rpgcore.nms.api.packet.wrapper.VolatileGameProfile;
import me.blutkrone.rpgcore.node.struct.AbstractNode;
import me.blutkrone.rpgcore.node.struct.NodeActive;
import me.blutkrone.rpgcore.node.struct.NodeData;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import me.blutkrone.rpgcore.npc.trait.impl.CoreQuestTrait;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.quest.task.AbstractQuestTask;
import me.blutkrone.rpgcore.quest.task.impl.CoreQuestTaskDeliver;
import me.blutkrone.rpgcore.quest.task.impl.CoreQuestTaskTalk;
import me.blutkrone.rpgcore.skin.CoreSkin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entity meant for miscellaneous purposes.
 *
 * @see AbstractNode we implement a node to bypass the need for a separate spawner implementation
 */
public class CoreNPC extends AbstractNode {

    // language ID for NPC name
    private String lc_name;
    private boolean staring;
    // the string to use for the NPC
    private String skin = null;
    // traits available for the NPC
    private List<AbstractCoreTrait> traits;
    // must have completed all quests
    private List<String> quest_whitelist;
    // cannot have any of these quests completed
    private List<String> quest_blacklist;
    // must have all tags
    private List<String> tag_whitelist;
    // cannot have any of these tags
    private List<String> tag_blacklist;
    // items to equip on NPC
    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack pants;
    private ItemStack boots;
    private ItemStack mainhand;
    private ItemStack offhand;

    public CoreNPC(String id, EditorNPC editor) {
        super(id, 32, editor.getPreview());

        if (!editor.skin.equalsIgnoreCase("NOTHINGNESS")) {
            this.skin = editor.skin;
            RPGCore.inst().getSkinPool().query(editor.skin);
        }

        this.lc_name = editor.lc_name;
        this.staring = editor.staring;
        this.traits = new ArrayList<>();
        for (IEditorBundle trait : editor.traits) {
            this.traits.add(((AbstractEditorNPCTrait) trait).build());
        }
        this.quest_whitelist = new ArrayList<>(editor.quest_required);
        this.quest_blacklist = new ArrayList<>(editor.quest_forbidden);
        this.tag_whitelist = new ArrayList<>(editor.tag_required);
        this.tag_blacklist = new ArrayList<>(editor.tag_forbidden);

        if (!editor.item_helmet.equalsIgnoreCase("nothingness")) {
            this.helmet = RPGCore.inst().getItemManager().getItemIndex().get(editor.item_helmet).getTemplate();
        }
        if (!editor.item_chestplate.equalsIgnoreCase("nothingness")) {
            this.chestplate = RPGCore.inst().getItemManager().getItemIndex().get(editor.item_chestplate).getTemplate();
        }
        if (!editor.item_boots.equalsIgnoreCase("nothingness")) {
            this.boots = RPGCore.inst().getItemManager().getItemIndex().get(editor.item_boots).getTemplate();
        }
        if (!editor.item_mainhand.equalsIgnoreCase("nothingness")) {
            this.mainhand = RPGCore.inst().getItemManager().getItemIndex().get(editor.item_mainhand).getTemplate();
        }
        if (!editor.item_offhand.equalsIgnoreCase("nothingness")) {
            this.offhand = RPGCore.inst().getItemManager().getItemIndex().get(editor.item_offhand).getTemplate();
        }
        if (!editor.item_pants.equalsIgnoreCase("nothingness")) {
            this.pants = RPGCore.inst().getItemManager().getItemIndex().get(editor.item_pants).getTemplate();
        }
    }

    /**
     * Quests that cannot be completed to be visible
     *
     * @return blacklisted quests
     */
    public List<String> getQuestBlacklist() {
        return quest_blacklist;
    }

    /**
     * Quests that must be completed to be visible
     *
     * @return whitelisted quests
     */
    public List<String> getQuestWhitelist() {
        return quest_whitelist;
    }

    /**
     * Tags forbidden to see NPC
     *
     * @return blacklisted tags
     */
    public List<String> getTagBlacklist() {
        return tag_blacklist;
    }

    /**
     * Tags required to see NPC
     *
     * @return whitelisted tags
     */
    public List<String> getTagWhitelist() {
        return tag_whitelist;
    }

    /**
     * Will rotate to always look at player
     *
     * @return stare at player
     */
    public boolean isStaring() {
        return staring;
    }

    /**
     * Language code for the name of the NPC
     *
     * @return language code for name
     */
    public String getNameLC() {
        return lc_name;
    }

    /**
     * Which traits the NPC has
     *
     * @return traits of the NPC
     */
    public List<AbstractCoreTrait> getTraits() {
        return traits;
    }

    /**
     * Search for a trait exactly of the given class.
     *
     * @param clazz the trait class to search
     * @return the trait that was found.
     */
    public <K> K getTrait(Class<K> clazz) {
        for (AbstractCoreTrait trait : this.traits) {
            if (trait.getClass() == clazz) {
                return (K) trait;
            }
        }

        return null;
    }

    /**
     * Skin of the NPC
     *
     * @return the skin of the NPC
     */
    public String getSkin() {
        return skin;
    }

    /**
     * Supply a new profile, which should belong to a distinct
     * copy of this NPC template.
     *
     * @return a new profile for a distinct NPC instance.
     */
    public VolatileGameProfile profile() {
        // setup a profile including a skin
        VolatileGameProfile profile = new VolatileGameProfile();
        // initialize the skin for the profile
        if (this.skin != null) {
            CoreSkin fetched = RPGCore.inst().getSkinPool().get(this.skin);
            if (fetched != null) {
                profile.addProperty("textures", fetched.value, fetched.signature);
            } else {
                RPGCore.inst().getLogger().warning("NPC was requested before skin has finished!");
            }
        }
        // offer up the profile we built
        return profile;
    }

    /**
     * Search for any reward offer pending for the player.
     *
     * @param player whose quests to check against
     * @return the offer if available
     */
    public CoreQuest getQuestRewardOffer(CorePlayer player) {
        Set<String> quests_offered_by_npc = new HashSet<>();
        CoreQuestTrait trait = getTrait(CoreQuestTrait.class);
        if (trait != null) {
            quests_offered_by_npc.addAll(trait.getQuests());
        }

        // search for any quest that has no more tasks (finished)
        for (String id : player.getActiveQuestIds()) {
            CoreQuest quest = RPGCore.inst().getQuestManager().getIndexQuest().get(id);
            String npc = quest.getRewardNPC();
            if (npc == null) {
                // if quest has no reward npc, quest giver offers reward
                if (!quests_offered_by_npc.contains(id)) {
                    continue;
                }
            } else if (!npc.equalsIgnoreCase(this.getId())) {
                // if reward npc is set, we check for that NPC
                continue;
            }

            // if no tasks are left, we can claim a reward
            if (quest.getCurrentTask(player) == null) {
                return quest;
            }
        }

        // this NPC has no quests to claim
        return null;
    }

    /**
     * Search for any quest dialogue pending for the player.
     *
     * @param player whose quests to check against
     * @return the offer if available
     */
    public CoreQuestTaskTalk.QuestDialogue getQuestDialogueOffer(CorePlayer player) {
        // search for any quest task with open dialogue
        for (String id : player.getActiveQuestIds()) {
            CoreQuest quest = RPGCore.inst().getQuestManager().getIndexQuest().get(id);
            AbstractQuestTask task = quest.getCurrentTask(player);
            if (task instanceof CoreQuestTaskTalk) {
                CoreQuestTaskTalk.QuestDialogue dialogue = ((CoreQuestTaskTalk) task).getWaiting(player, this);
                if (dialogue != null) {
                    return dialogue;
                }
            }
        }
        // no dialogue is pending.
        return null;
    }

    /**
     * Search for any delivery task pending for the player
     *
     * @param player whose quests to check against
     * @return the offer if available
     */
    public CoreQuestTaskDeliver getQuestDeliverOffer(CorePlayer player, boolean accurate) {
        // search for any dialogue which is pending
        for (String id : player.getActiveQuestIds()) {
            CoreQuest quest = RPGCore.inst().getQuestManager().getIndexQuest().get(id);
            // ensure we got a delivery task
            AbstractQuestTask task = quest.getCurrentTask(player);
            if (!(task instanceof CoreQuestTaskDeliver)) {
                continue;
            }
            // ensure the NPC accepts drop-offs for that task
            CoreQuestTaskDeliver delivery = (CoreQuestTaskDeliver) task;
            if (delivery.isDropOff(this) && delivery.canMeetDemand(player, accurate)) {
                return delivery;
            }
        }
        // no delivery job is pending.
        return null;
    }

    /**
     * Check what traits are available to the player.
     *
     * @param player Whose traits to list up
     * @return Available traits
     */
    public List<AbstractCoreTrait> getAvailableTraits(Player player) {
        // ensure player is logged-in
        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
        if (core_player == null) {
            return new ArrayList<>();
        }
        // find the traits we can interact with
        List<AbstractCoreTrait> traits = new ArrayList<>(this.traits);
        traits.removeIf((trait -> !trait.isAvailable(core_player)));
        return traits;
    }

    /**
     * The cortex allows to organize traits of an NPC into a single
     * menu.
     *
     * @param player   who wants to view the cortex
     * @param shortcut bypass cortex selection if we have a priority
     */
    public void interact(Player player, boolean shortcut) {
        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
        if (core_player == null) {
            return;
        }

        // handle shortcuts if appropriate
        if (shortcut) {
            // if we got a reward, we present that one
            CoreQuest reward = getQuestRewardOffer(core_player);
            if (reward != null) {
                RPGCore.inst().getHUDManager().getQuestMenu().reward(reward, player, this);
                return;
            }

            // if we got dialogue, we present that one
            CoreQuestTaskTalk.QuestDialogue dialogue = getQuestDialogueOffer(core_player);
            if (dialogue != null) {
                RPGCore.inst().getHUDManager().getDialogueMenu().open(dialogue.dialogue, player, this, dialogue.task);
                return;
            }

            // if we got a delivery, we present that one
            CoreQuestTaskDeliver delivery = getQuestDeliverOffer(core_player, true);
            if (delivery != null) {
                RPGCore.inst().getHUDManager().getQuestMenu().delivery(delivery, player, this);
                return;
            }
        }

        // find the traits we can interact with
        List<AbstractCoreTrait> traits = new ArrayList<>(this.traits);
        traits.removeIf((trait -> !trait.isAvailable(core_player)));

        // present the cortex menu itself
        if (traits.size() == 1) {
            traits.get(0).engage(player, this);
        } else if (traits.size() == 2) {
            new CortexMenu.Cortex2(traits, this).finish(player);
        } else if (traits.size() == 3) {
            new CortexMenu.Cortex3(traits, this).finish(player);
        } else if (traits.size() == 4) {
            new CortexMenu.Cortex4(traits, this).finish(player);
        } else if (traits.size() == 5) {
            new CortexMenu.Cortex5(traits, this).finish(player);
        } else if (traits.size() >= 6) {
            new CortexMenu.Cortex6(traits, this).finish(player);
        }
    }

    /**
     * Item model to show on the NPC
     *
     * @return Item model, or null
     */
    public ItemStack getHelmet() {
        return helmet;
    }

    /**
     * Item model to show on the NPC
     *
     * @return Item model, or null
     */
    public ItemStack getChestplate() {
        return chestplate;
    }

    /**
     * Item model to show on the NPC
     *
     * @return Item model, or null
     */
    public ItemStack getPants() {
        return pants;
    }

    /**
     * Item model to show on the NPC
     *
     * @return Item model, or null
     */
    public ItemStack getBoots() {
        return boots;
    }

    /**
     * Item model to show on the NPC
     *
     * @return Item model, or null
     */
    public ItemStack getMainhand() {
        return mainhand;
    }

    /**
     * Item model to show on the NPC
     *
     * @return Item model, or null
     */
    public ItemStack getOffhand() {
        return offhand;
    }

    @Override
    public void tick(World world, NodeActive active, List<Player> players) {
        Location where = new Location(world, active.getX() + 0.5d, active.getY(), active.getZ() + 0.5d);

        // do not update if no players are nearby
        if (players.isEmpty()) {
            return;
        }

        // create data if missing
        NodeDataSpawnerNPC data = (NodeDataSpawnerNPC) active.getData();
        if (data == null) {
            data = new NodeDataSpawnerNPC();
            data.active = RPGCore.inst().getNPCManager().create(this.getId(), where, active);
            active.setData(data);
        }

        // handle re-sync in case NPC template changed
        if (data.active.template() != this) {
            data.abandon();
            active.setData(null);
        }
    }

    @Override
    public void right(World world, NodeActive active, Player player) {
    }

    @Override
    public void left(World world, NodeActive active, Player player) {
    }

    private class NodeDataSpawnerNPC extends NodeData {

        // active instance of the npc
        private ActiveCoreNPC active;

        NodeDataSpawnerNPC() {
        }


        @Override
        public void abandon() {
            RPGCore.inst().getNPCManager().remove(this.active);
        }
    }
}
