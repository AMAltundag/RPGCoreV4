package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.quest.dialogue.CoreDialogue;
import me.blutkrone.rpgcore.quest.dialogue.CoreDialogueChoice;
import me.blutkrone.rpgcore.resourcepack.utils.IndexedTexture;
import me.blutkrone.rpgcore.util.ItemBuilder;
import me.blutkrone.rpgcore.util.Utility;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DialogueMenu extends AbstractCoreMenu {

    private CoreDialogue dialogue;
    private List<String> contents;
    private IndexedTexture portrait;
    private ItemStack next_page;
    private String quest_task_complete;

    public DialogueMenu(CoreDialogue dialogue, String quest_task_complete) {
        super(6);
        this.dialogue = dialogue;
        this.contents = this.getDialogueProcessed(dialogue);
        this.quest_task_complete = quest_task_complete;
        this.portrait = null;
        this.next_page = RPGCore.inst().getLanguageManager().getAsItem("viewport_right").build();
    }

    @Override
    public void rebuild() {
        this.getMenu().clearItems();

        MagicStringBuilder msb = new MagicStringBuilder();

        if (!contents.isEmpty()) {
            // create basic menu layout
            msb.shiftToExact(-242);
            msb.append(resourcepack().texture("menu_dialogue_text"), ChatColor.WHITE);

            // show dialogue contents
            for (int i = 0; i < 8 && !contents.isEmpty(); i++) {
                String line = contents.get(0);
                if (line.equalsIgnoreCase("new_page")) {
                    contents.remove(0);
                    break;
                } else if (line.startsWith("selfie_")) {
                    // cannot update portrait twice
                    if (portrait != null) {
                        break;
                    }
                    // update the portrait of the menu
                    line = contents.remove(0);
                    portrait = resourcepack().texture("static_" + line.substring(7, line.length()) + "_selfie");
                } else if (line.startsWith("dialogue_")) {
                    // quest image is the only thing rendered
                    if (i == 0) {
                        line = contents.remove(0);
                        IndexedTexture texture = resourcepack().texture(line);
                        msb.shiftToExact(-242);
                        msb.append(texture, ChatColor.WHITE);
                    }

                    break;
                } else {
                    // write generic text
                    line = contents.remove(0);
                    msb.shiftToExact(-8 - 34 + 5);
                    msb.append(line, "menu_text_" + (i + 2));
                }
            }

            // apply portrait if requested
            if (portrait != null) {
                msb.shiftToExact(-8 - 34);
                msb.append(portrait);
            }

            // next-dialogue button
            this.getMenu().setItemAt(53, this.next_page);
        } else {
            // render basic dialogue overlay
            msb.shiftToExact(-208);
            int choices = Math.min(4, dialogue.choices.size());
            msb.append(resourcepack().texture("menu_dialogue_choice_" + choices), ChatColor.WHITE);

            // present the dialogue question
            msb.shiftToExact(2);
            List<String> choice_text = RPGCore.inst().getLanguageManager().getTranslationList(dialogue.lc_dialogue_question);
            msb.append(choice_text.get(0), "dialogue_choice_question");
            for (int i = 0; i < 9; i++) {
                ItemBuilder builder = RPGCore.inst().getLanguageManager().getAsItem("invisible");
                builder.name(choice_text.get(0));
                builder.lore(choice_text.subList(1, choice_text.size()));
                ItemStack stack = builder.build();
                this.getMenu().setItemAt(i, stack);
            }

            // present up to 4 choices for the player
            for (int i = 0; i < choices; i++) {
                CoreDialogueChoice choice = dialogue.choices.get(i);
                choice_text = RPGCore.inst().getLanguageManager().getTranslationList(choice.lc_choice_text);
                // first line is rendered directly to menu
                msb.shiftToExact(2);
                msb.append(choice_text.get(0), "dialogue_choice_" + (i + 1));
                // other lines are rendered on the item lore
                for (int j = 0; j < 9; j++) {
                    ItemBuilder builder = RPGCore.inst().getLanguageManager().getAsItem("invisible");
                    builder.name(choice_text.get(0));
                    builder.lore(choice_text.subList(1, choice_text.size()));
                    ItemStack stack = builder.build();
                    IChestMenu.setBrand(stack, RPGCore.inst(), "dialogue_choice", String.valueOf(i));
                    this.getMenu().setItemAt(((i + 2) * 9) + j, stack);
                }
            }
        }

        this.getMenu().setTitle(msb.compile());
    }

    @Override
    public void click(InventoryClickEvent event) {
        event.setCancelled(true);

        if (this.next_page.isSimilar(event.getCurrentItem())) {
            if (contents.isEmpty() && dialogue.choices.isEmpty()) {
                // click-thorough dialogue is always completed
                getMenu().stalled(() -> {
                    getMenu().getViewer().closeInventory();
                    if (quest_task_complete != null) {
                        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(event.getWhoClicked());
                        core_player.getProgressQuests().put(quest_task_complete, 1);
                    }
                });
            } else {
                // a rebuild will show the next page or choices
                this.getMenu().queryRebuild();
            }
        } else {
            String index = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "dialogue_choice", null);
            if (index != null) {
                // identify which choice was clicked
                int i = Integer.parseInt(index);
                CoreDialogueChoice choice = dialogue.choices.get(i);

                if (choice.dialogue_next != null) {
                    this.getMenu().stalled(() -> {
                        // close current dialogue
                        this.getMenu().getViewer().closeInventory();
                        // open next dialogue
                        new DialogueMenu(choice.dialogue_next, quest_task_complete).finish(this.getMenu().getViewer());
                    });
                } else {
                    // handle task processing
                    if (quest_task_complete != null && choice.correct) {
                        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(event.getWhoClicked());
                        core_player.getProgressQuests().put(quest_task_complete, 1);
                    }
                    // close the menu
                    this.getMenu().stalled(() -> this.getMenu().getViewer().closeInventory());
                }
            }
        }
    }

    /*
     * Retrieve processed dialogue which we can render directly.
     *
     * @param dialogue the dialogue we've received.
     * @return the processed dialogue output.
     */
    private List<String> getDialogueProcessed(CoreDialogue dialogue) {
        List<String> output = new ArrayList<>();

        // pre-process the contents into readable shape
        List<String> contents = RPGCore.inst().getLanguageManager().getTranslationList(dialogue.lc_dialogue_text);
        for (String line : contents) {
            // split words to respect line-breaks
            List<String> words = new ArrayList<>(Arrays.asList(line.split(" ")));

            // create lines with word length limits
            StringBuilder sb = new StringBuilder();
            int length = Utility.measureWidth(words.get(0));
            sb.append(words.remove(0));

            // remaining words are to be appended
            for (String word : words) {
                // manually create a line-break
                if (word.equalsIgnoreCase("new_page")) {
                    // offer up previous line
                    output.add(sb.toString());
                    sb = new StringBuilder();
                    length = 0;
                    // add line break
                    contents.add("new_page");
                    break;
                } else {
                    // accumulate words until linebreak
                    int word_length = Utility.measureWidth(" " + word);
                    if ((length + word_length) > 234) {
                        output.add(sb.toString());
                        sb = new StringBuilder();
                        length = 0;
                    }
                    // pool the remaining words
                    length += word_length;
                    sb.append(" ").append(word);
                }
            }
            // pool remaining words
            if (length != 0) {
                output.add(sb.toString());
            }
        }

        output.replaceAll(String::trim);
        return output;
    }
}
