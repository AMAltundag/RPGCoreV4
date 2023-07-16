package me.blutkrone.rpgcore.npc;

import me.blutkrone.external.juliarn.npc.AbstractPlayerNPC;
import me.blutkrone.external.juliarn.npc.NPCPool;
import me.blutkrone.external.juliarn.npc.modifier.EquipmentModifier;
import me.blutkrone.external.juliarn.npc.modifier.MetadataModifier;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.nms.api.packet.wrapper.VolatileSkin;
import me.blutkrone.rpgcore.npc.trait.impl.CoreQuestTrait;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.resourcepack.utils.IndexedTexture;
import me.blutkrone.rpgcore.util.Utility;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Due to the complexity, an NPC by itself will not ensure integrity
 * of the type. Whatever external source is providing the NPC should
 * be ensuring that the template is up-to-date.
 */
public class ActiveCoreNPC extends AbstractPlayerNPC {

    // the node we are associated with
    private UUID node;
    // the template the NPC is based of
    private CoreNPC template;

    /**
     * Creates a new npc instance.
     *
     * @param pool     The pool we are initialized by.
     * @param location The location of the npc.
     */
    public ActiveCoreNPC(CoreNPC template, NPCPool pool, Location location, UUID node) {
        super(pool, location, template.profile());
        this.node = node;
        this.template = template;
    }

    /**
     * Creates a new npc instance.
     *
     * @param pool     The pool we are initialized by.
     * @param location The location of the npc.
     */
    public ActiveCoreNPC(CoreNPC template, NPCPool pool, Location location) {
        this(template, pool, location, null);
    }

    /**
     * The UUID of the node which created the NPC, null if we are not
     * associated with a node.
     *
     * @return node we were instanced by, may be null.
     */
    public UUID node() {
        return this.node;
    }

    /**
     * The template which defines how the NPC is to behave.
     *
     * @return template handling logic
     */
    public CoreNPC template() {
        return this.template;
    }

    @Override
    protected boolean canSee(Player player) {
        // must have logged on to load NPC
        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
        if (core_player == null) {
            return false;
        }
        // check for quests
        for (String id : template().getQuestBlacklist()) {
            if (core_player.getCompletedQuests().contains(id)) {
                return false;
            }
        }
        for (String id : template().getQuestWhitelist()) {
            if (!core_player.getCompletedQuests().contains(id)) {
                return false;
            }
        }
        // check for tags
        for (String id : template().getTagBlacklist()) {
            if (core_player.getPersistentTags().contains(id)) {
                return false;
            }
        }
        for (String id : template().getTagWhitelist()) {
            if (!core_player.getPersistentTags().contains(id)) {
                return false;
            }
        }
        // npc can be shown
        return true;
    }

    @Override
    public void prepare(Player player) {
        // make sure to show all skin components
        MetadataModifier metadata = this.metadata();
        metadata.skin(VolatileSkin.values());
        metadata.flush(player);
        // deploy equipment packets
        EquipmentModifier equipment = this.equipment();
        if (this.template.getHelmet() != null) {
            equipment.queue(EquipmentSlot.HEAD, this.template.getHelmet());
        }
        if (this.template.getBoots() != null) {
            equipment.queue(EquipmentSlot.FEET, this.template.getBoots());
        }
        if (this.template.getChestplate() != null) {
            equipment.queue(EquipmentSlot.CHEST, this.template.getChestplate());
        }
        if (this.template.getPants() != null) {
            equipment.queue(EquipmentSlot.LEGS, this.template.getPants());
        }
        if (this.template.getMainhand() != null) {
            equipment.queue(EquipmentSlot.HAND, this.template.getMainhand());
        }
        if (this.template.getOffhand() != null) {
            equipment.queue(EquipmentSlot.OFF_HAND, this.template.getOffhand());
        }
        equipment.flush();
        // create a hologram for the NPC name
        hologram().spawn(player, location());
        hologram().mount(player, this.id());
        hologram().message(player, describe(player), true, false);
    }

    @Override
    public void tick(Player player) {
        // make NPC stare at the player
        if (this.template().isStaring()) {
            this.rotation().target(player.getEyeLocation()).flush(player);
        }
        // update name shown via hologram
        if (Math.random() <= 0.25d) {
            hologram().message(player, describe(player), true, false);
        }
    }

    @Override
    public void interact(Player player) {
        this.template.interact(player, true);
    }

    /*
     * Create a nameplate that matches the NPC.
     *
     * @return nameplate description
     */
    private BaseComponent[] describe(Player player) {
        CoreNPC template = this.template();
        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(player);
        if (core_player == null) {
            return new BaseComponent[0];
        }

        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        MagicStringBuilder msb = new MagicStringBuilder();

        // retrieve the description for the mob
        List<String> contents = RPGCore.inst().getLanguageManager().getTranslationList(template.getNameLC());
        // if we got a header, draw that as the background
        if (rpm.textures().containsKey(contents.get(0))) {
            String header = contents.remove(0);
            IndexedTexture background_texture = RPGCore.inst().getResourcePackManager().texture(header);
            msb.shiftCentered(0, background_texture.width).append(background_texture);
        }
        // draw remaining lines backwards
        Collections.reverse(contents);
        for (int i = 0; i < contents.size() && i < 24; i++) {
            msb.shiftCentered(0, Utility.measure(contents.get(i)));
            msb.append(contents.get(i), "nameplate_" + i);
        }

        // show info symbols for quest offers
        IndexedTexture texture = null;
        if (template.getQuestRewardOffer(core_player) != null) {
            texture = rpm.texture("static_reward_quest_icon");
        } else if (template.getQuestDialogueOffer(core_player) != null) {
            texture = rpm.texture("static_dialogue_quest_icon");
        } else if (template.getQuestDeliverOffer(core_player, false) != null) {
            texture = rpm.texture("static_delivery_quest_icon");
        } else {
            CoreQuestTrait trait = template.getTrait(CoreQuestTrait.class);
            if (trait != null) {
                List<CoreQuest> available = trait.getQuestAvailable(core_player);
                if (!available.isEmpty()) {
                    texture = rpm.texture(available.get(0).getSymbol());
                }
            }
        }

        // make sure we generated a texture
        if (texture != null) {
            msb.shiftCentered(0, texture.width);
            msb.append(texture);
        }

        return msb.shiftToExact(-2).compile();
    }
}
