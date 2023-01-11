package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.AbstractCommand;
import me.blutkrone.rpgcore.hud.editor.index.EditorIndex;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReloadCommand extends AbstractCommand {
    private Map<String, EditorIndex> indexes = null;

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("rpg.admin");
    }

    @Override
    public BaseComponent[] getHelpText() {
        return TextComponent.fromLegacyText("§fReloads some parts of the core, not everything!");
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        long stamp = System.currentTimeMillis();
        // reload the language files
        RPGCore.inst().getLanguageManager().reload();
        // reload the indexes
        getIndexes().forEach((id, index) -> index.reload());
        // reload the node manager
        RPGCore.inst().getNodeManager().reload();

        sender.sendMessage("§fReloaded RPGCore in: §c%.2f§f seconds!".formatted((System.currentTimeMillis() - stamp) / 1000d));
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        return null;
    }

    private Map<String, EditorIndex> getIndexes() {
        if (this.indexes == null) {
            this.indexes = new HashMap<>();
            this.indexes.put("attribute", RPGCore.inst().getAttributeManager().getIndex());
            this.indexes.put("item", RPGCore.inst().getItemManager().getItemIndex());
            this.indexes.put("refine", RPGCore.inst().getItemManager().getRefineIndex());
            this.indexes.put("modifier", RPGCore.inst().getItemManager().getModifierIndex());
            this.indexes.put("effect", RPGCore.inst().getEffectManager().getIndex());
            this.indexes.put("job", RPGCore.inst().getJobManager().getIndexJob());
            this.indexes.put("skill", RPGCore.inst().getSkillManager().getIndex());
            this.indexes.put("collectible", RPGCore.inst().getNodeManager().getIndexCollectible());
            this.indexes.put("hotspot", RPGCore.inst().getNodeManager().getIndexHotspot());
            this.indexes.put("spawner", RPGCore.inst().getNodeManager().getIndexSpawner());
            this.indexes.put("box", RPGCore.inst().getNodeManager().getIndexBox());
            this.indexes.put("npc", RPGCore.inst().getNPCManager().getIndex());
            this.indexes.put("quest", RPGCore.inst().getQuestManager().getIndexQuest());
            this.indexes.put("dialogue", RPGCore.inst().getQuestManager().getIndexDialogue());
            this.indexes.put("mob", RPGCore.inst().getMobManager().getIndex());
            this.indexes.put("passive", RPGCore.inst().getPassiveManager().getNodeIndex());
            this.indexes.put("tree", RPGCore.inst().getPassiveManager().getTreeIndex());
            this.indexes.put("craft", RPGCore.inst().getItemManager().getCraftIndex());
        }

        return this.indexes;
    }
}
