package Yukami.guiAPI.Events;

import Yukami.guiAPI.TradeWindow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TradeItemRemoveEvent {

    private final TradeWindow window;
    private final Player adder;
    private final Player tradePartner;
    private final ItemStack item;
    private boolean canceled = false;
    private boolean addBackToInv = true;

    public TradeItemRemoveEvent(TradeWindow window, Player adder, Player tradePartner, ItemStack item) {
        this.window = window;
        this.adder = adder;
        this.tradePartner = tradePartner;
        this.item = item;
    }

    public void setAddBackToInv(boolean addBackToInv) {
        this.addBackToInv = addBackToInv;
    }

    public boolean isAddBackToInv() {
        return addBackToInv;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
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


}
