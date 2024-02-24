package me.blutkrone.rpgcore.npc.trait.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.bundle.npc.EditorQuestTrait;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.npc.CoreNPC;
import me.blutkrone.rpgcore.npc.trait.AbstractCoreTrait;
import me.blutkrone.rpgcore.quest.CoreQuest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * This trait is <b>ONLY</b>> responsible for providing quests to players, the
 * actual quest handling is done independently.<br>
 * <br>
 * Do note that quest handling associated with the NPC will take precedence
 * over allowing interactions with traits.<br>
 * <br>
 * Should this trait be available, it means that this NPC has quests that
 * can be claimed.
 */
public class CoreQuestTrait extends AbstractCoreTrait {

    // all quests which we do offer
    private Set<String> quests = new LinkedHashSet<>();
    // cache for async operations
    private Map<CorePlayer, List<CoreQuest>> async_cache = Collections.synchronizedMap(new WeakHashMap<>());

    public CoreQuestTrait(EditorQuestTrait editor) {
        super(editor);

        this.quests.addAll(editor.quests);
    }

    @Override
    public boolean isAvailable(CorePlayer player) {
        return super.isAvailable(player)
                && !getQuestAvailable(player).isEmpty();
    }

    /**
     * All quests which are offered by this NPC.
     *
     * @return offered quests.
     */
    public Set<String> getQuests() {
        return quests;
    }

    /**
     * Retrieve an approximation of the available quests.
     *
     * @return A list of all quests to claim
     */
    public List<CoreQuest> getQuestAvailableApproximate(CorePlayer player) {
        Bukkit.getScheduler().runTask(RPGCore.inst(), () -> getQuestAvailable(player));
        return this.async_cache.getOrDefault(player, new ArrayList<>());
    }

    /**
     * A listing of all quests available to be claimed.
     *
     * @param player whose quests to check
     * @return a list of all quests to claim
     */
    public List<CoreQuest> getQuestAvailable(CorePlayer player) {
        List<CoreQuest> available = new ArrayList<>();

        // search all quests now up for picking
        for (String id : this.quests) {
            // ignore active quests
            if (player.getActiveQuestIds().contains(id)) {
                continue;
            }
            // ignore finished quests
            if (player.getCompletedQuests().contains(id)) {
                continue;
            }
            // ignore conditional quests
            CoreQuest quest = RPGCore.inst().getQuestManager().getIndexQuest().get(id);
            if (quest.canAcceptQuest(player)) {
                available.add(quest);
            }
        }

        // snapshot for async information
        this.async_cache.put(player, available);

        return available;
    }

    @Override
    public void engage(Player player, CoreNPC npc) {
        // present a menu allowing to accept quests
        RPGCore.inst().getHUDManager().getQuestMenu().quests(this, player, npc);
    }
}