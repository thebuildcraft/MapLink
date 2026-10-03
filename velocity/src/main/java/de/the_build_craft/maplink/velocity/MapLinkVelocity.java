/*
 *    This file is part of the Map Link mod
 *    licensed under the GNU GPL v3 License.
 *
 *    Copyright (C) 2026  Leander Knüttel and contributors
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.the_build_craft.maplink.velocity;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Tells the Map Link mod which web map belongs to the server behind the proxy that the player is on.
 * Sends UTF-8 JSON on the "maplink:server" channel, see ServerMapInfo in the mod.
 *
 * @author d0ddn
 * @version 03.10.2026
 */
@Plugin(id = "maplink-velocity", name = "Map Link Velocity", version = "1.0.0",
        description = "Tells the Map Link mod which web map belongs to which server behind the proxy",
        url = "https://github.com/thebuildcraft/MapLink", authors = {"d0ddn"})
public class MapLinkVelocity {
    public static final MinecraftChannelIdentifier CHANNEL = MinecraftChannelIdentifier.create("maplink", "server");
    private static final List<String> MAP_TYPES = List.of("squaremap", "bluemap", "dynmap", "pl3xmap", "liveatlas");
    /** the client may still be loading the world right after the switch, so the info is sent a few times */
    private static final long[] SEND_DELAYS_MS = {500, 1500, 3000};

    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDirectory;
    private volatile Map<String, ServerMap> serverMaps = new HashMap<>();

    private record ServerMap(String type, String link) {
    }

    @Inject
    public MapLinkVelocity(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        proxy.getChannelRegistrar().register(CHANNEL);
        loadConfig();
        proxy.getCommandManager().register(
                proxy.getCommandManager().metaBuilder("maplinkvelocity").plugin(this).build(),
                new ReloadCommand());
    }

    @Subscribe
    public void onServerPostConnect(ServerPostConnectEvent event) {
        Player player = event.getPlayer();
        player.getCurrentServer().ifPresent(connection -> {
            String serverName = connection.getServerInfo().getName();
            byte[] message = createMessage(serverName);
            for (long delay : SEND_DELAYS_MS) {
                proxy.getScheduler().buildTask(this, () -> send(player, serverName, message))
                        .delay(delay, TimeUnit.MILLISECONDS).schedule();
            }
        });
    }

    /** only the proxy may tell the client which web map to use */
    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (CHANNEL.equals(event.getIdentifier()) && event.getSource() instanceof ServerConnection) {
            event.setResult(PluginMessageEvent.ForwardResult.handled());
        }
    }

    private void send(Player player, String serverName, byte[] message) {
        if (!player.isActive()) return;
        Optional<ServerConnection> currentServer = player.getCurrentServer();
        if (currentServer.isEmpty() || !currentServer.get().getServerInfo().getName().equals(serverName)) return;
        player.sendPluginMessage(CHANNEL, message);
    }

    private byte[] createMessage(String serverName) {
        JsonObject json = new JsonObject();
        json.addProperty("v", 1);
        json.addProperty("server", serverName);
        ServerMap serverMap = serverMaps.get(serverName.toLowerCase(Locale.ROOT));
        if (serverMap != null) {
            json.addProperty("type", serverMap.type());
            json.addProperty("link", serverMap.link());
        }
        return json.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void loadConfig() {
        try {
            Path configFile = dataDirectory.resolve("config.properties");
            if (Files.notExists(configFile)) {
                Files.createDirectories(dataDirectory);
                try (InputStream defaultConfig = getClass().getResourceAsStream("/config.properties")) {
                    if (defaultConfig != null) Files.copy(defaultConfig, configFile);
                }
            }

            Properties properties = new Properties();
            try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }

            Map<String, ServerMap> newServerMaps = new HashMap<>();
            for (String serverName : properties.stringPropertyNames()) {
                String[] value = properties.getProperty(serverName).trim().split("\\s+", 2);
                String type = value[0].toLowerCase(Locale.ROOT);
                String link = value.length > 1 ? value[1].trim() : "";
                if (proxy.getServer(serverName).isEmpty()) {
                    logger.warn("Unknown server in config.properties: {}", serverName);
                }
                if (type.equals("none")) {
                    newServerMaps.put(serverName.toLowerCase(Locale.ROOT), new ServerMap("none", ""));
                } else if (!MAP_TYPES.contains(type) || link.isEmpty()) {
                    logger.warn("Invalid entry for {} in config.properties, expected \"<{}> <link>\" or \"none\"",
                            serverName, String.join("|", MAP_TYPES));
                } else {
                    newServerMaps.put(serverName.toLowerCase(Locale.ROOT), new ServerMap(type, link));
                }
            }
            serverMaps = newServerMaps;
            logger.info("Loaded web maps for {} server(s)", newServerMaps.size());
        } catch (IOException e) {
            logger.error("Could not load config.properties", e);
        }
    }

    private class ReloadCommand implements SimpleCommand {
        @Override
        public void execute(Invocation invocation) {
            if (invocation.arguments().length == 1 && invocation.arguments()[0].equalsIgnoreCase("reload")) {
                loadConfig();
                for (Player player : proxy.getAllPlayers()) {
                    player.getCurrentServer().ifPresent(connection -> {
                        String serverName = connection.getServerInfo().getName();
                        send(player, serverName, createMessage(serverName));
                    });
                }
                invocation.source().sendMessage(Component.text("Map Link Velocity: reloaded, web maps: " + serverMaps.size()));
            } else {
                invocation.source().sendMessage(Component.text("Usage: /maplinkvelocity reload"));
            }
        }

        @Override
        public List<String> suggest(Invocation invocation) {
            return List.of("reload");
        }

        @Override
        public boolean hasPermission(Invocation invocation) {
            return invocation.source().hasPermission("maplink.reload");
        }
    }
}
