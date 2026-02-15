/*
 *    This file is part of the Map Link mod
 *    licensed under the GNU GPL v3 License.
 *
 *    Copyright (C) 2025 - 2026  Leander Knüttel and contributors
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

package de.the_build_craft.maplink.common.clientMapHandlers;

import de.the_build_craft.maplink.common.AbstractModInitializer;
import de.the_build_craft.maplink.common.MainThreadTaskQueue;
import de.the_build_craft.maplink.common.waypoints.AreaMarker;
import de.the_build_craft.maplink.common.waypoints.Int3;
import de.the_build_craft.maplink.common.waypoints.MathUtils;
import de.the_build_craft.maplink.common.waypoints.MutablePlayerPosition;
import de.the_build_craft.maplink.common.waypoints.PlayerPosition;
import de.the_build_craft.maplink.common.waypoints.Position;
import de.the_build_craft.maplink.common.waypoints.WaypointState;
import net.minecraft.client.multiplayer.PlayerInfo;
import it.unimi.dsi.fastutil.longs.*;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.the_build_craft.maplink.common.AbstractModInitializer.LOGGER;
import static de.the_build_craft.maplink.common.CommonModConfig.*;
import static de.the_build_craft.maplink.common.FastUpdateTask.playerPositions;

/**
 * @author Leander Knüttel
 * @version 15.02.2026
 */
