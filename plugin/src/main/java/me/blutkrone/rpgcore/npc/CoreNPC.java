package me.blutkrone.rpgcore.npc;

import me.blutkrone.external.juliarn.npc.NPC;
import me.blutkrone.external.juliarn.npc.NPCPool;
import me.blutkrone.external.juliarn.npc.modifier.MetadataModifier;
import me.blutkrone.external.juliarn.npc.profile.Profile;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.npc.AbstractEditorNPCTrait;
import me.blutkrone.rpgcore.hud.editor.root.npc.EditorNPC;
import me.blutkrone.rpgcore.menu.CortexMenu;
import me.blutkrone.rpgcore.node.struct.AbstractNode;
import me.blutkrone.rpgcore.node.struct.NodeActive;
import me.blutkrone.rpgcore.node.struct.NodeData;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import me.blutkrone.rpgcore.npc.trait.impl.CoreQuestTrait;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.quest.task.impl.CoreQuestTaskDeliver;
import me.blutkrone.rpgcore.quest.task.impl.CoreQuestTaskTalk;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.resourcepack.utils.IndexedTexture;
import me.blutkrone.rpgcore.skin.CoreSkin;
import me.blutkrone.rpgcore.util.Utility;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Entity meant for miscellaneous purposes.
 *
 * @see AbstractNode we implement a node to bypass the need for a separate spawner implementation
 */
public class CoreNPC extends AbstractNode {

    // metadata wrapper for making name of NPC visible
    private static MetadataModifier.EntityMetadata<Boolean, Boolean> ENTITY_CUSTOM_NAME_VISIBLE =
            new MetadataModifier.EntityMetadata<>(
                    3,
                    Boolean.class,
                    Collections.emptyList(),
                    flag -> flag
            );

    // language ID for NPC name
    private String lc_name;
    // makes the npc look at a player
    private boolean staring;
    // the string to use for the NPC
    private String skin = null;
    // traits available for the NPC
    private List<AbstractCoreTrait> traits;
    // must have completed all quests
    private List<String> quest_whitelist;
    // cannot have any of these quests completed
    private List<String> quest_blacklist;
    // items to equip on NPC
    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack pants;
    private ItemStack boots;
    private ItemStack mainhand;
    private ItemStack offhand;

