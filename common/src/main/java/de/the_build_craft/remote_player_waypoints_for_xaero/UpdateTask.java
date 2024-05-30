/*      Remote player waypoints for Xaero's Map
        Copyright (C) 2024  Leander Knüttel
        (some parts of this file are originally from "RemotePlayers" by ewpratten)

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>.*/

package de.the_build_craft.remote_player_waypoints_for_xaero;

import de.the_build_craft.remote_player_waypoints_for_xaero.connections.BlueMapConnection;
import de.the_build_craft.remote_player_waypoints_for_xaero.connections.DynmapConnection;
import de.the_build_craft.remote_player_waypoints_for_xaero.connections.Pl3xMapConnection;
import de.the_build_craft.remote_player_waypoints_for_xaero.connections.SquareMapConnection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import xaero.common.HudMod;
import xaero.common.XaeroMinimapSession;
import xaero.common.minimap.waypoints.WaypointWorld;
import xaero.common.minimap.waypoints.WaypointsManager;

import java.io.IOException;
import java.util.*;

/**
 * Threaded task that is run once every few seconds to fetch data from the online map
 * and update the local maps
 */
public class UpdateTask extends TimerTask {
    private final MinecraftClient mc;

    public UpdateTask() {
        this.mc = MinecraftClient.getInstance();
    }
    private static final String DEFAULT_PLAYER_SET_NAME = "RemotePlayerWaypointsForXaero_Temp";

    private boolean connectionErrorWasShown = false;
    private boolean cantFindServerErrorWasShown = false;
    private boolean cantGetPlayerPositionsErrorWasShown = false;
    public boolean linkBrokenErrorWasShown = false;

    private String currentServerIP = "";

