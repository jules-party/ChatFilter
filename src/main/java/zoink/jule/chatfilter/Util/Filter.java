package zoink.jule.chatfilter.Util;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import zoink.jule.chatfilter.ChatFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Filter implements Listener {
    private final ChatFilter plugin;

    public Filter(ChatFilter plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    private void onPlayerChat(AsyncChatEvent chatEvent) {
        final Player player = chatEvent.getPlayer();
        String wordsFilePath = plugin.getDataFolder().getAbsolutePath() + "/words.txt";
        File wordsFile = new File(wordsFilePath);

        if (!wordsFile.exists()) {
            try {
                wordsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        List<String> filterWords = getFilterWords(wordsFilePath);

        // Filter Message
        if(stringContainsItemFromList(chatEvent.message().toString(), filterWords)) {
            player.sendMessage(ChatColor.RED + "I see that.");
            chatEvent.setCancelled(true);
        } else {
            return;
        }

        String playerDataPath = plugin.getDataFolder().getAbsolutePath() + "/players.yml";
        File playerDataFile = new File(playerDataPath);

        // Make players.yml if it doesn't exist
        if (!playerDataFile.exists()) {
            try {
                playerDataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileConfiguration playerData = YamlConfiguration.loadConfiguration(playerDataFile);

        // Add 1 offense to the players total offenses
        String playerUUID = player.getUniqueId().toString();
        playerData.set(playerUUID + ".offenses", playerData.getInt(playerUUID + ".offenses") + 1);
        int playerKicks = playerData.getInt(playerUUID + ".kicks");
        int playerOffenses = playerData.getInt(playerUUID + ".offenses");
        int playerBans = playerData.getInt(playerUUID + ".bans");
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);

        if (playerOffenses == 10 && playerKicks != 5) {
            playerData.set(playerUUID + ".offenses", 0);
            playerData.set(playerUUID + ".kicks", playerData.getInt(playerUUID + ".kicks") + 1);
            playerKicks = playerData.getInt(playerUUID + ".kicks");
            if (playerKicks != 5)
                Bukkit.getScheduler().runTask(plugin, () -> player.kickPlayer(ChatColor.RED + "Kicked for Saying a Filtered Word 10 Times!"));
        }

        if (playerKicks == 5) {
            // Ban Message should look something like this
            /*
            Been kicked too many times by ChatFilter!
            Banned on Thu May 25 10:40:50 EST 2023 for 6 hours
            Talk about your ban here: example.com
             */
            playerData.set(playerUUID + ".kicks", 0);
            playerData.set(playerUUID + ".bans", playerBans + 1);

            playerBans = playerData.getInt(playerUUID + ".bans");
            Date banTime = new Date(System.currentTimeMillis() + 60 * 60 * (playerBans * 6000));

            banList.addBan(player.getName(), getBanMessage(banTime.toString(), (playerBans * 6)), banTime, null);

            int finalPlayerBans = playerBans;
            Bukkit.getScheduler().runTask(plugin, () -> player.kickPlayer(getBanMessage(banTime.toString(), (finalPlayerBans * 6))));
        }
        try {
            playerData.save(playerDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getFilterWords(String filename) {
        BufferedReader reader;
        List<String> words = new ArrayList<String>();

        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();

            while (line != null) {
                words.add(line);
                line = reader.readLine();
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return words;
    }

    public String getBanMessage(String date, int banTime) {
        String banMessage = plugin.getConfig().getString("banMessage");
        banMessage = ChatColor.translateAlternateColorCodes('&', banMessage);
        banMessage = banMessage.replace("{DATE}", date).replace("{TIME}", String.valueOf(banTime));
        return banMessage;
    }

    public boolean stringContainsItemFromList(String input, List<String> items) {
        return items.stream().anyMatch(word -> input.contains(word));
    }
}
