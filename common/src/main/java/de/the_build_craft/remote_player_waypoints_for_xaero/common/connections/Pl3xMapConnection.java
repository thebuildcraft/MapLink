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

import com.google.common.reflect.TypeToken;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.*;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.clientMapHandlers.ClientMapHandler;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.configurations.Pl3xMapConfiguration;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.mapUpdates.Pl3xMapAltMarkerUpdate;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.mapUpdates.Pl3xMapMarkerLayerConfig;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.mapUpdates.Pl3xMapMarkerUpdate;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.mapUpdates.Pl3xMapPlayerUpdate;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.waypoints.PlayerPosition;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.waypoints.Position;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.wrappers.Utils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.the_build_craft.remote_player_waypoints_for_xaero.common.CommonModConfig.*;

/**
 * @author Leander Knüttel
 * @author eatmyvenom
 * @version 25.08.2025
 */
public class Pl3xMapConnection extends MapConnection{
    private String markerLayerStringTemplate = "";
    private String markerStringTemplate = "";
    private String markerIconLinkTemplate = "";
    int version;
    public Pl3xMapConnection(ModConfig.ServerEntry serverEntry, UpdateTask updateTask) throws IOException {
        try {
            generateLink(serverEntry, true);
        }
        catch (Exception ignored){
            try {
                generateLink(serverEntry, false);
            }
            catch (Exception e){
                if (!updateTask.linkBrokenErrorWasShown){
                    updateTask.linkBrokenErrorWasShown = true;
                    Utils.sendErrorToClientChat("[" + AbstractModInitializer.MOD_NAME + "]: Error: Your Pl3xMap link is broken!");
                }
                throw e;
            }
        }
    }

    public Pl3xMapConnection(String baseURL, String link, boolean partOfLifeAtlas) throws IOException {
        this.partOfLiveAtlas = partOfLifeAtlas;
        Matcher matcher = Pattern.compile(".*?//\\w*(\\.\\w+)+(:\\w+)?").matcher(baseURL);
        if (!matcher.find()) throw new RuntimeException("wrong url pattern");
        baseURL = matcher.group();
        if (link.contains("//")) {
            baseURL = link;
        } else {
            if (!link.startsWith("/")) link = "/" + link;
            baseURL += link;
        }
        if (baseURL.endsWith("/")) baseURL = baseURL.substring(0, baseURL.length() - 1);
        setup(baseURL);
    }

    private void generateLink(ModConfig.ServerEntry serverEntry, boolean useHttps) throws IOException {
        setup(getBaseURL(serverEntry, useHttps));
    }

    private void setup(String baseURL) throws IOException {
        onlineMapConfigLink = baseURL + "/tiles/settings.json";
        markerLayerStringTemplate = baseURL + "/tiles/{world}/markers.json";
        markerIconLinkTemplate = baseURL + "/images/icon/registered/{icon}.png";

        // test which version is needed
        if (HTTP.makeTextHttpRequest(URI.create(onlineMapConfigLink).toURL()).contains("\"players\":[")) {
            version = 0;
            queryURL = URI.create(onlineMapConfigLink).toURL();
            markerStringTemplate = baseURL + "/tiles/{world}/markers/{layerName}.json";
        } else {
            version = 1;
            queryURL = URI.create(baseURL + "/tiles/players.json").toURL();
        }

        // Test the url
        this.getPlayerPositions();

        AbstractModInitializer.LOGGER.info("new link: " + queryURL);
        if (config.general.debugMode){
            Utils.sendToClientChat("new link: " + queryURL);
        }
    }

