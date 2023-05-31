package me.blutkrone.rpgcore.dungeon.structure;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.IOrigin;
import me.blutkrone.rpgcore.dungeon.instance.ActiveDungeonInstance;
import me.blutkrone.rpgcore.dungeon.instance.EditorDungeonInstance;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.bundle.IEditorBundle;
import me.blutkrone.rpgcore.hud.editor.bundle.dungeon.EditorDungeonTreasure;
import me.blutkrone.rpgcore.hud.editor.bundle.item.EditorLoot;
import me.blutkrone.rpgcore.hud.editor.bundle.other.EditorTreasure;
import me.blutkrone.rpgcore.hud.editor.bundle.selector.AbstractEditorSelector;
import me.blutkrone.rpgcore.hud.editor.index.IndexAttachment;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.menu.AbstractCoreMenu;
import me.blutkrone.rpgcore.skill.selector.AbstractCoreSelector;
import me.blutkrone.rpgcore.util.collection.WeightedRandomMap;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

public class TreasureStructure extends AbstractDungeonStructure<TreasureStructure.ActiveTreasure> {

    private List<Treasure> treasures = new ArrayList<>();

    public TreasureStructure(EditorDungeonTreasure editor) {
        super(editor);
        for (IEditorBundle treasure : editor.treasures) {
            this.treasures.add(new Treasure(((EditorTreasure) treasure)));
        }
    }

    @Override
    public void update(ActiveDungeonInstance instance, List<StructureData<ActiveTreasure>> where) {
        if (RPGCore.inst().getTimestamp() % 100 != 0) {
            return;
        }

        for (StructureData<ActiveTreasure> structure : where) {
            if (structure.activated) {

                if (structure.data != null) {
                    // check that the treasure is spawned
                    structure.data.getEntity();
                } else {
                    // identify what treasures can be spawned
                    WeightedRandomMap<Treasure> options = new WeightedRandomMap<>();
                    for (Treasure treasure : treasures) {
                        if (!AbstractCoreSelector.doSelect(treasure.condition, structure.context, Collections.singletonList(new IOrigin.SnapshotOrigin(structure.where))).isEmpty()) {
                            options.add(treasure.weight, treasure);
                        }
                    }

                    if (!options.isEmpty()) {
                        // spawn treasure if anything can spawn
                        structure.data = new ActiveTreasure(options.next(), structure.where.clone().add(0.5d, 0d, 0.5d));
                        instance.getTreasures().put(structure.data.uuid, structure.data);
                        for (Player player : instance.getPlayers(false)) {
                            structure.data.loots.put(player.getUniqueId(), new ItemStack[0]);
                        }
                    } else {
                        // null reference so we do not re-spawn
                        structure.data = new ActiveTreasure(null, null);
                    }
                }
            }
        }
    }

    @Override
    public void update(EditorDungeonInstance instance, List<StructureData<ActiveTreasure>> where) {
        if (RPGCore.inst().getTimestamp() % 20 == 0) {
            for (StructureData<?> structure : where) {
                if (structure.highlight == null) {
                    int x = structure.where.getBlockX();
                    int y = structure.where.getBlockY();
                    int z = structure.where.getBlockZ();
                    structure.highlight = RPGCore.inst().getVolatileManager().getPackets().highlight(x, y, z);
                }

                List<Player> watching = RPGCore.inst().getEntityManager().getObserving(structure.where);
                watching.removeIf(player -> player.getLocation().distance(structure.where) > 32);
                for (Player player : watching) {
                    structure.highlight.enable(player);
                    Bukkit.getScheduler().runTaskLater(RPGCore.inst(), () -> {
                        structure.highlight.disable(player);
                    }, 10);
                }
            }
        }
    }

    private class TreasureMenu extends AbstractCoreMenu {

        // array to remember updates
        private final ItemStack[] loots;
        // menu design to use
        private final String design;

        public TreasureMenu(ItemStack[] loots, String design) {
            super(6);
            this.loots = loots;
            this.design = design;
        }

        @Override
        public void rebuild() {
            throw new UnsupportedOperationException("Cannot rebuild treasure menu");
        }

        @Override
        public void open(InventoryOpenEvent event) {
            // offer the loot contents
            event.getInventory().setContents(loots);
            // build basic background
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.shiftToExact(-208);
            msb.append(resourcepack().texture("menu_" + this.design), ChatColor.WHITE);
            this.getMenu().setTitle(msb.compile());
        }

        @Override
        public void close(InventoryCloseEvent event) {
            // write-back into loots array, to persist looting
            ItemStack[] contents = event.getView().getTopInventory().getContents();
            for (int i = 0; i < contents.length; i++) {
                this.loots[i] = contents[i];
            }
        }

        @Override
        public void click(InventoryClickEvent event) {
            // no logic attached to treasure box
        }
    }

