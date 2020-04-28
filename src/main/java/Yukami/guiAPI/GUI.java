package Yukami.guiAPI;

/*
This is a simple GUI API, which allows easy to create GUIs without having to create a class for each one
Author: Yukami
Last Updated: April 21, 2020
 */

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.List;

public class GUI {

    private guiWindow currWindow = null;
    private List<guiWindow> windows = new ArrayList<>();
    private Player p;
    private JavaPlugin plugin;

    /**
     * Creates the Main GUI that handles all windows
     * @param p Player that all the windows should be opened for
     * @param plugin plugin reference
     */
    public GUI(Player p, JavaPlugin plugin) {
        this.p = p;
        this.plugin = plugin;
    }

    /**
     * Creates a window (an inventory)
     * @param name Title of the inventory
     * @param rows rows of the inv (min. 1, max. 6)
     * @param type WindowType, this determines the further look of the inventory
     * @return the created window
     */
    public guiWindow createWindow(String name, int rows, WindowType type) {
        guiWindow window = new guiWindow(p, name, rows, type, plugin);
        windows.add(window);
        return window;
    }

    /**
     * Opens a specified window (inventory) for the player
     * @param window window to open
     */
    public void open(guiWindow window) {
        if (currWindow != null) {
            currWindow.unregister();
        }
        p.closeInventory();
        currWindow = window;
        window.open();
    }

    /**
     * Removes a window
     * @param window Window to be deleted
     */
    public void removeWindows(guiWindow window) {
        window.delete();
        windows.remove(window);
    }

    /**
     *
     * @return all windows handled by this GUI
     */
    public List<guiWindow> getWindows() {
        return windows;
    }

    /**
     *
     * @return The window that is currently open
     */
    public guiWindow getCurrWindow() {
        return currWindow;
    }
}
