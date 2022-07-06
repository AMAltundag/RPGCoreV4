package me.blutkrone.rpgcore.hud.initiator;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.roster.IRosterInitiator;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.job.CoreJob;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.resourcepack.ResourcePackManager;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClassInitiator implements IRosterInitiator {

    private ItemStack viewport_left;
    private ItemStack viewport_right;
    private ItemStack confirm_class;

    public ClassInitiator(ConfigWrapper config) {
        this.viewport_left = RPGCore.inst().getLanguageManager().getAsItem("viewport_left").build();
        this.viewport_right = RPGCore.inst().getLanguageManager().getAsItem("viewport_right").build();
        this.confirm_class = RPGCore.inst().getLanguageManager().getAsItem("roster_job_confirm").build();
    }

    @Override
    public int priority() {
        return 3;
    }

    @Override
    public boolean initiate(CorePlayer player) {
        ResourcePackManager rpm = RPGCore.inst().getResourcePackManager();

        // only offer this if we got no jobs listed
        if (player.getJob() != null)
            return false;

        // shuffle job list to avoid favourites
        List<CoreJob> options = new ArrayList<>(RPGCore.inst().getJobManager().getIndex().getAll());
        options.removeIf(job -> !job.isDefaults());
        Collections.shuffle(options);

        // process single-choice options
        if (options.size() == 0) {
            return false;
        } else if (options.size() == 1) {
            player.setJob(options.get(0).getId());
            return false;
        }

        // offer up menu to pick jobs from
        IChestMenu menu = RPGCore.inst().getVolatileManager().createMenu(6, player.getEntity());
        menu.setData("jobs", options);
        menu.setData("focus", 0);
        menu.setData("viewport", 0);

        menu.setRebuilder(() -> {
            menu.clearItems();
            MagicStringBuilder msb = new MagicStringBuilder();

            // fetch class which is focused
            List<CoreJob> jobs = menu.getData("jobs");
            int focus = menu.getData("focus", 0);
            int viewport = menu.getData("viewport", 0);
            CoreJob job = jobs.get(focus);

            // draw frame based on the focused job
            msb.retreat(8);
            msb.append(rpm.texture("menu_class_choice_" + job.getId()), ChatColor.WHITE);

            if (jobs.size() <= 9) {
                // we can just view everything
                for (int i = 0; i < jobs.size(); i++) {
                    menu.setItemAt(45 + i, jobs.get(i).getEmblemIcon());
                }
            } else {
                // create viewport for scrolling
                menu.setItemAt(45, this.viewport_left);
                menu.setItemAt(53, this.viewport_right);
                for (int i = viewport; i < viewport + 7; i++) {
                    menu.setItemAt(46 + i, jobs.get(i).getEmblemIcon());
                }
            }

            // place the weapon item on the respective slot
            menu.setItemAt(31, job.getWeaponIcon());

            // confirmation buttons
            menu.setItemAt(33, this.confirm_class);
            menu.setItemAt(34, this.confirm_class);
            menu.setItemAt(35, this.confirm_class);

            // write the info text about the job
            List<String> info = RPGCore.inst().getLanguageManager()
                    .getTranslationList(job.getId() + "_hint");
            info = info.size() <= 4 ? info : info.subList(0, 4);
            for (int i = 0; i < info.size(); i++) {
                msb.shiftToExact(56).append(info.get(i), "roster_create_info_" + (i + 1));
            }

            InstructionBuilder instructions = new InstructionBuilder();
            instructions.add(RPGCore.inst().getLanguageManager().getTranslationList("instruction_player_class"));
            instructions.apply(msb);

            // send the title to the player
            menu.setTitle(msb.compile());
        });
        menu.setClickHandler((event) -> {
            event.setCancelled(true);
            if (event.getClick() != ClickType.LEFT)
                return;
            if (event.getCurrentItem() == null)
                return;
            if (event.getCurrentItem().getType() == Material.AIR)
                return;
            ItemMeta meta = event.getCurrentItem().getItemMeta();
            if (meta == null)
                return;

            List<CoreJob> jobs = menu.getData("jobs");
            int viewport = menu.getData("viewport", 0);

            if (this.confirm_class.isSimilar(event.getCurrentItem())) {
                // confirm this class selection
                CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(event.getWhoClicked().getUniqueId());
                core_player.setJob(jobs.get(menu.getData("focus", 0)).getId());
                core_player.setPortrait(core_player.getRawJob());
                Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                    event.getWhoClicked().closeInventory();
                });
            } else if (this.viewport_left.isSimilar(event.getCurrentItem())) {
                // shift viewport to the left
                menu.setData("viewport", Math.max(0, Math.min(viewport - 1, jobs.size() - 7)));
            } else if (this.viewport_right.isSimilar(event.getCurrentItem())) {
                // shift viewport to the right
                menu.setData("viewport", Math.max(0, Math.min(viewport + 1, jobs.size() - 7)));
            } else {
                // focus on info of another job
                String jobId = meta.getPersistentDataContainer().getOrDefault(new NamespacedKey(RPGCore.inst(), "job-id"), PersistentDataType.STRING, "");
                CoreJob job = jobId.isEmpty() ? null : RPGCore.inst().getJobManager().getIndex().get(jobId);
                if (job != null) {
                    menu.setData("focus", jobs.indexOf(job));
                }
            }

            // rebuild after interaction
            menu.rebuild();
        });

        menu.open();
        return true;
    }
}
