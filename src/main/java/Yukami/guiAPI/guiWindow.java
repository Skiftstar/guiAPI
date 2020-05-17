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

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
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
    private Material fillMat = null, borderMat = null, nextPageMat = null, prevPageMat = null;
    private String fillName = null, borderName = null, nextPageName = null, prevPageName = null;
    private List<guiItem> pageChangersForward = new ArrayList<>(), pageChangersBackward = new ArrayList<>();
    private WindowType type;
    private int currPage = 1;
    private Map<Integer, guiItem[]> pages = new HashMap<>();
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
    public guiItem setItemStack(Material mat, String name, int slot, Integer... pageArgs) {
        guiItem item;
        guiItem[] pageItems = new guiItem[slots];
        if (pageArgs.length > 0) {
            // Prevents saving Items where they won't be used
            if (!usePages) {
                System.out.println(Util.Color("&c[guiAPI] You tried adding an item to a page but didn't enable pages first!\n&cMake sure you enable pages for this window before adding items to pages!"));
                return null;
            }
            int page = pageArgs[0];
            // If the page doesn't exist yet
            if (pages.size() < page || page <= 0) {
                System.out.println(Util.Color("&c[guiAPI] You tried adding an item to a page that doesn't exist!\n&cRemember that pages start at 1 (e.g. Page 1 is 1, Page 2 is 2, etc.) !"));
                return null;
            }
            // Get the items on the page
            pageItems = pages.get(page);
            // Check if any Item is already in that slot, if so, remove it
            if (pageItems[slot] != null) {
                clickableItems.remove(pageItems[slot].getItemStack());
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
        if (pageArgs.length > 0) {
            pageItems[slot] = item;
            pages.replace(pageArgs[0], pages.get(pageArgs[0]), pageItems);
        }
        // add it as a clickable item and return it
        clickableItems.put(item.getItemStack(), item);
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
        if (pageArgs.length > 0) {
            // Possible Errors
            if (!usePages) {
                System.out.println(Util.Color("&c[guiAPI] You tried adding an item to a page but didn't enable pages first!\n&cMake sure you enable pages for this window before adding items to pages!"));
                return null;
            }
            int page = pageArgs[0];
            if (pages.size() < page || page <= 0) {
                System.out.println(Util.Color("&c[guiAPI] You tried adding an item to a page that doesn't exist!\n&cRemember that pages start at 1 (e.g. Page 1 is 1, Page 2 is 2, etc.) !"));
                return null;
            }

            guiItem[] pageItems = pages.get(page);
            // Check if there is a free slot on the page
            int slot = getNextFree(page);
            if (slot == -1) {
                System.out.println(Util.Color("&c[guiAPI] There was an attempt to add an Item to a page that is full!\n&cItem was not Added!"));
                return null;
            }
            // if there is a free slot, create the item and add it to the page and clickable items
            item = new guiItem(this, mat, name, slot);
            clickableItems.put(item.getItemStack(), item);
            pageItems[slot] = item;
            pages.replace(pages.size(), pages.get(pages.size()), pageItems);
            if (currPage == page) {
                inv.setItem(slot, item.getItemStack());
            }
            return item;
        }
        // If no page is provided but pages are used
        if (usePages) {
            //check if the current page is full, if so add a new one
            int slot = getNextFree(pages.size());
            if (slot == -1) {
                addNewPage();
            }
            // then just add the item
            slot = getNextFree(pages.size());
            guiItem[] pageItems = pages.get(pages.size());
            item = new guiItem(this, mat, name, slot);
            pageItems[slot] = item;
            clickableItems.put(item.getItemStack(), item);
            pages.replace(pages.size(), pages.get(pages.size()), pageItems);
            if (currPage == pages.size()) {
                inv.setItem(slot, item.getItemStack());
            }
            return item;
        }
        // Same slot checking as above
        int slot = getNextFree();
        if (slot == -1) {
            System.out.println(Util.Color("&c[guiAPI] There was an attempt to add an Item to the inventory but it is full!\n&cItem was not Added!"));
            return null;
        }
        item = new guiItem(this, mat, name, slot);
        clickableItems.put(item.getItemStack(), item);
        inv.setItem(item.getSlot(), item.getItemStack());
        return item;
    }

    /**
     * Returns the next free slot in the entire inventory or on a specific page (if pages are enabled)
     *
     * @param pageArgs <b>[Optional]</b> page you want to get the next free slot of
     * @return the next free slot or -1 if the inv/page is full
     */
    public int getNextFree(Integer... pageArgs) {
        // If the next free slot on a specific page is wanted
        if (pageArgs.length > 0) {
            int page = pageArgs[0];
            // Page doesn't exist, just say it's full
            if (pages.size() < page) {
                System.out.println(Util.Color("&c[guiAPI] There was an attempt to get the next free slot of a page that doesn't exist!\n&cRemember pages start at 1!\nReturned a full inventory to not cause any errors."));
                return -1;
            }
            guiItem[] items = pages.get(page);
            // Loop through the items until the first empty index is found
            for (int i = 0; i < items.length; i++) {
                if (items[i] == null) {
                    return i;
                }
            }
            // if there is no empty index, return -1 to show the inv is full
            return -1;
        }
        // If there isn't a specific page wanted, just use the current open inv and use the function provided by spigot
        return inv.firstEmpty();
    }

    private void addNewPage() {
        guiItem moveForward;
        guiItem[] page = pages.get(pages.size());
        if (pages.size() == 1) {
            moveForward = page[slots - 1];
        } else {
            moveForward = page[slots - 2];
        }
        guiItem pageForward = nextPageMat == null ? new guiItem(this, Material.ARROW, "&aNext Page", slots - 1) : new guiItem(this, nextPageMat, "&aNext Page", slots - 1);
        if (nextPageName != null) {
            pageForward.setName(nextPageName);
        }
        pageForward.setOnClick(e -> {
            changePage(currPage + 1);
        });
        pageChangersForward.add(pageForward);
        clickableItems.put(pageForward.getItemStack(), pageForward);
        if (pages.size() > 1) {
            guiItem oldBack = page[slots - 1];
            page[slots - 2] = oldBack;
        }
        if (currPage == pages.size()) {
            inv.setItem(slots - 1, pageForward.getItemStack());
        }
        page[slots - 1] = pageForward;
        pages.replace(pages.size(), pages.get(pages.size()), page);
        guiItem[] newPage = new guiItem[slots];
        guiItem pageBack = prevPageMat == null ? new guiItem(this, Material.ARROW, "&cPrevious Page", slots - 1) : new guiItem(this, prevPageMat, "&cPrevious Page", slots - 1);
        if (prevPageName != null) {
            pageBack.setName(prevPageName);
        }
        pageBack.setOnClick(e -> {
            changePage(currPage - 1);
        });
        pageChangersBackward.add(pageBack);
        clickableItems.put(pageBack.getItemStack(), pageBack);
        newPage[slots - 1] = pageBack;
        newPage[0] = moveForward;
        System.out.println("Davor: " + Arrays.toString(newPage));
        pages.put(pages.size() + 1, newPage);
        System.out.println("Danach: " + Arrays.toString(newPage));
    }

    /**
     * Opens the specified page
     *
     * @param page Page to open
     */
    public void changePage(int page) {
        if (!usePages) {
            System.out.println("&c[guiAPI] There was an attempt to change pages but pages aren't enabled yet!\n&cMake sure to enable them before using any related methods!");
            return;
        }
        if (page < 1 || page > pages.size()) {
            System.out.println("&c[guiAPI] There was an attempt to change to a page that doesn't exist!\n&cRemember that Pages start at 1 !");
            return;
        }
        currPage = page;
        updateInventory();
    }

    public void updateInventory() {
        //Handles all the Items added by the user
        inv.clear();
        if (usePages) {
            guiItem[] page = pages.get(currPage);
            for (int i = 0; i < page.length; i++) {
                if (page[i] == null) {
                    continue;
                }
                page[i].setSlot(i);
                inv.setItem(i, page[i].getItemStack());
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
     * creates the inventory and adds all items to it
     */
    private void invSetup() {
        //Handles all the Items added by the user
        inv = Bukkit.createInventory(p, rows * 9, Util.Color(name));
        if (usePages) {
            System.out.println(pages.size() + "\n" + pages.get(1).length);
            for (guiItem is : pages.get(1)) {
                if (is == null) {
                    continue;
                }
                inv.setItem(is.getSlot(), is.getItemStack());
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

    /**
     * Enables Page view which allows more items than you can fit in one inventory per guiWindow
     *
     * @param enabled if pages should be enabled
     */
    public void setPagesEnabled(boolean enabled) {
        usePages = enabled;
        if (pages.size() == 0) {
            pages.put(1, new guiItem[slots]);
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
     * @return the inventory
     */
    public Inventory getInv() {
        return inv;
    }
}
