package me.alexprogrammerde.ChatBridge;

import discord4j.rest.util.Color;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Logger;

public class PistonChatBridge extends JavaPlugin {
    Logger console;
    DiscordMain discord;
    PlayerEvents listener;
    String prefix;

    public void onEnable() {
        console = getLogger();
        prefix = "" + ChatColor.GREEN;
        
        console.info(prefix + "Loading config.");
        saveDefaultConfig();

        console.info(prefix + "Starting discord bridge");
        discord = new DiscordMain(this);
        discord.scheduletasks = true;

        new BukkitRunnable() {
            @Override
            public void run() {
                discord.main();
            }
        }.runTaskAsynchronously(this);

        console.info(prefix + "Registering listeners");
        listener = new PlayerEvents(discord);
        getServer().getPluginManager().registerEvents(listener,this);
    }

    @Override
    public void onDisable() {
        console.info(prefix + "Telling discord bridge not to send messages anymore");
        discord.scheduletasks = false;

        console.info(prefix + "Unregistering all listeners");
        HandlerList.unregisterAll(listener);

        console.info(prefix + "Sending shutdown message");
        if (discord.hasstarted) {
            discord.channel.createEmbed(spec -> spec
                    .setTitle("Server stopping!")
                    .setColor(Color.RED)).block();
        } else {
            console.info("Oh no! The client hasn't even started yet! Skipping that message.");
        }

        console.info(prefix + "Killing discord task");
        Bukkit.getScheduler().cancelTasks(this);

        console.info(prefix + "Finished unloading! Goodbye!");
    }
}
