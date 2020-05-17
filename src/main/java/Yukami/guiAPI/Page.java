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

    public Page(int slots, int pageNr, guiWindow window) {
        this.slots = slots;
        this.pageNr = pageNr;
        this.window = window;
        items = new guiItem[slots];
    }
    public Page(int slots, guiItem[] items, int pageNr, guiWindow window) {
        this.slots = slots;
        this.items = items;
        this.window = window;
        this.pageNr = pageNr;
    }

    public void removeItem(guiItem item) {
        for (int i = 0; i < items.length; i++) {
            if (items[i].equals(item)) {
                items[i] = null;
                window.clickableItems.remove(items[i].getItemStack());
                changedSlots.add(i);
            }
        }
        checkUpdate();
    }

    public void removeItem(ItemStack is) {
        for (int i = 0; i < items.length; i++) {
            if (items[i].getItemStack().equals(is)) {
                items[i] = null;
                window.clickableItems.remove(items[i].getItemStack());
                changedSlots.add(i);
            }
        }
        checkUpdate();
    }

    public void removeItem(int slot) {
        if (items[slot] != null) {
            window.clickableItems.remove(items[slot].getItemStack());
            items[slot] = null;
            changedSlots.add(slot);
            checkUpdate();
        }
    }

    public void addItem(guiItem item, Integer... slotArgs) {
        int slot;
        if (slotArgs.length > 0) {
            slot = slotArgs[0];
            System.out.println("slot: " + slot);
            if (slot < 0 || slot > slots - 1) {
                System.out.println(Util.Color("&c[guiAPI] There was an attempt to add an item to a slot that is out of bounds!\nItem was not added!"));
                return;
            }
            if (items[slot] != null) {
                removeItem(slot);
            }
        } else {
            slot = getNextFree();
            if (slot == -1) {
                System.out.println(Util.Color("&c[guiAPI] There was an attemp to add an item to a page that is full!\n&cThe item was not added!"));
            }
        }
        items[slot] = item;
        changedSlots.add(slot);
        window.clickableItems.put(item.getItemStack(), item);
        checkUpdate();
    }

    public void moveItem(guiItem item, int slot) {
        items[item.getSlot()] = null;
        changedSlots.add(item.getSlot());
        items[slot] = item;
        item.setSlot(slot);
        changedSlots.add(slot);
        checkUpdate();
    }

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

    private void checkUpdate() {
        final List<Integer> temp = new ArrayList<>(changedSlots);
        changedSlots.clear();
        if (window.getCurrPage() != pageNr || temp.size() == 0) {
            return;
        }
        for (int slot : temp) {
            if (items[slot] != null) {
                window.getInv().setItem(slot, items[slot].getItemStack());
            } else {
                window.getInv().setItem(slot, new ItemStack(Material.AIR));
            }
        }
        temp.clear();
    }

    /*
    ========================================================
                        Get Methods
    ========================================================
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

    public guiItem getItem(int slot) {
        if (slot > slots - 1 || slot < 0) {
            return null;
        }
        return items[slot];
    }

    public int getPageNr() {
        return pageNr;
    }

    public guiItem[] getItems() {
        return items;
    }

    public guiItem getNextPage() {
        return nextPage;
    }

    public guiItem getPrevPage() {
        return prevPage;
    }
}