    @Override
    public HashMap<String, PlayerPosition> getPlayerPositions() throws IOException {
        // Make request for all players
        Pl3xMapPlayerUpdate update = HTTP.makeJSONHTTPRequest(queryURL, Pl3xMapPlayerUpdate.class);

        // Build a list of positions
        PlayerPosition[] positions = new PlayerPosition[update.players.length];
        for (int i = 0; i < update.players.length; i++){
            Pl3xMapPlayerUpdate.Player player = update.players[i];
            PlayerPosition playerPosition = null;
            if (version == 0) playerPosition = new PlayerPosition(player.name, player.position.x, config.general.defaultY, player.position.z, player.world);
            else if (version == 1) playerPosition = new PlayerPosition(player.name, player.x, config.general.defaultY, player.z, player.world);
            if (playerPosition == null) continue;
            ClientMapHandler.registerPlayerPosition(playerPosition, "https://mc-heads.net/avatar/" + player.uuid + "/32");
            positions[i] = playerPosition;
        }

        return HandlePlayerPositions(positions);
    }

    private String lastMarkerDimension = "";
    int lastMarkerHash;
    //int lastAreaMarkerHash;
    List<Position> positions = new ArrayList<>();
    //List<AreaMarker> areaMarkers = new ArrayList<>();

    @Override
    public void getWaypointPositions() throws IOException {
        if (markerLayerStringTemplate.isEmpty() || currentDimension.isEmpty()) {
            if (ClientMapHandler.getInstance() != null) ClientMapHandler.getInstance().removeAllMarkerWaypoints();
            return;
        }
        ModConfig.ServerEntry serverEntry = getCurrentServerEntry();
        if (serverEntry.needsMarkerLayerUpdate() && !partOfLiveAtlas) {
            serverEntry.setMarkerLayers(new ArrayList<>(getMarkerLayers()));
        }

        if (ClientMapHandler.getInstance() == null) return;

        if (markerStringTemplate.isEmpty() && version == 0) {
            ClientMapHandler.getInstance().removeAllMarkerWaypoints();
            return;
        }
        int newMarkerHash = getMarkerVisibilityHash();
        //int newAreaMarkerHash = CommonModConfig.Instance.getAreaMarkerVisibilityHash();
        if (lastMarkerDimension.equals(currentDimension)
                //&& newAreaMarkerHash == lastAreaMarkerHash
                && newMarkerHash == lastMarkerHash) {
            ClientMapHandler.getInstance().handleMarkerWaypoints(positions);
            //ClientMapHandler.getInstance().handleAreaMarkers(areaMarkers, false);
            return;
        }
        lastMarkerDimension = currentDimension;
        lastMarkerHash = newMarkerHash;
        //lastAreaMarkerHash = newAreaMarkerHash;

        positions.clear();
        //areaMarkers.clear();

        if (version == 0) {
            for (String layer : getMarkerLayers(false)){
                Type apiResponseType = new TypeToken<Pl3xMapMarkerUpdate[]>() {}.getType();
                URL reqUrl = URI.create(markerStringTemplate.replace("{world}", currentDimension.replaceAll(":", "-"))
                        .replace("{layerName}", layer)).toURL();
                Pl3xMapMarkerUpdate[] markers = HTTP.makeJSONHTTPRequest(reqUrl, apiResponseType);

                for (Pl3xMapMarkerUpdate marker : markers){
                    if (Objects.equals(marker.type, "icon") && serverEntry.includeMarkerLayer(layer)) {
                        Position position = new Position(marker.options.tooltip.content, marker.data.point.x, config.general.defaultY, marker.data.point.z, currentDimension + layer + marker.data.key, layer);
                        ClientMapHandler.registerPosition(position, marker.data.image.equals("marker-icon") ? null : markerIconLinkTemplate.replace("{icon}", marker.data.image));
                        positions.add(position);
                    }
                }
            }
        } else if (version == 1) {
            Type apiResponseType = new TypeToken<Pl3xMapAltMarkerUpdate[]>() {}.getType();
            URL reqUrl = URI.create(markerLayerStringTemplate.replace("{world}", currentDimension.replaceAll(":", "-"))).toURL();
            Pl3xMapAltMarkerUpdate[] markerLayers = HTTP.makeJSONHTTPRequest(reqUrl, apiResponseType);

            for (Pl3xMapAltMarkerUpdate layer : markerLayers){
                if (!Objects.equals(layer.id, "pl3xmap_players")) {
                    for (Pl3xMapAltMarkerUpdate.Marker marker : layer.markers) {
                        if (Objects.equals(marker.type, "icon")  && serverEntry.includeMarkerLayer(layer.id)) {
                            Position position = new Position(marker.tooltip, marker.point.x, config.general.defaultY, marker.point.z, currentDimension + layer.id + marker.tooltip + marker.point.x + marker.point.z, layer.id);
                            ClientMapHandler.registerPosition(position, marker.icon.equals("marker-icon") ? null : markerIconLinkTemplate.replace("{icon}", marker.icon));
                            positions.add(position);
                        }
                    }
                }
            }
        }
        ClientMapHandler.getInstance().handleMarkerWaypoints(positions);
        //ClientMapHandler.getInstance().handleAreaMarkers(areaMarkers, true);
    }

