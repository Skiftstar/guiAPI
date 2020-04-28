package Yukami.guiAPI;

/*
This is a simple GUI API, which allows easy to create GUIs without having to create a class for each one
Author: Yukami
Last Updated: April 21, 2020
 */

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class GUI {

    private guiWindow currWindow = null;
    private List<guiWindow> windows = new ArrayList<>();
    private Player p;
    private JavaPlugin plugin;

    public GUI(Player p, JavaPlugin plugin) {
        this.p = p;
        this.plugin = plugin;
    }

    public guiWindow createWindow(String name, int rows) {
        guiWindow window = new guiWindow(p, name, rows, plugin);
        windows.add(window);
        return window;
    }

    public void open(guiWindow window) {
        if (currWindow != null) {
            currWindow.unregister();
        }
        p.closeInventory();
        currWindow = window;
        window.open();
    }

    public void removeWindows(guiWindow window) {
        window.delete();
        windows.remove(window);
    }

    public List<guiWindow> getWindows() {
        return windows;
    }

    public guiWindow getCurrWindow() {
        return currWindow;
    }
}
