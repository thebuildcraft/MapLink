/*
 *    This file is part of the Map Link mod
 *    licensed under the GNU GPL v3 License.
 *    (some parts of this file are originally from "RemotePlayers" by ewpratten)
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

package de.the_build_craft.maplink.common.waypoints;

import de.the_build_craft.maplink.common.clientMapHandlers.ClientMapHandler;
import xaero.common.minimap.waypoints.Waypoint;
#if MC_VER != MC_1_17_1
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.WaypointPurpose;
#endif

/**
 * A wrapper to improve creating temp waypoints
 *
 * @author Leander Knüttel
 * @version 25.08.2025
 */
public abstract class TempWaypoint extends Waypoint {
    public final String id;
    private WaypointState waypointState;

    public TempWaypoint(int x, int y, int z, String name, int color, String id, WaypointState waypointState) {
        super(x, y, z, name, id,
                #if MC_VER == MC_1_17_1
                color, 0, true);
                #else
                WaypointColor.fromIndex(color), WaypointPurpose.NORMAL, true);
                #endif
        this.id = id;
        this.waypointState = waypointState;
    }

    public WaypointState getWaypointState() {
        if (waypointState.isOld) waypointState = ClientMapHandler.getWaypointState(id);
        return waypointState;
    }
}