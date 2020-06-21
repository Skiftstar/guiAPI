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

    private Window currWindow = null;
    private List<Window> windows = new ArrayList<>();
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
     * @return the created window
     */
    public ChestWindow createWindow(String name, int rows) {
        ChestWindow window = new ChestWindow(p, name, rows, plugin);
        windows.add(window);
        return window;
    }

    /**
     * Creates a new AnvilWindow
     * @param title Title of the window
     * @return the created AnvilWindow
     */
    public AnvilWindow createAnvilWindow(String title) {
        AnvilWindow window = new AnvilWindow(p, title);
        windows.add(window);
        return window;
    }

    /*
    ==========================================
                User Accessible
    ==========================================
     */

    /**
     * Opens a specified window (inventory) for the player
     * @param window window to open
     */
    public void open(Window window) {
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
    public void removeWindow(Window window) {
        window.unregister();
        windows.remove(window);
    }

    /*
    ==========================================
                Get Methods
    ==========================================
     */

    /**
     * @return all windows handled by this GUI
     */
    public List<Window> getWindows() {
        return windows;
    }

    /**
     * @return The window that is currently open
     */
    public Window getCurrWindow() {
        return currWindow;
    }
}
