/*
 *    This file is part of the Remote player waypoints for Xaero's Map mod
 *    licensed under the GNU GPL v3 License.
 *
 *    Copyright (C) 2025  Leander Knüttel
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

import de.the_build_craft.remote_player_waypoints_for_xaero.common.AbstractModInitializer;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.CommonModConfig;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.waypoints.*;
#if MC_VER == MC_1_17_1
import xaero.common.AXaeroMinimap;
import xaero.common.XaeroMinimapSession;
import xaero.common.minimap.waypoints.WaypointWorld;
#else
import xaero.common.HudMod;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;
#endif
import xaero.common.minimap.waypoints.Waypoint;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Leander Knüttel
 * @version 23.07.2025
 */
public class XaeroClientMapHandler extends ClientMapHandler {
    #if MC_VER == MC_1_17_1
    private ArrayList<Waypoint> playerWaypointList = null;
    private ArrayList<Waypoint> markerWaypointList = null;
    #else
    private WaypointSet playerWaypointList = null;
    private WaypointSet markerWaypointList = null;
    #endif
    private static final String PLAYER_SET_NAME = AbstractModInitializer.MOD_NAME +  "_Player";
    private static final String MARKER_SET_NAME = AbstractModInitializer.MOD_NAME +  "_Marker";

    Map<String, Integer> playerWaypointNamesIndexes;
    Map<String, Integer> markerWaypointKeysIndexes;

    public static Map<Long, ChunkHighlight> chunkHighlightMap = new HashMap<>();
    public static Set<Long> regionsWithChunkHighlights = new HashSet<>();
    public static int chunkHighlightHash;
    public static MapHighlightClearer mapHighlightClearer;

    @Override
    public void handlePlayerWaypoints(Map<String, PlayerPosition> playerPositions) {
        initWaypointSets();
        super.handlePlayerWaypoints(playerPositions);
    }

    @Override
    void addOrUpdatePlayerWaypoint(PlayerPosition playerPosition) {
        synchronized (playerWaypointList) {
            // If a waypoint for this player already exists, update it
            if (playerWaypointNamesIndexes.containsKey(playerPosition.name)) {
                Waypoint waypoint = playerWaypointList.get(playerWaypointNamesIndexes.get(playerPosition.name));

                waypoint.setX(playerPosition.x);
                waypoint.setY(playerPosition.y);
                waypoint.setZ(playerPosition.z);
            }
            // Otherwise, add a waypoint for the player
            else {
                playerWaypointList.add(new PlayerWaypoint(playerPosition));
            }
        }
    }

    private void initWaypointSets() {
        #if MC_VER == MC_1_17_1
        AXaeroMinimap.INSTANCE.getSettings().renderAllSets = true;
        #else
        HudMod.INSTANCE.getSettings().renderAllSets = true;
        #endif

        // Access the current waypoint world
        #if MC_VER == MC_1_17_1
        WaypointWorld currentWorld = XaeroMinimapSession.getCurrentSession().getWaypointsManager().getCurrentWorld();

        if (currentWorld.getSets().get(PLAYER_SET_NAME) == null){
            currentWorld.addSet(PLAYER_SET_NAME);
        }
        if (currentWorld.getSets().get(MARKER_SET_NAME) == null){
            currentWorld.addSet(MARKER_SET_NAME);
        }

        playerWaypointList = currentWorld.getSets().get(PLAYER_SET_NAME).getList();
        markerWaypointList = currentWorld.getSets().get(MARKER_SET_NAME).getList();
        #else
        MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
        MinimapWorld currentWorld = session.getWorldManager().getCurrentWorld();

        if (currentWorld.getWaypointSet(PLAYER_SET_NAME) == null){
            currentWorld.addWaypointSet(PLAYER_SET_NAME);
        }
        if (currentWorld.getWaypointSet(MARKER_SET_NAME) == null){
            currentWorld.addWaypointSet(MARKER_SET_NAME);
        }

        playerWaypointList = currentWorld.getWaypointSet(PLAYER_SET_NAME);
        markerWaypointList = currentWorld.getWaypointSet(MARKER_SET_NAME);
        #endif

        synchronized (playerWaypointList) {
            // Create indexes of matching player names to waypoints to update the waypoints by index
            playerWaypointNamesIndexes = new HashMap<>(playerWaypointList.size());
            for (int i = 0; i < playerWaypointList.size(); i++) {
                playerWaypointNamesIndexes.put(playerWaypointList.get(i).getName(), i);
            }
        }

        synchronized (markerWaypointList) {
            // Create indexes of matching marker keys to waypoints to update the waypoints by index
            markerWaypointKeysIndexes = new HashMap<>(markerWaypointList.size());
            for (int i = 0; i < markerWaypointList.size(); i++) {
                markerWaypointKeysIndexes.put(TempWaypoint.getWaypointKey(markerWaypointList.get(i)), i);
            }
        }
    }

