package me.blutkrone.rpgcore.chat;

import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.item.styling.StylingRule;
import me.blutkrone.rpgcore.item.styling.descriptor.PlayerDescriptor;
import me.blutkrone.rpgcore.util.io.ConfigWrapper;
import me.blutkrone.rpgcore.util.io.FileUtil;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class ChatManager implements Listener {

    // whether to allow rpgcore to do chat manipulation
    private boolean enabled;
    // override chat format if defined
    private String chat_format = "";
    // rate limit applied to chat
    private int oldest = -1;
    private List<Integer[]> rate_limits = new ArrayList<>();
    private Map<UUID, NavigableSet<Long>> rate_limit_tracker = new ConcurrentHashMap<>();
    // regular snapshot of player info
    private Map<UUID, PlayerSnapshot> player_snapshot = new ConcurrentHashMap<>();
    private Map<UUID, ItemStack> holding_snapshot = new ConcurrentHashMap<>();

    public ChatManager() {
        this.enabled = RPGCore.inst().getVolatileManager().getMajorVersion() >= 19;
        if (this.enabled) {
            try {
                ConfigWrapper config = FileUtil.asConfigYML(FileUtil.file("chat.yml"));
                this.enabled = config.getBoolean("use-core-format", false);
                this.chat_format = config.getString("custom-format", "");
                this.chat_format = ChatColor.translateAlternateColorCodes('&', this.chat_format);
                for (String limit : config.getStringList("rate-limitation")) {
                    String[] split = limit.split("\\:");
                    this.rate_limits.add(new Integer[] {Integer.parseInt(split[0]), Integer.parseInt(split[1])*1000});
                }
                for (Integer[] rate_limit : rate_limits) {
                    this.oldest = Math.max(oldest, rate_limit[1]);
                }
                if (this.enabled) {
                    Bukkit.getPluginManager().registerEvents(this, RPGCore.inst());
                    Bukkit.getScheduler().runTaskTimer(RPGCore.inst(), () -> {
                        player_snapshot.clear();
                        holding_snapshot.clear();
                    }, 1, 60);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Whether the custom RPGCore chat format is enabled.
     *
     * @return Enable RPGCore chat format.
     */
    public boolean isEnabled() {
        return enabled;
    }

    private List<BaseComponent> getMessageHeader(Player bukkit_sender, String format) {
        // snapshot holding player information
        PlayerSnapshot snapshot = this.player_snapshot.computeIfAbsent(bukkit_sender.getUniqueId(), PlayerSnapshot::new);

        // construct the header of the message
        String header = format.substring(0, format.indexOf("%2$s"));
        header = header.replace("%1$s", bukkit_sender.getDisplayName());
        header = header.replace("%3$s", "");
        header = header.replace("%4$s", String.valueOf(snapshot.level));
        header = header.replace("%5$s", bukkit_sender.getName());

        // identify what style to build tooltip with
        StylingRule styling = RPGCore.inst().getItemManager().getStylingRule("chat_" + snapshot.portrait);
        if (styling == null) {
            styling = RPGCore.inst().getItemManager().getStylingRule("chat_nothing");
        }

        // compile a header segment
        ComponentBuilder header_builder = new ComponentBuilder(header);
        List<BaseComponent> header_parts = header_builder.getParts();

        // tooltip to provide player information
        if (styling != null) {
            ItemStack describe = PlayerDescriptor.describe(snapshot, styling);
            String item_info = RPGCore.inst().getVolatileManager().getItemTagAsJSON(describe);
            for (BaseComponent component : header_parts) {
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rpg social " + bukkit_sender.getUniqueId()));
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{ new TextComponent(item_info) }));
            }
        }

        return Arrays.asList(header_builder.create());
    }

    private List<BaseComponent> getMessageBody(String message, ItemStack holding) {
        // construct following message
        if (message.contains("%item%")) {
            // pull item name from backing description
            String item_name = RPGCore.inst().getItemManager().getItemFrom(holding)
                    .map(core_item -> {
                        List<String> lore = RPGCore.inst().getLanguageManager().getTranslationList(core_item.getLCText());
                        return lore.isEmpty() ? "name is missing" : lore.remove(0);
                    }).orElse("");
            if (item_name.equals("")) {
                ItemMeta meta = holding.getItemMeta();
                if (meta != null) {
                    // fallback to snapshot internal name
                    PersistentDataContainer data = meta.getPersistentDataContainer();
                    item_name = data.getOrDefault(new NamespacedKey(RPGCore.inst(), "rpgcore-name"), PersistentDataType.STRING, "");
                    // fallback to displayname if absent
                    if (meta.hasDisplayName() && !meta.getDisplayName().isBlank()) {
                        item_name = meta.getDisplayName();
                    }
                }
            }
            // use reflected item to strip complex item data
            if (!RPGCore.inst().getHUDManager().getEquipMenu().isReflected(holding)) {
                holding = RPGCore.inst().getHUDManager().getEquipMenu().reflect(holding);
            }
            String item_info = RPGCore.inst().getVolatileManager().getItemTagAsJSON(holding);
            // prepare the message to put thorough
            message = ChatColor.RESET + message + ChatColor.RESET;
            String[] broken = message.split(Pattern.quote("%item%"));
            // pool up the message
            boolean only_apply_to_first = !item_name.isBlank();
            List<BaseComponent> components = new ArrayList<>();
            components.addAll(Arrays.asList(new ComponentBuilder(broken[0]).create()));
            for (int i = 1; i < broken.length; i++) {
                // insert item link
                if (only_apply_to_first) {
                    TextComponent component = new TextComponent("");
                    component.setExtra(Arrays.asList(TextComponent.fromLegacyText("§f[" + item_name + "§f]")));
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{ new TextComponent(item_info) }));
                    components.add(component);
                    only_apply_to_first = false;
                } else {
                    components.add(new TextComponent("[???]"));
                }
                // insert message component
                components.addAll(Arrays.asList(new ComponentBuilder(broken[i]).create()));
            }

            return components;
        } else {
            BaseComponent[] components = new ComponentBuilder(message).getParts().toArray(new BaseComponent[0]);
            return Arrays.asList(components);
        }
    }

    /*
     * Perform internal flow of RPGCore based chat
     */
    private void doChatFlow(String message, String format, Player bukkit_sender, ItemStack holding) {
        CorePlayer core_sender = RPGCore.inst().getEntityManager().getPlayer(bukkit_sender);

        // update the channel selection
        String channel = core_sender.getLastChatChannel();
        if (message.startsWith("@")) {
            channel = message.split(" ")[0];
        } else if (message.startsWith("%")) {
            channel = "party";
            message = message.substring(1);
        } else if (message.startsWith("#")) {
            channel = "global";
            message = message.substring(1);
        }
        core_sender.setLastChatChannel(channel);

        // no message without body
        if (message.isBlank()) {
            return;
        }

        // prepare the message to be deployed
        List<BaseComponent> output = new ArrayList<>();
        try {
            // construct leading title
            output.addAll(this.getMessageHeader(bukkit_sender, format));
            output.addAll(this.getMessageBody(message, holding));
        } catch (Exception ex) {
            ex.printStackTrace();
            output.add(new TextComponent("[RPGCore] Unexpected chat error, malformed chat format?"));
        }
        // todo: preview prior to message
        BaseComponent[] array = output.toArray(new BaseComponent[0]);
        // deploy message via social handler
        RPGCore.inst().getSocialManager().getPlayerHandler().talk(bukkit_sender, channel, array);
    }

    /*
     * Check if the player matches the rate limitation, do note that
     * this will also count to the rate limit.
     *
     * @param player Whose rate limit to check
     */
    private boolean checkRateLimit(Player player) {
        // no rate limit means always pass
        if (rate_limits.isEmpty()) {
            return true;
        }

        // get rid of anything too old
        NavigableSet<Long> timestamps = rate_limit_tracker.computeIfAbsent(player.getUniqueId(),
                (k -> Collections.synchronizedNavigableSet(new TreeSet<>())));
        timestamps.subSet(0L, System.currentTimeMillis() - oldest).clear();
        for (Integer[] rate_limit : rate_limits) {
            // check if messages sent in past #s exceeds threshold
            int count = timestamps.subSet(System.currentTimeMillis()-rate_limit[1], System.currentTimeMillis()).size();
            if (count < rate_limit[0]) {
                continue;
            }
            // inform about failing rate limit
            RPGCore.inst().getLanguageManager().sendMessage(player, "chat_rate_limit_exceed");
            return false;
        }
        // track a timestamp for the message
        timestamps.add(System.currentTimeMillis());
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        CorePlayer core_player = RPGCore.inst().getEntityManager().getPlayer(event.getPlayer());
        if (this.isEnabled() && core_player != null) {
            // we deploy it ourselves, fire no event
            event.setCancelled(true);
            // verify rate limiter
            if (!checkRateLimit(event.getPlayer())) {
                return;
            }
            // prepare basic message information
            String message = event.getMessage();
            String format = this.chat_format.isEmpty() ? event.getFormat() : this.chat_format;
            Player sender = event.getPlayer();
            // verify snapshot information
            ItemStack snapshot_holding = this.holding_snapshot.get(sender.getUniqueId());
            // re-build snapshots before deploying
            if (event.getMessage().contains("%item%") && snapshot_holding == null) {
                Bukkit.getScheduler().runTask(RPGCore.inst(), () -> {
                    // retrieve or snapshot the item
                    ItemStack item = this.holding_snapshot.computeIfAbsent(sender.getUniqueId(), (k -> {
                        return sender.getInventory().getItemInMainHand().clone();
                    }));
                    // prepare dispatch asynchronously
                    Bukkit.getScheduler().runTaskAsynchronously(RPGCore.inst(), () -> {
                        doChatFlow(message, format, sender, item);
                    });
                });
            } else {
                Bukkit.getScheduler().runTaskAsynchronously(RPGCore.inst(), () -> {
                    doChatFlow(message, format, sender, snapshot_holding);
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onClean(PlayerQuitEvent event) {
        rate_limit_tracker.remove(event.getPlayer().getUniqueId());
    }

}
