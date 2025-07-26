/*
 *    This file is part of the Remote player waypoints for Xaero's Map mod
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

package de.the_build_craft.remote_player_waypoints_for_xaero.common;

import de.the_build_craft.remote_player_waypoints_for_xaero.common.clientMapHandlers.ClientMapHandler;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.connections.*;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.wrappers.Text;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.wrappers.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;

import java.io.IOException;
import java.util.*;

/**
 * Threaded task that is run once every few seconds to fetch data from the online map
 * and update the local maps
 *
 * @author ewpratten
 * @author eatmyvenom
 * @author TheMrEngMan
 * @author Leander Knüttel
 * @version 26.07.2025
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

    public void run() {
        try{
            runUpdate();
        }
        catch (Exception e){
            e.printStackTrace();
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

        AbstractModInitializer.enabled = CommonModConfig.Instance.enabled();

        // Skip if disabled
        if (!AbstractModInitializer.enabled) {
            Reset();
            if (ClientMapHandler.getInstance() != null) {
                ClientMapHandler.getInstance().removeAllPlayerWaypoints();
                ClientMapHandler.getInstance().removeAllMarkerWaypoints();
                ClientMapHandler.getInstance().removeAllAreaMarkers(true);
            }
            return;
        }

        // Get the IP of this server
        String serverIP = mc.getCurrentServer().ip.toLowerCase(Locale.ROOT);

        if (!Objects.equals(currentServerIP, serverIP)){
            if (ClientMapHandler.getInstance() != null) ClientMapHandler.getInstance().removeAllAreaMarkers(true);
            currentServerIP = serverIP;
            Reset();
            AbstractModInitializer.LOGGER.info("Server ip has changed!");
        }

        if (AbstractModInitializer.getConnection() == null){
            try {
                CommonModConfig.ServerEntry serverEntry = CommonModConfig.Instance.getCurrentServerEntry();

                if (Objects.equals(serverEntry, null)) {
                    if (!(CommonModConfig.Instance.ignoredServers().contains(serverIP) || cantFindServerErrorWasShown)) {
                        String message = "[" + AbstractModInitializer.MOD_NAME + "]: " +
                                "Could not find an online map link for this server. " +
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
                if (Objects.requireNonNull(serverEntry.maptype) == CommonModConfig.ServerEntry.Maptype.Dynmap) {
                    AbstractModInitializer.setConnection(new DynmapConnection(serverEntry, this));
                } else if (serverEntry.maptype == CommonModConfig.ServerEntry.Maptype.Squaremap) {
                    AbstractModInitializer.setConnection(new SquareMapConnection(serverEntry, this));
                } else if (serverEntry.maptype == CommonModConfig.ServerEntry.Maptype.Bluemap) {
                    AbstractModInitializer.setConnection(new BlueMapConnection(serverEntry, this));
                } else if (serverEntry.maptype == CommonModConfig.ServerEntry.Maptype.Pl3xMap) {
                    AbstractModInitializer.setConnection(new Pl3xMapConnection(serverEntry, this));
                } else if (serverEntry.maptype == CommonModConfig.ServerEntry.Maptype.LiveAtlas) {
                    AbstractModInitializer.setConnection(new LiveAtlasConnection(serverEntry, this));
                } else {
                    throw new IllegalStateException("Unexpected value: " + serverEntry.maptype);
                }
            } catch (Exception e) {
                if (!connectionErrorWasShown){
                    connectionErrorWasShown = true;
                    Utils.sendErrorToClientChat("[" + AbstractModInitializer.MOD_NAME + "]: " +
                            "Error while connecting to the online map. " +
                            "Please check you config or report a bug.");
                    e.printStackTrace();
                }
                AbstractModInitializer.connected = false;
                return;
            }
        }

        // Get a list of all player's positions
        try {
            // this must be run no matter if it's activated in the config, to get the "currentDimension" and AFK info
            FastUpdateTask.getInstance().updateFromOnlineMap(AbstractModInitializer.getConnection().getPlayerPositions());
        } catch (IOException e) {
            if (!cantGetPlayerPositionsErrorWasShown){
                cantGetPlayerPositionsErrorWasShown = true;
                Utils.sendErrorToClientChat("[" + AbstractModInitializer.MOD_NAME + "]: " +
                        "Failed to make online map request (for player waypoints). Please check your config (probably your link...) or report a bug.");
            }
            e.printStackTrace();
            AbstractModInitializer.setConnection(null);
            return;
        }

        if (CommonModConfig.Instance.enableMarkerWaypoints() || CommonModConfig.Instance.enableAreaMarkers()){
            try {
                AbstractModInitializer.getConnection().getWaypointPositions();
            } catch (IOException e) {
                if (!cantGetMarkerPositionsErrorWasShown) {
                    cantGetMarkerPositionsErrorWasShown = true;
                    Utils.sendErrorToClientChat("[" + AbstractModInitializer.MOD_NAME + "]: " +
                            "Failed to make online map request (for marker waypoints). Please check your config (probably your link...) or report a bug.");
                }
                e.printStackTrace();
            }
        } else if (ClientMapHandler.getInstance() != null) {
            ClientMapHandler.getInstance().removeAllMarkerWaypoints();
            ClientMapHandler.getInstance().removeAllAreaMarkers(true);
        }

        AbstractModInitializer.connected = true;
        AbstractModInitializer.AfkColor = CommonModConfig.Instance.AfkColor();
        AbstractModInitializer.unknownAfkStateColor = CommonModConfig.Instance.unknownAfkStateColor();
        AbstractModInitializer.showAfkInTabList = CommonModConfig.Instance.showAfkInTabList();
        AbstractModInitializer.showAfkTimeInTabList = CommonModConfig.Instance.showAfkTimeInTabList();
        AbstractModInitializer.hideAfkMinutes = CommonModConfig.Instance.hideAfkMinutes();

        if (CommonModConfig.Instance.updateDelay() != AbstractModInitializer.TimerDelay){
            AbstractModInitializer.setUpdateDelay(CommonModConfig.Instance.updateDelay());
        }
    }

    private void Reset() {
        AbstractModInitializer.setConnection(null);
        connectionErrorWasShown = false;
        cantFindServerErrorWasShown = false;
        cantGetPlayerPositionsErrorWasShown = false;
        cantGetMarkerPositionsErrorWasShown = false;
        linkBrokenErrorWasShown = false;
    }
}