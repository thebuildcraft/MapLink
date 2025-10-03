/*
 *    This file is part of the Map Link mod
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

package de.the_build_craft.maplink.common;

import de.the_build_craft.maplink.common.clientMapHandlers.ClientMapHandler;
import de.the_build_craft.maplink.common.waypoints.Double3;
import de.the_build_craft.maplink.common.waypoints.PlayerPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
#if MC_VER >= MC_1_21_6
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.waypoints.TrackedWaypoint;
#endif

import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

import static de.the_build_craft.maplink.common.CommonModConfig.*;

/**
 * @author Leander Knüttel
 * @version 03.10.2025
 */
public class FastUpdateTask {
    private final Minecraft mc;
    public static final Map<String, PlayerPosition> playerPositions = new ConcurrentHashMap<>();
    private final Map<String, PlayerPosition> onlinePlayerPositions = new ConcurrentHashMap<>();
    private Set<String> currentLocalPlayerNames = new HashSet<>();
    private final Map<String, Integer> skipOnlineUpdates = new ConcurrentHashMap<>();
    private final Map<String, Double3> lastLocalVector = new ConcurrentHashMap<>();
    private final Map<String, PlayerPosition> lastLocalPosition = new HashMap<>();
    private static FastUpdateTask instance;

    public FastUpdateTask() {
        this.mc = Minecraft.getInstance();
        instance = this;
    }

    public void run() {
        try{
            runUpdate();
        }
        catch (Exception ignored){}
    }