    @Override
    public HashSet<String> getMarkerLayers() {
        return getMarkerLayers(true);
    }

    private HashSet<String> getMarkerLayers(boolean all) {
        try {
            if (version == 0) {
                Type apiResponseType = new TypeToken<Pl3xMapMarkerLayerConfig[]>() {}.getType();

                if (all) {
                    Pl3xMapPlayerUpdate update = HTTP.makeJSONHTTPRequest(queryURL, Pl3xMapPlayerUpdate.class);
                    HashSet<String> layerSet = new HashSet<>();
                    for (Pl3xMapPlayerUpdate.WorldSetting ws : update.worldSettings) {
                        Pl3xMapMarkerLayerConfig[] mls = HTTP.makeJSONHTTPRequest(URI.create(markerLayerStringTemplate
                                .replace("{world}", ws.name.replaceAll(":", "-"))).toURL(), apiResponseType);
                        for (Pl3xMapMarkerLayerConfig ml : mls) {
                            if (!Objects.equals(ml.key, "pl3xmap_players")) {
                                layerSet.add(ml.key);
                            }
                        }
                    }
                    return layerSet;
                }

                URL reqUrl = URI.create(markerLayerStringTemplate.replace("{world}", currentDimension.replaceAll(":", "-"))).toURL();
                Pl3xMapMarkerLayerConfig[] markerLayers = HTTP.makeJSONHTTPRequest(reqUrl, apiResponseType);

                HashSet<String> layers = new HashSet<>();

                for (Pl3xMapMarkerLayerConfig layer : markerLayers){
                    if (!Objects.equals(layer.key, "pl3xmap_players")) {
                        layers.add(layer.key);
                    }
                }

                return layers;
            }
            if (version == 1) {
                Type apiResponseType = new TypeToken<Pl3xMapAltMarkerUpdate[]>() {}.getType();

                if (all) {
                    Pl3xMapConfiguration configuration = HTTP.makeJSONHTTPRequest(URI.create(onlineMapConfigLink).toURL(), Pl3xMapConfiguration.class);
                    HashSet<String> layerSet = new HashSet<>();
                    for (Pl3xMapConfiguration.World ws : configuration.worlds) {
                        Pl3xMapAltMarkerUpdate[] mls = HTTP.makeJSONHTTPRequest(URI.create(markerLayerStringTemplate.replace("{world}", ws.name.replaceAll(":", "-"))).toURL(), apiResponseType);
                        for (Pl3xMapAltMarkerUpdate ml : mls) {
                            if (!Objects.equals(ml.id, "pl3xmap_players")) {
                                layerSet.add(ml.id);
                            }
                        }
                    }
                    return layerSet;
                }

                URL reqUrl = URI.create(markerLayerStringTemplate.replace("{world}", currentDimension.replaceAll(":", "-"))).toURL();
                Pl3xMapAltMarkerUpdate[] markerLayers = HTTP.makeJSONHTTPRequest(reqUrl, apiResponseType);

                HashSet<String> layers = new HashSet<>();

                for (Pl3xMapAltMarkerUpdate layer : markerLayers){
                    if (!Objects.equals(layer.id, "pl3xmap_players")) {
                        layers.add(layer.id);
                    }
                }

                return layers;
            }
            throw new IllegalStateException();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
