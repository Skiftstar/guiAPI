package Yukami.guiAPI;

import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    public ConsoleCommandSender console;

    public void onEnable() {
        instance = this;
        Server server = this.getServer();
        console = server.getConsoleSender();
    }

    public static Main getInstance() {
        return instance;
    }
}
