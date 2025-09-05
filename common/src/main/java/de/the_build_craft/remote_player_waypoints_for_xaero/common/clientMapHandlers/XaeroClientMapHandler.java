/*
 *    This file is part of the Remote player waypoints for Xaero's Map mod
 *    licensed under the GNU GPL v3 License.
 *
 *    Copyright (C) 2025  Leander Knüttel and contributors
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

package de.the_build_craft.remote_player_waypoints_for_xaero.common.clientMapHandlers;

#if MC_VER >= MC_1_21_5
import com.mojang.blaze3d.textures.GpuTexture;
#endif
import de.the_build_craft.remote_player_waypoints_for_xaero.common.waypoints.*;
import de.the_build_craft.remote_player_waypoints_for_xaero.mixins.common.mods.xaeroworldmap.WorldMapWaypointAccessor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.texture.DynamicTexture;
import xaero.common.settings.ModSettings;
#if MC_VER != MC_1_17_1
import xaero.hud.minimap.waypoint.WaypointColor;
#endif
import it.unimi.dsi.fastutil.longs.*;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.map.icon.XaeroIcon;
import xaero.map.icon.XaeroIconAtlas;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.the_build_craft.remote_player_waypoints_for_xaero.common.AbstractModInitializer.LOGGER;
import static de.the_build_craft.remote_player_waypoints_for_xaero.common.CommonModConfig.*;
import static de.the_build_craft.remote_player_waypoints_for_xaero.common.FastUpdateTask.playerPositions;

/**
 * @author Leander Knüttel
 * @version 05.09.2025
 */
