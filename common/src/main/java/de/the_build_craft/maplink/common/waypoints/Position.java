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

import java.util.regex.Pattern;

/**
 * A marker's name and position
 *
 * @author Leander Knüttel
 * @version 05.09.2025
 */
public class Position {
    private static final Pattern nameRegexPattern = Pattern.compile("<.+?>|\\R|\\n");
    public final String name;
    public final Double3 pos;
    public final String id;
    public final MarkerLayer layer;

    public Position(String name, int x, int y, int z, String id, MarkerLayer layer) {
        this.name = getDisplayName(name);
        this.pos = new Double3(x + 0.5f, y, z + 0.5f);
        this.id = ClientMapHandler.waypointPrefix +id;
        this.layer = layer;
    }

    public Position(String name, double x, double y, double z, String id, MarkerLayer layer) {
        this.name = getDisplayName(name);
        this.pos = new Double3(x, y,z);
        this.id = ClientMapHandler.waypointPrefix + id;
        this.layer = layer;
    }

    static String getDisplayName(String name){
        return org.apache.commons.lang3.StringEscapeUtils
                .unescapeHtml4(nameRegexPattern.matcher(name).replaceAll("").trim());
    }
}