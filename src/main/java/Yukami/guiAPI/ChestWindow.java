package Yukami.guiAPI;

import Yukami.guiAPI.Exceptions.NoSuchPageException;
import Yukami.guiAPI.Exceptions.PagesNotEnabledException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;
import java.util.function.Consumer;

public class ChestWindow extends Window implements Listener {

    private Inventory inv;
    private Player p;
    private int rows;
    private int slots;
    private String name;
    private boolean fill = false;
    private JavaPlugin plugin;
    Material fillMat = null, nextPageMat = null, prevPageMat = null;
    String fillName = null, nextPageName = null, prevPageName = null;
    private List<GuiItem> pageChangersForward = new ArrayList<>(), pageChangersBackward = new ArrayList<>();
    private Consumer<InventoryClickEvent> playerInvClickFunction = null;
    private int currPage = 1;
    private Map<Integer, Page> pages = new HashMap<>();
    private boolean usePages = false;
    private Consumer<InventoryOpenEvent> onOpen = null;
    private Consumer<InventoryCloseEvent> onClose = null;

    /**
     * Creates a Window (an inventory) that can be further customized
     *
     * @param p      Player of that the inv will be opened to
     * @param name   title of the inventory
     * @param rows   Amount of rows, min. 1, max. 6
     * @param plugin Plugin reference
     */
    public ChestWindow(Player p, String name, int rows, JavaPlugin plugin) {
        this.p = p;
        this.name = name;
        this.plugin = plugin;
        this.rows = rows;
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
    protected void open() {
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
     * Sets the items that fill the empty slots in the inventory
     *
     * @param mat      Material of the fill items
     * @param nameArgs <b>[Optional]</b> Name of the Fill (use " " if you want no name)
     */
    public void setFillInv(Material mat, String... nameArgs) {
        fill = true;
        if (nameArgs.length > 0) {
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
        for (GuiItem item : pageChangersForward) {
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
        for (GuiItem item : pageChangersBackward) {
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
        for (GuiItem item : pageChangersForward) {
            item.setMaterial(mat);
            if (nameArgs != null) {
                item.setName(nameArgs[0]);
            }
        }
        prevPageMat = mat;
        for (GuiItem item : pageChangersBackward) {
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
     * Clears the entire inventory
     */
    public void clear() {
        clickableItems.clear();
        for (Page page : pages.values()) {
            page.clear();
        }
    }

    /**
     * Sets the function that will be executed once the player clicks an item in his inventory
     * @param func Function to be executed
     */
    public void setOnPlayerInvClick(Consumer<InventoryClickEvent> func) {
        playerInvClickFunction = func;
    }

    public GuiItem setItem(ItemStack is, int slot, Integer... pageArgs) {
        GuiItem item;
        Page page;
        if (pageArgs.length > 0) {
            // Prevents saving Items where they won't be used
            if (!usePages) {
                throw new PagesNotEnabledException();
            }
            int pageNr = pageArgs[0];
            // If the page doesn't exist yet
            if (pages.size() < pageNr || pageNr <= 0) {
                throw new NoSuchPageException();
            }
            // Get the page
            page = pages.get(pageNr);
        } else {
            page = pages.get(1);
        }
        // Create the item and add it to the page if a page is provided.
        item = new GuiItem(this, is, slot);
        item.setPage(page.getPageNr());
        page.addItem(item, slot);
        return item;
    }

    public void removeItem(GuiItem item) {
        for (Page page : pages.values()) {
            page.removeItem(item);
        }
    }

    public GuiItem addItem(ItemStack is, Integer... pageArgs) {
        GuiItem item;
        Page page;
        int pageNr;
        if (pageArgs.length > 0) {
            // Possible Errors
            if (!usePages) {
                throw new PagesNotEnabledException();
            }
            pageNr = pageArgs[0];
            if (pages.size() < pageNr || pageNr <= 0) {
                throw new NoSuchPageException();
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
        item = new GuiItem(this, is, page.getNextFree());
        item.setPage(page.getPageNr());
        page.addItem(item, page.getNextFree());
        pages.replace(page.getPageNr(), pages.get(page.getPageNr()), page);
        return item;
    }

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
    public GuiItem setItem(Material mat, String name, int slot, Integer... pageArgs) {
        GuiItem item;
        Page page;
        if (pageArgs.length > 0) {
            // Prevents saving Items where they won't be used
            if (!usePages) {
                throw new PagesNotEnabledException();
            }
            int pageNr = pageArgs[0];
            // If the page doesn't exist yet
            if (pages.size() < pageNr || pageNr <= 0) {
                throw new NoSuchPageException();
            }
            // Get the page
            page = pages.get(pageNr);
        } else {
            page = pages.get(1);
        }
        // Create the item and add it to the page if a page is provided.
        ItemStack is = new ItemStack(mat);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(Util.Color(name));
        is.setItemMeta(im);
        item = new GuiItem(this, is, slot);
        item.setPage(page.getPageNr());
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
    public GuiItem addItem(Material mat, String name, Integer... pageArgs) {
        GuiItem item;
        Page page;
        int pageNr;
        if (pageArgs.length > 0) {
            // Possible Errors
            if (!usePages) {
                throw new PagesNotEnabledException();
            }
            pageNr = pageArgs[0];
            if (pages.size() < pageNr || pageNr <= 0) {
                throw new NoSuchPageException();
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
        ItemStack is = new ItemStack(mat);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(Util.Color(name));
        is.setItemMeta(im);
        item = new GuiItem(this, is, page.getNextFree());
        item.setPage(page.getPageNr());
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
            GuiItem[] items = page.getItems();
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
            GuiItem item = clickableItems.get(is);
            inv.setItem(item.getSlot(), item.getItemStack());
        }
    }

    /**
     * Sets a function to execute when the inventory opens
     * @param func function to execute
     */
    public void setOnOpen(Consumer<InventoryOpenEvent> func) {
        this.onOpen = func;
    }

    /**
     * Sets a function to execute when the inventory closes
     * @param func function to execute
     */
    public void setOnClose(Consumer<InventoryCloseEvent> func) {
        this.onClose = func;
    }

    /*
    ========================================================
                    Page Related
    ========================================================
     */

    private void addNewPage() {
        // Get the item that blocks the pageSwapper
        GuiItem moveForward;
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
            GuiItem oldBack = page.getPrevPageItem();
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
            throw new PagesNotEnabledException();
        }
        // Checks for monkey input
        if (page < 1 || page > pages.size()) {
            throw new NoSuchPageException();
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

    @Override
    void update(int... PageArgs) {
        if (PageArgs.length > 0 && PageArgs[0] != -1) {
            for (int page : PageArgs) {
                if (getPage(page) == null) {
                    continue;
                }
                getPage(page).checkUpdate();
            }
        } else {
            for (Page page : pages.values()) {
                page.checkUpdate();
            }
        }
    }

    @EventHandler
    protected void onInvClick(InventoryClickEvent e) {
        Inventory clicked = e.getClickedInventory();
        if (clicked == null) {
            return;
        }

        if (!e.getWhoClicked().equals(p)) {
            return;
        }
        if (clicked.equals(p.getInventory())) {
            if (playerInvClickFunction == null) {
                e.setCancelled(true);
            } else {
                playerInvClickFunction.accept(e);
            }
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
    protected void onInvClose(InventoryCloseEvent e) {
        if (!e.getPlayer().equals(p)) {
            return;
        }
        if (!e.getInventory().equals(inv)) {
            return;
        }
        if (onClose != null) {
            onClose.accept(e);
        }
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    protected void onInvOpen(InventoryOpenEvent e) {
        if (!e.getInventory().equals(inv)) {
            return;
        }
        if (onOpen != null) {
            onOpen.accept(e);
        }
    }

    /*
    ==========================================
                Get Methods
    ==========================================
     */

    /**
     * Returns all items in the inventory
     * @return items in the inv
     */
    public ItemStack[] getItems() {
        return inv.getContents();
    }

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

    /**
     *
     * @param pageNum Number of page to be returned (Starts with 1)
     * @return the page or null of the page doesn't exist
     */
    public Page getPage(int pageNum) {
        return pages.size() < pageNum ? null : pages.get(pageNum);
    }

    /**
     * @return the size of the inventory
     */
    public int getSize() {
        return inv.getSize();
    }
}
