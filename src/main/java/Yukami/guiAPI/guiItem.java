package Yukami.guiAPI;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class guiItem {

    private ItemStack is;
    private Function<Void, Void> functionClick = null;
    private guiWindow window;
    private int slot;
    private Inventory inv;

    public guiItem(guiWindow window, Material mat, String name, int amount, int slot) {
        this.window = window;
        this.inv = window.getInv();
        this.slot = slot;
        is = new ItemStack(mat, amount);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(Util.Color(name));
        is.setItemMeta(im);
    }

    public guiItem(guiWindow window, Material mat, int amount, int slot) {
        this.window = window;
        this.inv = window.getInv();
        this.slot = slot;
        is = new ItemStack(mat, amount);
        ItemMeta im = is.getItemMeta();
    }

    public guiItem(guiWindow window, Material mat, String name, int slot) {
        this.window = window;
        this.inv = window.getInv();
        this.slot = slot;
        is = new ItemStack(mat);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(Util.Color(name));
        is.setItemMeta(im);
    }

    public void setSlot(int slot) {
        this.slot = slot;
        inv.setItem(slot, is);
    }

    public void setMaterial(Material mat) {
        window.clickableItems.remove(is);
        is.setType(mat);
        window.clickableItems.put(is, this);
        inv.setItem(slot, is);
    }

    public void setAmount(int count) {
        if (count < 1) {
            count = 1;
        }
        else if (count > 64) {
            count = 64;
        }
        window.clickableItems.remove(is);
        is.setAmount(count);
        window.clickableItems.put(is, this);
        inv.setItem(slot, is);
    }

    public void updateLore(List<String> lore) {
        ItemMeta im = is.getItemMeta();
        window.clickableItems.remove(is);
        im.setLore(lore);
        is.setItemMeta(im);
        inv.setItem(slot, is);
        window.clickableItems.put(is, this);
    }

    public void addLore(String lore) {
        ItemMeta im = is.getItemMeta();
        window.clickableItems.remove(is);
        List<String> currLore;
        if (im.hasLore()) {
            currLore = im.getLore();
        } else {
            currLore = new ArrayList<>();
        }
        currLore.add(lore);
        im.setLore(currLore);
        is.setItemMeta(im);
        inv.setItem(slot, is);
        window.clickableItems.put(is, this);
    }

    public void removeLore(String lore) {
        ItemMeta im = is.getItemMeta();
        if (!im.hasLore()) {
            return;
        }
        List<String> currLore = im.getLore();
        for (int i = 0; i < currLore.size(); i++) {
            if (currLore.get(i).equals(lore)) {
                currLore.remove(lore);
            }
        }
        if (currLore.size() == 0) {
            im.setLore(null);
        } else {
            im.setLore(currLore);
        }
        window.clickableItems.remove(is);
        is.setItemMeta(im);
        window.clickableItems.put(is, this);
        inv.setItem(slot, is);
    }

    public void removeLore(int line) {
        ItemMeta im = is.getItemMeta();
        if (!im.hasLore()) {
            return;
        }
        if (line < 0) {
            return;
        }
        List<String> currLore = im.getLore();
        if (line > currLore.size() - 1) {
            return;
        }
        currLore.remove(line);
        if (currLore.size() == 0) {
            im.setLore(null);
        } else {
            im.setLore(currLore);
        }
        window.clickableItems.remove(is);
        is.setItemMeta(im);
        window.clickableItems.put(is, this);
        inv.setItem(slot, is);
    }

    public void updateName(String name) {
        ItemMeta im = is.getItemMeta();
        window.clickableItems.remove(is);
        im.setDisplayName(Util.Color(name));
        is.setItemMeta(im);
        inv.setItem(slot, is);
        window.clickableItems.put(is, this);
    }

    public void setOnClick(Function<Void, Void> function) {
        functionClick = function;
    }

    public void onClick() {
        if (functionClick == null) {
            return;
        }
        functionClick.apply(null);
    }

    public ItemStack getItemStack() {
        return is;
    }

    public int getSlot() {
        return slot;
    }
}