    @Override
    public void run() {
        if (RemotePlayerWaypointsForXaero.mapModInstalled){
            var cw = WaypointsManager.getCustomWaypoints("RemotePlayerWaypointsForXaero");
            cw.clear();
        }

        RemotePlayerWaypointsForXaero.enabled = CommonModConfig.Instance.enabled();

        // Skip if disabled
        if (!CommonModConfig.Instance.enabled()) {
            Reset();
            return;
        }

        // Skip if not in game
        if (mc.isInSingleplayer() || mc.getCurrentServerEntry() == null || mc.getNetworkHandler() == null
                || !mc.getNetworkHandler().getConnection().isOpen()) {
            Reset();
            return;
        }

        // Get the IP of this server
        String serverIP = mc.getCurrentServerEntry().address.toLowerCase(Locale.ROOT);

        if (!Objects.equals(currentServerIP, serverIP)){
            currentServerIP = "";
            Reset();
            RemotePlayerWaypointsForXaero.LOGGER.info("Server ip has changed!");
        }

        if (RemotePlayerWaypointsForXaero.getConnection() == null){
            try {
                CommonModConfig.ServerEntry serverEntry = null;
                for (var server : CommonModConfig.Instance.serverEntries()){
                    if (Objects.equals(serverIP, server.ip.toLowerCase(Locale.ROOT))){
                        serverEntry = server;
                    }
                }
                if (Objects.equals(serverEntry, null)) {
                    if (!(CommonModConfig.Instance.ignoredServers().contains(serverIP) || cantFindServerErrorWasShown)) {
                        if ((RemotePlayerWaypointsForXaero.loaderType == RemotePlayerWaypointsForXaero.LoaderType.Fabric)
                                || (RemotePlayerWaypointsForXaero.loaderType == RemotePlayerWaypointsForXaero.LoaderType.Quilt)) {
                            mc.inGameHud.getChatHud().addMessage(Text.literal("[" + RemotePlayerWaypointsForXaero.MOD_NAME + "]: " +
                                            "Could not find an online map link for this server. " +
                                            "Make sure to add it to the config. (this server ip was detected: " + serverIP + ") ")
                                    .setStyle(Style.EMPTY.withColor(Formatting.GOLD)).append(Text.literal("[ignore this server]")
                                            .setStyle(Style.EMPTY.withColor(Formatting.GREEN).withBold(true)
                                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ignore_server")))));
                        } else {
                            mc.inGameHud.getChatHud().addMessage(Text.literal("[" + RemotePlayerWaypointsForXaero.MOD_NAME + "]: " +
                                            "Could not find an online map link for this server. " +
                                            "Make sure to add it to the config. (this server ip was detected: " + serverIP + ") ")
                                    .setStyle(Style.EMPTY.withColor(Formatting.GOLD)).append(Text.literal("[ignore this server]")
                                            .setStyle(Style.EMPTY.withColor(Formatting.GREEN).withBold(true)
                                                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ignore_server")))));
                        }//                                                        RUN_COMMAND doesn't seem to work on Forge and NeoForge...

                        cantFindServerErrorWasShown = true;
                    }
                    RemotePlayerWaypointsForXaero.connected = false;
                    return;
                }
                switch (serverEntry.maptype) {
                    case Dynmap -> RemotePlayerWaypointsForXaero.setConnection(new DynmapConnection(serverEntry, this));
                    case Squaremap -> RemotePlayerWaypointsForXaero.setConnection(new SquareMapConnection(serverEntry, this));
                    case Bluemap -> RemotePlayerWaypointsForXaero.setConnection(new BlueMapConnection(serverEntry, this));
                    case Pl3xMap -> RemotePlayerWaypointsForXaero.setConnection(new Pl3xMapConnection(serverEntry, this));
                    default -> throw new IllegalStateException("Unexpected value: " + serverEntry.maptype);
                }
            } catch (Exception e) {
                if (!connectionErrorWasShown){
                    connectionErrorWasShown = true;
                    mc.inGameHud.getChatHud().addMessage(Text.literal("[" + RemotePlayerWaypointsForXaero.MOD_NAME + "]: " +
                            "Error while connecting to the online map. " +
                            "Please check you config or report a bug.").setStyle(Style.EMPTY.withColor(Formatting.RED)));
                    e.printStackTrace();
                }
                RemotePlayerWaypointsForXaero.connected = false;
                return;
            }
        }
        currentServerIP = serverIP;

        // Get a list of all player's positions
        PlayerPosition[] playerPositions;
        WaypointPosition[] waypointPositions;
        try {
            // this must be run no matter if it's activated in the config, to get the "currentDimension" and AFK info
            playerPositions = RemotePlayerWaypointsForXaero.getConnection().getPlayerPositions();
            if (CommonModConfig.Instance.enableMarkerWaypoints()){
                waypointPositions = RemotePlayerWaypointsForXaero.getConnection().getWaypointPositions();
            }
            else {
                waypointPositions = new WaypointPosition[0];
            }
        } catch (IOException e) {
            if (!cantGetPlayerPositionsErrorWasShown){
                cantGetPlayerPositionsErrorWasShown = true;
                mc.inGameHud.getChatHud().addMessage(Text.literal("[" + RemotePlayerWaypointsForXaero.MOD_NAME + "]: " +
                        "Failed to make online map request. Please check your config (probably your link...) or report a bug.")
                        .setStyle(Style.EMPTY.withColor(Formatting.RED)));
            }
            e.printStackTrace();
            RemotePlayerWaypointsForXaero.setConnection(null);
            return;
        }

        RemotePlayerWaypointsForXaero.connected = true;
        RemotePlayerWaypointsForXaero.AfkColor = CommonModConfig.Instance.AfkColor();
        RemotePlayerWaypointsForXaero.unknownAfkStateColor = CommonModConfig.Instance.unknownAfkStateColor();
        RemotePlayerWaypointsForXaero.showAfkTimeInTabList = CommonModConfig.Instance.showAfkTimeInTabList();

        if (!RemotePlayerWaypointsForXaero.mapModInstalled){
            if (CommonModConfig.Instance.updateDelay() != RemotePlayerWaypointsForXaero.TimerDelay){
                RemotePlayerWaypointsForXaero.setUpdateDelay(CommonModConfig.Instance.updateDelay());
            }
            return;
        }

        WaypointWorld currentWorld = null;
        try{
            // Access the current waypoint world
            currentWorld = XaeroMinimapSession.getCurrentSession().getWaypointsManager().getCurrentWorld();
        }
        catch (Exception ignored){
        }

        // Skip if the world is null
        if (currentWorld == null) {
            RemotePlayerWaypointsForXaero.LOGGER.info("WaypointWorld is null, disconnecting from online map");
            Reset();
            return;
        }
        HudMod.INSTANCE.getSettings().renderAllSets = true;

        if (!currentWorld.getSets().containsKey(DEFAULT_PLAYER_SET_NAME)){
            currentWorld.addSet(DEFAULT_PLAYER_SET_NAME);
        }
        var waypointList = currentWorld.getSets().get(DEFAULT_PLAYER_SET_NAME).getList();
        var clientPlayer = MinecraftClient.getInstance().player;
        if (clientPlayer == null) return;

        try {
            synchronized (waypointList) {
                waypointList.clear();

                if (CommonModConfig.Instance.enablePlayerWaypoints()){
                    // Add each player to the map
                    for (PlayerPosition playerPosition : playerPositions) {
                        if (playerPosition == null) continue;

                        var d = clientPlayer.getPos().distanceTo(new Vec3d(playerPosition.x, playerPosition.y, playerPosition.z));
                        if (d < CommonModConfig.Instance.minDistance() || d > CommonModConfig.Instance.maxDistance()) continue;
                        // Add waypoint for the player
                        try {
                            waypointList.add(new PlayerWaypoint(playerPosition));
                        } catch (NullPointerException e) {
                            RemotePlayerWaypointsForXaero.LOGGER.warn("can't add player waypoint");
                            e.printStackTrace();
                        }
                    }
                }

                for( WaypointPosition waypointPosition : waypointPositions){
                    if (waypointPosition == null) continue;

                    var d = clientPlayer.getPos().distanceTo(new Vec3d(waypointPosition.x, waypointPosition.y, waypointPosition.z));
                    if (d < CommonModConfig.Instance.minDistanceMarker() || d > CommonModConfig.Instance.maxDistanceMarker()) continue;
                    // Add waypoint for the marker
                    try {
                        waypointList.add(new FixedWaypoint(waypointPosition));
                    } catch (NullPointerException e) {
                        RemotePlayerWaypointsForXaero.LOGGER.warn("can't add marker waypoint");
                        e.printStackTrace();
                    }
                }
            }
        } catch (ConcurrentModificationException e) {
            RemotePlayerWaypointsForXaero.LOGGER.warn("waypoint error");
            e.printStackTrace();
        }

        if (CommonModConfig.Instance.updateDelay() != RemotePlayerWaypointsForXaero.TimerDelay){
            RemotePlayerWaypointsForXaero.setUpdateDelay(CommonModConfig.Instance.updateDelay());
        }
    }

    private void Reset() {
        RemotePlayerWaypointsForXaero.setConnection(null);
        connectionErrorWasShown = false;
        cantFindServerErrorWasShown = false;
        cantGetPlayerPositionsErrorWasShown = false;
        linkBrokenErrorWasShown = false;
    }
}