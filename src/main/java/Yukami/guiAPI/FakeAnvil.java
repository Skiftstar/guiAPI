package Yukami.guiAPI;

import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.plugin.java.JavaPlugin;

class FakeAnvil extends ContainerAnvil {

    private EntityPlayer p;
    private String title;

    public FakeAnvil(Player player, String title) {
        super(((CraftPlayer) player).getHandle().nextContainerCounter(), ((CraftPlayer) player).getHandle().inventory, ContainerAccess.at(((CraftPlayer) player).getHandle().world, new BlockPosition(0,0,0)));
        this.checkReachable = false;
        p = ((CraftPlayer) player).getHandle();
        this.title = title;
        setTitle(new ChatMessage(Util.Color(title)));
    }

    public AnvilInventory getInventory() {
        return (AnvilInventory) getBukkitView().getTopInventory();
    }

    public void open() {
        p.activeContainer = this;
        p.activeContainer.addSlotListener(p);

        p.playerConnection.sendPacket(new PacketPlayOutOpenWindow(windowId, Containers.ANVIL, new ChatMessage(Util.Color(title))));
    }

    @Override
    public void e() {
        super.e();
        this.levelCost.set(0);
    }


}
