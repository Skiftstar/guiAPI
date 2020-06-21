package Yukami.guiAPI;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

abstract class Window {
    String windowTitle = null;
    Player p = null;
    Map<ItemStack, GuiItem> clickableItems = new HashMap<>();

    abstract void onInvClick(InventoryClickEvent e);

    abstract void onInvClose(InventoryCloseEvent e);

    public abstract void removeItem(GuiItem item);

    abstract void open();

    public abstract GuiItem setItemStack(Material mat, String name, int slot, Integer... pageArgs);

    public abstract GuiItem setItemStack(ItemStack is, int slot, Integer... pageArgs);

    public abstract void unregister();

    public String getWindowTitle() {
        return windowTitle;
    }

    abstract void update(int... PageArgs);

    public abstract Inventory getInv();
}
