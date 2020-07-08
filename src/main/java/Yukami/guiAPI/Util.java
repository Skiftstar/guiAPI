package Yukami.guiAPI;

import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

class Util {

    static int getContentLength(ItemStack[] Array) {
        for (int i = 0; i < Array.length; i++) {
            if (Array[i] == null) {
                return i;
            }
        }
        return Array.length;
    }

    static int getFreeSlotsInInv(Inventory inv) {
        int emptySlots = 0;
        for (int i = 0; i < inv.getContents().length; i++) {
            if (inv.getContents()[i] == null) {
                emptySlots++;
            }
        }
        return emptySlots;
    }

    static boolean contains(ItemStack[] items, ItemStack is) {
        for (ItemStack i : items) {
            if (i.equals(is)) {
                return true;
            }
        }
        return false;
    }

    static void remove(ItemStack[] items, ItemStack is) {
        for (int i = 0; i < items.length; i++) {
            if (items[i].equals(is)) {
                items[i] = null;
            }
        }
        List<Integer> empty = new ArrayList<>();
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null) {
                empty.add(i);
            } else if (empty.size() > 0) {
                items[empty.get(0)] = items[i];
                items[i] = null;
                empty.remove(0);
                empty.add(i);
            }
        }
    }

    static int contains(ItemStack[] items, GuiItem item) {
        int counter = 0;
        for (ItemStack i : items) {
            if (i.equals(item.getItemStack())) {
                return counter;
            }
            counter++;
        }
        return -1;
    }

    /*
     * Colors a String
     * @param s string with color codes
     * @return the colored string
     */
    static String Color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
