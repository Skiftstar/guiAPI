package Yukami.guiAPI;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class guiItem {

    private ItemStack is;
    private Consumer<InventoryClickEvent> functionClick = null;
    private guiWindow window;
    private int slot;
    private Inventory inv;
    private int page = -1;

    /**
     *
     * @param window window of the item
     * @param mat Material of the item
     * @param name Name of the item
     * @param amount Amount of items in the ItemStack
     * @param slot slot the item will be put in
     */
    guiItem(guiWindow window, Material mat, String name, int amount, int slot) {
        this.window = window;
        this.inv = window.getInv();
        this.slot = slot;
        is = new ItemStack(mat, amount);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(Util.Color(name));
        is.setItemMeta(im);
    }

    /**
     * Same as other constructor, but without name. Name will be the material name
     */
    guiItem(guiWindow window, Material mat, int amount, int slot) {
        this.window = window;
        this.inv = window.getInv();
        this.slot = slot;
        is = new ItemStack(mat, amount);
        ItemMeta im = is.getItemMeta();
    }

    guiItem(guiWindow window, ItemStack is, int slot) {
        this.is = is;
        this.slot = slot;
        this.window = window;
    }

    /**
     * same as other constructor, but without item amount, amount will be 1
     */
    guiItem(guiWindow window, Material mat, String name, int slot) {
        this.window = window;
        this.inv = window.getInv();
        this.slot = slot;
        is = new ItemStack(mat);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(Util.Color(name));
        is.setItemMeta(im);
    }

    /**
     * same as other constructors, but without name or amount, name will be item name, amount will be 1
     */
    guiItem(guiWindow window, Material mat, int slot) {
        this.window = window;
        this.inv = window.getInv();
        this.slot = slot;
        is = new ItemStack(mat);
    }

    /*
    ==========================================
                Lore Related
    ==========================================
     */

    /**
     * Sets a preset Lore
     * @param lore - StringList with the pre set lore
     */
    public void updateLore(List<String> lore) {
        ItemMeta im = is.getItemMeta();
        window.clickableItems.remove(is);
        im.setLore(lore);
        is.setItemMeta(im);
        //inv.setItem(slot, is);
        window.clickableItems.put(is, this);
    }

    /**
     * Clears the lore of the item
     */
    public void clearLore() {
        ItemMeta im = is.getItemMeta();
        window.clickableItems.remove(is);
        if (im.hasLore()) {
            im.setLore(null);
        }
        is.setItemMeta(im);
        //inv.setItem(slot, is);
        window.clickableItems.put(is, this);
    }

    /**
     * Adds a single line to the Item lore
     * @param lore - Line to be added
     */
    public void addLore(String lore) {
        ItemMeta im = is.getItemMeta();
        window.clickableItems.remove(is);
        List<String> currLore;
        if (im.hasLore()) {
            currLore = im.getLore();
        } else {
            currLore = new ArrayList<>();
        }
        currLore.add(lore);
        im.setLore(currLore);
        is.setItemMeta(im);
        //inv.setItem(slot, is);
        window.clickableItems.put(is, this);
    }

    /**
     * Removes a specified line from the lore. If the lore has no lines after this, it will be removed completely
     * @param lore - Line to be removed
     */
    public void removeLore(String lore) {
        ItemMeta im = is.getItemMeta();
        if (!im.hasLore()) {
            return;
        }
        List<String> currLore = im.getLore();
        for (int i = 0; i < currLore.size(); i++) {
            if (currLore.get(i).equals(lore)) {
                currLore.remove(lore);
            }
        }
        if (currLore.size() == 0) {
            im.setLore(null);
        } else {
            im.setLore(currLore);
        }
        window.clickableItems.remove(is);
        is.setItemMeta(im);
        window.clickableItems.put(is, this);
        //inv.setItem(slot, is);
    }

    /**
     * Removes the given line number from the item lore
     * @param line - line number to be removed
     */
    public void removeLore(int line) {
        ItemMeta im = is.getItemMeta();
        if (!im.hasLore()) {
            return;
        }
        if (line < 0) {
            return;
        }
        List<String> currLore = im.getLore();
        if (line > currLore.size() - 1) {
            return;
        }
        currLore.remove(line);
        if (currLore.size() == 0) {
            im.setLore(null);
        } else {
            im.setLore(currLore);
        }
        window.clickableItems.remove(is);
        is.setItemMeta(im);
        window.clickableItems.put(is, this);
        //inv.setItem(slot, is);
    }

    /*
    ==========================================
                Set Methods
    ==========================================
     */

    /**
     * Updates the item name
     * @param name - New name of the item
     */
    public void setName(String name) {
        ItemMeta im = is.getItemMeta();
        window.clickableItems.remove(is);
        im.setDisplayName(Util.Color(name));
        is.setItemMeta(im);
        //inv.setItem(slot, is);
        window.clickableItems.put(is, this);
    }

    /**
     * Changes the item slot. Item in old slot will be removed
     * @param slot slot the item will be put in
     */
    public void setSlot(int slot) {
        this.slot = slot;
        //inv.setItem(slot, is);
    }

    /**
     * Changes the Item material
     * @param mat - Material the item should be changed to
     */
    public void setMaterial(Material mat) {
        window.clickableItems.remove(is);
        is.setType(mat);
        window.clickableItems.put(is, this);
        //inv.setItem(slot, is);
    }

    /**
     * Changes the amount of items in the ItemStack
     * @param count - amount of items in the ItemStack
     */
    public void setAmount(int count) {
        if (count < 1) {
            count = 1;
        }
        else if (count > 64) {
            count = 64;
        }
        window.clickableItems.remove(is);
        is.setAmount(count);
        window.clickableItems.put(is, this);
        //inv.setItem(slot, is);
    }

    //Only private for now, will change later so that items can be moved around
    void setPage(int page) {
        this.page = page;
    }

    /*
    ==========================================
                Event Related
    ==========================================
     */

    /*
     * Gets called when the item is clicked
     */
    void onClick(InventoryClickEvent e) {
        if (functionClick == null) {
            return;
        }
        functionClick.accept(e);
    }

    /**
     * Adds a function that will be executed once the item gets clicked
     * @param function - A Void,Void function (it's java.lang.Void so it has to return null)
     */
    public void setOnClick(Consumer<InventoryClickEvent> function) {
        functionClick = function;
    }


    /*
    ==========================================
                Get Methods
    ==========================================
     */

    /**
     *
     * @return ItemStack of the item
     */
    public ItemStack getItemStack() {
        return is;
    }

    /**
     *
     * @return slot the item is in
     */
    public int getSlot() {
        return slot;
    }

    /**
     * @return Name of the item without colorCodes
     */
    public String getNoColorName() {
        ItemMeta im = is.getItemMeta();
        if (im.hasDisplayName()) {
            return ChatColor.stripColor(im.getDisplayName());
        }
        return null;
    }

    /**
     * @return Page the item is on or -1 if the item isn't on a page/pages are disabled
     */
    public int getPage() {
        return page;
    }
}
