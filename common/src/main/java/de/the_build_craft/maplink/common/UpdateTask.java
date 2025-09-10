/*
 *    This file is part of the Map Link mod
 *    licensed under the GNU GPL v3 License.
 *    (some parts of this file are originally from "RemotePlayers" by ewpratten)
 *
 *    Copyright (C) 2024 - 2025  Leander Knüttel and contributors
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

package de.the_build_craft.maplink.common;

import de.the_build_craft.maplink.common.clientMapHandlers.ClientMapHandler;
import de.the_build_craft.maplink.common.connections.*;
import de.the_build_craft.maplink.common.wrappers.Text;
import de.the_build_craft.maplink.common.wrappers.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;

import java.util.*;

import static de.the_build_craft.maplink.common.AbstractModInitializer.LOGGER;
import static de.the_build_craft.maplink.common.CommonModConfig.*;

/**
 * Threaded task that is run once every few seconds to fetch data from the online map
 * and update the local maps
 *
 * @author ewpratten
 * @author eatmyvenom
 * @author TheMrEngMan
 * @author Leander Knüttel
 * @version 06.09.2025
 */
public class UpdateTask {
    private final Minecraft mc;

    public UpdateTask() {
        this.mc = Minecraft.getInstance();
    }

    private boolean connectionErrorWasShown = false;
    private boolean cantFindServerErrorWasShown = false;
    private boolean cantGetPlayerPositionsErrorWasShown = false;
    private boolean cantGetMarkerPositionsErrorWasShown = false;
    public boolean linkBrokenErrorWasShown = false;

    private String currentServerIP = "";

    public static int nextUpdateDelay = 1000;

    public void run() {
        try {
            runUpdate();
        }
        catch (Exception e) {
            LOGGER.error("Error in slow Update Task", e);
        }
        try {
            AbstractModInitializer.setUpdateDelay(nextUpdateDelay);
        } catch (Exception e) {
            LOGGER.error("Error updating update-delay!", e);
        }
    }

