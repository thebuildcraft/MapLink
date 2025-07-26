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

package de.the_build_craft.remote_player_waypoints_for_xaero.common;

import de.the_build_craft.remote_player_waypoints_for_xaero.common.clientMapHandlers.ClientMapHandler;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.waypoints.PlayerPosition;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.waypoints.TrackedWaypoint;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Leander Knüttel
 * @version 26.07.2025
 */
public class FastUpdateTask {
    private final Minecraft mc;
    public static final Map<String, PlayerPosition> playerPositions = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
    final Map<String, PlayerPosition> onlinePlayerPositions = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
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
                || mc.cameraEntity == null
                || (mc.getSingleplayerServer() != null && !mc.getSingleplayerServer().isPublished())
                || mc.getCurrentServer() == null
                || mc.getConnection() == null
                || !mc.getConnection().getConnection().isConnected()
                || !AbstractModInitializer.enabled
                || ClientMapHandler.getInstance() == null) {
            return;
        }

        playerPositions.clear();
        playerPositions.putAll(onlinePlayerPositions);

        #if MC_VER >= MC_1_21_6
        Map<UUID, String> uuidPlayerMap = mc.getConnection().getOnlinePlayers().stream()
                .collect(Collectors.toMap(playerInfo -> playerInfo.getProfile().getId(),
                        playerInfo -> playerInfo.getProfile().getName()));

        mc.player.connection.getWaypointManager().forEachWaypoint(mc.cameraEntity,
                trackedWaypoint -> {
                    if (trackedWaypoint.type == TrackedWaypoint.Type.VEC3I) {
                        TrackedWaypoint.Vec3iWaypoint vec3iWaypoint = (TrackedWaypoint.Vec3iWaypoint) trackedWaypoint;
                        vec3iWaypoint.id().left().ifPresent(uuid -> {
                            if (uuidPlayerMap.containsKey(uuid)) playerPositions.put(uuidPlayerMap.get(uuid),
                                    new PlayerPosition(uuidPlayerMap.get(uuid),
                                            vec3iWaypoint.vector.getX(),
                                            vec3iWaypoint.vector.getY(),
                                            vec3iWaypoint.vector.getZ(),
                                            ""));
                        });
                    }
                });
        #endif

        for (AbstractClientPlayer player : mc.level.players()) {
            if (player instanceof RemotePlayer) {
                playerPositions.put(player.getGameProfile().getName(), new PlayerPosition(player));
            }
        }

        ClientMapHandler.getInstance().handlePlayerWaypoints(playerPositions);
    }

    public void updateFromOnlineMap(HashMap<String, PlayerPosition> onlinePlayerPositions) {
        this.onlinePlayerPositions.clear();
        this.onlinePlayerPositions.putAll(onlinePlayerPositions);
    }

    public static FastUpdateTask getInstance() {
        return instance;
    }
}
