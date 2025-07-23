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

package de.the_build_craft.remote_player_waypoints_for_xaero.common.waypoints;

import java.util.Arrays;

/**
 * @author Leander Knüttel
 * @version 23.07.2025
 */
public class AreaMarker extends Position {
    public final Float3[] points;
    public final Color lineColor;
    public final Color fillColor;
    public String SetName;

    public AreaMarker(String name, int x, int y, int z, Float3[] points, Color lineColor, Color fillColor, String setName) {
        super(name, x, y, z);
        this.points = points;
        this.lineColor = lineColor;
        this.fillColor = fillColor;
        this.SetName = setName;
    }

    public AreaMarker(String name, float x, float y, float z, Float3[] points, Color lineColor, Color fillColor, String setName) {
        super(name, x, y, z);
        this.points = points;
        this.lineColor = lineColor;
        this.fillColor = fillColor;
        this.SetName = setName;
    }

    @Override
    public String getKey() {
        return super.getKey() + " " + Arrays.toString(points);
    }
}
