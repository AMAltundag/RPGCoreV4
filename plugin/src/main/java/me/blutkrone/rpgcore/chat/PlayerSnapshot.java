package me.blutkrone.rpgcore.chat;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.damage.DamageMetric;
import me.blutkrone.rpgcore.data.DataBundle;
import me.blutkrone.rpgcore.item.styling.IDescriptionRequester;
import me.blutkrone.rpgcore.util.io.BukkitSerialization;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.*;

/**
 * A snapshot of player data, primarily meant for chat
 * information.
 */
public class PlayerSnapshot implements IDescriptionRequester {

    public UUID uuid;
    public String alias = "??";
    public String portrait = "nothing";
    public String user = "??";
    public Map<String, ItemStack> equipment = new HashMap<>();
    public String[] skillbar = new String[6];
    public int level = 1;
    public double experience = 0d;
    public String job = "nothing";
    public Map<String, Integer> passive_refunds = new HashMap<>();
    public Map<String, Integer> passive_points = new HashMap<>();
    public Map<String, Long> passive_viewport = new HashMap<>();
    public Map<String, Long> passive_integrity = new HashMap<>();
    public Map<String, Set<Long>> passive_allocated = new HashMap<>();
    public Map<String, Map<Long, ItemStack>> passive_socketed = new HashMap<>();
    public long timestamp;

    public PlayerSnapshot(UUID uuid) {
        this.uuid = uuid;
        this.timestamp = System.currentTimeMillis();
        Map<String, DataBundle> data = RPGCore.inst().getDataManager().getLastData(this.uuid);
        loadSkillInfo(data.getOrDefault("skill", new DataBundle()));
        loadItemInfo(data.getOrDefault("item", new DataBundle()));
        loadLevelInfo(data.getOrDefault("level", new DataBundle()));
        loadJobInfo(data.getOrDefault("job", new DataBundle()));
        loadPassiveInfo(data.getOrDefault("passive", new DataBundle()));
        loadDisplay(data.getOrDefault("display", new DataBundle()));
    }

    public PlayerSnapshot(UUID uuid, Map<String, DataBundle> data) {
        this.uuid = uuid;
        this.timestamp = System.currentTimeMillis();
        loadSkillInfo(data.getOrDefault("skill", new DataBundle()));
        loadItemInfo(data.getOrDefault("item", new DataBundle()));
        loadLevelInfo(data.getOrDefault("level", new DataBundle()));
        loadJobInfo(data.getOrDefault("job", new DataBundle()));
        loadPassiveInfo(data.getOrDefault("passive", new DataBundle()));
        loadDisplay(data.getOrDefault("display", new DataBundle()));
    }

    private void loadDisplay(DataBundle bundle) {
        if (!bundle.isEmpty()) {
            if (bundle.getNumber(0).intValue() == 0) {
                this.alias = bundle.getString(1);
                this.portrait = bundle.getString(2);
            } else if (bundle.getNumber(0).intValue() == 1) {
                this.alias = bundle.getString(1);
                this.portrait = bundle.getString(2);
                this.user = bundle.getString(3);
            }
        }
    }

    private void loadSkillInfo(DataBundle bundle) {
        if (!bundle.isEmpty()) {
            for (int i = 0; i < 6; i++) {
                this.skillbar[i] = bundle.getString(1 + i);
            }
        } else {
            for (int i = 0; i < 6; i++) {
                this.skillbar[i] = "nothing";
            }
        }
    }

    private void loadLevelInfo(DataBundle bundle) {
        if (!bundle.isEmpty()) {
            this.level = bundle.getNumber(1).intValue();
            this.experience = bundle.getNumber(2).doubleValue();
        }
    }

    private void loadJobInfo(DataBundle bundle) {
        if (!bundle.isEmpty()) {
            this.job = bundle.getString(1);
        }
    }

    private void loadPassiveInfo(DataBundle bundle) {
        if (!bundle.isEmpty()) {
            int header = 1;
            int size;

            size = bundle.getNumber(header++).intValue();
            for (int i = 0; i < size; i++) {
                String key = bundle.getString(header++);
                int value = bundle.getNumber(header++).intValue();
                passive_refunds.put(key, value);
            }

            size = bundle.getNumber(header++).intValue();
            for (int i = 0; i < size; i++) {
                String key = bundle.getString(header++);
                int value = bundle.getNumber(header++).intValue();
                passive_points.put(key, value);
            }

            size = bundle.getNumber(header++).intValue();
            for (int i = 0; i < size; i++) {
                String key = bundle.getString(header++);
                long value = bundle.getNumber(header++).longValue();
                passive_viewport.put(key, value);
            }

            size = bundle.getNumber(header++).intValue();
            for (int i = 0; i < size; i++) {
                String key = bundle.getString(header++);
                long value = bundle.getNumber(header++).longValue();
                passive_integrity.put(key, value);
            }

            size = bundle.getNumber(header++).intValue();
            for (int i = 0; i < size; i++) {
                String tree = bundle.getString(header++);
                int size2 = bundle.getNumber(header++).intValue();
                for (int j = 0; j < size2; j++) {
                    long where = bundle.getNumber(header++).longValue();
                    String socketedB64 = bundle.getString(header++);
                    try {
                        ItemStack socketed = BukkitSerialization.fromBase64(socketedB64)[0];
                        passive_socketed.computeIfAbsent(tree, (k -> new HashMap<>())).put(where, socketed);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            size = bundle.getNumber(header++).intValue();
            for (int i = 0; i < size; i++) {
                String tree = bundle.getString(header++);
                int size2 = bundle.getNumber(header++).intValue();
                Set<Long> allocated = new HashSet<>();
                for (int j = 0; j < size2; j++) {
                    long encoded_position = bundle.getNumber(header++).longValue();
                    allocated.add(encoded_position);
                }
                passive_allocated.put(tree, allocated);
            }
        }
    }

    private void loadItemInfo(DataBundle bundle) {
        if (!bundle.isEmpty()) {
            int length = bundle.getNumber(2).intValue();
            int header = 3;
            for (int i = 0; i < length; i++) {
                String slot = bundle.getString(header++);
                String item = bundle.getString(header++);
                try {
                    ItemStack stack = BukkitSerialization.fromBase64(item)[0];
                    if (stack != null && RPGCore.inst().getHUDManager().getEquipMenu().isReflected(stack)) {
                        stack.setType(Material.AIR);
                    }
                    this.equipment.put(slot, stack);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Map<String, Map<Long, ItemStack>> getPassiveSocketed() {
        return this.passive_socketed;
    }

    @Override
    public DamageMetric getMetric(String metric) {
        return null;
    }
}