    @Override
    void removeOldPlayerWaypoints() {
        synchronized (playerWaypointList) {
            // Remove any waypoints for players not shown on the map anymore
            #if MC_VER == MC_1_17_1
            playerWaypointList.removeIf(waypoint -> !currentPlayerWaypointNames.contains(waypoint.getName()));
            #else
            Iterator<Waypoint> iterator = playerWaypointList.getWaypoints().iterator();
            while (iterator.hasNext()) {
                Waypoint w = iterator.next();
                if (!currentPlayerWaypointNames.contains(w.getName())) iterator.remove();
            }
            #endif
        }
    }

    @Override
    public void removeAllPlayerWaypoints() {
        synchronized (playerWaypointList) {
            playerWaypointList.clear();
        }
    }

    @Override
    void updatePlayerWaypointColors() {
        synchronized (playerWaypointList) {
            #if MC_VER == MC_1_17_1
            for (Waypoint waypoint : playerWaypointList) {
                waypoint.setColor(CommonModConfig.Instance.getPlayerWaypointColor(waypoint.getName()));
            }
            #else
            for (Waypoint waypoint : playerWaypointList.getWaypoints()) {
                waypoint.setWaypointColor(WaypointColor.fromIndex(CommonModConfig.Instance.getPlayerWaypointColor(waypoint.getName())));
            }
            #endif
        }
    }

    @Override
    void addOrUpdateMarkerWaypoint(WaypointPosition markerPosition) {
        synchronized (markerWaypointList) {
            // If a waypoint for this marker already exists, update it
            if (markerWaypointKeysIndexes.containsKey(markerPosition.getKey())) {
                Waypoint waypoint = markerWaypointList.get(markerWaypointKeysIndexes.get(markerPosition.getKey()));

                waypoint.setX(markerPosition.x);
                waypoint.setY(markerPosition.y);
                waypoint.setZ(markerPosition.z);
            }
            // Otherwise, add a waypoint for the marker
            else {
                markerWaypointList.add(new FixedWaypoint(markerPosition));
            }
        }
    }

    @Override
    void removeOldMarkerWaypoints() {
        synchronized (markerWaypointList) {
            // Remove any waypoints for markers not shown on the map anymore
            #if MC_VER == MC_1_17_1
            markerWaypointList.removeIf(waypoint -> !currentMarkerWaypointKeys.contains(TempWaypoint.getWaypointKey(waypoint)));
            #else
            Iterator<Waypoint> iterator = markerWaypointList.getWaypoints().iterator();
            while (iterator.hasNext()) {
                Waypoint w = iterator.next();
                if (!currentMarkerWaypointKeys.contains(TempWaypoint.getWaypointKey(w))) iterator.remove();
            }
            #endif
        }
    }

    @Override
    public void removeAllMarkerWaypoints() {
        synchronized (markerWaypointList) {
            markerWaypointList.clear();
        }
    }

    @Override
    void updateMarkerWaypointColors() {
        synchronized (markerWaypointList) {
            #if MC_VER == MC_1_17_1
            for (Waypoint waypoint : markerWaypointList) {
                waypoint.setColor(CommonModConfig.Instance.markerWaypointColor());
            }
            #else
            for (Waypoint waypoint : markerWaypointList.getWaypoints()){
                waypoint.setWaypointColor(WaypointColor.fromIndex(CommonModConfig.Instance.markerWaypointColor()));
            }
            #endif
        }
    }

