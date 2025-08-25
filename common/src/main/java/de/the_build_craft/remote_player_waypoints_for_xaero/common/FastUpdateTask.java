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
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
#if MC_VER >= MC_1_21_6
import net.minecraft.world.waypoints.TrackedWaypoint;
import java.util.UUID;
import java.util.stream.Collectors;
#endif

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static de.the_build_craft.remote_player_waypoints_for_xaero.common.CommonModConfig.*;

/**
 * @author Leander Knüttel
 * @version 25.08.2025
 */
public class FastUpdateTask {
    private final Minecraft mc;
    public static final Map<String, PlayerPosition> playerPositions = new ConcurrentHashMap<>();
    private final Map<String, PlayerPosition> onlinePlayerPositions = new ConcurrentHashMap<>();
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
                || !config.general.enabled
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
                            if (uuidPlayerMap.containsKey(uuid)) {
                                String name = uuidPlayerMap.get(uuid);
                                PlayerPosition playerPosition = new PlayerPosition(name,
                                        vec3iWaypoint.vector.getX(),
                                        vec3iWaypoint.vector.getY(),
                                        vec3iWaypoint.vector.getZ(),
                                        "");
                                ClientMapHandler.registerTempPlayerPosition(playerPosition);
                                playerPositions.put(name, playerPosition);
                            }
                        });
                    }
                });
        #endif

        for (AbstractClientPlayer player : mc.level.players()) {
            if (player instanceof RemotePlayer) {
                PlayerPosition playerPosition = new PlayerPosition(player);
                ClientMapHandler.registerTempPlayerPosition(playerPosition);
                playerPositions.put(player.getGameProfile().getName(), playerPosition);
            }
        }

        ClientMapHandler.getInstance().handlePlayerWaypoints();
    }

    public void updateFromOnlineMap(HashMap<String, PlayerPosition> onlinePlayerPositions) {
        this.onlinePlayerPositions.clear();
        this.onlinePlayerPositions.putAll(onlinePlayerPositions);
    }

    public static FastUpdateTask getInstance() {
        return instance;
    }
}
