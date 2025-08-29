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

package de.the_build_craft.remote_player_waypoints_for_xaero.forge;

import de.the_build_craft.remote_player_waypoints_for_xaero.common.waypoints.MutablePlayerPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import xaero.hud.minimap.player.tracker.system.ITrackedPlayerReader;

import java.util.UUID;

/**
 * @author Leander Knüttel
 * @version 29.08.2025
 */
public class RemotePlayerTrackerReader implements ITrackedPlayerReader<MutablePlayerPosition>, xaero.map.radar.tracker.system.ITrackedPlayerReader<MutablePlayerPosition> {
    @Override
    public UUID getId(MutablePlayerPosition playerPosition) {
        return playerPosition.uuid;
    }

    @Override
    public double getX(MutablePlayerPosition playerPosition) {
        return playerPosition.pos.x;
    }

    @Override
    public double getY(MutablePlayerPosition playerPosition) {
        return playerPosition.pos.y;
    }

    @Override
    public double getZ(MutablePlayerPosition playerPosition) {
        return playerPosition.pos.z;
    }

    @Override
    public ResourceKey<Level> getDimension(MutablePlayerPosition playerPosition) {
        return Minecraft.getInstance().level.dimension();
    }
}
