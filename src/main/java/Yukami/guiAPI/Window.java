package Yukami.guiAPI;

import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.Hash;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

abstract class Window {
    String windowTitle = null;
    Consumer<InventoryClickEvent> onPlayerInvClick = null;
    Player p = null;
    Map<ItemStack, GuiItem> clickableItems = new HashMap<>();

    public abstract void setOnPlayerInvClick(Consumer<InventoryClickEvent> function);

    abstract void onInvClick(InventoryClickEvent e);

    abstract void onInvClose(InventoryCloseEvent e);

    public abstract void removeItem(GuiItem item);

    abstract void open();

    public abstract GuiItem setItemStack(Material mat, String name, int slot, Integer... pageArgs);

    public abstract GuiItem setItemStack(ItemStack is, int slot, Integer... pageArgs);

    public abstract void unregister();

    public abstract ItemStack[] getItems();

    public String getWindowTitle() {
        return windowTitle;
    }

    abstract void update(int... PageArgs);

    public abstract Inventory getInv();
}
