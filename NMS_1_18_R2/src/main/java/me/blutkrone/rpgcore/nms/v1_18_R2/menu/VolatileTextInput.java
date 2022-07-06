package me.blutkrone.rpgcore.nms.v1_18_R2.menu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.blutkrone.rpgcore.nms.api.menu.ITextInput;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.*;
import net.md_5.bungee.chat.*;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.ContainerAccess;
import net.minecraft.world.inventory.ContainerAnvil;
import net.minecraft.world.inventory.Containers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

public class VolatileTextInput implements ITextInput {

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

    private final JavaPlugin plugin;
    private final Player player;

    private Consumer<String> response = (text) -> {
    };
    private Consumer<String> ticker = (text) -> {
    };
    private Consumer<String> updater = (text) -> {
    };

    private NMSAnvil nms_anvil;
    private Inventory bukkit_anvil;
    private String last_input = "";

    public VolatileTextInput(JavaPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;

        // initialize the anvil menu backing us up.
        EntityPlayer playerHandle = ((CraftPlayer) player).getHandle();
        this.nms_anvil = new NMSAnvil(playerHandle, playerHandle.nextContainerCounter());
        this.bukkit_anvil = this.nms_anvil.getBukkitView().getTopInventory();
    }

    public void tick() {
        String current = this.nms_anvil.getText();

        this.ticker.accept(current);
        if (!this.last_input.equals(current)) {
            this.updater.accept(current);
            this.last_input = current;
        }
    }

    @Override
    public void stalled(Runnable runnable) {
        Bukkit.getScheduler().runTask(this.plugin, runnable);
    }

    @Override
    public Player getViewer() {
        return this.player;
    }

    @Override
    public void setItemAt(int slot, ItemStack item) {
        this.bukkit_anvil.setItem(slot, item);
    }

    @Override
    public ItemStack getItemAt(int slot) {
        ItemStack item = this.bukkit_anvil.getItem(slot);
        return item == null ? new ItemStack(Material.AIR) : item;
    }

    @Override
    public void setResponse(Consumer<String> response) {
        this.response = response;
    }

    @Override
    public void setTicking(Consumer<String> ticker) {
        this.ticker = ticker;
    }

    @Override
    public void setUpdating(Consumer<String> updater) {
        this.updater = updater;
    }

    @Override
    public void open() {
        EntityPlayer nmsPlayer = ((CraftPlayer) this.player).getHandle();
        // ensure that previous inventories are closed
        CraftEventFactory.handleInventoryCloseEvent(nmsPlayer);
        nmsPlayer.bV = nmsPlayer.bU;
        // open the anvil menu for the player
        nmsPlayer.b.a(new PacketPlayOutOpenWindow(this.nms_anvil.j, Containers.h, new ChatComponentText("opened without a title!")));
        nmsPlayer.bV = this.nms_anvil;
        nmsPlayer.a(this.nms_anvil);
    }

    @Override
    public void setTitle(BaseComponent[] title) {
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            if (this.player == null)
                return;
            // ensure this menu is still the one opened
            if (this.player.getOpenInventory().getTopInventory() != this.bukkit_anvil)
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
    public void conclude() {
        String response = this.nms_anvil.getText();
        if (response == null) {
            response = "";
        }

        this.response.accept(response.trim());
    }

    @Override
    public void setCurrent(String content) {
        this.nms_anvil.v = content;
    }

    public class BukkitAnvil extends CraftInventoryAnvil {

        private final ContainerAnvil container;

        BukkitAnvil(Location location, IInventory inventory, IInventory resultInventory, ContainerAnvil container) {
            super(location, inventory, resultInventory, container);
            this.container = container;
        }

        public ContainerAnvil getContainer() {
            return container;
        }

        public ITextInput getVolatile() {
            return VolatileTextInput.this;
        }
    }

    public class NMSAnvil extends ContainerAnvil {

        private final CraftInventoryView craftInventoryView;

        NMSAnvil(EntityPlayer player, int containerId) {
            super(containerId, player.fr(), ContainerAccess.a(player.s, new BlockPosition(0, 0, 0)));
            // bukkit accessor for anvil
            BukkitAnvil bukkitAnvil = new BukkitAnvil(super.q.getLocation(), super.p, super.o, this);
            this.craftInventoryView = new CraftInventoryView(super.r.getBukkitEntity(), bukkitAnvil, this);
            // virtual anvil can be opened anywhere
            super.checkReachable = false;
            // cap repair cost at zero
            super.maximumRepairCost = 0;
        }

        public String getText() {
            return super.v == null ? "" : super.v;
        }

        @Override
        public CraftInventoryView getBukkitView() {
            return craftInventoryView;
        }

        public ITextInput getVolatile() {
            return VolatileTextInput.this;
        }

        @Override
        public void l() {
            super.l();
            this.w.a(0);
        }

        @Override
        public void b(EntityHuman player) {
        }

        @Override
        protected void a(EntityHuman player, IInventory container) {
        }
    }
}