    public CoreNPC(String id, EditorNPC editor) {
        super(id, 32);

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
            // initial creation of the NPC
            data = new NodeDataSpawnerNPC();
            data.active = RPGCore.inst().getNPCManager().create(this.getId(), where, active);
            active.setData(data);
        } else {
            // replace NPC if their template changed
            CoreNPC old_design = RPGCore.inst().getNPCManager().getDesign(data.active);
            if (old_design != this) {
                RPGCore.inst().getNPCManager().remove(data.active);
                data.active = RPGCore.inst().getNPCManager().create(this.getId(), where, active);
            }
        }
    }

    @Override
    public void right(World world, NodeActive active, Player player) {
        // interaction is handled separately.
    }

    @Override
    public void left(World world, NodeActive active, Player player) {
        // interaction is handled separately.
    }

    /**
     * Language ID for the name.
     *
     * @return the name identifier.
     */
    public String getNameLC() {
        return lc_name;
    }

    /**
     * Create a nameplate description for an NPC instance.
     *
     * @return nameplate description
     */
    public BaseComponent[] describe(Player player) {
        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
        if (core_player == null) {
            return new BaseComponent[0];
        }

        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        MagicStringBuilder msb = new MagicStringBuilder();

        // retrieve the description for the mob
        List<String> contents = RPGCore.inst().getLanguageManager().getTranslationList(this.getNameLC());
        // if we got a header, draw that as the background
        if (rpm.textures().containsKey(contents.get(0))) {
            String header = contents.remove(0);
            IndexedTexture background_texture = RPGCore.inst().getResourcePackManager().texture(header);
            msb.shiftCentered(0, background_texture.width).append(background_texture);
        }
        // draw remaining lines backwards
        Collections.reverse(contents);
        for (int i = 0; i < contents.size() && i < 24; i++) {
            msb.shiftCentered(0, Utility.measureWidthExact(contents.get(i)));
            msb.append(contents.get(i), "nameplate_" + i);
        }
        // special symbols for quests
        List<AbstractCoreTrait> traits = getSymbolTraits(core_player);
        for (AbstractCoreTrait trait : traits) {
            if (trait instanceof CoreQuestTrait) {
                // identify a quest symbol to work with
                IndexedTexture texture = null;
                if (((CoreQuestTrait) trait).getQuestUnclaimed(player, this) != null) {
                    texture = rpm.texture("static_reward_quest_icon");
                } else if (((CoreQuestTrait) trait).getQuestDialogue(player, this) != null) {
                    texture = rpm.texture("static_dialogue_quest_icon");
                } else if (((CoreQuestTrait) trait).getQuestDelivery(player, this) != null) {
                    texture = rpm.texture("static_delivery_quest_icon");
                } else {
                    List<CoreQuest> available = ((CoreQuestTrait) trait).getQuestAvailable(core_player);
                    if (!available.isEmpty()) {
                        texture = rpm.texture(available.get(0).getSymbol());
                    }
                }

                // make sure we generated a texture
                if (texture != null) {
                    msb.shiftCentered(0, texture.width);
                    msb.append(texture);
                }
            }
        }

        return msb.shiftToExact(-2).compile();
    }

    /**
     * The cortex allows to organize traits of an NPC into a single
     * menu.
     *
     * @param player    who wants to view the cortex
     * @param shortcuts jump to prioritized traits
     */
    public void showCortex(Player player, boolean shortcuts) {
        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
        if (core_player == null) {
            return;
        }
        List<AbstractCoreTrait> traits = getCortexTraits(core_player);

        // interaction requires traits
        if (traits.isEmpty()) {
            return;
        }

        // if we got a quest priority, flag it as such
        if (shortcuts) {
            for (AbstractCoreTrait trait : traits) {
                if ((trait instanceof CoreQuestTrait)) {
                    CoreQuestTrait qt = (CoreQuestTrait) trait;

                    // if we got a reward, we present that one
                    CoreQuest reward = qt.getQuestUnclaimed(player, this);
                    if (reward != null) {
                        RPGCore.inst().getHUDManager().getQuestMenu().reward(reward, player, this);
                        return;
                    }

                    // if we got dialogue, we present that one
                    CoreQuestTaskTalk.QuestDialogue dialogue = qt.getQuestDialogue(player, this);
                    if (dialogue != null) {
                        RPGCore.inst().getHUDManager().getDialogueMenu().open(dialogue.dialogue, player, dialogue.task.getUniqueId() + "_" + dialogue.npc);
                        return;
                    }

                    // if we got a delivery, we present that one
                    CoreQuestTaskDeliver delivery = qt.getQuestDelivery(player, this);
                    if (delivery != null) {
                        RPGCore.inst().getHUDManager().getQuestMenu().delivery(delivery, player, this);
                        return;
                    }
                }
            }
        }

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

    /*
     * Construct an instance of this NPC at the given
     * location.
     *
     * @param pool     which pool to construct thorough
     * @param location where to spawn the NPC
     * @return the NPC that was created
     */
    NPC create(NPCPool pool, Location location) {
        // setup a profile including a skin
        Profile profile = new Profile(UUID.randomUUID());
        profile.setName("rpgcore" + ThreadLocalRandom.current().nextInt(1, 99999));

        if (this.skin != null) {
            CoreSkin fetched = RPGCore.inst().getSkinPool().get(this.skin);
            if (fetched != null) {
                profile.setProperty(new Profile.Property("textures", fetched.value, fetched.signature));
            } else {
                Bukkit.getLogger().severe("NPC was requested before skin has finished!");
            }
        }

        profile.complete();
        // configure the basic NPC settings
        NPC.Builder builder = NPC.builder();
        builder.profile(profile);
        builder.lookAtPlayer(this.staring);
        builder.location(location);
        // apply visibility filtering
        builder.visible((player -> {
            // must have logged on to load NPC
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
            if (core_player == null) {
                return false;
            }
            // cannot have completed any blacklist quests
            for (String id : this.quest_blacklist) {
                if (core_player.getCompletedQuests().contains(id)) {
                    return false;
                }
            }
            // must have completed all whitelist quests
            for (String id : this.quest_whitelist) {
                if (!core_player.getCompletedQuests().contains(id)) {
                    return false;
                }
            }
            // npc can be shown
            return true;
        }));
        // spawn preparation for the NPC
        builder.spawnCustomizer((npc, player) -> {
            // hide name of the NPC
            MetadataModifier metadata = npc.metadata();
            metadata.queue(ENTITY_CUSTOM_NAME_VISIBLE, true);
            metadata.queue(MetadataModifier.EntityMetadata.SKIN_LAYERS, true);
            metadata.send(player);
        });
        return builder.build(pool);
    }

    /*
     * Fetch the first six traits which can be shown on
     * a cortex menu.
     *
     * @return up to six cortex-able traits
     */
    private List<AbstractCoreTrait> getCortexTraits(CorePlayer core_player) {
        List<AbstractCoreTrait> traits = new ArrayList<>(this.traits);
        traits.removeIf((trait -> !trait.isAvailable(core_player)));

        return traits;
    }

    /*
     * Fetch the first six traits which can be shown on
     * a cortex menu.
     *
     * @return up to six cortex-able traits
     */
    private List<AbstractCoreTrait> getSymbolTraits(CorePlayer core_player) {
        List<AbstractCoreTrait> traits = new ArrayList<>(this.traits);
        traits.removeIf((trait -> !trait.isAvailable(core_player)));
        traits.removeIf(trait -> trait.getSymbol().equalsIgnoreCase("default"));
        return traits;
    }

    /*
     * A wrapper to help us keep track of NPC spawns.
     */
    private class NodeDataSpawnerNPC extends NodeData {

        // active instance of the npc
        private NPC active;

        NodeDataSpawnerNPC() {
        }

        @Override
        public void highlight(int time) {

        }

        @Override
        public void abandon() {
            RPGCore.inst().getNPCManager().remove(this.active);
        }
    }
}
