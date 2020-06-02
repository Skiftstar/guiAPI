package Yukami.guiAPI;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Page {

    private final int slots;
    private final guiItem[] items;
    private final int pageNr;
    private final guiWindow window;
    private guiItem nextPage = null;
    private guiItem prevPage = null;
    private List<Integer> changedSlots = new ArrayList<>();

    /**
     * Creates a new page
     * @param slots slots on the page, changing this from the slots the window has causes errors!!
     * @param pageNr the page number, duplicates can cause errors!
     * @param window the window the page is on
     */
    public Page(int slots, int pageNr, guiWindow window) {
        this.slots = slots;
        this.pageNr = pageNr;
        this.window = window;
        items = new guiItem[slots];
    }

    /*
    ========================================================
                User Accessible
    ========================================================
     */

    /**
     * Creates a page with already given items
     * @param slots slots on the page, changing this from the slots the window has causes errors!!
     * @param items array of guiItems that should be added to the page already
     * @param pageNr the page number, duplicates can cause errors!
     * @param window the window the page is on
     */
    public Page(int slots, guiItem[] items, int pageNr, guiWindow window) {
        this.slots = slots;
        this.items = items;
        this.window = window;
        this.pageNr = pageNr;
    }

    /**
     * Clears all slots with this guiItem in it
     * @param item guiItem to remove
     */
    public void removeItem(guiItem item) {
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null) {
                continue;
            }
            if (items[i].equals(item)) {
                items[i] = null;
                window.clickableItems.remove(item.getItemStack());
                changedSlots.add(i);
            }
        }
        checkUpdate();
    }

    /**
     * Clears all slots with this itemStack in it
     * @param is ItemStack to remove
     */
    public void removeItem(ItemStack is) {
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null) {
                continue;
            }
            if (items[i].getItemStack().equals(is)) {
                window.clickableItems.remove(items[i].getItemStack());
                items[i] = null;
                changedSlots.add(i);
            }
        }
        checkUpdate();
    }

    /**
     * Clears a provided slot on the page
     * @param slot Slot to clear
     */
    public void removeItem(int slot) {
        if (items[slot] != null) {
            window.clickableItems.remove(items[slot].getItemStack());
            items[slot] = null;
            changedSlots.add(slot);
            checkUpdate();
        }
    }

    /**
     * Adds an item to the page
     * @param item item to add
     * @param slotArgs <b>[Optional]</b> slot to add the item, if no slot is provided it will be added to the next free slot
     */
    public void addItem(guiItem item, Integer... slotArgs) {
        int slot;
        if (slotArgs.length > 0) {
            slot = slotArgs[0];
            // Check for monkey input
            if (slot < 0 || slot > slots - 1) {
                System.out.println(Util.Color("&c[guiAPI] There was an attempt to add an item to a slot that is out of bounds!\nItem was not added!"));
                return;
            }
            if (items[slot] != null) {
                removeItem(slot);
            }
        }
        // No slot provided, just use the next free one
        else {
            slot = getNextFree();
            if (slot == -1) {
                System.out.println(Util.Color("&c[guiAPI] There was an attempt to add an item to a page that is full!\n&cThe item was not added!"));
                return;
            }
        }
        items[slot] = item;
        changedSlots.add(slot);
        window.clickableItems.put(item.getItemStack(), item);
        checkUpdate();
    }

    /**
     * Moves an item to a different slot
     * @param item guiItem to move
     * @param slot slot to move to
     */
    public void moveItem(guiItem item, int slot) {
        // Set old slot empty
        items[item.getSlot()] = null;
        changedSlots.add(item.getSlot());
        // Add item to new slot
        items[slot] = item;
        item.setSlot(slot);
        changedSlots.add(slot);
        checkUpdate();
    }

    /**
     * Adds the "Next Page" Item to the page
     */
    public void setNextPageItem() {
        if (nextPage != null) {
            window.clickableItems.remove(nextPage.getItemStack());
        }

        nextPage = window.nextPageMat == null ? new guiItem(window, Material.ARROW, "&aNext Page", slots - 1) : new guiItem(window, window.nextPageMat, "&aNext Page", slots - 1);
        if (window.nextPageName != null) {
            nextPage.setName(window.nextPageName);
        }
        nextPage.setOnClick(e -> {
            window.changePage(window.getCurrPage() + 1);
        });
        System.out.println(slots);
        addItem(nextPage, (slots - 1));
        window.clickableItems.put(nextPage.getItemStack(), nextPage);
        changedSlots.add(slots - 1);
        checkUpdate();
    }

    /**
     * Adds the "Previous Page" Item to the page
     */
    public void setPrevPageItem() {
        if (prevPage != null) {
            window.clickableItems.remove(prevPage.getItemStack());
        }

        prevPage = window.prevPageMat == null ? new guiItem(window, Material.ARROW, "&cPrevious Page", slots - 1) : new guiItem(window, window.prevPageMat, "&cPrevious Page", slots - 1);
        if (window.prevPageName != null) {
            prevPage.setName(window.prevPageName);
        }
        prevPage.setOnClick(e -> {
            window.changePage(window.getCurrPage() - 1);
        });
        addItem(prevPage, prevPage.getSlot());
        window.clickableItems.put(prevPage.getItemStack(), prevPage);
        changedSlots.add(prevPage.getSlot());
        checkUpdate();
    }

    /**
     * Updates the Page Changer Items using the values provided in the guiWindow
     */
    public void updatePageChangers() {
        if (prevPage == null || nextPage == null) {
            System.out.println(Util.Color("&c[guiAPI] There was an attempt to update the page changers when they weren't set yet!\n&cMake sure to set them first to avoid errors!"));
        }
        if (prevPage != null) {
            window.clickableItems.remove(prevPage.getItemStack());
            if (window.prevPageName != null) {
                prevPage.setName(window.prevPageName);
            }
            if (window.prevPageMat != null) {
                prevPage.setMaterial(window.prevPageMat);
            }
            window.clickableItems.put(prevPage.getItemStack(), prevPage);
            changedSlots.add(prevPage.getSlot());
        }
        if (nextPage != null) {
            window.clickableItems.remove(nextPage.getItemStack());
            if (window.nextPageName != null) {
                nextPage.setName(window.nextPageName);
            }
            if (window.nextPageMat != null) {
                nextPage.setMaterial(window.nextPageMat);
            }
            window.clickableItems.put(nextPage.getItemStack(), nextPage);
            changedSlots.add(nextPage.getSlot());
        }
        checkUpdate();
    }

    /*
    ========================================================
                    Private Methods
    ========================================================
     */

    void checkUpdate() {
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null) {
                window.getInv().setItem(i, new ItemStack(Material.AIR));
                continue;
            }
            window.getInv().setItem(i, items[i].getItemStack());
        }
        changedSlots.clear();


        /*
        Make a new List and just copy the values from the old List
        Because java is a piece of crap and just isn't fast enough to keep up with my
        cookie clicker skills
         */
