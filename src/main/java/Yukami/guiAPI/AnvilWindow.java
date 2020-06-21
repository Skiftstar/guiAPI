package Yukami.guiAPI;

import Yukami.guiAPI.Exceptions.AnvilSlotBoundsException;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class AnvilWindow extends Window implements Listener {

    private AnvilInventory inv;
    private Consumer<InventoryClickEvent> onClick = null;
    private Consumer<InventoryClickEvent> onPlayerInvClick = null;
    private GuiItem[] usedSlots = new GuiItem[3];
    private FakeAnvil fakeAnvil;

    public AnvilWindow(Player player, String title) {
        fakeAnvil = new FakeAnvil(player, title);
        inv = fakeAnvil.getInventory();
    }

    /*
    ==========================================
               GUI API Only
    ==========================================
     */

    @Override
    protected void open() {
        fakeAnvil.open();
    }

    @Override
    void update(int... PageArgs) {
        int i = 0;
        for (GuiItem item : usedSlots) {
            inv.setItem(0, item.getItemStack());
            i++;
        }
    }

    /*
    ==========================================
               Event handlers
    ==========================================
     */

    @Override
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
            if (onPlayerInvClick == null) {
                e.setCancelled(true);
            } else {
                onPlayerInvClick.accept(e);
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
            if (e.getSlot() == 2) {
                if (onClick != null) {
                    onClick.accept(e);
                    return;
                }
            }
        }
        e.setCancelled(true);
        if (!clickableItems.containsKey(is)) {
            return;
        }
        clickableItems.get(is).onClick(e);
    }

    @Override
    @EventHandler
    void onInvClose(InventoryCloseEvent e) {
        if (!e.getPlayer().equals(p)) {
            return;
        }
        unregister();
    }

    /*
    ==========================================
                User Accessible Methods
    ==========================================
     */

    /**
     * Puts a provided ItemStack in the slot provided
     * @param is ItemStack to add
     * @param slot Slot to put the item in (must be 0, 1 or 2!)
     * @param pageArgs Unused, required param from parent class
     * @return The GuiItem or null if there was an error
     */
    @Override
    public GuiItem setItemStack(ItemStack is, int slot, Integer... pageArgs) {
        if (slot > 2 || slot < 0) {
            throw new AnvilSlotBoundsException();
        }
        if (usedSlots[slot] != null) {
            clickableItems.remove(usedSlots[slot].getItemStack());
        }
        GuiItem item = new GuiItem(this, is, slot);
        clickableItems.put(item.getItemStack(), item);
        usedSlots[slot] = item;
        update();
        return item;
    }

    /**
     * The provided function will be executed when the player clicks on the item in slot 2
     * @param onClick Function to execute
     */
    public void setOnClick(Consumer<InventoryClickEvent> onClick) {
        this.onClick = onClick;
    }

    /**
     * The provided function will be executed when the player clicks on an item in his own inventory
     * @param function function to execute
     */
    public void setOnPlayerInvClick(Consumer<InventoryClickEvent> function) {
        onPlayerInvClick = function;
    }

    public void setLevelCost(boolean levelCost) {
        fakeAnvil.doLevelCost = levelCost;
    }

    /**
     * Removes the item from the inventory
     * @param item GuiItem to remove
     */
    @Override
    public void removeItem(GuiItem item) {
        for (int i = 0; i < usedSlots.length; i++) {
            if (usedSlots[i].equals(item)) {
                usedSlots[i] = null;
                clickableItems.remove(item.getItemStack());
            }
        }
    }

    /**
     * Unregisters the Listeners from this class. Does not need to be called manually to handle inventory closes, etc.
     */
    @Override
    public void unregister() {
        HandlerList.unregisterAll(this);
    }

    /**
     * Adds an item to the Window
     * @param mat Material for the Item
     * @param name Name of the item
     * @param slot slot in which the item should be (must be 0, 1 or 2!)
     * @param pageArgs Unused, required from parent class
     * @return the added GuiItem or null if there was an error
     */
    @Override
    public GuiItem setItemStack(Material mat, String name, int slot, Integer... pageArgs) {
        if (slot > 1 || slot < 0) {
            throw new AnvilSlotBoundsException();
        }
        if (usedSlots[slot] != null) {
            clickableItems.remove(usedSlots[slot].getItemStack());
        }
        GuiItem item = new GuiItem(this, mat, name, slot);
        clickableItems.put(item.getItemStack(), item);
        usedSlots[slot] = item;
        update();
        return item;
    }

    /*
    ==========================================
                Get Methods
    ==========================================
     */

    /**
     * Item in the inventory
     * @return Array of Inventory Contents
     */
    public ItemStack[] getItems() {
        return inv.getContents();
    }

    /**
     * @return Name of the inventory
     */
    @Override
    public String getWindowTitle() {
        return windowTitle;
    }

    /**
     *
     * @return the inventory (may be cased to AnvilInventory)
     */
    @Override
    public Inventory getInv() {
        return inv;
    }

    /**
     *
     * @return The Text in the Rename Item field of the anvil or an empty String if the name was not changed
     */
    public String getText() {
        return inv.getRenameText();
    }
}