    @Override
    public void handleAreaMarkers(List<AreaMarker> markerPositions) {
        removeAllAreaMarkers();
        for (Map.Entry<AreaMarker, List<Long>> cords : markerPositions.parallelStream()
                .collect(Collectors.toMap(a -> a, this::rasterizeAreaMarker)).entrySet()) {
            for (long cord : cords.getValue()) {
                createChunkHighlightAt(cords.getKey(), cord);
            }
        }
        if (mapHighlightClearer != null) mapHighlightClearer.clearHashCache();
    }

    static long getChunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | chunkZ & 0xFFFFFFFFL;
    }

    private void createChunkHighlightAt(AreaMarker areaMarker, long chunkKey) {
        ChunkHighlight chunkHighlight = chunkHighlightMap.get(chunkKey);
        if (chunkHighlight != null) {
            if (!chunkHighlight.name.equals(areaMarker.name)) chunkHighlight.combine(new ChunkHighlight(areaMarker));
        } else {
            chunkHighlightMap.put(chunkKey, new ChunkHighlight(areaMarker));
        }
        regionsWithChunkHighlights.add(((chunkKey >> 5) & 0xFFFFFFFF00000000L) | ((chunkKey & 0xFFFFFFFFL) >> 5));
    }

    @Override
    public void removeAllAreaMarkers() {
        chunkHighlightHash = (chunkHighlightHash + 1) % 10000;
        chunkHighlightMap.clear();
        regionsWithChunkHighlights.clear();
    }

    //Thanks to https://www.mathematik.uni-marburg.de/~thormae/lectures/graphics1/code_v2/RasterPoly/index.html
    //for the visualizer and reference implementation (I made some changes)
    List<Long> rasterizeAreaMarker(AreaMarker areaMarker) {
        try {
            if (areaMarker.points.length == 0) return new ArrayList<>();

            Int3[] points = Arrays.stream(areaMarker.points)
                    .map(f -> f.toInt3().toChunkCords()).toArray(Int3[]::new);

            List<Edge> edges = new ArrayList<>();
            List<Edge> horizontalEdges = new ArrayList<>();
            int globalXMin = Integer.MAX_VALUE;
            int globalXMax = Integer.MIN_VALUE;
            int globalZMin = Integer.MAX_VALUE;
            int globalZMax = Integer.MIN_VALUE;
            for (int i = 0; i < points.length; i++) {
                Int3 point = points[i];
                Int3 otherPoint = points[(i + 1) % points.length];
                globalXMin = Math.min(globalXMin, point.x);
                globalXMax = Math.max(globalXMax, point.x);
                globalZMin = Math.min(globalZMin, point.z);
                globalZMax = Math.max(globalZMax, point.z);
                if (point.z == otherPoint.z) {
                    horizontalEdges.add(new Edge(point, otherPoint));
                } else {
                    edges.add(new Edge(point, otherPoint));
                }
            }
            int area = (globalXMax - globalXMin) * (globalZMax - globalZMin);
            if (area > 50_000) {
                AbstractModInitializer.LOGGER.warn("polygon to large: " + areaMarker.name);
                return new ArrayList<>();
            }
            List<Long> result = new ArrayList<>(area);
            Collections.sort(edges);

            for (Edge edge : horizontalEdges) {
                for (int x = edge.xMin; x <= edge.xMax; x++) {
                    result.add(getChunkKey(x, edge.zMin));
                }
            }

            int z = edges.getFirst().zMin;
            List<Edge> activeEdges = new ArrayList<>();
            List<Float> intersections = new ArrayList<>();

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
                    if (e.zMax != z) intersections.add(e.xHit);
                }

                if (intersections.size() % 2 == 0) {
                    for (int i = 0; i < intersections.size() - 1; i += 2) {
                        int xMin = (int)Math.floor(intersections.get(i));
                        int xMax = (int)Math.ceil(intersections.get(i+1));
                        if (i != intersections.size() - 2) xMax--;

                        for (int x = xMin; x <= xMax; x++) {
                            result.add(getChunkKey(x, z));
                        }
                    }
                } else {
                    AbstractModInitializer.LOGGER.error("error in polygon rasterization: " + areaMarker.name);
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
            return result;
        } catch (Throwable t) {
            t.printStackTrace();
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