    public class ActiveTreasure {
        private final Treasure treasure;
        private final Location where;
        private final UUID uuid;

        private Reference<ArmorStand> entity;
        private Map<UUID, ItemStack[]> loots;

        public ActiveTreasure(Treasure treasure, Location where) {
            this.treasure = treasure;
            this.where = where;
            this.uuid = UUID.randomUUID();
            this.loots = new HashMap<>();
            this.entity = new WeakReference<>(null);
        }

        /**
         * Offer the treasure to the player, do note that this will fail
         * if the player wasn't present when the treasure was generated or
         * if they cleared the treasure inventory.
         *
         * @param player Who wants to retrieve the treasure
         */
        public void offerTo(Player player) {
            ItemStack[] loots = this.loots.get(player.getUniqueId());
            if (loots == null) {
                return;
            }
            // if non-generated, do generate new loot
            if (loots.length == 0) {
                loots = generate(RPGCore.inst().getEntityManager().getPlayer(player));
                this.loots.put(player.getUniqueId(), loots);
            }
            // only open if loot chest isn't emptied out
            boolean valid = false;
            for (ItemStack loot : loots) {
                valid = valid || (loot != null && !loot.getType().isAir());
            }
            // offer a chest with the loot if we can
            if (valid) {
                new TreasureMenu(loots, treasure.menu_design).finish(player);
            }
        }

        /*
         * Generate the items that match with this loot table.
         *
         * @param interested Who is interested in this loot
         * @return Generated loot
         */
        private ItemStack[] generate(CorePlayer interested) {
            // check the slots we can generate
            List<Integer> slots = new ArrayList<>(this.treasure.slots);
            if (slots.isEmpty()) {
                for (int i = 0; i < 54; i++) {
                    slots.add(i);
                }
            }
            // shuffle the slots
            Collections.shuffle(slots);
            // populate the slots
            ItemStack[] output = new ItemStack[54];
            for (int i = 0; i < this.treasure.count && !slots.isEmpty(); i++) {
                CoreItem item = this.treasure.loot.get().next();
                if (item != null) {
                    output[slots.remove(0)] = item.acquire(interested, 1d);
                }
            }
            // offer up what we've generated
            return output;
        }

        /*
         * The entity which allows us access to this treasure instance.
         *
         * @return Treasure entity
         */
        private ArmorStand getEntity() {
            // ensure we do not have a null entity
            if (this.treasure == null || this.where == null) {
                return null;
            }
            // ensure that the world is loaded
            if (!this.where.isWorldLoaded()) {
                return null;
            }
            // ensure that the chunk is loaded
            if (!this.where.getWorld().isChunkLoaded(this.where.getBlockX()>>4, this.where.getBlockZ()>>4)) {
                return null;
            }
            // re-spawn in case it despawned
            ArmorStand entity = this.entity.get();
            if (entity == null || !entity.isValid()) {
                entity = where.getWorld().spawn(where, ArmorStand.class);
                entity.setMetadata("rpgcore_loot", new FixedMetadataValue(RPGCore.inst(), uuid.toString()));
                entity.setInvisible(true);
                // entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 9999999, 1, false, false, false));
                entity.setInvulnerable(true);
                entity.setPersistent(false);
                entity.setSilent(true);
                entity.getEquipment().setHelmet(this.treasure.model, true);
                this.entity = new WeakReference<>(entity);
            }
            // offer the entity up again
            return entity;
        }
    }

    private class Treasure {
        ItemStack model;
        String menu_design;
        int count;
        List<Integer> slots;
        IndexAttachment<CoreItem, WeightedRandomMap<CoreItem>> loot;
        double weight;
        List<AbstractCoreSelector> condition;

        public Treasure(EditorTreasure treasure) {
            this.model = treasure.model.build();
            this.menu_design = treasure.menu_design;
            this.count = (int) treasure.count;
            this.loot = EditorLoot.build(treasure.item_weight);
            this.weight = treasure.weight;
            this.condition = AbstractEditorSelector.unwrap(treasure.condition);
            this.slots = new ArrayList<>();
            for (String line : treasure.exact) {
                for (int i = 0; i < 54; i++) {
                    if (new Range(line).contains(i)) {
                        this.slots.add(i);
                    }
                }
            }
        }
    }


    /**
     * A number range to constrain the score to.
     */
    public class Range {
        public int start;
        public int finish;

        public Range(String string) {
            try {
                String[] split = string.split("\\~");
                this.start = Double.valueOf(split[0]).intValue();
                if (split.length == 2) {
                    this.finish = Double.valueOf(split[1]).intValue();
                } else {
                    this.finish = Double.valueOf(split[0]).intValue();
                }
            } catch (Exception e) {
                Bukkit.getLogger().severe("Bad number range: " + string);
                this.start = 0;
                this.finish = 0;
            }
        }

        public boolean contains(double value) {
            return value >= start && value <= finish;
        }
    }
}