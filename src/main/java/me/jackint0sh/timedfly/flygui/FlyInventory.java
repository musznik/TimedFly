package me.jackint0sh.timedfly.flygui;

import me.jackint0sh.timedfly.utilities.MessageUtil;
import me.jackint0sh.timedfly.versions.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import net.kyori.adventure.text.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class FlyInventory {

    private Inventory inventory;
    private Map<Integer, Item> items = new HashMap<>();
    public static Map<String, FlyInventory> inventories = new HashMap<>();
    private Consumer<InventoryCloseEvent> closeConsumer;

    public FlyInventory(int rows) {
        this(rows, null);
    }

    public FlyInventory(int rows, String title) {
        if (rows > 6) throw new IndexOutOfBoundsException("The number of rows should be be from 0-6.");
        this.inventory = Bukkit.createInventory(null, rows * 9, MessageUtil.color(title));
        FlyInventory.inventories.put(MessageUtil.color(title), this);
    }

    public FlyInventory(InventoryType inventoryType) {
        this(inventoryType, null);
    }

    public FlyInventory(InventoryType inventoryType, String title) {
      //  this.inventory = Bukkit.createInventory(null, inventoryType, MessageUtil.color(title));

        this.inventory = Bukkit.createInventory(null, inventoryType, Component.text(MessageUtil.color(title)));

        FlyInventory.inventories.put(MessageUtil.color(title), this);
    }

    public FlyInventory addItem(Item itemStack) {
        this.inventory.addItem(itemStack.build());
        this.items.put(items.size() - (items.size() == 0 ? 0 : 1), itemStack);
        return this;
    }

    public FlyInventory addItems(Item... itemStack) {
        //this.inventory.addItem(itemStack.build());
        for (Item item : itemStack) {
            this.inventory.addItem(item.build());
        }

        for (Item item : itemStack) this.items.put(items.size() - (items.size() == 0 ? 0 : 1), item);
        return this;
    }

    public void setItem(Item itemStack, int slot) {
        this.inventory.setItem(slot, itemStack.build());
        this.items.put(slot, itemStack);
    }

    public void setItem(Item itemStack, int x, int y) {
        if (x > 9) throw new IndexOutOfBoundsException("Coordinate X should be be from 0-9.");
        if (y > 6) throw new IndexOutOfBoundsException("Coordinate Y should be be from 0-6.");

        int location = x + (y * 9);

        if (this.inventory.getSize() < location) {
            throw new IndexOutOfBoundsException("Could not find the location provided on the inventory.");
        }

        this.inventory.setItem(location, itemStack.build());
        this.items.put(location, itemStack);
    }

    public FlyInventory setItems(Item... itemStack) {
        this.items.clear();
        for (int i = 0; i < itemStack.length; i++) {
            Item item = itemStack[i];
            this.inventory.setItem(i, item.build());
            this.items.put(i, item);
        }
        return this;
    }

    public Item getItem(int slot) {
        return this.items.get(slot);
    }

    public Item getItem(int x, int y) {
        return this.items.get(x + (y * 9));
    }

    public Map<Integer, Item> getItems() {
        return this.items;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public static FlyInventory getFlyInventory(String title) {
        return FlyInventory.inventories.get(MessageUtil.color(title));
    }

    public void clearInventory() {
        this.inventory.clear();
        this.items.clear();
    }

    public FlyInventory onClose(Consumer<InventoryCloseEvent> eventConsumer) {
        this.closeConsumer = eventConsumer;
        return this;
    }

    public void callEvent(Event e) {
        if (e instanceof InventoryCloseEvent) {
            Arrays.stream(this.getInventory().getContents())
                    .filter(Objects::nonNull)
                    .filter(itemStack -> !ServerVersion.INSTANCE.hasTag(itemStack, "customitem"))
                    .forEach(item -> {
                        Player player = (Player) ((InventoryCloseEvent) e).getPlayer();
                        Map<Integer, ItemStack> contents = player.getInventory().addItem(item);
                        if (!contents.isEmpty()) player.getWorld().dropItem(player.getLocation(), item);
                    });
            if (this.closeConsumer != null){
                this.closeConsumer.andThen(event -> this.clearInventory()).accept((InventoryCloseEvent) e);
            }
        }
    }
}