//        final List<Integer> temp = new ArrayList<>(changedSlots);
//        // Then just make room for the next slot changes
//        changedSlots.clear();
//        if (window.getCurrPage() != pageNr || temp.size() == 0) {
//            return;
//        }
//        for (int slot : temp) {
//            if (items[slot] != null) {
//                window.getInv().setItem(slot, items[slot].getItemStack());
//            } else {
//                window.getInv().setItem(slot, new ItemStack(Material.AIR));
//            }
//        }
//        temp.clear();
    }

    /*
    ========================================================
                        Get Methods
    ========================================================
     */

    /**
     * @return Next Free slot or -1 if the page is full
     */
    public int getNextFree() {
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null) {
                return i;
            }
        }
        // if there is no empty index, return -1 to show the inv is full
        return -1;
    }

    /**
     * @param slot Slot you want the item of
     * @return guiItem in this slot or null if the slot is empty
     */
    public guiItem getItem(int slot) {
        if (slot > slots - 1 || slot < 0) {
            return null;
        }
        return items[slot];
    }

    /**
     * @return the number of the page
     */
    public int getPageNr() {
        return pageNr;
    }

    /**
     * @return all guiItems on the page (also the empty slots, they are null)
     */
    public guiItem[] getItems() {
        return items;
    }

    /**
     * @return the "next page" item
     */
    public guiItem getNextPageItem() {
        return nextPage;
    }

    /**
     * @return the "previous page" item
     */
    public guiItem getPrevPageItem() {
        return prevPage;
    }
}
