package me.blutkrone.rpgcore.mail;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.api.data.IDataAdapter;
import me.blutkrone.rpgcore.data.DataBundle;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Mails operate on snapshots and are volatile, actual processing
 * of the mail should only happen thorough these methods that are
 * meant to prevent duping.
 */
public class MailManager {

    private static long MAXIMUM_MAIL_AGE = 7776000000L; // maximum mail age is 90 days

    public MailManager() {
    }

    /**
     * Retrieve a <b>view</b> of mails, these serve merely to read
     * mails - not to accept them.
     *
     * @param inbox    whose inbox are we inspecting
     * @param callback the callback once mails are loaded.
     */
    public void viewMails(OfflinePlayer inbox, Consumer<Map<UUID, CoreMail>> callback) {
        IDataAdapter adapter = RPGCore.inst().getDataManager().getDataAdapter();
        try {
            Map<String, DataBundle> raw_data = adapter.loadCustom(inbox.getUniqueId(), "mailbox");
            Map<UUID, CoreMail> mailbox = new HashMap<>();
            raw_data.forEach((id, data) -> mailbox.put(UUID.fromString(id), new CoreMail(data)));
            callback.accept(mailbox);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send the given mail entry to the user, the mail can later be
     * retrieved thorough a {@link #viewMails} invocation.
     *
     * @param receiver who will receive the mail
     * @param mail     the mail for them to receive
     * @see #viewMails(OfflinePlayer, Consumer) to check sent mails
     */
    public void sendMail(OfflinePlayer receiver, CoreMail mail) {
        IDataAdapter adapter = RPGCore.inst().getDataManager().getDataAdapter();
        adapter.operateCustom(receiver.getUniqueId(), "mailbox", (mails -> {
            // mails older then 90 days are just deleted
            long expire_mail_before = System.currentTimeMillis() - MailManager.MAXIMUM_MAIL_AGE;
            mails.values().removeIf(old -> old.getNumber(3).longValue() <= expire_mail_before);
            // search for an unused mail identifier
            UUID id = UUID.randomUUID();
            while (mails.containsKey(id.toString())) {
                id = UUID.randomUUID();
            }
            // grant the user this particular mail
            mails.put(id.toString(), mail.export());
        }));
    }

    /**
     * Claim the attachment of a mail.
     * <br>
     * Assuming that the player has space in their inventory, we
     * can assume the attachment was claimed successfully.
     *
     * @param receiver who wants to claim a mail attachment
     * @param mailId   the unique identifier of the mail to claim
     */
    public void collectMail(Player receiver, UUID mailId) {
        IDataAdapter adapter = RPGCore.inst().getDataManager().getDataAdapter();

        adapter.operateCustom(receiver.getUniqueId(), "mailbox", (mails -> {
            // ensure we got a mail to claim at all
            DataBundle mail_to_claim = mails.get(mailId.toString());
            if (mail_to_claim == null) {
                String message = RPGCore.inst().getLanguageManager().getTranslation("could_not_claim_mail");
                receiver.sendMessage(message);
                return;
            }
            // unwrap the mail entry we got
            CoreMail mail = new CoreMail(mail_to_claim);
            // if mail has no attachment, do not try to claim
            if (mail.attachment.getType().isAir()) {
                return;
            }
            // warn player if not enough space
            if (receiver.getInventory().firstEmpty() == -1) {
                String message = RPGCore.inst().getLanguageManager().getTranslation("could_not_claim_mail");
                receiver.sendMessage(message);
                return;
            }
            // retrieve attachment from the mail
            receiver.getInventory().addItem(mail.attachment);
            mail.attachment = new ItemStack(Material.AIR);
            // update the mail since it was claimed
            mails.put(mailId.toString(), mail.export());
            // inform player about mail content being claimed
            String message = RPGCore.inst().getLanguageManager().getTranslation("mail_was_claimed");
            receiver.sendMessage(message);
        }));
    }

    /**
     * Flag a mail as being read.
     *
     * @param receiver whose mailbox are we updating
     * @param mailId   identifier of the mail that was read
     */
    public void readMail(OfflinePlayer receiver, UUID mailId) {
        IDataAdapter adapter = RPGCore.inst().getDataManager().getDataAdapter();

        adapter.operateCustom(receiver.getUniqueId(), "mailbox", (mails -> {
            // ensure we got a mail to claim at all
            DataBundle mail_to_claim = mails.get(mailId.toString());
            if (mail_to_claim == null) {
                return;
            }
            // unwrap the mail entry we got
            CoreMail mail = new CoreMail(mail_to_claim);
            // mark mail as read
            mail.read = true;
            // update the mail since it was claimed
            mails.put(mailId.toString(), mail.export());
        }));
    }
}