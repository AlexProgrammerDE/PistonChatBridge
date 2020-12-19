package me.alexprogrammerde.pistonchatbridge;

import discord4j.rest.util.Color;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Logger;

public class PistonChatBridge extends JavaPlugin {
    private Logger log;
    private DiscordMain discord;
    private PlayerListener listener;
    private String prefix;

    public void onEnable() {
        log = getLogger();
        prefix = "" + ChatColor.GREEN;
        
        log.info(prefix + "Loading config.");
        saveDefaultConfig();

        log.info(prefix + "Starting discord bridge");
        discord = new DiscordMain(this);
        discord.scheduleTasks = true;

        new BukkitRunnable() {
            @Override
            public void run() {
                discord.main();
            }
        }.runTaskAsynchronously(this);

        log.info(prefix + "Registering listeners");
        listener = new PlayerListener(discord);
        getServer().getPluginManager().registerEvents(listener,this);
    }

    @Override
    public void onDisable() {
        log.info(prefix + "Telling discord bridge not to send messages anymore");
        discord.scheduleTasks = false;

        log.info(prefix + "Unregistering all listeners");
        HandlerList.unregisterAll(listener);

        log.info(prefix + "Sending shutdown message");
        if (discord.hasStarted) {
            discord.channel.createEmbed(spec -> spec
                    .setTitle("Server stopping!")
                    .setColor(Color.RED)).block();
        } else {
            log.info("Oh no! The client hasn't even started yet! Skipping that message.");
        }

        log.info(prefix + "Killing discord task");
        Bukkit.getScheduler().cancelTasks(this);

        log.info(prefix + "Finished unloading! Goodbye!");
    }
}
