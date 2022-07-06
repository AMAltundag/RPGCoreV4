package me.blutkrone.rpgcore.npc;

import com.github.juliarn.npc.NPC;
import com.github.juliarn.npc.NPCPool;
import com.github.juliarn.npc.profile.Profile;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.root.EditorNPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.mineskin.MineskinClient;
import org.mineskin.data.Skin;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Entity meant for miscellaneous purposes
 */
public class CoreNPC {
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
        if (editor.skin.equalsIgnoreCase("NOTHINGNESS")) {
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
        profile.setName(RPGCore.inst().getLanguageManager().getTranslation(this.lc_name));
        if (this.skin_value == null || this.skin_signed == null) {
            Bukkit.getLogger().severe("NPC was requested before skin has finished!");
        } else {
            profile.setProperty(new Profile.Property("textures", this.skin_value, this.skin_signed));
        }
        // configure the basic NPC settings
        NPC.Builder builder = NPC.builder();
        builder.lookAtPlayer(staring);
        builder.location(location);
        // handle npc customization
        builder.spawnCustomizer((npc, player) -> {

        });
        return builder.build(pool);
    }
}
