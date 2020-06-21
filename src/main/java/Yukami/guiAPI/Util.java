package Yukami.guiAPI;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

class Util {

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
