package Yukami.guiAPI;

import com.sun.istack.internal.Nullable;
import org.bukkit.Bukkit;
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

import javax.swing.*;
import java.util.*;

public class guiWindow implements Listener {

    Map<ItemStack, guiItem> clickableItems = new HashMap<>();
    private Inventory inv;
    private Player p;
    private int rows;
    private int slots;
    private String name;
    private boolean fill = false;
    private JavaPlugin plugin;
    private Material fillMat = null, borderMat = null;
    private String fillName = null, borderName = null;
    private WindowType type;
    private int currPage = 1;
    private Map<Integer, List<guiItem>> pages = new HashMap<>();
    private boolean usePages = false;

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
            this.rows = 6;
        }
        slots = rows * 9;
        inv = Bukkit.createInventory(p, rows * 9, Util.Color(name));
    }

    /**
     * adds an ItemStack to the inventory or to a specific window.
     * If an item is in the same slot as this item it gets replaced.
     * @param mat Material of the Item
     * @param name Name of the Item
     * @param slot Slot the item is in (does not work if a border item is on that slot)
     * @param pageArgs <b>[Optional]</b> page that the item will be on (enable pages first!)
     * @return Returns the created <b>guiItem</b>
     */
    public guiItem setItemStack(Material mat, String name, int slot, Integer... pageArgs) {
        guiItem item;
        List<guiItem> pageItems = new ArrayList<>();
        if (pageArgs != null) {
            // Prevents saving Items where they won't be used
            if (!usePages) {
                System.out.println(Util.Color("&c[guiAPI] You tried adding an item to a page but didn't enable pages first!\nMake sure you enable pages for this window before adding items to pages!"));
                return null;
            }
            int page = pageArgs[0];
            // If the page doesn't exist yet
            if (pages.size() < page) {
                return null;
            }
            // Get the items on the page
            pageItems = pages.get(page);
            // Check if any Item is already in that slot, if so, remove it
            for (int i = 0; i < pageItems.size(); i++) {
                if (pageItems.get(i).getSlot() == slot) {
                    clickableItems.remove(pageItems.get(i).getItemStack());
                    pageItems.remove(i);
                    break;
                }
            }
        } else {
            // You need a different check for preexisting items when there is no page provided.
            ItemStack remove = null;
            // Loop through the clickable items (as they are all on one page we can actually do that.) and just check the slots
            for (ItemStack is : clickableItems.keySet()) {
                if (clickableItems.get(is).getSlot() == slot) {
                    remove = is;
                    break;
                }
            }
            if (remove != null) {
                clickableItems.remove(remove);
            }
        }
        // Create the item and add it to the page if a page is provided.
        item = new guiItem(this, mat, name, slot);
        if (pageArgs != null) {
            pageItems.add(item);
            pages.replace(pageArgs[0], pages.get(pageArgs[0]), pageItems);
        }
        // add it as a clickable item and return it
        clickableItems.put(item.getItemStack(), item);
        return item;
    }

    /**
     * Adds an item to the inventory or a specific page (if one is provided)
     *
     * @param mat Material of the Item
     * @param name Name of the Item
     * @param pageArgs <b>[Optional]</b> page the item will be on (enable pages first!)
     * @return the created Item
     */
    public guiItem addItemStack(Material mat, String name, Integer... pageArgs) {
        guiItem item;
        if (pageArgs != null) {
            if (!usePages) {
                System.out.println(Util.Color("&c[guiAPI] You tried adding an item to a page but didn't enable pages first!\nMake sure you enable pages for this window before adding items to pages!"));
                return null;
            }
            int page = pageArgs[0];
            List<guiItem> currPageItems = pages.size() == 0 ? new ArrayList<>() : pages.get(pages.size());
            int slot = getNextFree(page);
            if (slot == -1) {
                return null;
            }
            item = new guiItem(this, mat, name, slot);
            clickableItems.put(item.getItemStack(), item);
            currPageItems.add(item);
            if (pages.size() == 0) {
                pages.put(1, currPageItems);
            } else {
                pages.replace(pages.size(), pages.get(pages.size()), currPageItems);
            }
            return item;
        }
        int slot = getNextFree();
        if (slot == -1) {
            return null;
        }
        item = new guiItem(this, mat, name, slot);
        clickableItems.put(item.getItemStack(), item);
        return item;
    }

    /*
    Returns the next free slot in the entire inventory or on a specific page (if pages are enabled)
     */
    private int getNextFree(Integer... pageArgs) {
        if (pageArgs != null) {
            int page = pageArgs[0];
            if (pages.size() < page) {
                return -1;
            }
            if (pages.get(page).size() == slots) {
                return -1;
            }

            List<Integer> slots = new ArrayList<>();
            for (guiItem item : pages.get(page)) {
                slots.add(item.getSlot());
            }
            ArrayList<Integer> a = new ArrayList<>(Arrays.asList(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53).subList(0,slots.size()));
            a.add(-1);
            for( Integer f : slots){
                a.remove(f);
            }
            return a.get(0);
        }
        return inv.firstEmpty();
    }

    /**
     * creates the inventory and adds all items to it
     */
    private void invSetup() {
        //Handles all the Items added by the user
        inv = Bukkit.createInventory(p, rows * 9, Util.Color(name));
        for (ItemStack is : clickableItems.keySet()) {
            guiItem item = clickableItems.get(is);
            inv.setItem(item.getSlot(), item.getItemStack());
        }
        if (type.equals(WindowType.SPLIT_2)) {
            ItemStack borderItem = borderMat == null ? new ItemStack(Material.WHITE_STAINED_GLASS_PANE) : new ItemStack(borderMat);
            if (borderName != null) {
                ItemMeta im = borderItem.getItemMeta();
                im.setDisplayName(borderName);
                borderItem.setItemMeta(im);
            }
            for (int i = 0; i < rows; i++) {
                int slot = 4 + i * 9;
                inv.setItem(slot, borderItem);
            }
        }
    }

    /**
     * Enables Page view which allows more items than you can fit in one inventory per guiWindow
     * @param enabled if pages should be enabled
     */
    public void setPagesEnabled(boolean enabled) {
        usePages = enabled;
    }

    /**
     * Sets the items that are used for the border if the WindowType is Split_2
     * Default Item is White Stained Glass Pane
     * @param mat Material of the border items
     * @param nameArgs <b>[Optional]</b> Name of the border Items (use " " if you want no name)
     */
    public void setBorderInv(Material mat, String... nameArgs) {
        borderMat = mat;
        if (nameArgs != null) {
            borderName = nameArgs[0];
        }
    }

    /**
     * Sets the items that fill the empty slots in the inventory
     * @param mat Material of the fill items
     * @param nameArgs <b>[Optional]</b> Name of the Fill (use " " if you want no name)
     */
    public void setFillInv(Material mat, String... nameArgs) {
        fill = true;
        if (nameArgs != null) {
            fillName = Util.Color(nameArgs[0]);
        }
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
        if (fillName != null) {
            ItemMeta im = is.getItemMeta();
            im.setDisplayName(fillName);
            is.setItemMeta(im);
        }
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
        clickableItems.get(is).onClick(e);
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
