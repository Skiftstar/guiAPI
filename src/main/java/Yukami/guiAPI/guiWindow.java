package Yukami.guiAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class guiWindow implements Listener {

    Map<ItemStack, guiItem> clickableItems = new HashMap<>();
    private Inventory inv;
    private Player p;
    private Main plugin;
    private int rows;
    private String name;
    private boolean fill = false;
    private Material fillMat = null;

    public guiWindow(Player p, String name, int rows, Main plugin) {
        this.p = p;
        this.plugin = plugin;
        this.name = name;
        this.rows = rows;
        if (rows > 6 || rows < 1) {
            rows = 6;
            Main.getInstance().console.sendMessage(ChatColor.RED + "Can't create a GUI with more than 6 or less than 1 rows! Defaulting to 6 rows!");
        }
        inv = Bukkit.createInventory(p, rows * 9, Util.Color(name));
    }

    public guiItem addItemStack(Material mat, String name, int slot) {
        guiItem item = new guiItem(this, mat, name, slot);
        clickableItems.put(item.getItemStack(), item);
        return item;
    }

    public void invSetup() {
        inv = Bukkit.createInventory(p, rows * 9, Util.Color(name));
        for (ItemStack is : clickableItems.keySet()) {
            guiItem item = clickableItems.get(is);
            if (item == null) {
                System.out.println("is definitiv null");
            }
            inv.setItem(item.getSlot(), item.getItemStack());
        }
    }

    public void setFillInv(Material mat) {
        fill = true;
        fillMat = mat;
    }

    public void open() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        invSetup();
        if (fill) {
            fillInv();
        }
        p.openInventory(inv);
    }

    public void fillInv() {
        ItemStack is = new ItemStack(fillMat);
        for (int i = 0; i < inv.getSize(); i++) {
            try {
                if (inv.getItem(i) == null || inv.getItem(i).getType().equals(Material.AIR)) {
                    inv.setItem(i, is);
                }
            } catch (NullPointerException e) {
                inv.setItem(i, is);
            }
        }
    }

    public void delete() {
        HandlerList.unregisterAll(this);
    }

    public void unregister() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        Inventory clicked = e.getClickedInventory();
        if (clicked == null) {
            return;
        }
        if (!e.getWhoClicked().equals(p)) {
            return;
        }
        if (clicked.equals(p.getInventory())) {
            e.setCancelled(true);
            return;
        }
        if (e.getAction().equals(InventoryAction.HOTBAR_SWAP)) {
            e.setCancelled(true);
            return;
        }
        if (!clicked.equals(inv)) {
            return;
        }
        ItemStack is = e.getCurrentItem();
        if (is == null || is.getType().equals(Material.AIR)) {
            return;
        }
        e.setCancelled(true);
        if (!clickableItems.containsKey(is)) {
            return;
        }
        clickableItems.get(is).onClick();
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent e) {
        if (!e.getPlayer().equals(p)) {
            return;
        }
        HandlerList.unregisterAll(this);
    }

    public Inventory getInv() {
        return inv;
    }
}
