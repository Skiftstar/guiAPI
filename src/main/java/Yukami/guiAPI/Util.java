package Yukami.guiAPI;

import org.bukkit.ChatColor;

public class Util {

    /**
     * Colors a String
     * @param s string with color codes
     * @return the colored string
     */
    static String Color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
