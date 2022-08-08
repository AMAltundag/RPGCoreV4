package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.npc.CoreNPC;
import me.blutkrone.rpgcore.quest.CoreQuest;
import me.blutkrone.rpgcore.quest.reward.AbstractQuestReward;
import me.blutkrone.rpgcore.quest.task.impl.CoreQuestTaskDeliver;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class QuestMenu {

    private QuestMenu() {
    }

    public static class Quest extends AbstractCoreMenu {

        private CoreNPC npc;
        private List<String> quests;
        private int offset;
        private ItemStack invisible = RPGCore.inst().getLanguageManager().getAsItem("invisible").build();

        public Quest(List<String> quests, CoreNPC npc) {
            super(6);
            this.quests = quests;
            this.npc = npc;
            this.offset = 0;
        }

        @Override
        public void rebuild() {
            getMenu().clearItems();
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());

            // base texture
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.shiftToExact(-208);
            msb.append(resourcepack().texture("menu_scroller_mono"), ChatColor.WHITE);

            for (int i = 0; i < 6; i++) {
                String qId = quests.size() > (offset + i) ? quests.get(offset + i) : null;
                if (qId != null) {
                    CoreQuest quest = RPGCore.inst().getQuestManager().getIndexQuest().get(qId);
                    // track the item on the menu
                    ItemStack icon = quest.getIcon();
                    IChestMenu.setBrand(icon, RPGCore.inst(), "quest-id", qId);
                    getMenu().setItemAt(i * 9, icon);
                    // shallow copies to make the line clickable
                    for (int j = 1; j < 8; j++) {
                        getMenu().setItemAt((i * 9) + j, ItemBuilder.of(icon.clone()).inheritIcon(this.invisible).build());
                    }
                    // write quest name down
                    msb.shiftToExact(20);
                    if (core_player.getActiveQuestIds().contains(qId)) {
                        msb.append(quest.getName(), "scroller_text_" + (i + 1), ChatColor.GREEN);
                    } else {
                        msb.append(quest.getName(), "scroller_text_" + (i + 1), ChatColor.WHITE);
                    }
                }
            }

            // render scroll-bar for the viewport
            msb.shiftToExact(150);
            if (quests.size() <= 6) {
                msb.append(resourcepack().texture("pointer_huge_0"), ChatColor.WHITE);
            } else if (quests.size() <= 12) {
                double ratio = (offset - 6d) / (quests.size() - 6);
                msb.append(resourcepack().texture("pointer_medium_" + (int) (100 * ratio)), ChatColor.WHITE);
            } else if (quests.size() <= 24) {
                double ratio = (offset - 6d) / (quests.size() - 6);
                msb.append(resourcepack().texture("pointer_small_" + (int) (100 * ratio)), ChatColor.WHITE);
            } else {
                double ratio = (offset - 6d) / (quests.size() - 6);
                msb.append(resourcepack().texture("pointer_tiny_" + (int) (100 * ratio)), ChatColor.WHITE);
            }

            InstructionBuilder instructions = new InstructionBuilder();
            instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_quest"));
            instructions.apply(msb);

            getMenu().setTitle(msb.compile());
        }

        @Override
        public void click(InventoryClickEvent event) {
            event.setCancelled(true);

            if (event.getView().getTopInventory() == event.getClickedInventory()) {
                if (event.getSlot() == 8) {
                    // scroll up by one
                    offset = Math.max(0, offset - 1);
                    getMenu().queryRebuild();
                } else if (event.getSlot() == 17) {
                    // scroll to top
                    offset = 0;
                    getMenu().queryRebuild();
                } else if (event.getSlot() == 26) {
                    // ignore other clicks
                } else if (event.getSlot() == 35) {
                    // ignore other clicks
                } else if (event.getSlot() == 44) {
                    // scroll to bottom
                    offset = Math.max(0, quests.size() - 6);
                    getMenu().queryRebuild();
                } else if (event.getSlot() == 53) {
                    // scroll down by one
                    int floor = Math.max(0, quests.size() - 6);
                    offset = Math.min(floor, offset + 1);
                    getMenu().queryRebuild();
                } else {
                    String id = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "quest-id", null);
                    if (id != null) {
                        // interact with a quest
                        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(event.getWhoClicked());
                        CoreQuest quest = RPGCore.inst().getQuestManager().getIndexQuest().get(id);

                        if (!core_player.getActiveQuestIds().contains(id)) {
                            // check if we can claim this quest
                            if (core_player.getActiveQuestIds().size() < 50) {
                                if (quest.canAcceptQuest(core_player)) {
                                    // accept the quest
                                    quest.acceptQuest(core_player);
                                    // hide the quest from the list
                                    quests.remove(id);
                                    // refresh the UX
                                    getMenu().queryRebuild();
                                }
                            } else {
                                String warning = RPGCore.inst().getLanguageManager().getTranslation("too_many_quests");
                                event.getWhoClicked().sendMessage(warning);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void close(InventoryCloseEvent event) {
            getMenu().stalled(() -> {
                if (getMenu().getViewer().getOpenInventory().getType() == InventoryType.CRAFTING) {
                    npc.showCortex(getMenu().getViewer(), false);
                }
            });
        }
    }

    public static class Reward extends AbstractCoreMenu {

        private CoreQuest quest;
        private CoreNPC npc;

        private int optional = -1;

        public Reward(CoreQuest quest, CoreNPC npc) {
            super(6);
            this.quest = quest;
            this.npc = npc;
        }

        @Override
        public void rebuild() {
            getMenu().clearItems();
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(getMenu().getViewer());

            // base texture
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.shiftToExact(-208);
            msb.append(resourcepack().texture("menu_quest_reward"), ChatColor.WHITE);

            // render the quest rewards
            for (int i = 0; i < quest.getFixedRewards().size() && i < 9; i++) {
                AbstractQuestReward reward = quest.getFixedRewards().get(i);
                getMenu().setItemAt(2 * 9 + i, reward.getPreview(core_player));
            }
            for (int i = 0; i < quest.getChoiceRewards().size() && i < 9; i++) {
                AbstractQuestReward reward = quest.getChoiceRewards().get(i);
                ItemStack preview = reward.getPreview(core_player);
                IChestMenu.setBrand(preview, RPGCore.inst(), "choice-id", String.valueOf(i));
                getMenu().setItemAt(3 * 9 + i, preview);
            }

            // write quest name on the menu
            msb.shiftToExact(2).append(quest.getName(), "scroller_text_1");
            getMenu().setTitle(msb.compile());
        }

        @Override
        public void click(InventoryClickEvent event) {
            event.setCancelled(true);

            if (event.getClickedInventory() == event.getView().getTopInventory()) {
                if (event.getSlot() >= 27 && event.getSlot() <= 35) {
                    if (event.getCurrentItem() != null) {
                        // strip away previous selection
                        if (optional != -1) {
                            getMenu().getItemAt(optional).removeEnchantment(Enchantment.DURABILITY);
                        }
                        // highlight selection and track it
                        event.getCurrentItem().addEnchantment(Enchantment.DURABILITY, 1);
                        optional = event.getSlot();
                    }
                } else if (event.getSlot() >= 47 && event.getSlot() <= 51) {
                    // check for click on accept slot
                    if (quest.getChoiceRewards().isEmpty() || optional != -1) {
                        // retrieve the rewards for this quest
                        List<AbstractQuestReward> rewards = new ArrayList<>(quest.getFixedRewards());
                        if (optional != -1) {
                            ItemStack choice = getMenu().getItemAt(optional);
                            optional = Integer.parseInt(IChestMenu.getBrand(choice, RPGCore.inst(), "choice-id", "-1"));
                            rewards.add(quest.getChoiceRewards().get(optional));
                        }
                        // give the rewards to the player
                        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(event.getWhoClicked());
                        for (AbstractQuestReward reward : rewards) {
                            try {
                                reward.giveReward(core_player);
                            } catch (Exception e1) {
                                // quest reward dupe protection
                                e1.printStackTrace();
                            }
                        }
                        // mark as complete and close the menu
                        quest.completeQuest(core_player);
                        // close the menu since we claimed rewards
                        getMenu().stalled(() -> getMenu().getViewer().closeInventory());
                    }
                }
            }
        }

        @Override
        public void close(InventoryCloseEvent event) {
            getMenu().stalled(() -> {
                if (getMenu().getViewer().getOpenInventory().getType() == InventoryType.CRAFTING) {
                    npc.showCortex(getMenu().getViewer(), false);
                }
            });
        }
    }

    public static class Delivery extends AbstractCoreMenu {

        private CoreQuestTaskDeliver task;
        private CoreNPC npc;

        public Delivery(CoreQuestTaskDeliver task, CoreNPC npc) {
            super(2);
            this.task = task;
            this.npc = npc;
        }

        @Override
        public void rebuild() {
            getMenu().clearItems();

            // base texture
            MagicStringBuilder msb = new MagicStringBuilder();
            msb.shiftToExact(-208);
            msb.append(resourcepack().texture("menu_quest_deliver"), ChatColor.WHITE);

            // generate previews for every item of importance
            List<ItemStack> previews = new ArrayList<>();
            task.getDemand().forEach((item, amount) -> {
                while (amount > 0) {
                    ItemStack stack = RPGCore.inst().getItemManager().getItemIndex().get(item).unidentified();
                    int absorb = Math.min(64, amount);
                    stack.setAmount(absorb);
                    amount -= absorb;
                    previews.add(stack);
                }
            });
            // put up preview items
            for (int i = 0; i < 12 && !previews.isEmpty(); i++) {
                getMenu().setItemAt((i / 6) * 9 + (i % 6), previews.remove(0));
            }

            getMenu().setTitle(msb.compile());
        }

        @Override
        public void click(InventoryClickEvent event) {
            event.setCancelled(true);

            if (event.getClickedInventory() == event.getView().getTopInventory() && event.getSlot() % 9 >= 7) {
                task.updateQuest(RPGCore.inst().getEntityManager().getPlayer(event.getWhoClicked()), new Object());
                getMenu().stalled(() -> event.getWhoClicked().closeInventory());
            }
        }

        @Override
        public void close(InventoryCloseEvent event) {
            getMenu().stalled(() -> {
                if (getMenu().getViewer().getOpenInventory().getType() == InventoryType.CRAFTING) {
                    npc.showCortex(getMenu().getViewer(), false);
                }
            });
        }
    }
}
