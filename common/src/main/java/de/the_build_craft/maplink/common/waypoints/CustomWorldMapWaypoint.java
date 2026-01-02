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

package de.the_build_craft.maplink.common.waypoints;

import de.the_build_craft.maplink.common.clientMapHandlers.ClientMapHandler;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.map.mods.gui.Waypoint;

/**
 * @author Leander Knüttel
 * @version 25.08.2025
 */
public class CustomWorldMapWaypoint extends Waypoint {
    public final String id;
    private WaypointState waypointState;

    public CustomWorldMapWaypoint(TempWaypoint w) {
        super(w, w.getX(), w.getY(), w.getZ(), w.getName(), w.getInitials(), WaypointColor.fromIndex(w.getWaypointColor().ordinal()).getHex(), w.getPurpose().ordinal(), false, ClientMapHandler.waypointPrefix, w.isYIncluded(), 1);
        setTemporary(w.isTemporary());
        setGlobal(w.isGlobal());
        this.id = w.id;
        this.waypointState = w.getWaypointState();
    }

    public WaypointState getWaypointState() {
        if (waypointState.isOld) waypointState = ClientMapHandler.getWaypointState(id);
        return waypointState;
    }
}
