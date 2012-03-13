package com.precipicegames.zeryl.randomtp;

import java.io.File;
import java.util.HashMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.configuration.file.FileConfiguration;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class RandomTP extends JavaPlugin {

    public FileConfiguration config;
    private File configFile = new File(this.getDataFolder(), "config.yml");
    private HashMap<Player, Long> lastrtp = new HashMap<Player, Long>();

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        PluginDescriptionFile pdf = this.getDescription();
        buildConfig();

        System.out.println(pdf.getName() + " is now enabled");
    }

    @Override
    public void onDisable() {
        PluginDescriptionFile pdf = this.getDescription();
        saveConfig();
        System.out.println(pdf.getName() + " is now disbled");
    }

    private void buildConfig() {
        config = getConfig();

        if (!config.isSet("time"))
            config.set("time", 5);

        if (!config.isSet("max-coords"))
            config.set("max-coords", 5000);

        saveConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("rtp")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("precipice.canrtp")) {
                    if (lastrtp.containsKey(player)) {
                        if (player.isOp())
                            randomTP(player);

                        Timestamp last = new Timestamp(lastrtp.get(player) + (config.getLong("time") * 60 * 1000));
                        Date date = new Date();
                        Timestamp now = new Timestamp(date.getTime());

                        if (now.after(last)) {
                            randomTP(player);
                        } else {
                            player.sendMessage("Sorry, you must wait to randomly teleport again");
                        }
                    } else {
                        randomTP(player);
                    }
                } else {
                    player.sendMessage("You don't have permission to run this command");
                }
            } else {
                sender.sendMessage("This doesn't make sense to run from the console.");
            }
        }
        return true;
    }

    private void randomTP(Player player) {
        Date date = new Date();
        Random rand = new Random();

        if (lastrtp.containsKey(player)) {
            lastrtp.remove(player);
        }

        lastrtp.put(player, date.getTime());

        int max_coords = config.getInt("max-coords");
        int randx = rand.nextInt(max_coords * 2) - max_coords;
        int randz = rand.nextInt(max_coords * 2) - max_coords;
        int randy = rand.nextInt(player.getWorld().getMaxHeight()) + 1;

        Block block = player.getWorld().getBlockAt(randx, randy, randz);

        if (block.getTypeId() != 0)
            randy = player.getWorld().getHighestBlockYAt(randx, randz);

        Location location = new Location(player.getWorld(), (double) randx, (double) randy, (double) randz);
        player.teleport(location);
    }
}