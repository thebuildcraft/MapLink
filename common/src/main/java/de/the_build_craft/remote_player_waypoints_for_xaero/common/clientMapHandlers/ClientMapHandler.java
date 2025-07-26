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

import de.the_build_craft.remote_player_waypoints_for_xaero.common.AbstractModInitializer;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.CommonModConfig;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.waypoints.*;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.wrappers.Text;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.wrappers.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Leander Knüttel
 * @version 26.07.2025
 */
public abstract class ClientMapHandler {
    private static ClientMapHandler instance;

    private final Minecraft mc;

    private static final int maxMarkerCountBeforeWarning = 25;
    private boolean markerMessageWasShown = false;

    private int previousPlayerWaypointColor = 0;
    private int previousMarkerWaypointColor = 0;
    private int previousFriendWaypointColor = 0;
    private boolean previousFriendColorOverride = false;
    private int previousFriendListHashCode = 0;

    public static Map<String, PlayerPosition> playerPositions = new HashMap<>();
    Set<String> currentPlayerWaypointNames = new HashSet<>();
    Set<String> currentMarkerWaypointKeys = new HashSet<>();

    public ClientMapHandler() {
        instance = this;
        mc = Minecraft.getInstance();
    }

    public static ClientMapHandler getInstance() {
        return instance;
    }

    public void handlePlayerWaypoints(Map<String, PlayerPosition> playerPositions) {
        ClientMapHandler.playerPositions = playerPositions;
        // Update the player positions obtained from Dynmap with GameProfile data from the actual logged-in players
        // This is required so that the entity radar properly shows the player's skin on player head icons
        if(CommonModConfig.Instance.enableEntityRadar()) {
            Collection<PlayerInfo> playerList = mc.getConnection().getOnlinePlayers();
            for (PlayerInfo playerListEntity : playerList) {
                String playerName = playerListEntity.getProfile().getName();
                if (playerPositions.containsKey(playerName)) {
                    playerPositions.get(playerName).gameProfile = playerListEntity.getProfile();
                }
            }
        }

        if (CommonModConfig.Instance.enablePlayerWaypoints()) {
            // Keep track of which waypoints were previously shown
            // to remove any that are not to be shown anymore
            currentPlayerWaypointNames.clear();

            Map<String, AbstractClientPlayer> playerClientEntityMap = mc.level.players().stream().collect(
                    Collectors.toMap(a -> a.getGameProfile().getName(), a -> a));

            // Add each player to the map
            for (PlayerPosition playerPosition : playerPositions.values()) {
                if (playerPosition == null) continue;
                String playerName = playerPosition.name;

                boolean isFriend = CommonModConfig.Instance.friendList().contains(playerName);

                if (CommonModConfig.Instance.onlyShowFriendsWaypoints() && !isFriend) continue;

                int minimumWaypointDistanceToUse;
                int maximumWaypointDistanceToUse;
                if (CommonModConfig.Instance.overwriteFriendDistances() && isFriend) {
                    minimumWaypointDistanceToUse = CommonModConfig.Instance.minFriendDistance();
                    maximumWaypointDistanceToUse = CommonModConfig.Instance.maxFriendDistance();
                } else {
                    minimumWaypointDistanceToUse = CommonModConfig.Instance.minDistance();
                    maximumWaypointDistanceToUse = CommonModConfig.Instance.maxDistance();
                }

                if (minimumWaypointDistanceToUse > maximumWaypointDistanceToUse)
                    maximumWaypointDistanceToUse = minimumWaypointDistanceToUse;

                // If closer than the minimum waypoint distance or further away than the maximum waypoint distance,
                // don't show waypoint
                double d = mc.cameraEntity.position().distanceTo(new Vec3(playerPosition.x, playerPosition.y, playerPosition.z));
                if (d < minimumWaypointDistanceToUse || d > maximumWaypointDistanceToUse) continue;

                // Check if this player is within the server's player entity tracking range
                if (playerClientEntityMap.containsKey(playerName)) {
                    ClipContext clipContext = new ClipContext(mc.cameraEntity.getEyePosition(),
                            playerClientEntityMap.get(playerName).position().add(0, 1, 0),
                            ClipContext.Block.VISUAL, ClipContext.Fluid.ANY, CollisionContext.empty());
                    // If this player is visible, don't show waypoint
                    if (mc.level.clip(clipContext).getType() != HitResult.Type.BLOCK) continue;
                }
                currentPlayerWaypointNames.add(playerName);
                addOrUpdatePlayerWaypoint(playerPosition);
            }
            removeOldPlayerWaypoints();

            int newPlayerWaypointColor = CommonModConfig.Instance.playerWaypointColor();
            int newFriendWaypointColor = CommonModConfig.Instance.friendWaypointColor();
            boolean newFriendColorOverride = CommonModConfig.Instance.overwriteFriendWaypointColor();
            int newFriendListHashCode = CommonModConfig.Instance.friendList().hashCode();
            if ((previousPlayerWaypointColor != newPlayerWaypointColor)
                    || (previousFriendWaypointColor != newFriendWaypointColor)
                    || (previousFriendColorOverride != newFriendColorOverride)
                    || (previousFriendListHashCode != newFriendListHashCode)) {
                previousPlayerWaypointColor = newPlayerWaypointColor;
                previousFriendWaypointColor = newFriendWaypointColor;
                previousFriendColorOverride = newFriendColorOverride;
                previousFriendListHashCode = newFriendListHashCode;

                updatePlayerWaypointColors();
            }
        } else {
            removeAllPlayerWaypoints();
        }
    }

