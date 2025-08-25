/*
 *    This file is part of the Remote player waypoints for Xaero's Map mod
 *    licensed under the GNU GPL v3 License.
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

package de.the_build_craft.remote_player_waypoints_for_xaero.common.connections;

import de.the_build_craft.remote_player_waypoints_for_xaero.common.*;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.clientMapHandlers.ClientMapHandler;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.configurations.BlueMapConfiguration;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.mapUpdates.BlueMapMarkerSet;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.mapUpdates.BlueMapPlayerUpdate;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;

import com.google.common.reflect.TypeToken;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.waypoints.*;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.wrappers.Utils;

import java.lang.reflect.Type;

import static de.the_build_craft.remote_player_waypoints_for_xaero.common.CommonModConfig.*;

/**
 * @author Leander Knüttel
 * @author eatmyvenom
 * @version 25.08.2025
 */
public class BlueMapConnection extends MapConnection {
    List<Integer> lastWorldIndices = new ArrayList<>(Collections.singletonList(0));
    List<URL> playerUrls = new ArrayList<>();
    List<URL> markerUrls = new ArrayList<>();
    List<String> worlds = new ArrayList<>();
    List<String> playerHeadIconUrlTemplates = new ArrayList<>();
    private String markerIconLinkTemplate = "";

    public BlueMapConnection(ModConfig.ServerEntry serverEntry, UpdateTask updateTask) throws IOException {
        try {
            generateLinks(serverEntry, true);
        }
        catch (Exception suppressed){
            try {
                generateLinks(serverEntry, false);
            }
            catch (Exception e){
                if (!updateTask.linkBrokenErrorWasShown){
                    updateTask.linkBrokenErrorWasShown = true;
                    Utils.sendErrorToClientChat("[" + AbstractModInitializer.MOD_NAME + "]: Error: Your Bluemap link is broken!");
                }
                e.addSuppressed(suppressed);
                throw e;
            }
        }
    }

    private void generateLinks(ModConfig.ServerEntry serverEntry, boolean useHttps) throws IOException {
        playerUrls.clear();
        markerUrls.clear();
        worlds.clear();
        playerHeadIconUrlTemplates.clear();

        String baseURL = getBaseURL(serverEntry, useHttps);
        AbstractModInitializer.LOGGER.info("baseURL " + baseURL);
        // Get config and build the urls
        for (String w : HTTP.makeJSONHTTPRequest(
                URI.create(baseURL + "/settings.json?").toURL(), BlueMapConfiguration.class).maps){
            playerUrls.add(URI.create((baseURL + "/maps/" + w + "/live/players.json?").replace(" ", "%20")).toURL());
            markerUrls.add(URI.create((baseURL + "/maps/" + w + "/live/markers.json?").replace(" ", "%20")).toURL());
            worlds.add(w);
            playerHeadIconUrlTemplates.add(baseURL + "/maps/" + w + "/assets/playerheads/{uuid}.png");
        }

        onlineMapConfigLink = baseURL + "/settings.json?";
        markerIconLinkTemplate = baseURL + "/{icon}";

        // Test the urls
        this.getPlayerPositions();

        for (URL url : playerUrls){
            AbstractModInitializer.LOGGER.info("new player link: " + url);
            if (config.general.debugMode){
                Utils.sendToClientChat("new link: " + url);
            }
        }

        for (URL url : markerUrls){
            AbstractModInitializer.LOGGER.info("new marker link: " + url);
            if (config.general.debugMode){
                Utils.sendToClientChat("new link: " + url);
            }
        }
    }

