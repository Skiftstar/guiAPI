package Yukami.guiAPI;

import Yukami.guiAPI.Events.FullTradeAction;
import Yukami.guiAPI.Events.TradeItemAddEvent;
import Yukami.guiAPI.Events.TradeItemRemoveEvent;
import Yukami.guiAPI.Exceptions.InvalidItemException;
import Yukami.guiAPI.Exceptions.InvalidPlayerException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class TradeWindow extends Window implements Listener {

    private Player p1, p2;
    private JavaPlugin plugin;
    private ItemStack[] p1Items = new ItemStack[24];
    private ItemStack[] p2Items = new ItemStack[24];
    private Map<Player, Boolean> ready = new HashMap<>();
    private ItemStack borderItem, borderReadyItem, setReadyItem;
    private Inventory invP1, invP2;
    private Consumer<TradeItemAddEvent> onItemAdd = null;
    private Consumer<TradeItemRemoveEvent> onItemRemove = null;
    private int countdownTask, countdown = 3;
    private boolean doCountdown = false;
    private String countdownMessage = "&aTrade will be completed in &6%sec seconds&a!", tradeCompleteMess = "&aTrade completed!";
    private String noInvSpace = "&cYou do not have enough space in your inventory!", noInvSpacePartner = "&cYou partner does not have enough space in his inventory!";


    /**
     * Creates a new Trade Windows
     * @param p1 Player 1 that is participating in the trade
     * @param p2 Player 2 that is participating in the trade
     * @param title Title of the trade window
     * @param plugin Your plugin
     */
    public TradeWindow(Player p1, Player p2, String title, JavaPlugin plugin) {
        windowTitle = title;
        this.p1 = p1;
        this.p2 = p2;
        this.plugin = plugin;
        ready.put(p1, false);
        ready.put(p2, false);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        borderItem = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        borderReadyItem = new ItemStack(Material.GREEN_STAINED_GLASS);
        invP1 = Bukkit.createInventory(p1, 54, Util.Color(title));
        invP2 = Bukkit.createInventory(p2, 54,Util.Color(title));
    }

    /*
    ========================================================
                       User Accessible
    ========================================================
     */

    @Override
    public void unregister() {
        HandlerList.unregisterAll(this);
    }

    /**
     * Adds an item to the trade offers of a player
     * @param p To what players trade offers the item should be added
     * @param is Item that should be added
     */
    public void addToTrade(Player p, ItemStack is) {
        if (!(p.equals(p1) || p.equals(p2))) {
            throw new InvalidPlayerException();
        }
        TradeItemAddEvent e = new TradeItemAddEvent(this, p, p.equals(p1) ? p2 : p1, is);
        if (onItemAdd != null) {
            onItemAdd.accept(e);
        }
        if (e.isCanceled()) {
            return;
        }
        is = e.getItem();
        if (e.isRemoveFromInv()) {
            p.getInventory().remove(is);
        }
        int nextFree = getFirstFree(p);
        if (nextFree == -1) {
            if (e.getOnTradeFull().equals(FullTradeAction.REPLACE_LAST)) {
                if (p.equals(p1)) {
                    p1Items[p1Items.length - 1] = is;
                } else {
                    p2Items[p2Items.length - 1] = is;
                }
            } else {
                return;
            }
        } else {
            if (p.equals(p1)) {
                p1Items[nextFree] = is;
            } else {
                p2Items[nextFree] = is;
            }
        }
        update();
    }

    /**
     * Removes an item from the trade offers of a specified player
     * @param p From what players trade offers the item should be removed
     * @param is the item that should be removed
     */
    public void removeFromTrade(Player p, ItemStack is) {
        if (!(p.equals(p1) || p.equals(p2))) {
            throw new InvalidPlayerException();
        }
        TradeItemRemoveEvent e = new TradeItemRemoveEvent(this, p, p.equals(p1) ? p2 : p1, is);
        if (onItemRemove != null) {
            onItemRemove.accept(e);
        }
        if (p.equals(p1) && !Util.contains(p1Items, is)) {
            throw new InvalidItemException();
        } else if (p.equals(p2) && !Util.contains(p2Items, is)) {
            throw new InvalidItemException();
        }
        if (e.isCanceled()) {
            return;
        }
        is = e.getItem();
        if (e.isAddBackToInv()) {
            p.getInventory().addItem(is);
        }
        if (p.equals(p1)) {
            Util.remove(p1Items, is);
        } else {
            Util.remove(p2Items, is);
        }
        update();
    }

    /**
     * Don't use this Method. Use removeFromTrade instead!
     * Only reason this method is here is because it's from the Window interface.
     * It doesn't check for the GuiItem but for the ItemStack that it refers.
     * @param item null
     */
    @Deprecated
    @Override
    public void removeItem(GuiItem item) {
        invP1.removeItem(item.getItemStack());
        invP2.removeItem(item.getItemStack());
        int containsP1 = Util.contains(p1Items, item);
        if (containsP1 >= 0) {
            p1Items[containsP1] = null;
        }
        int containsP2 = Util.contains(p2Items, item);
        if (containsP2 >= 0) {
            p2Items[containsP2] = null;
        }
        update();
    }

    /**
     * Completes the trade
     * @param force If true, skips the countdown
     */
    public void completeTrade(boolean force) {
        if (force) {

        } else {
            doCountdown = true;
            countdownTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
                @Override
                public void run() {
                    p1.sendMessage(Util.Color(countdownMessage.replace("%sec", "" + countdown)));
                    p2.sendMessage(Util.Color(countdownMessage.replace("%sec", "" + countdown)));
                    countdown--;
                    if (countdown == 0) {
                        p1.sendMessage(Util.Color(tradeCompleteMess));
                        p2.sendMessage(Util.Color(tradeCompleteMess));
                        doCountdown = false;
                        distributeItems();
                        p1.closeInventory();
                        p2.closeInventory();
                        Bukkit.getScheduler().cancelTask(countdownTask);
                    }
                }
            }, 0, countdown * 20);
        }
    }

    /*
    ========================================================
                       Private Methods
    ========================================================
     */

    private void distributeItems() {
        for (ItemStack itemStack : p1Items) {
            if (itemStack == null) {
                break;
            }
            p1.getInventory().remove(itemStack);
        }
        for (ItemStack p2Item : p2Items) {
            if (p2Item == null) {
                break;
            }
            p1.getInventory().addItem(p2Item);
            p2.getInventory().remove(p2Item);
        }
        for (ItemStack p1Item : p1Items) {
            if (p1Item == null) {
                break;
            }
            p2.getInventory().addItem(p1Item);
        }
    }

    private boolean invSpaceCheck() {
        if (Util.getContentLength(p1Items) > Util.getFreeSlotsInInv(p2.getInventory()) - Util.getContentLength(p2Items)) {
            p1.sendMessage(Util.Color(noInvSpacePartner));
            p2.sendMessage(Util.Color(noInvSpace));
            return false;
        }
        if (Util.getContentLength(p2Items) > Util.getFreeSlotsInInv(p1.getInventory()) - Util.getContentLength(p1Items)) {
            p1.sendMessage(Util.Color(noInvSpace));
            p2.sendMessage(Util.Color(noInvSpacePartner));
            return false;
        }
        return true;
    }

    /*
    ========================================================
                       Package only
    ========================================================
     */

    @Override
    void open() {
        update();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        p1.openInventory(invP1);
        p2.openInventory(invP2);
    }

    @Override
    void update(int... PageArgs) {
        Player[] players = new Player[] {p1, p2};
        Inventory[] invs = new Inventory[] {invP1, invP2};
        ItemStack[][] items = new ItemStack[][] {p1Items, p2Items};

        invP1.clear();
        invP2.clear();

        // Do for both players
        for (int player = 0; player < 2; player++) {
            // Handles the border
            for (int slot = 4; slot < 56; slot += 9) {
                if (slot < 27 && ready.get(players[player])) {
                    invs[player].setItem(slot, borderReadyItem);
                } else if (slot > 27 && ready.get(players[(player + 1) % 2])) {
                    invs[player].setItem(slot, borderReadyItem);
                } else {
                    invs[player].setItem(slot, borderItem);
                }
            }
            int item = 0;
            // Handles left side
            for (int slot = 0; slot < 48; slot++) {
                if (slot < 9 || slot % 9 < 4) {
                    if (items[player][item] == null) {
                        break;
                    }
                    invs[player].setItem(slot, items[player][item]);
                    item++;
                } else {
                    slot = (int) slot / 9 * 9;
                }
            }
            item = 0;
            // Handles right side
            for (int slot = 5; slot < 54; slot++) {
                if (slot < 9 || slot % 9 >= 4) {
                    if (items[(player + 1) % 2][item] == null) {
                        break;
                    }
                    invs[player].setItem(slot, items[(player + 1) % 2][item]);
                    item++;
                } else {
                    slot = (int) slot / 9 * 9 + 5;
                }
            }
        }

    }

    /*
    ========================================================
                            Setters
    ========================================================
     */

    /**
     * Sets the message displayed when the player doesn't have enough space in his inventory
     * @param mess the message
     */
    public void setNoInvSpaceMess(String mess) {
        noInvSpace = mess;
    }

    /**
     * Sets the message when the players trade partner doesn't have enough space in his inventory
     * @param mess the message
     */
    public void setNoInvSpacePartnerMess(String mess) {
        noInvSpacePartner = mess;
    }

    /**
     * Sets the Item that border between the trade offers
     * @param is Item the border should be
     */
    public void setBorder(ItemStack is) {
        is.setAmount(1);
        borderItem = is;
    }

    /**
     * See above
     * @param mat Material of the Item
     * @param nameArgs <b>Nullable</b> name of the item
     */
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

    /**
     * Sets the item the border is made out of when the player clicks ready
     * @param is Item the border is out of
     */
    public void setReadyBorder(ItemStack is) {
        is.setAmount(1);
        borderReadyItem = is;
    }

    /**
     * See above
     * @param mat Material of the item
     * @param nameArgs <b>Nullable</b> Name of the Item
     */
    public void setReadyBorder(Material mat, String... nameArgs) {
        ItemStack is = new ItemStack(mat);
        if (nameArgs.length > 0) {
            String name = nameArgs[0];
            ItemMeta im = is.getItemMeta();
            im.setDisplayName(Util.Color(name));
            is.setItemMeta(im);
        }
        borderReadyItem = is;
    }

    /**
     * Item that the player has to click to show he is/isn't ready
     * @param is The Item
     */
    public void setReadyItem(ItemStack is) {
        is.setAmount(1);
        setReadyItem = is;
    }

    /**
     * See above
     * @param mat Material of the Item
     * @param nameArgs <b>Nullable</b> Name of the item
     */
    public void setReadyItem(Material mat, String... nameArgs) {
        ItemStack is = new ItemStack(mat);
        if (nameArgs.length > 0) {
            String name = nameArgs[0];
            ItemMeta im = is.getItemMeta();
            im.setDisplayName(Util.Color(name));
            is.setItemMeta(im);
        }
        setReadyItem = is;
    }

    /**
     * This event will be triggered once an item will be added to the trade
     * @param onItemAdd Function that handles the TradeItemAddEvent
     */
    public void setOnItemAdd(Consumer<TradeItemAddEvent> onItemAdd) {
        this.onItemAdd = onItemAdd;
    }

    /**
     * This event will be triggered once an item will be removed from the trade
     * @param onItemRemove Function that handles the TradeItemRemoveEvent
     */
    public void setOnItemRemove(Consumer<TradeItemRemoveEvent> onItemRemove) {
        this.onItemRemove = onItemRemove;
    }

    /**
     * Force sets the Player ready/not ready
     * @param p player to set ready
     */
    public void setReady(Player p, boolean isReady) {
        if (!(p.equals(p1) || p.equals(p2))) {
            throw new InvalidPlayerException();
        }
        ready.replace(p, ready.get(p), isReady);
        if (ready.get(p1) && ready.get(p2)) {
            if (!invSpaceCheck()) {
                ready.replace(p1, true, false);
                ready.replace(p1, true, false);
                update();
                return;
            }
            completeTrade(false);
        } else {
            if (doCountdown) {
                doCountdown = false;
                Bukkit.getScheduler().cancelTask(countdownTask);
            }
        }
        update();
    }

    public void setTradeCompleteMess(String message) {
        tradeCompleteMess = message;
    }

    /**
     * Sets the countdown message, use %sec as a placeholder for the remaining seconds
     * @param message - The countdown message
     */
    public void setCountdownMessage(String message) {
        countdownMessage = message;
    }

    /**
     * Sets the length of the countdown of the trade before it completes
     * @param countdown - countdown in seconds
     */
    public void setCountdown(int countdown) {
        this.countdownTask = countdown;
    }

    /**
     * Don't use this method. Use addToTrade or removeFromTrade instead!
     * Only reason this method is here because it's from the Window interface!
     * @param mat null
     * @param name null
     * @param slot null
     * @param pageArgs null
     * @return null
     */
    @Deprecated
    @Override
    public GuiItem setItemStack(Material mat, String name, int slot, Integer... pageArgs) {
        return null;
    }

    /**
     * Don't use this method. Use addToTrade or removeFromTrade instead!
     * Only reason this method is here because it's from the Window interface!
     * @param is null
     * @param slot null
     * @param pageArgs null
     * @return null
     */
    @Deprecated
    @Override
    public GuiItem setItemStack(ItemStack is, int slot, Integer... pageArgs) {
        return null;
    }

    /*
    ========================================================
                            Getters
    ========================================================
     */

    /**
     * Returns the inv of a provided player
     * @param p the player you want the inventory of
     * @return the inv of the provided player
     */
    public Inventory getInv(Player p) {
        if (p.equals(p1)) {
            return invP1;
        } else if (p.equals(p2)) {
            return invP2;
        } else {
            throw new InvalidItemException();
        }
    }

    /**
     * Don't use this method, use getInv(Player p) instead !
     * @return null
     */
    @Deprecated
    @Override
    public Inventory getInv() {
        return null;
    }

    /**
     * Get Items player offers to trade
     * @param p the player you want the items of
     * @return The items a provided player offers
     */
    public ItemStack[] getItems(Player p) {
        if (p.equals(p1)) {
            return p1Items;
        } else if (p.equals(p2)) {
            return p2Items;
        } else {
            throw new InvalidItemException();
        }
    }


    private int getFirstFree(ItemStack[] items) {
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null) {
                return i;
            }
        }
        return -1;
    }

    private int getFirstFree(Player p) {
        ItemStack[] items;
        if (p.equals(p1)) {
            items = p1Items;
        } else if (p.equals(p2)) {
            items = p2Items;
        } else {
            throw new InvalidPlayerException();
        }
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null) {
                return i;
            }
        }
        return -1;
    }

    /*
    ========================================================
                        Event Handlers
    ========================================================
     */

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
        // P1 Inventory
        if (clicked.equals(p1.getInventory())) {
            // If the player is not the inv owner
            if (!e.getWhoClicked().equals(p1)) {
                return;
            }
            ItemStack is = e.getCurrentItem();
            //If no item is clicked
            if (is == null || is.getType().equals(Material.AIR)) {
                return;
            }
            // add the clicked item to the trade
            addToTrade(p1, is);
            return;
        }
        // Same for Player 2
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
        // Cancel hotbar swapping
        if (e.getAction().equals(InventoryAction.HOTBAR_SWAP)) {
            e.setCancelled(true);
            return;
        }
        // If the clicked inv is not one of the trade windows
        if (!(clicked.equals(invP1) || clicked.equals(invP2))) {
            return;
        }
        ItemStack is = e.getCurrentItem();
        // if no items are clicked
        if (is == null || is.getType().equals(Material.AIR)) {
            return;
        }
        e.setCancelled(true);
        // If the item is a item p1 offers
        if (Util.contains(p1Items, is) && e.getWhoClicked().equals(p1)) {
            removeFromTrade(p1, is);
        // if the item is a item p2 offers
        } else if (Util.contains(p2Items, is) && e.getWhoClicked().equals(p2)) {
            removeFromTrade(p2, is);
        // Check if its the ready button
        } else if (is.equals(setReadyItem)) {
            setReady(p, !ready.get(p));
        }
    }

    @EventHandler
    @Override
    // Cancel inv close if the it's not the inv owner who closes the inv
    void onInvClose(InventoryCloseEvent e) {
        if (e.getInventory().equals(invP1)) {
            if (!e.getPlayer().equals(p1)) {
                return;
            } else {
                unregister();
                p2.closeInventory();
            }
        } else if (e.getInventory().equals(invP2)) {
            if (!e.getPlayer().equals(p2)) {
                return;
            } else {
                unregister();
                p1.closeInventory();
            }
        }
    }
}