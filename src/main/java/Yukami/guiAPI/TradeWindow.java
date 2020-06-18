package Yukami.guiAPI;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

public class TradeWindow extends Window implements Listener {

    Player p1, p2;
    JavaPlugin plugin;
    ItemStack[] p1Items = new ItemStack[24];
    ItemStack[] p2Items = new ItemStack[24];
    ItemStack borderItem;
    Inventory invP1, invP2;

    public TradeWindow(Player p1, Player p2, String title, JavaPlugin plugin) {
        windowTitle = title;
        this.p1 = p1;
        this.p2 = p2;
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        borderItem = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        invP1 = Bukkit.createInventory(p1, 54, Util.Color(title));
        invP2 = Bukkit.createInventory(p2, 54,Util.Color(title));
    }

    public void setBorder(ItemStack is) {
        is.setAmount(1);
        borderItem = is;
    }

    public void setBorder(Material mat, String... nameArgs) {
        ItemStack is = new ItemStack(mat);
        if (nameArgs.length > 0) {
            String name = nameArgs[0];
            ItemMeta im = is.getItemMeta();
            im.setDisplayName(Util.Color(name));
            is.setItemMeta(im);
        }
        borderItem = is;
    }

    @Override
    public void setOnPlayerInvClick(Consumer<InventoryClickEvent> function) {

    }

    @EventHandler
    @Override
    void onInvClick(InventoryClickEvent e) {
        Inventory clicked = e.getClickedInventory();
        if (clicked == null) {
            return;
        }

        if (!(e.getWhoClicked().equals(p1) || e.getWhoClicked().equals(p2))) {
            return;
        }
        if (clicked.equals(p1.getInventory())) {
            if (!e.getWhoClicked().equals(p1)) {
                return;
            }
            ItemStack is = e.getCurrentItem();
            if (is == null || is.getType().equals(Material.AIR)) {
                return;
            }
            addToTrade(p1, is);
            return;
        }
        if (clicked.equals(p2.getInventory())) {
            if (!e.getWhoClicked().equals(p2)) {
                return;
            }
            ItemStack is = e.getCurrentItem();
            if (is == null || is.getType().equals(Material.AIR)) {
                return;
            }
            addToTrade(p2, is);
            return;
        }
        if (e.getAction().equals(InventoryAction.HOTBAR_SWAP)) {
            e.setCancelled(true);
            return;
        }
        if (!(clicked.equals(invP1) || clicked.equals(invP2))) {
            return;
        }
        ItemStack is = e.getCurrentItem();
        if (is == null || is.getType().equals(Material.AIR)) {
            return;
        }
        e.setCancelled(true);
        if (Util.contains(p1Items, is)) {
            removeFromTrade(p1, is);
        } else if (Util.contains(p2Items, is)) {
            removeFromTrade(p2, is);
        } else if (is.equals(borderItem)) {
            setReady((Player) e.getWhoClicked());
        }
    }

    public void setReady(Player p) {

    }

    public void addToTrade(Player p, ItemStack is) {
        if (!(p.equals(p1) || p.equals(p2))) {

        }
    }

    public void removeFromTrade(Player p, ItemStack is) {

    }

    private int getFirstFree(ItemStack[] items) {
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null) {
                return i;
            }
        }
        return -1;
    }

    @EventHandler
    @Override
    void onInvClose(InventoryCloseEvent e) {
        if (!e.getPlayer().equals(p)) {
            return;
        }
        unregister();
    }

    @Override
    public void removeItem(GuiItem item) {

    }

    @Override
    void open() {

    }

    @Override
    public GuiItem setItemStack(Material mat, String name, int slot, Integer... pageArgs) {
        return null;
    }

    @Override
    public GuiItem setItemStack(ItemStack is, int slot, Integer... pageArgs) {
        return null;
    }

    @Override
    public void unregister() {

    }

    @Override
    public ItemStack[] getItems() {
        return new ItemStack[0];
    }

    @Override
    void update(int... PageArgs) {

    }

    @Override
    public Inventory getInv() {
        return null;
    }
}
