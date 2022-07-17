package me.blutkrone.rpgcore.mail;

import me.blutkrone.rpgcore.data.DataBundle;
import me.blutkrone.rpgcore.util.io.BukkitSerialization;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An instance of a mail, do note that this is a snapshot that
 * is not backed by the database - changes will not persist!
 */
public class CoreMail {

    // subject for mail
    String subject;
    // info on who sent the mail
    String sender;
    // info on whether mail is new
    boolean read;
    // timestamp of when the mail was sent
    long timestamp;
    // what item to attach into the mail
    ItemStack attachment;
    // extra text in the mail
    List<String> contents = new ArrayList<>();

    /**
     * Construct a mail on runtime.
     *
     * @param subject    subject of the mail
     * @param sender     who sent the mail (Only for readability!)
     * @param attachment item attached to this mail
     * @param contents   additional contents of the mail
     */
    public CoreMail(String subject, String sender, ItemStack attachment, String... contents) {
        this.subject = subject == null ? "Untitled" : subject;
        this.sender = sender == null ? "RPGCore" : sender;
        this.read = false;
        this.timestamp = System.currentTimeMillis();
        this.attachment = attachment == null ? new ItemStack(Material.AIR) : attachment;
        this.contents = Arrays.asList(contents);
    }

    /**
     * Deserialize a mail that was saved in the data adapter.
     *
     * @param bundle serialized mail
     */
    public CoreMail(DataBundle bundle) {
        // retrieve subject of mail
        this.subject = bundle.getString(0);
        // retrieve info of who sent mail
        this.sender = bundle.getString(1);
        // retrieve info if mail was read
        this.read = bundle.getBoolean(2);
        // timestamp of when the mail was sent
        this.timestamp = bundle.getNumber(3).longValue();
        // retrieve item attached to mail
        try {
            String b64 = bundle.getString(4);
            this.attachment = BukkitSerialization.fromBase64(b64)[0];
        } catch (IOException e) {
            this.attachment = new ItemStack(Material.AIR);
        }

        // retrieve additional text in mail
        int size = bundle.getNumber(5).intValue();
        for (int i = 0; i < size; i++) {
            this.contents.add(bundle.getString(6 + i));
        }
    }

    /**
     * Subject line of this mail.
     *
     * @return mail subject
     */
    public String getSubject() {
        return this.subject;
    }

    /**
     * Hint on who sent the mail, accuracy not guaranteed.
     *
     * @return mail sender
     */
    public String getSender() {
        return this.sender;
    }

    /**
     * Whether this mail has been read by the receiver.
     *
     * @return mail was read
     */
    public boolean isRead() {
        return this.read;
    }

    /**
     * A UNIX timestamp of when the mail was instantiated.
     *
     * @return instantiation date of mail
     */
    public long getTimestamp() {
        return this.timestamp;
    }

    /**
     * Attachment on the mail.
     *
     * @return who is attached to the mail, read-only.
     */
    public ItemStack getAttachment() {
        return new ItemStack(this.attachment);
    }

    /**
     * Contents on the mail.
     *
     * @return additional mail contents, read-only.
     */
    public List<String> getContents() {
        return new ArrayList<>(this.contents);
    }

    /**
     * Export into a bundle which can persist.
     *
     * @return export result.
     */
    public DataBundle export() {
        DataBundle bundle = new DataBundle();
        bundle.addString(this.subject);
        bundle.addString(this.sender);
        bundle.addBoolean(this.read);
        bundle.addNumber(this.timestamp);
        bundle.addString(BukkitSerialization.toBase64(this.attachment));

        bundle.addNumber(this.contents.size());
        for (String content : this.contents) {
            bundle.addString(content);
        }

        return bundle;
    }
}
