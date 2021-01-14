package me.alexprogrammerde.pistonchatbridge;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ConnectEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.rest.util.Color;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;
import java.util.Optional;

public class DiscordMain {
    private GatewayDiscordClient gateway;
    protected MessageChannel channel;

    private final PistonChatBridge plugin;

    protected boolean scheduleTasks;
    protected boolean hasStarted;

    public DiscordMain(PistonChatBridge plugin) {
        this.plugin = plugin;
    }

    public void main() {
        FileConfiguration config = plugin.getConfig();

        DiscordClient client = DiscordClient.create(config.getString("token"));
        gateway = Objects.requireNonNull(client.login().block());

        channel = gateway.getChannelById(Snowflake.of(config.getString("channel"))).ofType(MessageChannel.class).block();

        gateway.updatePresence(Presence.online(Activity.playing(config.getString("url")))).subscribe();

        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            if (scheduleTasks) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Message message = event.getMessage();

                        if (message.getChannel().block() instanceof GuildMessageChannel) {
                            GuildMessageChannel messageChannel = Objects.requireNonNull((GuildMessageChannel) message.getChannel().block());

                            Optional<User> author = message.getAuthor();

                            if (author.isPresent()
                                    && !author.get().getId().equals(gateway.getSelfId())
                                    && messageChannel.getId().equals(Snowflake.of(config.getString("channel")))) {
                                Color roleColor = Objects.requireNonNull(Objects.requireNonNull(message.getAuthorAsMember().block()).getHighestRole().block()).getColor();

                                ChatColor roleChatColor = ColorUtil.fromRGB(roleColor);

                                Bukkit.broadcastMessage(
                                        "["
                                                + roleChatColor
                                                + Objects.requireNonNull(Objects.requireNonNull(message.getAuthorAsMember().block()).getHighestRole().block()).getName()
                                                + ChatColor.RESET
                                                + "] "
                                                + ChatColor.BOLD
                                                + Objects.requireNonNull(message.getAuthorAsMember().block()).getDisplayName()
                                                + " > "
                                                + message.getContent());
                            }
                        }
                    }
                }.runTaskAsynchronously(plugin);
            }
        });

        gateway.on(ConnectEvent.class).subscribe(event -> {
            if (scheduleTasks) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        hasStarted = true;
                        channel.createEmbed(spec -> spec
                                .setTitle("Server started!")
                                .setColor(Color.GREEN)).block();
                    }
                }.runTaskAsynchronously(plugin);
            }
        });

        gateway.onDisconnect().block();
    }

    public void sendMessage(Player player, String message) {
        if (scheduleTasks) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    channel.createEmbed(spec -> spec
                            .setTitle("**" + ChatColor.stripColor(player.getDisplayName())
                                    .replace("@", "(at)") + "** > " + message.replace("@", "(at)"))
                            .setColor(Color.YELLOW)).block();
                }
            }.runTaskAsynchronously(plugin);
        }
    }

    public void sendMessage(String message) {
        if (scheduleTasks) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    channel.createEmbed(spec -> spec
                            .setTitle("**" + ChatColor.stripColor(message)
                                    .replace("@", "(at)") + "**")
                            .setColor(Color.YELLOW)).block();
                }
            }.runTaskAsynchronously(plugin);
        }
    }
}
