package me.blutkrone.rpgcore.npc;

import com.github.juliarn.npc.NPC;
import com.github.juliarn.npc.NPCPool;
import com.github.juliarn.npc.event.PlayerNPCInteractEvent;
import com.github.juliarn.npc.modifier.MetadataModifier;
import com.github.juliarn.npc.profile.Profile;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.root.EditorNPC;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.resourcepack.utils.IndexedTexture;
import me.blutkrone.rpgcore.util.Utility;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.mineskin.MineskinClient;
import org.mineskin.data.Skin;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Entity meant for miscellaneous purposes
 */
public class CoreNPC {

    // metadata wrapper for making name of NPC visible
    private static MetadataModifier.EntityMetadata<Boolean, Boolean> ENTITY_CUSTOM_NAME_VISIBLE =
            new MetadataModifier.EntityMetadata<>(
                    3,
                    Boolean.class,
                    Collections.emptyList(),
                    flag -> flag
            );

    private String id;

    private String lc_name;
    private String skin_value;
    private String skin_signed;

    private boolean staring;

    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack pants;
    private ItemStack boots;
    private ItemStack mainhand;
    private ItemStack offhand;

    public CoreNPC(String id, EditorNPC editor) {
        this.id = id;

        if (!editor.skin.equalsIgnoreCase("NOTHINGNESS")) {
            try {
                MineskinClient skin_client = new MineskinClient("RPGCore/4");
                CompletableFuture<Skin> generated = skin_client.generateUrl(editor.skin);
                generated.thenAccept((skin -> {
                    this.skin_value = skin.data.texture.value;
                    this.skin_signed = skin.data.texture.signature;
                }));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        this.lc_name = editor.lc_name;
        this.staring = editor.staring;
    }

    /**
     * Identifier of the npc design template.
     *
     * @return id of this template.
     */
    public String getId() {
        return id;
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
    public BaseComponent[] describe(NPC npc) {
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
        for (int i = 0; i < contents.size() && i < 10; i++) {
            msb.shiftCentered(0, Utility.measureWidthExact(contents.get(i)));
            msb.append(contents.get(i), "nameplate_" + i);
        }

        return msb.shiftToExact(-2).compile();
    }

    /**
     * A callback when an NPC created by this template is
     * interacted with.
     *
     * @param e the interaction
     */
    public void interact(PlayerNPCInteractEvent e) {
        e.getPlayer().sendMessage("§fInteracted with NPC: " + this.getId());
    }

    /**
     * Construct an instance of this NPC at the given
     * location.
     *
     * @param pool which pool to construct thorough
     * @param location where to spawn the NPC
     * @return the NPC that was created
     */
    NPC create(NPCPool pool, Location location) {
        // setup a profile including a skin
        Profile profile = new Profile(UUID.randomUUID());
        profile.setName("rpgcore" + ThreadLocalRandom.current().nextInt(1, 99999));
        if (this.skin_value == null || this.skin_signed == null) {
            Bukkit.getLogger().severe("NPC was requested before skin has finished!");
        } else {
            profile.setProperty(new Profile.Property("textures", this.skin_value, this.skin_signed));
        }
        profile.complete();
        // configure the basic NPC settings
        NPC.Builder builder = NPC.builder();
        builder.profile(profile);
        builder.imitatePlayer(false);
        builder.lookAtPlayer(this.staring);
        builder.location(location);
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
}
