package me.blutkrone.rpgcore.menu;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.hud.editor.instruction.InstructionBuilder;
import me.blutkrone.rpgcore.job.CoreJob;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import me.blutkrone.rpgcore.util.fontmagic.MagicStringBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class JobAdvancementMenu extends AbstractCoreMenu {

    private int focus;
    private int viewport;

    private ItemStack viewport_left;
    private ItemStack viewport_right;
    private ItemStack confirm_class;

    public JobAdvancementMenu() {
        super(6);

        this.focus = 0;
        this.viewport = 0;
        this.viewport_left = RPGCore.inst().getLanguageManager().getAsItem("viewport_left").build();
        this.viewport_right = RPGCore.inst().getLanguageManager().getAsItem("viewport_right").build();
        this.confirm_class = RPGCore.inst().getLanguageManager().getAsItem("roster_job_confirm").build();
    }

    /**
     * Jobs which we can offer.
     *
     * @return
     */
    public List<CoreJob> getJobs() {
        Player viewer = getMenu().getViewer();
        CorePlayer player = RPGCore.inst().getEntityManager().getPlayer(viewer);
        if (player == null) {
            return new ArrayList<>();
        }
        CoreJob job = player.getJob();
        if (job != null) {
            return player.getJob().getAdvancements();
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void rebuild() {
        this.getMenu().clearItems();
        MagicStringBuilder msb = new MagicStringBuilder();

        // fetch class which is focused
        CoreJob job = getJobs().get(focus);

        // draw frame based on the focused job
        msb.shiftToExact(-208);
        msb.append(resourcepack().texture("menu_class_choice_" + job.getId()), ChatColor.WHITE);

        if (getJobs().size() <= 9) {
            // we can just view everything
            for (int i = 0; i < getJobs().size(); i++) {
                this.getMenu().setItemAt(45 + i, getJobs().get(i).getEmblemIcon());
            }
        } else {
            // create viewport for scrolling
            this.getMenu().setItemAt(45, this.viewport_left);
            this.getMenu().setItemAt(53, this.viewport_right);
            for (int i = viewport; i < viewport + 7; i++) {
                this.getMenu().setItemAt(46 + i, getJobs().get(i).getEmblemIcon());
            }
        }

        // place the weapon item on the respective slot
        this.getMenu().setItemAt(31, job.getWeaponIcon());

        // confirmation buttons
        this.getMenu().setItemAt(33, this.confirm_class);
        this.getMenu().setItemAt(34, this.confirm_class);
        this.getMenu().setItemAt(35, this.confirm_class);

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
        this.getMenu().setTitle(msb.compile());
    }

    @Override
    public void click(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getClick() != ClickType.LEFT || !isRelevant(event.getCurrentItem())) {
            return;
        }

        if (this.confirm_class.isSimilar(event.getCurrentItem())) {
            // confirm this class selection
            CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(event.getWhoClicked().getUniqueId());
            core_player.setJob(getJobs().get(focus).getId());
            core_player.setPortrait(core_player.getRawJob());
            core_player.getPersistentTags().remove("job_advancement_waiting");
            Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                event.getWhoClicked().closeInventory();
            });
        } else if (this.viewport_left.isSimilar(event.getCurrentItem())) {
            // shift viewport to the left
            viewport = Math.max(0, Math.min(viewport - 1, getJobs().size() - 7));
        } else if (this.viewport_right.isSimilar(event.getCurrentItem())) {
            // shift viewport to the right
            viewport = Math.max(0, Math.min(viewport + 1, getJobs().size() - 7));
        } else {
            // focus on info of another job
            String id = IChestMenu.getBrand(event.getCurrentItem(), RPGCore.inst(), "job-id", "");
            CoreJob job = id.isEmpty() ? null : RPGCore.inst().getJobManager().getIndex().get(id);
            if (job != null) {
                focus = getJobs().indexOf(job);
            }
        }

        // rebuild after interaction
        this.getMenu().queryRebuild();
    }
}