    @Override
    public HashSet<String> getMarkerLayers() {
        Type apiResponseType = new TypeToken<Map<String, BlueMapMarkerSet>>() {}.getType();
        HashSet<String> layers = new HashSet<>();
        for (URL url : markerUrls) {
            Map<String, BlueMapMarkerSet> sets;
            try {
                sets = HTTP.makeJSONHTTPRequest(url, apiResponseType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            layers.addAll(sets.keySet());
        }
        return layers;
    }

    int lastWorldIndicesHash;
    int lastMarkerHash;
    int lastAreaMarkerHash;
    List<Position> positions = new ArrayList<>();
    List<AreaMarker> areaMarkers = new ArrayList<>();

    @Override
    public void getWaypointPositions() throws IOException {
        Type apiResponseType = new TypeToken<Map<String, BlueMapMarkerSet>>() {}.getType();

        ModConfig.ServerEntry serverEntry = getCurrentServerEntry();
        if (serverEntry.needsMarkerLayerUpdate()) {
            serverEntry.setMarkerLayers(new ArrayList<>(getMarkerLayers()));
        }

        if (ClientMapHandler.getInstance() == null) return;

        int newWorldIndicesHash = lastWorldIndices.hashCode();
        int newMarkerHash = getMarkerVisibilityHash();
        int newAreaMarkerHash = getAreaMarkerVisibilityHash();
        if (newWorldIndicesHash == lastWorldIndicesHash
                && newMarkerHash == lastMarkerHash
                && newAreaMarkerHash == lastAreaMarkerHash
        ) {
            ClientMapHandler.getInstance().handleMarkerWaypoints(positions);
            ClientMapHandler.getInstance().handleAreaMarkers(areaMarkers, false);
            return;
        }
        lastWorldIndicesHash = newWorldIndicesHash;
        lastMarkerHash = newMarkerHash;
        lastAreaMarkerHash = newAreaMarkerHash;
        positions.clear();
        areaMarkers.clear();

        for (int i : lastWorldIndices) {
            Map<String, BlueMapMarkerSet> markerSets = HTTP.makeJSONHTTPRequest(markerUrls.get(i), apiResponseType);
            for (Map.Entry<String, BlueMapMarkerSet> markerSetEntry : markerSets.entrySet()) {
                if (config.general.debugMode && config.general.chatLogInDebugMode) {
                    Utils.sendToClientChat("====================================");
                    Utils.sendToClientChat("markerSet: " + markerSetEntry.getKey());
                }

                for (Map.Entry<String, BlueMapMarkerSet.Marker> markerEntry : markerSetEntry.getValue().markers.entrySet()) {
                    BlueMapMarkerSet.Marker marker = markerEntry.getValue();
                    Float3 pos = marker.position;
                    if (Objects.equals(marker.type, "poi") || Objects.equals(marker.type, "html")) {
                        if (!serverEntry.includeMarkerLayer(markerSetEntry.getKey())) continue;
                        Position position = new Position(marker.label, pos.x, pos.y, pos.z, i + markerSetEntry.getKey() + markerEntry.getKey(), markerSetEntry.getKey());
                        positions.add(position);
                        ClientMapHandler.registerPosition(position, marker.icon.startsWith("http") ? marker.icon : (marker.icon.equals("assets/poi.svg") ? null : markerIconLinkTemplate.replace("{icon}", marker.icon)));
                    } else if (Objects.equals(marker.type, "shape")) {
                        if (!serverEntry.includeAreaMarkerLayer(markerSetEntry.getKey())) continue;
                        areaMarkers.add(new AreaMarker(marker.label, pos.x, pos.y, pos.z, marker.shape, marker.lineColor, marker.fillColor, markerSetEntry.getKey() + markerEntry.getKey(), markerSetEntry.getKey()));
                    }
                }
            }
        }
        ClientMapHandler.getInstance().handleMarkerWaypoints(positions);
        ClientMapHandler.getInstance().handleAreaMarkers(areaMarkers, true);
    }

    private boolean correctWorld = false;
    @Override
    public HashMap<String, PlayerPosition> getPlayerPositions() throws IOException {
        assert mc.player != null;
        String clientName = mc.player.getName().getString();
        correctWorld = false;
        BlueMapPlayerUpdate update = null;

        if (AbstractModInitializer.overwriteCurrentDimension && !Objects.equals(currentDimension, "")){
            lastWorldIndices.clear();
            lastWorldIndices.add(worlds.indexOf(currentDimension));
            update = HTTP.makeJSONHTTPRequest(playerUrls.get(lastWorldIndices.get(0)), BlueMapPlayerUpdate.class);
        }
        else{
            if (!lastWorldIndices.isEmpty()) update = getBlueMapPlayerUpdate(clientName, update, lastWorldIndices.get(0));
            if (!correctWorld){
                lastWorldIndices.clear();
                for (int i = 0; i < playerUrls.size(); i++) {
                    correctWorld = false;
                    update = getBlueMapPlayerUpdate(clientName, update, i);
                    if (correctWorld) lastWorldIndices.add(i);
                }
            }
        }
        correctWorld = !lastWorldIndices.isEmpty();

        if ((update == null) || playerUrls.isEmpty()){
            throw new IllegalStateException("Can't get player positions. All Bluemap links are broken!");
        }
        // Build a list of positions
        PlayerPosition[] positions = new PlayerPosition[update.players.length];
        for (int i = 0; i < update.players.length; i++) {
            BlueMapPlayerUpdate.Player player = update.players[i];
            PlayerPosition playerPosition = new PlayerPosition(player.name, player.position.x, player.position.y, player.position.z, correctWorld ? (player.foreign ? "foreign" : "thisWorld") : "unknown");
            positions[i] = playerPosition;
            if (!correctWorld) continue;
            ClientMapHandler.registerPlayerPosition(playerPosition, playerHeadIconUrlTemplates.get(lastWorldIndices.get(0)).replace("{uuid}", player.uuid));
        }
        return HandlePlayerPositions(positions);
    }

    private BlueMapPlayerUpdate getBlueMapPlayerUpdate(String clientName, BlueMapPlayerUpdate update, int worldIndex) {
        try{
            update = HTTP.makeJSONHTTPRequest(playerUrls.get(worldIndex), BlueMapPlayerUpdate.class);
            for (BlueMapPlayerUpdate.Player p : update.players){
                if (Objects.equals(p.name, clientName)){
                    correctWorld = !p.foreign;
                    break;
                }
            }
        }
        catch (Exception ignored){
            if (config.general.debugMode){
                Utils.sendToClientChat("removed broken link: " + playerUrls.get(worldIndex));
            }
            playerUrls.remove(worldIndex);
            markerUrls.remove(worldIndex);
            worlds.remove(worldIndex);
        }
        return update;
    }
}
