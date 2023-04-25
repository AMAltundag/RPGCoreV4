package me.blutkrone.rpgcore.data.defaults;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.data.DataBundle;
import me.blutkrone.rpgcore.data.structure.DataProtocol;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.util.io.BukkitSerialization;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PassiveProtocol implements DataProtocol {
    @Override
    public boolean isRosterData() {
        return false;
    }

    @Override
    public void save(CorePlayer player, DataBundle bundle) {
        Map<String, Integer> passiveRefunds = player.getPassiveRefunds();
        Map<String, Map<Long, ItemStack>> passiveSocketed = player.getPassiveSocketed();
        Map<String, Integer> passivePoints = player.getPassivePoints();
        Map<String, Long> passiveViewport = player.getPassiveViewport();
        Map<String, Long> passiveIntegrity = player.getPassiveIntegrity();
        Map<String, Set<Long>> passiveAllocated = player.getPassiveAllocated();

        bundle.addNumber(passiveRefunds.size());
        passiveRefunds.forEach((tree, refunds) -> {
            bundle.addString(tree);
            bundle.addNumber(refunds);
        });

        bundle.addNumber(passivePoints.size());
        passivePoints.forEach((tree, points) -> {
            bundle.addString(tree);
            bundle.addNumber(points);
        });

        bundle.addNumber(passiveViewport.size());
        passiveViewport.forEach((tree, viewport) -> {
            bundle.addString(tree);
            bundle.addNumber(viewport);
        });

        bundle.addNumber(passiveIntegrity.size());
        passiveIntegrity.forEach((tree, integrity) -> {
            bundle.addString(tree);
            bundle.addNumber(integrity);
        });

        bundle.addNumber(passiveSocketed.size());
        passiveSocketed.forEach((tree, socketed) -> {
            bundle.addString(tree);
            bundle.addNumber(socketed.size());
            socketed.forEach((where, item) -> {
                bundle.addNumber(where);
                bundle.addString(BukkitSerialization.toBase64(item));
            });
        });

        bundle.addNumber(passiveAllocated.size());
        passiveAllocated.forEach((tree, allocated) -> {
            bundle.addString(tree);
            bundle.addNumber(allocated.size());
            for (long where : allocated) {
                bundle.addNumber(where);
            }
        });
    }

    @Override
    public void load(CorePlayer player, DataBundle bundle, int version) {
        Map<String, Integer> passiveRefunds = player.getPassiveRefunds();
        Map<String, Integer> passivePoints = player.getPassivePoints();
        Map<String, Long> passiveViewport = player.getPassiveViewport();
        Map<String, Long> passiveIntegrity = player.getPassiveIntegrity();

        if (!bundle.isEmpty()) {
            int header = 0;
            int size;

            size = bundle.getNumber(header++).intValue();
            for (int i = 0; i < size; i++) {
                String key = bundle.getString(header++);
                int value = bundle.getNumber(header++).intValue();
                passiveRefunds.put(key, value);
            }

            size = bundle.getNumber(header++).intValue();
            for (int i = 0; i < size; i++) {
                String key = bundle.getString(header++);
                int value = bundle.getNumber(header++).intValue();
                passivePoints.put(key, value);
            }

            size = bundle.getNumber(header++).intValue();
            for (int i = 0; i < size; i++) {
                String key = bundle.getString(header++);
                long value = bundle.getNumber(header++).longValue();
                passiveViewport.put(key, value);
            }

            size = bundle.getNumber(header++).intValue();
            for (int i = 0; i < size; i++) {
                String key = bundle.getString(header++);
                long value = bundle.getNumber(header++).longValue();
                passiveIntegrity.put(key, value);
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
                        RPGCore.inst().getItemManager().describe(socketed, player);
                        player.setPassiveSocketed(tree, where, socketed);
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
                player.getAllocated(tree).addAll(allocated);
            }
        }

        player.updatePassiveTree();
    }
}