    private void runUpdate() {
        // Skip if not in game
        if (mc.level == null
                || mc.player == null
                || mc.cameraEntity == null
                || (mc.getSingleplayerServer() != null && !mc.getSingleplayerServer().isPublished())
                || mc.getCurrentServer() == null
                || mc.getConnection() == null
                || !mc.getConnection().getConnection().isConnected()) {
            Reset();
            return;
        }

        // Skip if disabled
        if (!config.general.enabled) {
            Reset();
            return;
        }

        // Get the IP of this server
        String serverIP = mc.getCurrentServer().ip.toLowerCase(Locale.ROOT);

        if (!Objects.equals(currentServerIP, serverIP)){
            currentServerIP = serverIP;
            Reset();
            LOGGER.info("Server ip has changed!");
        }

        if (AbstractModInitializer.getConnection() == null){
            nextUpdateDelay = 1000;
            try {
                ModConfig.ServerEntry serverEntry = getCurrentServerEntry();

                if (Objects.equals(serverEntry, null)) {
                    if (!(config.general.ignoredServers.contains(serverIP) || cantFindServerErrorWasShown)) {
                        String message = "[" + AbstractModInitializer.MOD_NAME + "]: " +
                                "Could not find a web map link for this server. " +
                                "Make sure to add it to the config. (this server ip was detected: " + serverIP + ") ";
                        if ((AbstractModInitializer.INSTANCE.loaderType == LoaderType.Fabric)
                                || (AbstractModInitializer.INSTANCE.loaderType == LoaderType.Quilt)) {
                            Utils.sendToClientChat(Text.literal(message)
                                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)).append(Text.literal("[ignore this server]")
                                            .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withBold(true)
                                                    #if MC_VER < MC_1_21_5
                                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + AbstractModInitializer.MOD_ID + " ignore_server")))));
                                                    #else
                                                    .withClickEvent(new ClickEvent.RunCommand("/" + AbstractModInitializer.MOD_ID + " ignore_server")))));
                                                    #endif
                        } else {
                            Utils.sendToClientChat(Text.literal(message)
                                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)).append(Text.literal("[ignore this server]")
                                            .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withBold(true)
                                                    #if MC_VER < MC_1_21_5
                                                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + AbstractModInitializer.MOD_ID + " ignore_server")))));
                                                    #else
                                                    .withClickEvent(new ClickEvent.SuggestCommand("/" + AbstractModInitializer.MOD_ID + " ignore_server")))));
                                                    #endif
                        }//                                                        RUN_COMMAND doesn't seem to work on Forge and NeoForge...

                        cantFindServerErrorWasShown = true;
                    }
                    AbstractModInitializer.connected = false;
                    return;
                }
                if (Objects.requireNonNull(serverEntry.maptype) == ModConfig.ServerEntry.MapType.Dynmap) {
                    AbstractModInitializer.setConnection(new DynmapConnection(serverEntry, this));
                } else if (serverEntry.maptype == ModConfig.ServerEntry.MapType.Squaremap) {
                    AbstractModInitializer.setConnection(new SquareMapConnection(serverEntry, this));
                } else if (serverEntry.maptype == ModConfig.ServerEntry.MapType.Bluemap) {
                    AbstractModInitializer.setConnection(new BlueMapConnection(serverEntry, this));
                } else if (serverEntry.maptype == ModConfig.ServerEntry.MapType.Pl3xMap) {
                    AbstractModInitializer.setConnection(new Pl3xMapConnection(serverEntry, this));
                } else if (serverEntry.maptype == ModConfig.ServerEntry.MapType.LiveAtlas) {
                    AbstractModInitializer.setConnection(new LiveAtlasConnection(serverEntry, this));
                } else {
                    throw new IllegalStateException("Unexpected value: " + serverEntry.maptype);
                }
            } catch (Exception e) {
                if (!connectionErrorWasShown){
                    connectionErrorWasShown = true;
                    Utils.sendErrorToClientChat("[" + AbstractModInitializer.MOD_NAME + "]: " +
                            "Error while connecting to the web map. " +
                            "Please check you config or report a bug.");
                    LOGGER.error("Error while connecting to the web map.", e);
                }
                AbstractModInitializer.connected = false;
                nextUpdateDelay = 1000;
                return;
            }
        }

        // Get a list of all player's positions
        try {
            // this must be run no matter if it's activated in the config, to get the "currentDimension" and AFK info
            FastUpdateTask.getInstance().updateFromOnlineMap(AbstractModInitializer.getConnection().getPlayerPositions());
        } catch (Exception e) {
            if (!cantGetPlayerPositionsErrorWasShown){
                cantGetPlayerPositionsErrorWasShown = true;
                Utils.sendErrorToClientChat("[" + AbstractModInitializer.MOD_NAME + "]: " +
                        "Failed to make web map request (for player waypoints). Please check your config (maybe your link...) or report a bug.");
            }
            LOGGER.error("Failed to get player positions from the web map.", e);
            AbstractModInitializer.setConnection(null);
            return;
        }

        if (config.general.enableMarkerWaypoints || config.general.enableAreaMarkerOverlay){
            try {
                AbstractModInitializer.getConnection().getWaypointPositions();
            } catch (Exception e) {
                if (!cantGetMarkerPositionsErrorWasShown) {
                    cantGetMarkerPositionsErrorWasShown = true;
                    Utils.sendErrorToClientChat("[" + AbstractModInitializer.MOD_NAME + "]: " +
                            "Failed to make web map request (for marker waypoints). Please check your config (maybe your link...) or report a bug.");
                }
                LOGGER.error("Failed to get marker positions from the web map.", e);
            }
        } else if (ClientMapHandler.getInstance() != null) {
            ClientMapHandler.getInstance().removeAllMarkerWaypoints();
            ClientMapHandler.getInstance().removeAllAreaMarkers(true);
        }

        AbstractModInitializer.connected = true;
    }

    private void Reset() {
        AbstractModInitializer.setConnection(null);
        ClientMapHandler.clearRegisteredPositions();
        FastUpdateTask.getInstance().clearAllPlayerPositions();
        if (ClientMapHandler.getInstance() != null) ClientMapHandler.getInstance().reset();
        connectionErrorWasShown = false;
        cantFindServerErrorWasShown = false;
        cantGetPlayerPositionsErrorWasShown = false;
        cantGetMarkerPositionsErrorWasShown = false;
        linkBrokenErrorWasShown = false;
        nextUpdateDelay = 1000;
    }
}