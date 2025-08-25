/*
 *    This file is part of the Remote player waypoints for Xaero's Map mod
 *    licensed under the GNU GPL v3 License.
 *    (some parts of this file are originally from "RemotePlayers" by TheMrEngMan)
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

package de.the_build_craft.remote_player_waypoints_for_xaero.mixins.common.mods.xaerominimap;

import de.the_build_craft.remote_player_waypoints_for_xaero.common.FastUpdateTask;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.waypoints.PlayerPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
#if MC_VER == MC_1_17_1
import xaero.common.minimap.radar.MinimapRadar;
#else
import xaero.hud.minimap.radar.state.RadarStateUpdater;
#endif
import java.util.*;

import static de.the_build_craft.remote_player_waypoints_for_xaero.common.CommonModConfig.*;

/**
 * @author TheMrEngMan
 * @author Leander Knüttel
 * @version 25.08.2025
 */

@Pseudo
#if MC_VER == MC_1_17_1
@Mixin(MinimapRadar.class)
#else
@Mixin(RadarStateUpdater.class)
#endif
public class MinimapRadarMixin {
    @Unique
    private static final Map<ClientLevel, Map<String, RemotePlayer>> remote_player_waypoints_for_xaero$fakePlayerEntities = new HashMap<>();
    @Unique
    private static final Set<String> remote_player_waypoints_for_xaero$renderedPlayerNames = new HashSet<>();

    #if MC_VER == MC_1_17_1
    @ModifyVariable(method = "updateRadar", at = @At("STORE"), ordinal = 0)
    #else
    @ModifyVariable(method = "update", at = @At("STORE"), ordinal = 0)
    #endif
    private Iterable<Entity> updateRadarEntities(Iterable<Entity> worldEntities) {
        // Don't render if feature not enabled
        if(!config.general.enablePlayerRadar) return worldEntities;
        // Don't render if there is no remote players available
        if(FastUpdateTask.playerPositions.isEmpty()) return worldEntities;
        // Don't render if can't get access to world to check for players in range
        if(Minecraft.getInstance().level == null) return worldEntities;

        remote_player_waypoints_for_xaero$renderedPlayerNames.clear();
        for (AbstractClientPlayer playerClientEntity : Minecraft.getInstance().level.players()) {
            remote_player_waypoints_for_xaero$renderedPlayerNames.add(playerClientEntity.getName().plainCopy().getString());
        }

        if(!remote_player_waypoints_for_xaero$fakePlayerEntities.containsKey(Minecraft.getInstance().level)){
            remote_player_waypoints_for_xaero$fakePlayerEntities.put(Minecraft.getInstance().level, new HashMap<>());
        }

        List<Entity> worldEntitiesList = new ArrayList<>();
        worldEntities.forEach(worldEntitiesList::add);

        for (PlayerPosition playerPosition : FastUpdateTask.playerPositions.values()) {
            // Skip if player has invalid data
            if(playerPosition == null || playerPosition.gameProfile == null) continue;
            // Don't render same player when they are actually in range
            if(remote_player_waypoints_for_xaero$renderedPlayerNames.contains(playerPosition.name)) continue;

            // Add remote player to list as an entity
            RemotePlayer playerEntity;
            if(remote_player_waypoints_for_xaero$fakePlayerEntities.get(Minecraft.getInstance().level).containsKey(playerPosition.name)){
                playerEntity = remote_player_waypoints_for_xaero$fakePlayerEntities.get(Minecraft.getInstance().level).get(playerPosition.name);
            }
            else {
                #if MC_VER == MC_1_19_2
                playerEntity = new RemotePlayer(Minecraft.getInstance().level, playerPosition.gameProfile, null);
                #else
                playerEntity = new RemotePlayer(Minecraft.getInstance().level, playerPosition.gameProfile);
                #endif
                remote_player_waypoints_for_xaero$fakePlayerEntities.get(Minecraft.getInstance().level).put(playerPosition.name, playerEntity);
            }
            #if MC_VER < MC_1_21_5
            playerEntity.moveTo(playerPosition.x, playerPosition.y, playerPosition.z, 0, 0);
            #else
            playerEntity.snapTo(playerPosition.x, playerPosition.y, playerPosition.z, 0, 0);
            #endif
            // Add remote player entities to real entities in world
            worldEntitiesList.add(playerEntity);
        }
        return worldEntitiesList;
    }
}
