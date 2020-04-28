package Yukami.guiAPI;

import com.sun.istack.internal.Nullable;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.util.HashMap;
import java.util.Map;

public class guiWindow implements Listener {

    private Map<ItemStack, guiItem> clickableItems = new HashMap<>();
    private Map<ItemStack, guiItem> borderItems = new HashMap<>();
    private Inventory inv;
    private Player p;
    private int rows;
    private String name;
    private boolean fill = false;
    private JavaPlugin plugin;
    private Material fillMat = null;
    private WindowType type;

    /**
     * Creates a Window (an inventory) that can be further customized
     * @param p Player of that the inv will be opened to
     * @param name title of the inventory
     * @param rows Amount of rows, min. 1, max. 6
     * @param type WindowType, normal or split
     * @param plugin Plugin reference
     */
    public guiWindow(Player p, String name, int rows, WindowType type, JavaPlugin plugin) {
        this.p = p;
        this.name = name;
        this.plugin = plugin;
        this.rows = rows;
        this.type = type;
        if (rows > 6 || rows < 1) {
            rows = 6;
        }
        // Default border
        if (type.equals(WindowType.SPLIT_2)) {
            for (int i = 0; i < rows; i++) {
                guiItem item = new guiItem(this, Material.WHITE_STAINED_GLASS_PANE, 4 + (i * 9));
                borderItems.put(item.getItemStack(), item);
            }
        }
        inv = Bukkit.createInventory(p, rows * 9, Util.Color(name));
    }


    /**
     * If the WindowType is set to Split_2 or Split_4, you can change the border material and name
     * If name is null, the item name will be used
     * @param mat Material of the border
     * @param name Name of the Border
     */
    public void setBorder(Material mat, String name) {
        if (!(type.equals(WindowType.SPLIT_2) || type.equals(WindowType.SPLIT_4))) {
            return;
        }
        borderItems.clear();
        if (name == null) {
            for (int i = 0; i < rows; i++) {
                guiItem item = new guiItem(this, mat, 4 + (i * 9));
                borderItems.put(item.getItemStack(), item);
            }
        } else {
            for (int i = 0; i < rows; i++) {
                guiItem item = new guiItem(this, mat, name, 4 + (i * 9));
                borderItems.put(item.getItemStack(), item);
            }
        }
    }

    /**
     * adds an ItemStack to the window
     * @param mat Material of the Item
     * @param name Name of the Item
     * @param slot Slot the item is in (does not work if a border item is on that slot)
     * @return Returns the created <b>guiItem</b>
     */
    public guiItem addItemStack(Material mat, String name, int slot) {
        guiItem item = new guiItem(this, mat, name, slot);
        clickableItems.put(item.getItemStack(), item);
        return item;
    }

    /**
     * creates the inventory and adds all items to it
     */
    private void invSetup() {
        inv = Bukkit.createInventory(p, rows * 9, Util.Color(name));
        for (ItemStack is : clickableItems.keySet()) {
            guiItem item = clickableItems.get(is);
            inv.setItem(item.getSlot(), item.getItemStack());
        }
        for (ItemStack is : borderItems.keySet()) {
            guiItem item = borderItems.get(is);
            inv.setItem(item.getSlot(), item.getItemStack());
        }
    }

    /**
     * Set the FillMaterial for all empty slots of the inventory
     * @param mat Material of the fill
     */
    public void setFillInv(Material mat) {
        fill = true;
        fillMat = mat;
    }

    /**
     * opens in the inventory
     */
    void open() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        invSetup();
        if (fill) {
            fillInv();
        }
        p.openInventory(inv);
    }


    /**
     * Fills the empty slots of the inventory with the preassigned item
     */
    void fillInv() {
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

    /**
     * Unregisters the window from the Listeners so that the reference can be dumped
     */
    public void delete() {
        HandlerList.unregisterAll(this);
    }

    /**
     * unregisters the window, useful if you want to save on resources if the window has no need to listens to events for some time
     */
    public void unregister() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    private void onInvClick(InventoryClickEvent e) {
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
    private void onInvClose(InventoryCloseEvent e) {
        if (!e.getPlayer().equals(p)) {
            return;
        }
        HandlerList.unregisterAll(this);
    }

    /**
     *
     * @return the inventory
     */
    public Inventory getInv() {
        return inv;
    }
}
