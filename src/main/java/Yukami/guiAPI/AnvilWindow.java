package Yukami.guiAPI;

import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
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
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AnvilWindow extends Window implements Listener {

    private AnvilInventory inv;
    private Consumer<InventoryClickEvent> onClick = null;
    private GuiItem[] usedSlots = new GuiItem[2];
    private FakeAnvil fakeAnvil;

    public AnvilWindow(Player player, String title, JavaPlugin plugin) {
        fakeAnvil = new FakeAnvil(player, title);
        inv = fakeAnvil.getInventory();
    }

    public void setOnClick(Consumer<InventoryClickEvent> onClick) {
        this.onClick = onClick;
    }

    @Override
    public void setOnPlayerInvClick(Consumer<InventoryClickEvent> function) {
        onPlayerInvClick = function;
    }

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

    @Override
    public void removeItem(GuiItem item) {
        for (int i = 0; i < usedSlots.length; i++) {
            if (usedSlots[i].equals(item)) {
                usedSlots[i] = null;
                clickableItems.remove(item.getItemStack());
            }
        }
    }

    @Override
    protected void open() {
        fakeAnvil.open();
    }

    @Override
    public GuiItem setItemStack(Material mat, String name, int slot, Integer... pageArgs) {
        if (slot > 1 || slot < 0) {
            System.out.println(Util.Color("&c[GuiAPI] Slot cannot be less than 0 or higher than 1 in an AnvilWindow!"));
            return null;
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

    @Override
    public GuiItem setItemStack(ItemStack is, int slot, Integer... pageArgs) {
        if (slot > 1 || slot < 0) {
            System.out.println(Util.Color("&c[GuiAPI] Slot cannot be less than 0 or higher than 1 in an AnvilWindow!"));
            return null;
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

    @Override
    public void unregister() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public ItemStack[] getItems() {
        return inv.getContents();
    }

    @Override
    public String getWindowTitle() {
        return windowTitle;
    }

    @Override
    void update(int... PageArgs) {
        int i = 0;
        for (GuiItem item : usedSlots) {
            inv.setItem(0, item.getItemStack());
            i++;
        }
    }

    @Override
    public Inventory getInv() {
        return inv;
    }
}
