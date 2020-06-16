package Yukami.guiAPI;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TradeWindow extends Window {

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
        borderItem = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        invP1 = Bukkit.createInventory(p1, 54,Util.Color(title));
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

    @Override
    void onInvClick(InventoryClickEvent e) {

    }

    @Override
    void onInvClose(InventoryCloseEvent e) {

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
