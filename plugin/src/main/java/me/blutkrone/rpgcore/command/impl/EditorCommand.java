package me.blutkrone.rpgcore.command.impl;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.command.AbstractCommand;
import me.blutkrone.rpgcore.hud.editor.index.EditorIndex;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditorCommand extends AbstractCommand {

    private Map<String, EditorIndex> indexes = null;

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender instanceof Player;
    }

    @Override
    public BaseComponent[] getHelpText() {
        return TextComponent.fromLegacyText("<type> §fOpen the editor of a certain type");
    }

    @Override
    public void invoke(CommandSender sender, String... args) {
        if (args.length == 2) {
            EditorIndex<?, ?> index = this.getIndexes().get(args[1]);

            if (index != null) {
                RPGCore.inst().getHUDManager().getEditorMenu().edit(((Player) sender), index);
                sender.sendMessage("§aAn editor was found for this keyword!");
            } else {
                sender.sendMessage("§cNo editor is associated with this keyword!");
            }
        } else {
            sender.sendMessage("§cIllegal number of arguments!");
        }
    }

    @Override
    public List<String> suggest(CommandSender sender, String... args) {
        if (args.length == 2) {
            List<String> suggests = new ArrayList<>();
            getIndexes().forEach((key, index) -> {
                if (key.startsWith(args[1])) {
                    suggests.add(key);
                }
            });
            return suggests;
        }

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
            this.indexes.put("gate", RPGCore.inst().getNodeManager().getIndexGate());
            this.indexes.put("spawner", RPGCore.inst().getNodeManager().getIndexSpawner());
            this.indexes.put("box", RPGCore.inst().getNodeManager().getIndexBox());
            this.indexes.put("npc", RPGCore.inst().getNPCManager().getIndex());
            this.indexes.put("quest", RPGCore.inst().getQuestManager().getIndexQuest());
            this.indexes.put("dialogue", RPGCore.inst().getQuestManager().getIndexDialogue());
            this.indexes.put("mob", RPGCore.inst().getMobManager().getIndex());
            this.indexes.put("passive", RPGCore.inst().getPassiveManager().getNodeIndex());
            this.indexes.put("tree", RPGCore.inst().getPassiveManager().getTreeIndex());
            this.indexes.put("craft", RPGCore.inst().getItemManager().getCraftIndex());
            this.indexes.put("profession", RPGCore.inst().getJobManager().getIndexProfession());
            this.indexes.put("dungeon", RPGCore.inst().getDungeonManager().getDungeonIndex());
        }

        return this.indexes;
    }
}
