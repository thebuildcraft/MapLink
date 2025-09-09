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

import static de.the_build_craft.maplink.common.CommonModConfig.*;

/**
 * A wrapper to improve creating temp waypoints for players
 *
 * @author ewpratten
 * @author eatmyvenom
 * @author Leander Knüttel
 * @version 29.08.2025
 */
public class PlayerWaypoint extends TempWaypoint {
    public PlayerWaypoint(PlayerPosition p, WaypointState waypointState) {
        super((int) Math.floor(p.pos.x), (int) Math.floor(p.pos.y), (int) Math.floor(p.pos.z),
                p.name, getPlayerWaypointColor(p.name), p.id, waypointState);
    }
}