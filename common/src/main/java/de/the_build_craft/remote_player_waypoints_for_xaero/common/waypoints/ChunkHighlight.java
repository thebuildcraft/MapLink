/*
 *    This file is part of the Remote player waypoints for Xaero's Map mod
 *    licensed under the GNU GPL v3 License.
 *
 *    Copyright (C) 2025  Leander Knüttel
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

/**
 * @author Leander Knüttel
 * @version 23.07.2025
 */
public class ChunkHighlight {
    public String name;
    public String setName;
    public Color fillColor;
    public Color lineColor;

    public ChunkHighlight(String name, Color fillColor, Color lineColor, String setName) {
        this.name = name;
        this.fillColor = fillColor;
        this.lineColor = lineColor;
        this.setName = setName;
    }

    public ChunkHighlight(AreaMarker areaMarker) {
        this(areaMarker.name, areaMarker.fillColor, areaMarker.lineColor, areaMarker.SetName);
    }

    public void combine(ChunkHighlight chunkHighlight) {
        if (name.contains(chunkHighlight.name)) return;
        name += " | " + chunkHighlight.name;
        setName += " | " + chunkHighlight.setName;
        fillColor.combine(chunkHighlight.fillColor);
        lineColor.combine(chunkHighlight.lineColor);
    }
}