public class XaeroClientMapHandler extends ClientMapHandler {
    public static final Long2ObjectMap<Set<AreaMarker>> chunkHighlightMap = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());
    public static final LongSet regionsWithChunkHighlights = LongSets.synchronize(new LongOpenHashSet());
    public static int chunkHighlightHash;
    public static boolean currentlyRasterising;
    public static MapHighlightClearer mapHighlightClearer;

    public static final Map<UUID, MutablePlayerPosition> hudAndMinimapPlayerTrackerPositions = new ConcurrentHashMap<>();
    public static final Map<UUID, MutablePlayerPosition> worldmapPlayerTrackerPositions = new ConcurrentHashMap<>();

    private static final Set<UUID> currentHudAndMinimapPlayerTrackerUUIDs = new HashSet<>();
    private static final Set<UUID> currentWorldmapPlayerTrackerUUIDs = new HashSet<>();

    static final Map<String, MainThreadTaskQueue.QueuedTask<Void>> queuedTaskMap = new ConcurrentHashMap<>();

    public static IXaeroMiniMapSupport xaeroMiniMapSupport;
    public static IXaeroWorldMapSupport xaeroWorldMapSupport;

    @Override
    public void reset() {
        removeAllPlayerWaypoints();
        removeAllMarkerWaypoints();
        removeAllAreaMarkers(true);
        hudAndMinimapPlayerTrackerPositions.clear();
        worldmapPlayerTrackerPositions.clear();

        Iterator<Map.Entry<String, MainThreadTaskQueue.QueuedTask<Void>>> queuedTaskIterator = queuedTaskMap.entrySet().iterator();
        while (queuedTaskIterator.hasNext()) {
            Map.Entry<String, MainThreadTaskQueue.QueuedTask<Void>> queuedTaskEntry = queuedTaskIterator.next();
            if (queuedTaskEntry != null) queuedTaskEntry.getValue().cancel();
            queuedTaskIterator.remove();
        }
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
                #if MC_VER >= MC_1_21_9
                String playerName = playerInfo.getProfile().name();
                UUID id = playerInfo.getProfile().id();
                #else
                String playerName = playerInfo.getProfile().getName();
                UUID id = playerInfo.getProfile().getId();
                #endif
                PlayerPosition playerPosition = playerPositions.get(playerName);
                if (playerPosition == null || !currentPlayerIds.contains(playerPosition.id)) continue;

                WaypointState waypointState = getWaypointState(playerPosition.id);
                playerPosition.gameProfile = playerInfo.getProfile();

                if ((waypointState.renderOnHud || waypointState.renderOnMiniMap) && AbstractModInitializer.xaeroMiniMapInstalled) {
                    hudAndMinimapPlayerTrackerPositions.computeIfAbsent(id, uuid -> new MutablePlayerPosition(playerPosition, waypointState))
                            .updateFrom(playerPosition.pos);
                    currentHudAndMinimapPlayerTrackerUUIDs.add(id);
                }
                if (waypointState.renderOnWorldMap && AbstractModInitializer.xaeroWorldMapInstalled) {
                    worldmapPlayerTrackerPositions.computeIfAbsent(id, uuid -> new MutablePlayerPosition(playerPosition, waypointState))
                            .updateFrom(playerPosition.pos);
                    currentWorldmapPlayerTrackerUUIDs.add(id);
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
        if (waypointState.renderOnHud && AbstractModInitializer.xaeroMiniMapInstalled) {
            xaeroMiniMapSupport.addOrUpdateMiniMapWaypoint(playerPosition, waypointState, false);
        }
        if (waypointState.renderOnMiniMap && AbstractModInitializer.xaeroMiniMapInstalled) {
            xaeroMiniMapSupport.addOrUpdateMiniMapWaypoint(playerPosition, waypointState, true);
        }
        if (waypointState.renderOnWorldMap && AbstractModInitializer.xaeroWorldMapInstalled) {
            xaeroWorldMapSupport.addOrUpdateWorldMapWaypoint(playerPosition, waypointState);
        }
    }

    static <W> void removeOldWaypointsFromMap(Map<String, W> map, Predicate<WaypointState> removeIf, Set<String> idHasToBeIn) {
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
        if (AbstractModInitializer.xaeroMiniMapInstalled) xaeroMiniMapSupport.removeOldPlayerWaypoints();
        if (AbstractModInitializer.xaeroWorldMapInstalled) xaeroWorldMapSupport.removeOldPlayerWaypoints();
    }

    @Override
    void removeAllPlayerWaypoints() {
        if (AbstractModInitializer.xaeroMiniMapInstalled && xaeroMiniMapSupport != null) xaeroMiniMapSupport.removeAllPlayerWaypoints();
        if (AbstractModInitializer.xaeroWorldMapInstalled && xaeroWorldMapSupport != null) xaeroWorldMapSupport.removeAllPlayerWaypoints();
    }

    @Override
    void updatePlayerWaypointColors() {
        if (AbstractModInitializer.xaeroMiniMapInstalled) xaeroMiniMapSupport.updatePlayerWaypointColors();
        if (AbstractModInitializer.xaeroWorldMapInstalled) xaeroWorldMapSupport.updatePlayerWaypointColors();
    }

    @Override
    void addOrUpdateMarkerWaypoint(Position markerPosition, WaypointState waypointState) {
        if (waypointState.renderOnHud && AbstractModInitializer.xaeroMiniMapInstalled) {
            xaeroMiniMapSupport.addOrUpdateMiniMapWaypoint(markerPosition, waypointState, false);
        }
        if (waypointState.renderOnMiniMap && AbstractModInitializer.xaeroMiniMapInstalled) {
            xaeroMiniMapSupport.addOrUpdateMiniMapWaypoint(markerPosition, waypointState, true);
        }
        if (waypointState.renderOnWorldMap && AbstractModInitializer.xaeroWorldMapInstalled) {
            xaeroWorldMapSupport.addOrUpdateWorldMapWaypoint(markerPosition, waypointState);
        }
    }

    @Override
    void removeOldMarkerWaypoints() {
        if (AbstractModInitializer.xaeroMiniMapInstalled) xaeroMiniMapSupport.removeOldMarkerWaypoints();
        if (AbstractModInitializer.xaeroWorldMapInstalled) xaeroWorldMapSupport.removeOldMarkerWaypoints();
    }

    @Override
    public void removeAllMarkerWaypoints() {
        if (AbstractModInitializer.xaeroMiniMapInstalled && xaeroMiniMapSupport != null) xaeroMiniMapSupport.removeAllMarkerWaypoints();
        if (AbstractModInitializer.xaeroWorldMapInstalled && xaeroWorldMapSupport != null) xaeroWorldMapSupport.removeAllMarkerWaypoints();
    }

    @Override
    void updateMarkerWaypointColors() {
        if (AbstractModInitializer.xaeroMiniMapInstalled) xaeroMiniMapSupport.updateMarkerWaypointColors();
        if (AbstractModInitializer.xaeroWorldMapInstalled) xaeroWorldMapSupport.updateMarkerWaypointColors();
    }

    private boolean hasAreaMarkers = true;
    private Thread currentRasterThread;

    @Override
    public void handleAreaMarkers(List<AreaMarker> markerPositions) {
        currentlyRasterising = false;
        if (currentRasterThread != null) {
            try {
                currentRasterThread.join();
            } catch (InterruptedException ignored) {}
            currentRasterThread = null;
        }

        removeAllAreaMarkers(false);

        if (config.general.enableAreaMarkerOverlay) {
            currentlyRasterising = true;
            currentRasterThread = new Thread(() -> {
                for (Map.Entry<AreaMarker, List<Long>> cords : markerPositions.parallelStream()
                        .collect(Collectors.toMap(a -> a, this::rasterizeAreaMarker)).entrySet()) {
                    for (long cord : cords.getValue()) {
                        if (!currentlyRasterising) return;
                        createChunkHighlightAt(cords.getKey(), cord);
                    }
                }
                if (!currentlyRasterising) return;
                chunkHighlightHash = (chunkHighlightHash + 1) % 10000;
                if (mapHighlightClearer != null) mapHighlightClearer.clearHashCache();
                currentlyRasterising = false;
            });
            currentRasterThread.start();
        } else {
            chunkHighlightHash = (chunkHighlightHash + 1) % 10000;
            if (mapHighlightClearer != null) mapHighlightClearer.clearHashCache();
        }
    }

    private void createChunkHighlightAt(AreaMarker areaMarker, long chunkKey) {
        hasAreaMarkers = true;
        chunkHighlightMap.computeIfAbsent(chunkKey, key -> new HashSet<>()).add(areaMarker);
        regionsWithChunkHighlights.add(MathUtils.shiftRightIntsInLong(chunkKey, 5));
    }

    @Override
    public void removeAllAreaMarkers(boolean clearXaeroHash) {
        hasAreaMarkers = hasAreaMarkers || !chunkHighlightMap.isEmpty() || !regionsWithChunkHighlights.isEmpty();
        chunkHighlightMap.clear();
        regionsWithChunkHighlights.clear();
        currentlyRasterising = false;
        if (clearXaeroHash && hasAreaMarkers && mapHighlightClearer != null) {
            chunkHighlightHash = (chunkHighlightHash + 1) % 10000;
            mapHighlightClearer.clearHashCache();
            hasAreaMarkers = false;
        }
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
                if (area > config.general.maxChunkArea) {
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
                if (!currentlyRasterising) return new ArrayList<>();
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
                            if (!currentlyRasterising) return new ArrayList<>();
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
}
