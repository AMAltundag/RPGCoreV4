package me.blutkrone.rpgcore.nms.v1_19_R3.menu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.blutkrone.rpgcore.nms.api.menu.ITextInput;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.*;
import net.md_5.bungee.chat.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

public class VolatileTextInput  implements ITextInput {

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
        ServerPlayer playerHandle = ((CraftPlayer) player).getHandle();
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
        ServerPlayer nmsPlayer = ((CraftPlayer) this.player).getHandle();
        // ensure that previous inventories are closed
        CraftEventFactory.handleInventoryCloseEvent(nmsPlayer);
        nmsPlayer.containerMenu = nmsPlayer.inventoryMenu;
        // open the anvil menu for the player
        nmsPlayer.connection.send(new ClientboundOpenScreenPacket(this.nms_anvil.containerId, MenuType.ANVIL, Component.literal("opened without a title!")));
        nmsPlayer.containerMenu = this.nms_anvil;
        nmsPlayer.initMenu(this.nms_anvil);
    }

    @Override
    public void setTitle(BaseComponent[] title) {
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            if (this.player == null || !this.player.isValid())
                return;
            // ensure this menu is still the one opened
            if (this.player.getOpenInventory().getTopInventory() != this.bukkit_anvil)
                return;
            // transform components into basic json
            String bukkit_to_json = gson.toJson(title);
            Component component = CraftChatMessage.fromJSON(bukkit_to_json);
            // retrieve the nms equivalent backing the menu
            AbstractContainerMenu active_container = ((CraftPlayer) player).getHandle().containerMenu;
            int window_id = active_container.containerId;
            // process the update call we expected
            ((CraftPlayer) this.player).getHandle().connection.send(new ClientboundOpenScreenPacket(window_id, MenuType.ANVIL, component));
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
        this.nms_anvil.itemName = content;
    }

    public class BukkitAnvil extends CraftInventoryAnvil {

        private final AnvilMenu container;

        BukkitAnvil(Location location, Container input, Container result, AnvilMenu container) {
            super(location, input, result, container);
            this.container = container;
        }

        public AnvilMenu getContainer() {
            return container;
        }

        public ITextInput getVolatile() {
            return VolatileTextInput.this;
        }
    }

    public class NMSAnvil extends AnvilMenu {

        private final CraftInventoryView craftInventoryView;

        NMSAnvil(ServerPlayer player, int containerId) {
            super(containerId, player.getInventory(), ContainerLevelAccess.create(player.level, new BlockPos(0, 0, 0)));
            // bukkit accessor for anvil
            BukkitAnvil bukkitAnvil = new BukkitAnvil(super.access.getLocation(), super.inputSlots, super.resultSlots, this);
            this.craftInventoryView = new CraftInventoryView(player.getBukkitEntity(), bukkitAnvil, this);
            // virtual anvil can be opened anywhere
            super.checkReachable = false;
            // cap repair cost at zero
            super.maximumRepairCost = 0;
            Bukkit.getLogger().info("remove cost via injecting cost object");
        }

        public String getText() {
            return super.itemName == null ? "" : super.itemName;
        }

        @Override
        public CraftInventoryView getBukkitView() {
            return craftInventoryView;
        }

        public ITextInput getVolatile() {
            return VolatileTextInput.this;
        }

        @Override
        public void createResult() {
            super.createResult();
        }

        @Override
        public void removed(net.minecraft.world.entity.player.Player player) {
        }

        @Override
        protected void clearContainer(net.minecraft.world.entity.player.Player entityhuman, Container iinventory) {
        }
    }
}