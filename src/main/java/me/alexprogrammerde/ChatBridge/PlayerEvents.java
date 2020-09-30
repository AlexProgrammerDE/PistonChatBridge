package me.alexprogrammerde.ChatBridge;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerEvents implements Listener {
    DiscordMain discord;
    public PlayerEvents(DiscordMain discord) {
        this.discord = discord;
    }

    @EventHandler
    public void onMessage(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        discord.sendMessage(player, event.getMessage());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        discord.sendMessage(event.getJoinMessage());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        discord.sendMessage(event.getQuitMessage());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        discord.sendMessage(event.getDeathMessage());
    }
}
