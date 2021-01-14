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

    @Override
    public void onEnable() {
        log = getLogger();

        log.info(ChatColor.GREEN + "Loading config.");
        saveDefaultConfig();

        log.info(ChatColor.GREEN + "Starting discord bridge");
        discord = new DiscordMain(this);
        discord.scheduleTasks = true;

        new BukkitRunnable() {
            @Override
            public void run() {
                discord.main();
            }
        }.runTaskAsynchronously(this);

        log.info(ChatColor.GREEN + "Registering listeners");
        listener = new PlayerListener(discord);
        getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onDisable() {
        log.info(ChatColor.GREEN + "Telling discord bridge not to send messages anymore");
        discord.scheduleTasks = false;

        log.info(ChatColor.GREEN + "Unregistering all listeners");
        HandlerList.unregisterAll(listener);

        log.info(ChatColor.GREEN + "Sending shutdown message");
        if (discord.hasStarted) {
            discord.channel.createEmbed(spec -> spec
                    .setTitle("Server stopping!")
                    .setColor(Color.RED)).block();
        } else {
            log.info("Oh no! The client hasn't even started yet! Skipping that message.");
        }

        log.info(ChatColor.GREEN + "Killing discord task");
        Bukkit.getScheduler().cancelTasks(this);

        log.info(ChatColor.GREEN + "Finished unloading! Goodbye!");
    }
}