    private void runUpdate() {
        // Skip if not in game or disabled
        if (mc.level == null
                || mc.player == null
                #if MC_VER >= MC_1_21_9
                || mc.getCameraEntity() == null
                #else
                || mc.cameraEntity == null
                #endif
                || (mc.getSingleplayerServer() != null && !mc.getSingleplayerServer().isPublished())
                || mc.getCurrentServer() == null
                || mc.getConnection() == null
                || !mc.getConnection().getConnection().isConnected()
                || !config.general.enabled
                || ClientMapHandler.getInstance() == null) {
            return;
        }

        playerPositions.clear();

        if (!AbstractModInitializer.connected) {
            //Don't use client-side data if not connected to an OnlineMap to comply with Modrinth's content rules!
            ClientMapHandler.getInstance().handlePlayerWaypoints();
            return;
        }

        Set<String> prevLocalPlayerNames = currentLocalPlayerNames;
        currentLocalPlayerNames = new HashSet<>(prevLocalPlayerNames.size());

        synchronized (onlinePlayerPositions) {
            playerPositions.putAll(onlinePlayerPositions);

            #if MC_VER >= MC_1_21_6
            Map<UUID, String> uuidPlayerMap = mc.getConnection().getOnlinePlayers().stream()
                    #if MC_VER >= MC_1_21_9
                    .collect(Collectors.toMap(playerInfo -> playerInfo.getProfile().id(),
                            playerInfo -> playerInfo.getProfile().name()));
                    #else
                    .collect(Collectors.toMap(playerInfo -> playerInfo.getProfile().getId(),
                            playerInfo -> playerInfo.getProfile().getName()));
                    #endif

            #if MC_VER >= MC_1_21_9
            Vec3 cameraPos = mc.getCameraEntity().getEyePosition();
            #else
            Vec3 cameraPos = mc.cameraEntity.getEyePosition();
            #endif

            #if MC_VER >= MC_1_21_9
            mc.player.connection.getWaypointManager().forEachWaypoint(mc.getCameraEntity(),
            #else
            mc.player.connection.getWaypointManager().forEachWaypoint(mc.cameraEntity,
            #endif
                    trackedWaypoint -> {
                        if (trackedWaypoint.type == TrackedWaypoint.Type.VEC3I) {
                            TrackedWaypoint.Vec3iWaypoint vec3iWaypoint = (TrackedWaypoint.Vec3iWaypoint) trackedWaypoint;
                            vec3iWaypoint.id().left().ifPresent(uuid -> {
                                if (uuidPlayerMap.containsKey(uuid)) {
                                    String name = uuidPlayerMap.get(uuid);
                                    //Only show players that are visible on the OnlineMap to comply with Modrinth's content rules!
                                    if (onlinePlayerPositions.containsKey(name)
                                            //useless outside 200m range!
                                            && cameraPos.distanceToSqr(vec3iWaypoint.vector.getX(), vec3iWaypoint.vector.getY(), vec3iWaypoint.vector.getZ()) < 200 * 200) {
                                        PlayerPosition playerPosition = new PlayerPosition(name,
                                                vec3iWaypoint.vector.getX(),
                                                vec3iWaypoint.vector.getY(),
                                                vec3iWaypoint.vector.getZ(),
                                                "");
                                        updateFromLocalPosition(playerPosition);
                                    }
                                }
                            });
                        }
                    });
            #endif

            for (AbstractClientPlayer player : mc.level.players()) {
                #if MC_VER >= MC_1_21_9
                String name = player.getGameProfile().name();
                #else
                String name = player.getGameProfile().getName();
                #endif
                //Only show players that are visible on the OnlineMap to comply with Modrinth's content rules!
                if (player instanceof RemotePlayer && onlinePlayerPositions.containsKey(name)) {
                    updateFromLocalPosition(new PlayerPosition(player));
                }
            }

            for (String playerName : prevLocalPlayerNames) {
                if (!currentLocalPlayerNames.contains(playerName)) {
                    skipOnlineUpdates.put(playerName, 2);
                }
            }
        }

        ClientMapHandler.getInstance().handlePlayerWaypoints();
    }

    private void updateFromLocalPosition(PlayerPosition playerPosition) {
        ClientMapHandler.registerTempPlayerPosition(playerPosition);
        Double3 lastPos = lastLocalPosition.getOrDefault(playerPosition.name, playerPosition).pos;
        if (!lastPos.roughlyEqual(playerPosition.pos)) {
            lastLocalVector.put(playerPosition.name, playerPosition.pos.sub(lastPos));
        }
        lastLocalPosition.put(playerPosition.name, playerPosition);
        playerPositions.put(playerPosition.name, playerPosition);
        currentLocalPlayerNames.add(playerPosition.name);
        skipOnlineUpdates.remove(playerPosition.name);
        onlinePlayerPositions.put(playerPosition.name, playerPosition);
    }

    public void updateFromOnlineMap(HashMap<String, PlayerPosition> onlinePlayerPositions) {
        synchronized (this.onlinePlayerPositions) {
            for (PlayerPosition playerPosition : onlinePlayerPositions.values()) {
                if (skipOnlineUpdates.containsKey(playerPosition.name)) {
                    double dot = playerPosition.pos.sub(lastLocalPosition.get(playerPosition.name).pos)
                            .dot(lastLocalVector.getOrDefault(playerPosition.name, Double3.ZERO));
                    if (dot >= 0) {
                        skipOnlineUpdates.remove(playerPosition.name);
                        this.onlinePlayerPositions.put(playerPosition.name, playerPosition);
                        continue;
                    }
                    int skipCount = skipOnlineUpdates.get(playerPosition.name);
                    if (skipCount <= 0) {
                        skipOnlineUpdates.remove(playerPosition.name);
                        this.onlinePlayerPositions.put(playerPosition.name, playerPosition);
                        continue;
                    }
                    skipOnlineUpdates.put(playerPosition.name, skipCount - 1);
                } else {
                    this.onlinePlayerPositions.put(playerPosition.name, playerPosition);
                }
            }
            this.onlinePlayerPositions.entrySet().removeIf(e -> !onlinePlayerPositions.containsKey(e.getKey()));
            this.skipOnlineUpdates.entrySet().removeIf(e -> !onlinePlayerPositions.containsKey(e.getKey()));
        }
    }

    public void clearAllPlayerPositions() {
        playerPositions.clear();
        onlinePlayerPositions.clear();
    }

    public static FastUpdateTask getInstance() {
        return instance;
    }
}
