package Yukami.guiAPI;

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
    Material fillMat = null, borderMat = null, nextPageMat = null, prevPageMat = null;
    String fillName = null, borderName = null, nextPageName = null, prevPageName = null;
    private List<guiItem> pageChangersForward = new ArrayList<>(), pageChangersBackward = new ArrayList<>();
    private WindowType type;
    private int currPage = 1;
    private Map<Integer, Page> pages = new HashMap<>();
    private boolean usePages = false;

    /**
     * Creates a Window (an inventory) that can be further customized
     *
     * @param p      Player of that the inv will be opened to
     * @param name   title of the inventory
     * @param rows   Amount of rows, min. 1, max. 6
     * @param type   WindowType, normal or split
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
        pages.put(1, new Page(slots, 1, this));
    }

    /*
    ========================================================
                Accessible by package
    ========================================================
     */

    /**
     * opens in the inventory
     */
    void open() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        updateInventory();
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

    /*
    ========================================================
            Customization (Setter Methods)
    ========================================================
     */

    /**
     * Enables Page view which allows more items than you can fit in one inventory per guiWindow
     *
     * @param enabled if pages should be enabled
     */
    public void setPagesEnabled(boolean enabled) {
        usePages = enabled;
        if (pages.size() == 0) {
            pages.put(1, new Page(slots, 1, this));
        }
    }

    /**
     * Sets the items that are used for the border if the WindowType is Split_2
     * Default Item is White Stained Glass Pane
     *
     * @param mat      Material of the border items
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
     *
     * @param mat      Material of the fill items
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
     * Sets the Material and the name of the item used to go a page forward
     *
     * @param mat      Material of the Item
     * @param nameArgs <b>[Optional]</b> name of the item
     */
    public void setNextPageItem(Material mat, String... nameArgs) {
        if (nameArgs != null) {
            nextPageName = nameArgs[0];
        }
        nextPageMat = mat;
        // Update Existing Page Changers
        for (guiItem item : pageChangersForward) {
            item.setMaterial(mat);
            if (nameArgs != null) {
                item.setName(nameArgs[0]);
            }
        }
    }

    /**
     * Sets the Material and name of the item used to go a page back
     *
     * @param mat      Material of the Item
     * @param nameArgs <b>[Optional]</b> name of the item
     */
    public void setPrevPageItem(Material mat, String... nameArgs) {
        if (nameArgs != null) {
            prevPageName = nameArgs[0];
        }
        prevPageMat = mat;
        // Update existing PageChangers
        for (guiItem item : pageChangersBackward) {
            item.setMaterial(mat);
            if (nameArgs != null) {
                item.setName(nameArgs[0]);
            }
        }
    }

    /**
     * Changes the Material and Name of the items used to change between pages
     *
     * @param mat      Material of the items
     * @param nameArgs <b>[Optional</b> 1 - Name of the nextPage Item \n 2 - Name of the prevPage item
     */
    public void setPageChangeItems(Material mat, String... nameArgs) {
        if (nameArgs != null && nameArgs.length >= 2) {
            nextPageName = nameArgs[0];
            prevPageName = nameArgs[1];
        }
        nextPageMat = mat;
        // Update the existing pageChangers
        for (guiItem item : pageChangersForward) {
            item.setMaterial(mat);
            if (nameArgs != null) {
                item.setName(nameArgs[0]);
            }
        }
        prevPageMat = mat;
        for (guiItem item : pageChangersBackward) {
            item.setMaterial(mat);
            if (nameArgs != null) {
                item.setName(nameArgs[1]);
            }
        }
    }

    /*
    ========================================================
                    User Accessible
    ========================================================
     */

    /**
     * adds an ItemStack to the inventory or to a specific window.
     * If an item is in the same slot as this item it gets replaced.
     *
     * @param mat      Material of the Item
     * @param name     Name of the Item
     * @param slot     Slot the item is in (does not work if a border item is on that slot)
     * @param pageArgs <b>[Optional]</b> page that the item will be on (enable pages first!)
     * @return Returns the created <b>guiItem</b>
     */
    public guiItem setItemStack(Material mat, String name, int slot, Integer... pageArgs) {
        guiItem item;
        Page page;
        if (pageArgs.length > 0) {
            // Prevents saving Items where they won't be used
            if (!usePages) {
                System.out.println(Util.Color("&c[guiAPI] You tried adding an item to a page but didn't enable pages first!\n&cMake sure you enable pages for this window before adding items to pages!"));
                return null;
            }
            int pageNr = pageArgs[0];
            // If the page doesn't exist yet
            if (pages.size() < pageNr || pageNr <= 0) {
                System.out.println(Util.Color("&c[guiAPI] You tried adding an item to a page that doesn't exist!\n&cRemember that pages start at 1 (e.g. Page 1 is 1, Page 2 is 2, etc.) !"));
                return null;
            }
            // Get the page
            page = pages.get(pageNr);
        } else {
            page = pages.get(1);
        }
        // Create the item and add it to the page if a page is provided.
        item = new guiItem(this, mat, name, slot);
        page.addItem(item, slot);
        return item;
    }

    /**
     * Adds an item to the inventory or a specific page (if one is provided)
     *
     * @param mat      Material of the Item
     * @param name     Name of the Item
     * @param pageArgs <b>[Optional]</b> page the item will be on (enable pages first!)
     * @return the created Item
     */
    public guiItem addItemStack(Material mat, String name, Integer... pageArgs) {
        guiItem item;
        Page page;
        int pageNr;
        if (pageArgs.length > 0) {
            // Possible Errors
            if (!usePages) {
                System.out.println(Util.Color("&c[guiAPI] You tried adding an item to a page but didn't enable pages first!\n&cMake sure you enable pages for this window before adding items to pages!"));
                return null;
            }
            pageNr = pageArgs[0];
            if (pages.size() < pageNr || pageNr <= 0) {
                System.out.println(Util.Color("&c[guiAPI] You tried adding an item to a page that doesn't exist!\n&cRemember that pages start at 1 (e.g. Page 1 is 1, Page 2 is 2, etc.) !"));
                return null;
            }
            page = pages.get(pageNr);
        } else {
            // If no page is provided but pages are used
            if (usePages) {
                //check if the current page is full, if so add a new one
                if (pages.get(pages.size()).getNextFree() == -1) {
                    addNewPage();
                }
                page = pages.get(pages.size());
            } else {
                page = pages.get(1);
            }
        }
        item = new guiItem(this, mat, name, page.getNextFree());
        page.addItem(item, page.getNextFree());
        pages.replace(page.getPageNr(), pages.get(page.getPageNr()), page);
        return item;
    }

    /**
     * Returns the next free slot in the entire inventory or on a specific page (if pages are enabled)
     *
     * @param page <b>[Optional]</b> page you want to get the next free slot of
     * @return the next free slot or -1 if the inv/page is full
     */
    public int getNextFree(int page) {
        // Page doesn't exist, just say it's full
        if (pages.size() < page || page < 1) {
            System.out.println(Util.Color("&c[guiAPI] There was an attempt to get the next free slot of a page that doesn't exist!\n&cRemember pages start at 1!\nReturned a full inventory to not cause any errors."));
            return -1;
        }
        return pages.get(page).getNextFree();
    }

    /**
     * Used to update the inventory.
     * It will be cleared completely and then all items will be added back.
     */
    public void updateInventory() {
        inv.clear();
        if (usePages) {
            // Get the currentPage, loop through the items, update their slots just in case and place them in the inv
            Page page = pages.get(currPage);
            guiItem[] items = page.getItems();
            for (int i = 0; i < items.length; i++) {
                if (items[i] == null) {
                    continue;
                }
                items[i].setSlot(i);
                inv.setItem(i, items[i].getItemStack());
            }
            return;
        }
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

    /*
    ========================================================
                    Page Related
    ========================================================
     */

    private void addNewPage() {
        // Get the item that blocks the pageSwapper
        guiItem moveForward;
        Page page = pages.get(pages.size());
        // This needs to be a special case because page 1 is the only page without "go Back 1 Page" Item
        if (pages.size() == 1) {
            moveForward = page.getItem(slots - 1);
        } else {
            moveForward = page.getItem(slots - 2);
        }
        // Again, special case because this would not work for Page 1
        // Just move the "Page Back" Item one slot to the left so that it fits the layout
        if (pages.size() > 1) {
            guiItem oldBack = page.getPrevPageItem();
            page.moveItem(oldBack, slots - 2);
        }
        page.setNextPageItem();
        // Creates the new page and the item that allows the user to back one page
        Page newPage = new Page(slots, pages.size() + 1, this);
        // add the items to the page and save the page
        newPage.setPrevPageItem();
        newPage.addItem(moveForward);
        moveForward.setPage(moveForward.getPage() + 1);
        pages.put(pages.size() + 1, newPage);
    }

    /**
     * Opens the specified page
     *
     * @param page Page to open
     */
    public void changePage(int page) {
        // Checks if pages are actually enabled
        if (!usePages) {
            System.out.println(Util.Color("&c[guiAPI] There was an attempt to change pages but pages aren't enabled yet!\n&cMake sure to enable them before using any related methods!"));
            return;
        }
        // Checks for monkey input
        if (page < 1 || page > pages.size()) {
            System.out.println(page);
            System.out.println(Util.Color("&c[guiAPI] There was an attempt to change to a page that doesn't exist!\n&cRemember that Pages start at 1 !"));
            return;
        }
        currPage = page;
        updateInventory();
    }

    /*
    ========================================================
                    Event Handler Related
    ========================================================
     */

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

    /*
    ==========================================
                Get Methods
    ==========================================
     */

    /**
     * @return the inventory
     */
    public Inventory getInv() {
        return inv;
    }

    /**
     * @return What page the Player has open or -1 if pages are disabled
     */
    public int getCurrPage() {
        return usePages ? currPage : -1;
    }

    /**
     * @return The amount of pages or -1 if pages are disabled
     */
    public int getPageCount() {
        return usePages ? pages.size() : -1;
    }
}
