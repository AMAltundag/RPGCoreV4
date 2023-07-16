package me.blutkrone.rpgcore.npc.trait.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.bundle.npc.EditorBankerTrait;
import me.blutkrone.rpgcore.editor.index.IndexAttachment;
import me.blutkrone.rpgcore.item.CoreItem;
import me.blutkrone.rpgcore.menu.BankerMenu;
import me.blutkrone.rpgcore.npc.CoreNPC;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Storage primarily intended for "bulk" type items, which do not
 * care about their localized data.
 */
public class CoreBankerTrait extends AbstractCoreTrait {

    // what type of items can be banked away
    public List<String> banked;
    // banking tag mapped to items
    public IndexAttachment<CoreItem, Map<String, List<CoreItem>>> bank_to_item =
            RPGCore.inst().getItemManager().getItemIndex().createAttachment(index -> {
                Map<String, List<CoreItem>> cached = new HashMap<>();

                // re-index all items we cached
                for (CoreItem item : index.getAll()) {
                    String group = item.getBankGroup();
                    if (!group.equalsIgnoreCase("none")) {
                        cached.computeIfAbsent(group, (k -> new ArrayList<>())).add(item);
                    }
                }
                // sort the mappings by denomination
                cached.forEach((id, items) -> {
                    items.sort(Comparator.comparingInt(CoreItem::getBankQuantity));
                });

                return cached;
            });

    public CoreBankerTrait(EditorBankerTrait editor) {
        super(editor);
        this.banked = new ArrayList<>(editor.banked);
    }

    @Override
    public void engage(Player player, CoreNPC npc) {
        new BankerMenu(this).finish(player);
    }
}
