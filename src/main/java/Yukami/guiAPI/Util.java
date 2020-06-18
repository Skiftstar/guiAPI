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

    /*
     * Colors a String
     * @param s string with color codes
     * @return the colored string
     */
    static String Color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
