package me.blutkrone.rpgcore.npc;

import com.github.juliarn.npc.NPC;
import com.github.juliarn.npc.NPCPool;
import com.github.juliarn.npc.event.PlayerNPCInteractEvent;
import com.github.juliarn.npc.modifier.MetadataModifier;
import com.github.juliarn.npc.profile.Profile;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.root.npc.AbstractEditorNPCTrait;
import me.blutkrone.rpgcore.hud.editor.root.npc.EditorNPC;
import me.blutkrone.rpgcore.language.LanguageManager;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.node.struct.AbstractNode;
import me.blutkrone.rpgcore.node.struct.NodeActive;
import me.blutkrone.rpgcore.node.struct.NodeData;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import me.blutkrone.rpgcore.npc.trait.impl.CoreQuestTrait;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.resourcepack.utils.IndexedTexture;
import me.blutkrone.rpgcore.skin.CoreSkin;
import me.blutkrone.rpgcore.util.Utility;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Entity meant for miscellaneous purposes.
 *
 * @see AbstractNode we implement a node, which allows the NPC to be directly
 * assigned to node - otherwise we would require a spawner
 * node to be setup separately.
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

    private String lc_name;
    private boolean staring;
    private String skin = null;

    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack pants;
    private ItemStack boots;
    private ItemStack mainhand;
    private ItemStack offhand;

    private List<AbstractCoreTrait> traits;

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
    }

    /*
     * Fetch the first six traits which can be shown on
     * a cortex menu.
     *
     * @return up to six cortex-able traits
     */
    private List<AbstractCoreTrait> getCortexTraits() {
        // 1 trait is just directly used
        if (this.traits.size() == 1) {
            return new ArrayList<>(this.traits);
        }
        // pool only traits with icons
        List<AbstractCoreTrait> cortexed = new ArrayList<>();
        for (AbstractCoreTrait trait : this.traits) {
            if (!trait.getSymbol().equalsIgnoreCase("default")) {
                cortexed.add(trait);
            }
        }
        // offer up our traits
        return cortexed;
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
        for (int i = 0; i < contents.size() && i < 24; i++) {
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
        // interaction requires traits
        if (this.traits.isEmpty()) {
            return;
        }

        // if we got a quest priority, flag it as such
        for (AbstractCoreTrait trait : this.traits) {
            if ((trait instanceof CoreQuestTrait)) {
                if (((CoreQuestTrait) trait).isAvailable()) {
                    trait.engage(e.getPlayer());
                    return;
                }
            }
        }

        List<AbstractCoreTrait> traits = getCortexTraits();
        if (traits.size() == 0) {
            // 0 traits, do nothing.
            return;
        } else if (traits.size() == 1) {
            // 1 trait opens the menu directly
            traits.get(0).engage(e.getPlayer());
        } else if (traits.size() == 2) {
            // show a 2-piece cortex
            createCortex2(e.getPlayer(), traits);
        } else if (traits.size() == 3) {
            // show a 3-trait cortex
            createCortex3(e.getPlayer(), traits);
        } else if (traits.size() == 4) {
            // show a 4-trait cortex
            createCortex4(e.getPlayer(), traits);
        } else if (traits.size() == 5) {
            // show a 5-trait cortex
            createCortex5(e.getPlayer(), traits);
        } else {
            // show a 6-trait cortex
            createCortex6(e.getPlayer(), traits);
        }
    }

    /**
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
        CoreSkin skin = RPGCore.inst().getSkinPool().get(this.skin);

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

    /*
     * Create a cortex menu for 2 traits.
     *
     * @param player
     */
    private void createCortex2(Player player, List<AbstractCoreTrait> traits) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        LanguageManager lpm = RPGCore.inst().getLanguageManager();

        IChestMenu menu = RPGCore.inst().getVolatileManager().createMenu(6, player);
        menu.setRebuilder(() -> {
            menu.clearItems();

            // build basic background
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.retreat(8);
            msb.append(rpm.texture("menu_cortex_2"), ChatColor.WHITE);

            // extract traits we are working on
            AbstractCoreTrait trait1 = traits.get(0);
            AbstractCoreTrait trait2 = traits.get(1);

            // update menu design
            msb.shiftToExact(0).append(rpm.texture("cortex_large_" + trait1.getSymbol() + "_0", "cortex_large_default_0"), ChatColor.WHITE);
            msb.shiftToExact(0).append(rpm.texture("cortex_large_" + trait2.getSymbol() + "_1", "cortex_large_default_1"), ChatColor.WHITE);

            // place clickable items
            Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26)
                    .forEach(i -> menu.setItemAt(i, trait1.getIcon(0)));
            Arrays.asList(27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53)
                    .forEach(i -> menu.setItemAt(i, trait2.getIcon(1)));

            menu.setTitle(msb.compile());
        });
        menu.setClickHandler(e -> {
            e.setCancelled(true);
            String brand = IChestMenu.getBrand(e.getCurrentItem(), RPGCore.inst(), "cortex-id", null);
            if (brand != null) {
                int parsed = Integer.parseInt(brand);
                menu.stalled(() -> {
                    menu.getViewer().closeInventory();
                    this.traits.get(parsed).engage(menu.getViewer());
                });
            }
        });
        menu.open();
    }

    /*
     * Create a cortex menu for 3 traits.
     *
     * @param player
     */
    private void createCortex3(Player player, List<AbstractCoreTrait> traits) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        LanguageManager lpm = RPGCore.inst().getLanguageManager();

        IChestMenu menu = RPGCore.inst().getVolatileManager().createMenu(6, player);
        menu.setRebuilder(() -> {
            menu.clearItems();

            // build basic background
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.retreat(8);
            msb.append(rpm.texture("menu_cortex_3"), ChatColor.WHITE);

            // extract traits we are working on
            AbstractCoreTrait trait1 = traits.get(0);
            AbstractCoreTrait trait2 = traits.get(1);
            AbstractCoreTrait trait3 = traits.get(2);

            // update menu design
            msb.shiftToExact(0).append(rpm.texture("cortex_large_" + trait1.getSymbol() + "_0", "cortex_large_default_0"), ChatColor.WHITE);
            msb.shiftToExact(0).append(rpm.texture("cortex_medium_" + trait2.getSymbol() + "_1", "cortex_medium_default_1"), ChatColor.WHITE);
            msb.shiftToExact(81).append(rpm.texture("cortex_medium_" + trait3.getSymbol() + "_1", "cortex_medium_default_1"), ChatColor.WHITE);

            // place clickable items
            Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26)
                    .forEach(i -> menu.setItemAt(i, trait1.getIcon(0)));
            Arrays.asList(27, 28, 29, 30, 36, 37, 38, 39, 45, 46, 47, 48)
                    .forEach(i -> menu.setItemAt(i, trait2.getIcon(1)));
            Arrays.asList(32, 33, 34, 35, 41, 42, 43, 44, 50, 51, 52, 53)
                    .forEach(i -> menu.setItemAt(i, trait3.getIcon(2)));

            menu.setTitle(msb.compile());
        });
        menu.setClickHandler(e -> {
            e.setCancelled(true);
            String brand = IChestMenu.getBrand(e.getCurrentItem(), RPGCore.inst(), "cortex-id", null);
            if (brand != null) {
                int parsed = Integer.parseInt(brand);
                menu.stalled(() -> {
                    menu.getViewer().closeInventory();
                    this.traits.get(parsed).engage(menu.getViewer());
                });
            }
        });
        menu.open();
    }

    /*
     * Create a cortex menu for 4 traits.
     *
     * @param player
     */
    private void createCortex4(Player player, List<AbstractCoreTrait> traits) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        LanguageManager lpm = RPGCore.inst().getLanguageManager();

        IChestMenu menu = RPGCore.inst().getVolatileManager().createMenu(6, player);
        menu.setRebuilder(() -> {
            menu.clearItems();

            // build basic background
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.retreat(8);
            msb.append(rpm.texture("menu_cortex_4"), ChatColor.WHITE);

            // extract traits we are working on
            AbstractCoreTrait trait1 = traits.get(0);
            AbstractCoreTrait trait2 = traits.get(1);
            AbstractCoreTrait trait3 = traits.get(2);
            AbstractCoreTrait trait4 = traits.get(3);

            // update menu design
            msb.shiftToExact(0).append(rpm.texture("cortex_medium_" + trait1.getSymbol() + "_0", "cortex_medium_default_ß"), ChatColor.WHITE);
            msb.shiftToExact(81).append(rpm.texture("cortex_medium_" + trait2.getSymbol() + "_0", "cortex_medium_default_0"), ChatColor.WHITE);
            msb.shiftToExact(0).append(rpm.texture("cortex_medium_" + trait3.getSymbol() + "_1", "cortex_medium_default_1"), ChatColor.WHITE);
            msb.shiftToExact(81).append(rpm.texture("cortex_medium_" + trait4.getSymbol() + "_1", "cortex_medium_default_1"), ChatColor.WHITE);

            // place clickable items
            Arrays.asList(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21)
                    .forEach(i -> menu.setItemAt(i, trait1.getIcon(0)));
            Arrays.asList(5, 6, 7, 8, 14, 15, 16, 17, 23, 24, 25, 26)
                    .forEach(i -> menu.setItemAt(i, trait2.getIcon(1)));
            Arrays.asList(27, 28, 29, 30, 36, 37, 38, 39, 45, 46, 47, 48)
                    .forEach(i -> menu.setItemAt(i, trait3.getIcon(2)));
            Arrays.asList(32, 33, 34, 35, 41, 42, 43, 44, 50, 51, 52, 53)
                    .forEach(i -> menu.setItemAt(i, trait4.getIcon(3)));

            menu.setTitle(msb.compile());
        });
        menu.setClickHandler(e -> {
            e.setCancelled(true);
            String brand = IChestMenu.getBrand(e.getCurrentItem(), RPGCore.inst(), "cortex-id", null);
            if (brand != null) {
                int parsed = Integer.parseInt(brand);
                menu.stalled(() -> {
                    menu.getViewer().closeInventory();
                    this.traits.get(parsed).engage(menu.getViewer());
                });
            }
        });
        menu.open();
    }

    /*
     * Create a cortex menu for 5 traits.
     *
     * @param player
     */
    private void createCortex5(Player player, List<AbstractCoreTrait> traits) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        LanguageManager lpm = RPGCore.inst().getLanguageManager();

        IChestMenu menu = RPGCore.inst().getVolatileManager().createMenu(6, player);
        menu.setRebuilder(() -> {
            menu.clearItems();

            // build basic background
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.retreat(8);
            msb.append(rpm.texture("menu_cortex_5"), ChatColor.WHITE);

            // extract traits we are working on
            AbstractCoreTrait trait1 = traits.get(0);
            AbstractCoreTrait trait2 = traits.get(1);
            AbstractCoreTrait trait3 = traits.get(2);
            AbstractCoreTrait trait4 = traits.get(3);
            AbstractCoreTrait trait5 = traits.get(4);

            // update menu design
            msb.shiftToExact(0).append(rpm.texture("cortex_medium_" + trait1.getSymbol() + "_0", "cortex_medium_default_0"), ChatColor.WHITE);
            msb.shiftToExact(0).append(rpm.texture("cortex_medium_" + trait2.getSymbol() + "_1", "cortex_medium_default_1"), ChatColor.WHITE);
            msb.shiftToExact(81).append(rpm.texture("cortex_small_" + trait3.getSymbol() + "_0", "cortex_small_default_0"), ChatColor.WHITE);
            msb.shiftToExact(81).append(rpm.texture("cortex_small_" + trait4.getSymbol() + "_1", "cortex_small_default_1"), ChatColor.WHITE);
            msb.shiftToExact(81).append(rpm.texture("cortex_small_" + trait5.getSymbol() + "_2", "cortex_small_default_2"), ChatColor.WHITE);

            // place clickable items
            Arrays.asList(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21)
                    .forEach(i -> menu.setItemAt(i, trait1.getIcon(0)));
            Arrays.asList(27, 28, 29, 30, 36, 37, 38, 39, 45, 46, 47, 48)
                    .forEach(i -> menu.setItemAt(i, trait2.getIcon(1)));
            Arrays.asList(5, 6, 7, 8, 14, 15, 16, 17)
                    .forEach(i -> menu.setItemAt(i, trait3.getIcon(2)));
            Arrays.asList(23, 24, 25, 26, 32, 33, 34, 35)
                    .forEach(i -> menu.setItemAt(i, trait4.getIcon(3)));
            Arrays.asList(41, 42, 43, 44, 50, 51, 52, 53)
                    .forEach(i -> menu.setItemAt(i, trait5.getIcon(4)));

            menu.setTitle(msb.compile());
        });
        menu.setClickHandler(e -> {
            e.setCancelled(true);
            String brand = IChestMenu.getBrand(e.getCurrentItem(), RPGCore.inst(), "cortex-id", null);
            if (brand != null) {
                int parsed = Integer.parseInt(brand);
                menu.stalled(() -> {
                    menu.getViewer().closeInventory();
                    this.traits.get(parsed).engage(menu.getViewer());
                });
            }
        });
        menu.open();
    }

    /*
     * Create a cortex menu for 6 traits.
     *
     * @param player
     */
    private void createCortex6(Player player, List<AbstractCoreTrait> traits) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();
        LanguageManager lpm = RPGCore.inst().getLanguageManager();

        IChestMenu menu = RPGCore.inst().getVolatileManager().createMenu(6, player);
        menu.setRebuilder(() -> {
            menu.clearItems();

            // build basic background
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.retreat(8);
            msb.append(rpm.texture("menu_cortex_6"), ChatColor.WHITE);

            // extract traits we are working on
            AbstractCoreTrait trait1 = traits.get(0);
            AbstractCoreTrait trait2 = traits.get(1);
            AbstractCoreTrait trait3 = traits.get(2);
            AbstractCoreTrait trait4 = traits.get(3);
            AbstractCoreTrait trait5 = traits.get(4);
            AbstractCoreTrait trait6 = traits.get(5);

            // update menu design
            msb.shiftToExact(0).append(rpm.texture("cortex_small_" + trait1.getSymbol() + "_0", "cortex_small_default_0"), ChatColor.WHITE);
            msb.shiftToExact(0).append(rpm.texture("cortex_small_" + trait2.getSymbol() + "_1", "cortex_small_default_1"), ChatColor.WHITE);
            msb.shiftToExact(0).append(rpm.texture("cortex_small_" + trait3.getSymbol() + "_2", "cortex_small_default_2"), ChatColor.WHITE);
            msb.shiftToExact(81).append(rpm.texture("cortex_small_" + trait4.getSymbol() + "_0", "cortex_small_default_0"), ChatColor.WHITE);
            msb.shiftToExact(81).append(rpm.texture("cortex_small_" + trait5.getSymbol() + "_1", "cortex_small_default_1"), ChatColor.WHITE);
            msb.shiftToExact(81).append(rpm.texture("cortex_small_" + trait6.getSymbol() + "_2", "cortex_small_default_2"), ChatColor.WHITE);

            // place clickable items
            Arrays.asList(0, 1, 2, 3, 9, 10, 11, 12)
                    .forEach(i -> menu.setItemAt(i, trait1.getIcon(0)));
            Arrays.asList(18, 19, 20, 21, 27, 28, 29, 30)
                    .forEach(i -> menu.setItemAt(i, trait2.getIcon(1)));
            Arrays.asList(36, 37, 38, 39, 45, 46, 47, 48)
                    .forEach(i -> menu.setItemAt(i, trait2.getIcon(2)));
            Arrays.asList(5, 6, 7, 8, 14, 15, 16, 17)
                    .forEach(i -> menu.setItemAt(i, trait3.getIcon(3)));
            Arrays.asList(23, 24, 25, 26, 32, 33, 34, 35)
                    .forEach(i -> menu.setItemAt(i, trait4.getIcon(4)));
            Arrays.asList(41, 42, 43, 44, 50, 51, 52, 53)
                    .forEach(i -> menu.setItemAt(i, trait5.getIcon(5)));

            menu.setTitle(msb.compile());
        });
        menu.setClickHandler(e -> {
            e.setCancelled(true);
            String brand = IChestMenu.getBrand(e.getCurrentItem(), RPGCore.inst(), "cortex-id", null);
            if (brand != null) {
                int parsed = Integer.parseInt(brand);
                menu.stalled(() -> {
                    menu.getViewer().closeInventory();
                    this.traits.get(parsed).engage(menu.getViewer());
                });
            }
        });
        menu.open();
    }


    /*
     * Data used to track a spawned NPC
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
