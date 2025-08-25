/*
 *    This file is part of the Remote player waypoints for Xaero's Map mod
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

package de.the_build_craft.remote_player_waypoints_for_xaero.common.waypoints;

import de.the_build_craft.remote_player_waypoints_for_xaero.common.clientMapHandlers.ClientMapHandler;

/**
 * A marker's name and position
 *
 * @author Leander Knüttel
 * @version 25.08.2025
 */
public class Position {
    public final String name;
    public final int x;
    public final int y;
    public final int z;
    public final String id;
    public final String layer;

    public Position(String name, int x, int y, int z, String id, String layer) {
        this.name = getDisplayName(name);
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = ClientMapHandler.waypointPrefix +id;
        this.layer = layer;
    }

    public Position(String name, float x, float y, float z, String id, String layer) {
        this.name = getDisplayName(name);
        this.x = (int) Math.floor(x);
        this.y = (int) Math.floor(y);
        this.z = (int) Math.floor(z);
        this.id = ClientMapHandler.waypointPrefix + id;
        this.layer = layer;
    }

    public boolean CompareCords(Position otherPosition){
        return (this.x == otherPosition.x) && (this.y == otherPosition.y) && (this.z == otherPosition.z);
    }

    static String getDisplayName(String name){
        return org.apache.commons.lang3.StringEscapeUtils
                .unescapeHtml4(name.replaceAll("<.+?>|\\R|\\n", "").trim());
    }
}