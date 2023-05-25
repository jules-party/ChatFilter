package zoink.jule.chatfilter;

import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import zoink.jule.chatfilter.Util.Filter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChatFilter extends JavaPlugin {

    @Override
    public void onEnable() {
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        Bukkit.getPluginManager().registerEvents(new Filter(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