public class XaeroClientMapHandler extends ClientMapHandler {
    public static final Long2ObjectMap<ChunkHighlight> chunkHighlightMap = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());
    public static final LongSet regionsWithChunkHighlights = LongSets.synchronize(new LongOpenHashSet());
    public static int chunkHighlightHash;
    public static MapHighlightClearer mapHighlightClearer;

    private static final Map<String, XaeroIcon> iconLinkToXaeroIcon = new HashMap<>();

    public static final Map<String, TempWaypoint> idToHudPlayer = new ConcurrentHashMap<>();
    public static final Map<String, TempWaypoint> idToMiniMapPlayer = new ConcurrentHashMap<>();
    public static final Map<String, xaero.map.mods.gui.Waypoint> idToWorldMapPlayer = new ConcurrentHashMap<>();

    public static final Map<String, TempWaypoint> idToHudMarker = new ConcurrentHashMap<>();
    public static final Map<String, TempWaypoint> idToMiniMapMarker = new ConcurrentHashMap<>();
    public static final Map<String, xaero.map.mods.gui.Waypoint> idToWorldMapMarker = new ConcurrentHashMap<>();

    public static final Map<UUID, MutablePlayerPosition> hudAndMinimapPlayerTrackerPositions = new ConcurrentHashMap<>();
    public static final Map<UUID, MutablePlayerPosition> worldmapPlayerTrackerPositions = new ConcurrentHashMap<>();

    private final Set<UUID> currentHudAndMinimapPlayerTrackerUUIDs = new HashSet<>();
    private final Set<UUID> currentWorldmapPlayerTrackerUUIDs = new HashSet<>();

    @Override
    public void reset() {
        removeAllPlayerWaypoints();
        removeAllMarkerWaypoints();
        removeAllAreaMarkers(true);
        hudAndMinimapPlayerTrackerPositions.clear();
        worldmapPlayerTrackerPositions.clear();
    }

    @Override
    public void handlePlayerWaypoints() {
        super.handlePlayerWaypoints();
        // Update the player positions obtained from the online Map with GameProfile data from the actual logged-in players
        // This is required for the player tracker system
        if(config.general.enablePlayerWaypoints && config.general.showPlayerWaypointsAsTrackedPlayers && mc.getConnection() != null) {
            currentHudAndMinimapPlayerTrackerUUIDs.clear();
            currentWorldmapPlayerTrackerUUIDs.clear();

            for (PlayerInfo playerInfo : mc.getConnection().getOnlinePlayers()) {
                PlayerPosition playerPosition = playerPositions.get(playerInfo.getProfile().getName());
                if (playerPosition == null || !currentPlayerIds.contains(playerPosition.id)) continue;

                WaypointState waypointState = getWaypointState(playerPosition.id);
                playerPosition.gameProfile = playerInfo.getProfile();

                if (waypointState.renderOnHud || waypointState.renderOnMiniMap) {
                    hudAndMinimapPlayerTrackerPositions.computeIfAbsent(playerInfo.getProfile().getId(), uuid -> new MutablePlayerPosition(playerPosition, waypointState))
                            .updateFrom(playerPosition.pos);
                    currentHudAndMinimapPlayerTrackerUUIDs.add(playerInfo.getProfile().getId());
                }
                if (waypointState.renderOnWorldMap) {
                    worldmapPlayerTrackerPositions.computeIfAbsent(playerInfo.getProfile().getId(), uuid -> new MutablePlayerPosition(playerPosition, waypointState))
                            .updateFrom(playerPosition.pos);
                    currentWorldmapPlayerTrackerUUIDs.add(playerInfo.getProfile().getId());
                }
            }

            hudAndMinimapPlayerTrackerPositions.keySet().removeIf(p -> !currentHudAndMinimapPlayerTrackerUUIDs.contains(p));
            worldmapPlayerTrackerPositions.keySet().removeIf(p -> !currentWorldmapPlayerTrackerUUIDs.contains(p));
        } else {
            hudAndMinimapPlayerTrackerPositions.clear();
            worldmapPlayerTrackerPositions.clear();
        }
    }

    @Override
    void addOrUpdatePlayerWaypoint(PlayerPosition playerPosition, WaypointState waypointState) {
        if (waypointState.renderOnHud) {
            addOrUpdateMiniMapWaypoint(playerPosition, idToHudPlayer, (player) -> new PlayerWaypoint(player, waypointState));
        }
        if (waypointState.renderOnMiniMap) {
            addOrUpdateMiniMapWaypoint(playerPosition, idToMiniMapPlayer, (player) -> new PlayerWaypoint(player, waypointState));
        }
        if (waypointState.renderOnWorldMap) {
            addOrUpdateWorldMapWaypoint(playerPosition, idToWorldMapPlayer, (player) -> new PlayerWaypoint(player, waypointState));
        }
    }

    private <P extends Position> void addOrUpdateMiniMapWaypoint(P position, Map<String, TempWaypoint> idToWaypoint, Function<P, TempWaypoint> waypointInit) {
        if (idToWaypoint.containsKey(position.id)) {
            Waypoint w = idToWaypoint.get(position.id);
            Int3 pos = position.pos.floorToInt3();
            w.setX(pos.x);
            w.setY(pos.y);
            w.setZ(pos.z);
        } else {
            idToWaypoint.put(position.id, waypointInit.apply(position));
        }
    }

    private <P extends Position> void addOrUpdateWorldMapWaypoint(P position, Map<String, xaero.map.mods.gui.Waypoint> idToWaypoint, Function<P, TempWaypoint> waypointInit) {
        if (idToWaypoint.containsKey(position.id)) {
            WorldMapWaypointAccessor w = (WorldMapWaypointAccessor) idToWaypoint.get(position.id);
            Int3 pos = position.pos.floorToInt3();
            w.setX(pos.x);
            w.setY(pos.y);
            w.setZ(pos.z);
        } else {
            idToWaypoint.put(position.id, new CustomWorldMapWaypoint(waypointInit.apply(position)));
        }
    }

    private <W> void removeOldWaypointsFromMap(Map<String, W> map, Predicate<WaypointState> removeIf, Set<String> idHasToBeIn) {
        Iterator<Map.Entry<String, W>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, W> entry = iterator.next();
            WaypointState waypointState = ClientMapHandler.getWaypointState(entry.getKey());
            if (waypointState == null
                    || (waypointState.isPlayer && config.general.showPlayerWaypointsAsTrackedPlayers)
                    || removeIf.test(waypointState)
                    || !idHasToBeIn.contains(entry.getKey())) {
                iterator.remove();
            }
        }
    }

    @Override
    void removeOldPlayerWaypoints() {
        removeOldWaypointsFromMap(idToHudPlayer, w -> !w.renderOnHud, currentPlayerIds);
        removeOldWaypointsFromMap(idToMiniMapPlayer, w -> !w.renderOnMiniMap, currentPlayerIds);
        removeOldWaypointsFromMap(idToWorldMapPlayer, w -> !w.renderOnWorldMap, currentPlayerIds);
    }

    @Override
    void removeAllPlayerWaypoints() {
        idToHudPlayer.clear();
        idToMiniMapPlayer.clear();
        idToWorldMapPlayer.clear();
    }

    private void updateMiniMapWaypointColors(Map<String, TempWaypoint> map, Function<TempWaypoint, Integer> waypointToColor) {
        for (TempWaypoint waypoint : map.values()) {
            #if MC_VER == MC_1_17_1
            waypoint.setColor(waypointToColor.apply(waypoint));
            #else
            waypoint.setWaypointColor(WaypointColor.fromIndex(waypointToColor.apply(waypoint)));
            #endif
        }
    }

    private void updateWorldMapWaypointColors(Map<String, xaero.map.mods.gui.Waypoint> map, Function<xaero.map.mods.gui.Waypoint, Integer> waypointToColor) {
        for (xaero.map.mods.gui.Waypoint waypoint : map.values()) {
            ((WorldMapWaypointAccessor) waypoint).setColor(ModSettings.COLORS[waypointToColor.apply(waypoint)]);
        }
    }

    @Override
    void updatePlayerWaypointColors() {
        updateMiniMapWaypointColors(idToHudPlayer, w -> getPlayerWaypointColor(w.getName()));
        updateMiniMapWaypointColors(idToMiniMapPlayer, w -> getPlayerWaypointColor(w.getName()));
        updateWorldMapWaypointColors(idToWorldMapPlayer, w -> getPlayerWaypointColor(w.getName()));
    }

    @Override
    void addOrUpdateMarkerWaypoint(Position markerPosition, WaypointState waypointState) {
        if (waypointState.renderOnHud) {
            addOrUpdateMiniMapWaypoint(markerPosition, idToHudMarker, (p) -> new FixedWaypoint(p, waypointState));
        }
        if (waypointState.renderOnMiniMap) {
            addOrUpdateMiniMapWaypoint(markerPosition, idToMiniMapMarker, (p) -> new FixedWaypoint(p, waypointState));
        }
        if (waypointState.renderOnWorldMap) {
            addOrUpdateWorldMapWaypoint(markerPosition, idToWorldMapMarker, (p) -> new FixedWaypoint(p, waypointState));
        }
    }

    @Override
    void removeOldMarkerWaypoints() {
        removeOldWaypointsFromMap(idToHudMarker, w -> !w.renderOnHud, currentMarkerIds);
        removeOldWaypointsFromMap(idToMiniMapMarker, w -> !w.renderOnMiniMap, currentMarkerIds);
        removeOldWaypointsFromMap(idToWorldMapMarker, w -> !w.renderOnWorldMap, currentMarkerIds);
    }

    @Override
    public void removeAllMarkerWaypoints() {
        idToHudMarker.clear();
        idToMiniMapMarker.clear();
        idToWorldMapMarker.clear();
    }

    @Override
    void updateMarkerWaypointColors() {
        updateMiniMapWaypointColors(idToHudMarker, w -> config.general.markerWaypointColor.ordinal());
        updateMiniMapWaypointColors(idToMiniMapMarker, w -> config.general.markerWaypointColor.ordinal());
        updateWorldMapWaypointColors(idToWorldMapMarker, w -> config.general.markerWaypointColor.ordinal());
    }

    int lastAreaMarkerHash;

    @Override
    public void handleAreaMarkers(List<AreaMarker> markerPositions, boolean refresh) {
        int newAreaMarkerHash = getAreaMarkerVisibilityHash();
        if (lastAreaMarkerHash != newAreaMarkerHash) {
            refresh = true;
        }
        lastAreaMarkerHash = newAreaMarkerHash;
        if (!refresh) return;
        removeAllAreaMarkers(false);
        if (config.general.enableAreaMarkerOverlay) {
            for (Map.Entry<AreaMarker, List<Long>> cords : markerPositions.parallelStream()
                    .collect(Collectors.toMap(a -> a, this::rasterizeAreaMarker)).entrySet()) {
                for (long cord : cords.getValue()) {
                    createChunkHighlightAt(cords.getKey(), cord);
                }
            }
        }
        if (mapHighlightClearer != null) mapHighlightClearer.clearHashCache();
    }

    private void createChunkHighlightAt(AreaMarker areaMarker, long chunkKey) {
        ChunkHighlight chunkHighlight = chunkHighlightMap.get(chunkKey);
        if (chunkHighlight != null) {
            chunkHighlight.combine(areaMarker);
        } else {
            chunkHighlightMap.put(chunkKey, new ChunkHighlight(areaMarker));
        }
        regionsWithChunkHighlights.add(MathUtils.shiftRightIntsInLong(chunkKey, 5));
    }

    @Override
    public void removeAllAreaMarkers(boolean clearXaeroHash) {
        chunkHighlightHash = (chunkHighlightHash + 1) % 10000;
        chunkHighlightMap.clear();
        regionsWithChunkHighlights.clear();
        if (clearXaeroHash && mapHighlightClearer != null) mapHighlightClearer.clearHashCache();
    }

    //Thanks to https://www.mathematik.uni-marburg.de/~thormae/lectures/graphics1/code_v2/RasterPoly/index.html
    //for the visualizer and reference implementation (I made some changes)
    List<Long> rasterizeAreaMarker(AreaMarker areaMarker) {
        try {
            if (areaMarker.polygons.length == 0) return new ArrayList<>();

            Int3[][] polygons = Arrays.stream(areaMarker.polygons).toArray(Int3[][]::new);

            List<Edge> edges = new ArrayList<>();
            List<Long> additionalChunks = new ArrayList<>();
            List<Edge> tempEdges = new ArrayList<>();
            int area = 0;
            for (Int3[] polygon : polygons) {
                tempEdges.clear();

                int globalXMin = Integer.MAX_VALUE;
                int globalXMax = Integer.MIN_VALUE;
                int globalZMin = Integer.MAX_VALUE;
                int globalZMax = Integer.MIN_VALUE;

                for (int i = 0; i < polygon.length; i++) {
                    Int3 point = polygon[i];
                    Int3 otherPoint = polygon[(i + 1) % polygon.length];
                    globalXMin = Math.min(globalXMin, point.x);
                    globalXMax = Math.max(globalXMax, point.x);
                    globalZMin = Math.min(globalZMin, point.z);
                    globalZMax = Math.max(globalZMax, point.z);
                    if (point.z != otherPoint.z) {
                        tempEdges.add(new Edge(point, otherPoint));
                    }
                }

                globalXMin >>= 4;
                globalXMax >>= 4;
                globalZMin >>= 4;
                globalZMax >>= 4;

                area += (1 + globalXMax - globalXMin) * (1 + globalZMax - globalZMin);
                if (area > 500_000) {
                    LOGGER.warn("polygon to large: " + areaMarker.name);
                    return new ArrayList<>();
                }

                if (globalXMin == globalXMax) {
                    for (int z = globalZMin; z <= globalZMax; z++) {
                        additionalChunks.add(MathUtils.combineIntsToLong(globalXMin, z));
                    }
                } else if (globalZMin == globalZMax) {
                    for (int x = globalXMin; x <= globalXMax; x++) {
                        additionalChunks.add(MathUtils.combineIntsToLong(x, globalZMin));
                    }
                } else {
                    edges.addAll(tempEdges);
                }
            }

            if (edges.isEmpty()) return additionalChunks;

            Long2IntMap result = new Long2IntOpenHashMap(area);
            Collections.sort(edges);

            int z = edges.get(0).zMin;
            List<Edge> activeEdges = new ArrayList<>();
            List<Integer> intersections = new ArrayList<>();

            while (!edges.isEmpty() || !activeEdges.isEmpty()) {
                Iterator<Edge> edgeIterator = edges.iterator();
                Edge edge;
                while (edgeIterator.hasNext() && (edge = edgeIterator.next()).zMin == z) {
                    activeEdges.add(edge);
                    edgeIterator.remove();
                }
                activeEdges.sort(Comparator.comparing(e -> e.xHit));

                intersections.clear();
                for (Edge e : activeEdges) {
                    if (e.zMax != z) intersections.add(Math.round(e.xHit));
                }

                if (intersections.size() % 2 == 0) {
                    for (int i = 0; i < intersections.size() - 1; i += 2) {
                        int xMin = intersections.get(i);
                        int xMax = intersections.get(i+1);

                        for (int x = xMin; x <= xMax; x++) {
                            long key = MathUtils.combineIntsToLong(x >> 4, z >> 4);
                            result.put(key, result.get(key) + 1);
                        }
                    }
                } else {
                    LOGGER.error("error in polygon rasterization: " + areaMarker.name);
                    return new ArrayList<>();
                }

                edgeIterator = activeEdges.iterator();
                while (edgeIterator.hasNext()) {
                    Edge e = edgeIterator.next();
                    if (e.zMax == z) {
                        edgeIterator.remove();
                    } else {
                        e.xHit += e.mInv;
                    }
                }

                z++;
            }
            List<Long> resultList = result.long2IntEntrySet().stream()
                    .filter(entry -> entry.getIntValue() >= config.general.blocksPerChunkThreshold)
                    .map(Long2IntMap.Entry::getLongKey)
                    .collect(Collectors.toList());
            resultList.addAll(additionalChunks);
            return resultList;
        } catch (Throwable t) {
            LOGGER.error("unexpected error in polygon rasterization", t);
            return new ArrayList<>();
        }
    }

    static class Edge implements Comparable<Edge> {
        int zMin;
        int zMax;
        float xHit;
        float mInv;
        int xMin;
        int xMax;

        public Edge(Int3 a, Int3 b) {
            zMin = Math.min(a.z, b.z);
            zMax = Math.max(a.z, b.z);
            xHit = (zMin == a.z) ? a.x : b.x;
            mInv = (float) (b.x - a.x) / (float) (b.z - a.z);
            xMin = Math.min(a.x, b.x);
            xMax = Math.max(a.x, b.x);
        }

        @Override
        public int compareTo(Edge e) {
            if (zMin == e.zMin) return Float.compare(xHit, e.xHit);
            return Integer.compare(zMin, e.zMin);
        }
    }

    public static XaeroIcon getXaeroIcon(String link) {
        if (iconLinkToXaeroIcon.containsKey(link)) return iconLinkToXaeroIcon.get(link);
        DynamicTexture dynamicTexture = getDynamicTexture(link);
        if (dynamicTexture == null) return null;
        #if MC_VER >= MC_1_21_5
        GpuTexture texture = dynamicTexture.getTexture();
        XaeroIcon icon = XaeroIconAtlas.Builder.begin().setPreparedTexture(texture)
                .setIconWidth(texture.getWidth(0)).setWidth(texture.getWidth(0)).build().createIcon();
        #else
        int textureId = dynamicTexture.getId();
        XaeroIcon icon = XaeroIconAtlas.Builder.begin().setPreparedTexture(textureId)
                .setIconWidth(dynamicTexture.getPixels().getWidth()).setWidth(dynamicTexture.getPixels().getWidth()).build().createIcon();
        #endif
        iconLinkToXaeroIcon.put(link, icon);
        return icon;
    }
}
