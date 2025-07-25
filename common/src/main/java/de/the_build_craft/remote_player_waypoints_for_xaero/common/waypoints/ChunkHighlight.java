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

import java.util.HashSet;
import java.util.Set;

/**
 * @author Leander Knüttel
 * @version 25.07.2025
 */
public class ChunkHighlight {
    public String name;
    public String setName;
    public Color fillColor;
    public Color lineColor;
    Set<AreaMarker> areas = new HashSet<>();

    public static final ChunkHighlight NullHighlight = new ChunkHighlight("", new Color(), new Color(), "");

    private ChunkHighlight(String name, Color fillColor, Color lineColor, String setName) {
        this.name = name;
        this.fillColor = fillColor;
        this.lineColor = lineColor;
        this.setName = setName;
    }

    public ChunkHighlight(AreaMarker areaMarker) {
        this(areaMarker.name, areaMarker.fillColor, areaMarker.lineColor, areaMarker.SetName);
        areas.add(areaMarker);
    }

    public void combine(AreaMarker areaMarker) {
        if (areas.contains(areaMarker)) return;
        areas.add(areaMarker);
        ChunkHighlight chunkHighlight = new ChunkHighlight(areaMarker);
        name += " | " + chunkHighlight.name;
        setName += " | " + chunkHighlight.setName;
        fillColor = fillColor.combineToNew(chunkHighlight.fillColor);
        lineColor = lineColor.combineToNew(chunkHighlight.lineColor);
    }
}