    abstract void addOrUpdatePlayerWaypoint(PlayerPosition playerPosition);

    abstract void removeOldPlayerWaypoints();

    public abstract void removeAllPlayerWaypoints();

    abstract void updatePlayerWaypointColors();

    public void handleMarkerWaypoints(List<WaypointPosition> markerPositions) {
        if (CommonModConfig.Instance.enableMarkerWaypoints()) {
            // Keep track of which waypoints were previously shown
            // to remove any that are not to be shown anymore
            currentMarkerWaypointKeys.clear();

            for (WaypointPosition markerPosition : markerPositions) {
                int minimumWaypointDistanceToUse = CommonModConfig.Instance.minDistanceMarker();
                int maximumWaypointDistanceToUse = CommonModConfig.Instance.maxDistanceMarker();
                if (minimumWaypointDistanceToUse > maximumWaypointDistanceToUse)
                    maximumWaypointDistanceToUse = minimumWaypointDistanceToUse;

                // If closer than the minimum waypoint distance or further away than the maximum waypoint distance,
                // don't show waypoint
                double d = mc.cameraEntity.position().distanceTo(new Vec3(markerPosition.x, markerPosition.y, markerPosition.z));
                if (d < minimumWaypointDistanceToUse || d > maximumWaypointDistanceToUse) continue;

                currentMarkerWaypointKeys.add(markerPosition.getKey());
                addOrUpdateMarkerWaypoint(markerPosition);
            }
            removeOldMarkerWaypoints();

            int newMarkerWaypointColor = CommonModConfig.Instance.markerWaypointColor();
            if (previousMarkerWaypointColor != newMarkerWaypointColor) {
                previousMarkerWaypointColor = newMarkerWaypointColor;
                updateMarkerWaypointColors();
            }

            if (!markerMessageWasShown && currentMarkerWaypointKeys.size() > maxMarkerCountBeforeWarning && !CommonModConfig.Instance.ignoreMarkerMessage()) {
                markerMessageWasShown = true;
                Utils.sendToClientChat(Text.literal("[" + AbstractModInitializer.MOD_NAME + "]: " +
                                "Looks like you have quite a lot of markers from the server visible! " +
                                "Did you know that you can chose the marker layers that are shown in the config, decrease their maximum distance or disable marker waypoints entirely? ")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD))
                        .append(Text.literal("[Don't show this again]")
                                .withStyle(Style.EMPTY.withClickEvent(
                                    #if MC_VER < MC_1_21_5
                                    new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + AbstractModInitializer.MOD_ID + " ignore_marker_message"))
                                    #else
                                    new ClickEvent.SuggestCommand("/" + AbstractModInitializer.MOD_ID + " ignore_marker_message"))
                                    #endif
                                        .withColor(ChatFormatting.GREEN).withBold(true))));
            }
        } else {
            removeAllMarkerWaypoints();
        }
    }

    abstract void addOrUpdateMarkerWaypoint(WaypointPosition markerPosition);

    abstract void removeOldMarkerWaypoints();

    public abstract void removeAllMarkerWaypoints();

    abstract void updateMarkerWaypointColors();

    public abstract void handleAreaMarkers(List<AreaMarker> markerPositions, boolean refresh);

    public abstract void removeAllAreaMarkers(boolean clearXaeroHash);
}
