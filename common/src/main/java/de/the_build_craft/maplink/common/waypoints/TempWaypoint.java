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
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.WaypointPurpose;

import static de.the_build_craft.maplink.common.CommonModConfig.config;
import static de.the_build_craft.maplink.common.CommonModConfig.getPlayerWaypointColor;

/**
 * A wrapper to improve creating temp waypoints
 *
 * @author Leander Knüttel
 * @version 15.02.2026
 */
public class TempWaypoint extends Waypoint {
    public final String id;
    private WaypointState waypointState;

    public TempWaypoint(Position p, WaypointState waypointState) {
        super((int) Math.floor(p.pos.x), (int) Math.floor(p.pos.y), (int) Math.floor(p.pos.z), p.name, p.id,
                waypointState.isPlayer ? WaypointColor.fromIndex(getPlayerWaypointColor(p.name)) : WaypointColor.fromIndex(config.general.markerWaypointColor.ordinal()),
                WaypointPurpose.NORMAL, true);
        this.id = p.id;
        this.waypointState = waypointState;
    }

    public WaypointState getWaypointState() {
        if (waypointState.isOld) waypointState = ClientMapHandler.getWaypointState(id);
        return waypointState;
    }
}