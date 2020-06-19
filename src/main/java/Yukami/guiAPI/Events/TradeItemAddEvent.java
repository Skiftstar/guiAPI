package Yukami.guiAPI.Events;

import Yukami.guiAPI.TradeWindow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TradeItemAddEvent {

    private final TradeWindow window;
    private final Player adder;
    private final Player tradePartner;
    private ItemStack item;
    private boolean canceled = false;
    private boolean removeFromInv = true;
    private FullTradeAction onTradeFull = FullTradeAction.DONT_ADD;

    public TradeItemAddEvent(TradeWindow window, Player adder, Player tradePartner, ItemStack item) {
        this.window = window;
        this.adder = adder;
        this.tradePartner = tradePartner;
        this.item = item;
    }

    public void setRemoveFromInv(boolean removeFromInv) {
        this.removeFromInv = removeFromInv;
    }

    public void setOnTradeFull(FullTradeAction onTradeFull) {
        this.onTradeFull = onTradeFull;
    }

    public boolean isRemoveFromInv() {
        return removeFromInv;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public TradeWindow getWindow() {
        return window;
    }

    public Player getPlayer() {
        return adder;
    }

    public Player getTradePartner() {
        return tradePartner;
    }

    public ItemStack getItem() {
        return item;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public FullTradeAction getOnTradeFull() {
        return onTradeFull;
    }
}

