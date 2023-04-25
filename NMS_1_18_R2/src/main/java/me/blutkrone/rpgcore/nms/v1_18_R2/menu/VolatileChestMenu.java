package me.blutkrone.rpgcore.nms.v1_18_R2.menu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.blutkrone.rpgcore.nms.api.menu.IChestMenu;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.*;
import net.md_5.bungee.chat.*;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.Containers;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftInventoryCustom;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.function.Consumer;

public class VolatileChestMenu extends CraftInventoryCustom implements IChestMenu {

    private static final Gson gson = new GsonBuilder().
            registerTypeAdapter(BaseComponent.class, new ComponentSerializer()).
            registerTypeAdapter(TextComponent.class, new TextComponentSerializer()).
            registerTypeAdapter(TranslatableComponent.class, new TranslatableComponentSerializer()).
            registerTypeAdapter(KeybindComponent.class, new KeybindComponentSerializer()).
            registerTypeAdapter(ScoreComponent.class, new ScoreComponentSerializer()).
            registerTypeAdapter(SelectorComponent.class, new SelectorComponentSerializer()).
            registerTypeAdapter(Entity.class, new EntitySerializer()).
            registerTypeAdapter(Text.class, new TextSerializer()).
            registerTypeAdapter(Item.class, new ItemSerializer()).
            registerTypeAdapter(ItemTag.class, new ItemTag.Serializer()).
            create();

    private Player player;
    private JavaPlugin plugin;

    // delegated event handlers
    private Consumer<InventoryClickEvent> click_event_handler;
    private Consumer<InventoryDragEvent> drag_event_handler;
    private Consumer<InventoryOpenEvent> open_event_handler;
    private Consumer<InventoryCloseEvent> close_event_handler;
    private Consumer<InventoryCreativeEvent> creative_event_handler;
    private Runnable rebuild_handler;
    // ticker, only while menu is being viewed
    private Runnable ticking_handler;
    // used to not send packets (interaction detection fails.)
    private BaseComponent[] last_title = null;
    // handle used by the core
    private Object core_handle;

    public VolatileChestMenu(JavaPlugin plugin, int size, Player player, Object core_handle) {
        super(null, size, "opened without a title!");
        this.core_handle = core_handle;
        this.player = player;
        this.plugin = plugin;
        this.click_event_handler = (event -> {
        });
        this.drag_event_handler = (event -> {
            // if a 1-slot drag, emulate as a click
            if (event.getInventorySlots().size() == 1) {
                InventoryView view = event.getView();
                InventoryType.SlotType type = InventoryType.SlotType.CONTAINER;
                int slot = event.getRawSlots().iterator().next();
                ClickType click = event.getType() == DragType.EVEN ? ClickType.RIGHT : ClickType.LEFT;
                InventoryClickEvent fake = new InventoryClickEvent(view, type, slot, click, InventoryAction.UNKNOWN);
                this.on(fake);
                if (fake.isCancelled()) {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        });
        this.open_event_handler = (event -> rebuild_handler.run());
        this.close_event_handler = (event -> {
        });
        this.creative_event_handler = (event -> event.setCancelled(true));
        this.ticking_handler = (() -> {
        });
        this.rebuild_handler = (() -> {
        });
    }

    public void tick() {
        this.ticking_handler.run();
    }

    @Override
    public Player getViewer() {
        return this.player;
    }

    @Override
    public void stalled(Runnable runnable) {
        Bukkit.getScheduler().runTask(this.plugin, runnable);
    }

    @Override
    public void clearItems() {
        this.clear();
    }

    @Override
    public void queryRebuild() {
        Bukkit.getScheduler().runTask(this.plugin, () -> this.rebuild_handler.run());
    }

    @Override
    public void setRebuilder(Runnable rebuilder) {
        this.rebuild_handler = rebuilder;
    }

    @Override
    public void setTitle(BaseComponent[] title) {
        // prevent update with same title, the client will
        // not be able to detect any clicks when we do not
        // provide any gaps.
        if (Arrays.equals(this.last_title, title)) {
            return;
        }
        this.last_title = title;

        Bukkit.getScheduler().runTask(this.plugin, () -> {
            if (this.player == null)
                return;
            // ensure this menu is still the one opened
            if (this.player.getOpenInventory().getTopInventory() != this)
                return;
            // transform components into basic json
            String bukkit_to_json = gson.toJson(title);
            IChatBaseComponent component = CraftChatMessage.fromJSON(bukkit_to_json);
            // retrieve the nms equivalent backing the menu
            Container active_container = ((CraftPlayer) player).getHandle().bV;
            int window_id = active_container.j;
            Containers<?> container = active_container.a();
            // process the update call we expected
            PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(window_id, container, component);
            ((CraftPlayer) this.player).getHandle().b.a(packet);
            this.player.updateInventory();
        });
    }

    @Override
    public void setTickingHandler(Runnable ticking_handler) {
        this.ticking_handler = ticking_handler;
    }

    @Override
    public void setClickHandler(Consumer<InventoryClickEvent> handler) {
        this.click_event_handler = handler;
    }

    @Override
    public void setDragHandler(Consumer<InventoryDragEvent> handler) {
        this.drag_event_handler = handler;
    }

    @Override
    public void setOpenHandler(Consumer<InventoryOpenEvent> handler) {
        this.open_event_handler = handler;
    }

    @Override
    public void setCloseHandler(Consumer<InventoryCloseEvent> handler) {
        this.close_event_handler = handler;
    }

    @Override
    public void setItemAt(int slot, ItemStack item) {
        setItem(slot, item);
    }

    @Override
    public ItemStack getItemAt(int slot) {
        ItemStack item = getItem(slot);
        if (item == null) {
            item = new ItemStack(Material.AIR);
        }
        return item;
    }

    @Override
    public void open() {
        this.last_title = null;
        this.player.openInventory(this);
    }

    @Override
    public Object getLinkedHandle() {
        return core_handle;
    }

    public void on(InventoryClickEvent event) {
        this.click_event_handler.accept(event);
    }

    public void on(InventoryOpenEvent event) {
        this.last_title = null;
        this.open_event_handler.accept(event);
    }

    public void on(InventoryCloseEvent event) {
        this.last_title = null;
        this.close_event_handler.accept(event);
    }

    public void on(InventoryDragEvent event) {
        this.drag_event_handler.accept(event);
    }

    public void on(InventoryCreativeEvent event) {
        this.creative_event_handler.accept(event);
    }
}
