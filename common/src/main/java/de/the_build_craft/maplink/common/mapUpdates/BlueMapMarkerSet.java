/*
 *    This file is part of the Map Link mod
 *    licensed under the GNU GPL v3 License.
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

package de.the_build_craft.maplink.common.mapUpdates;

import de.the_build_craft.maplink.common.waypoints.Color;
import de.the_build_craft.maplink.common.waypoints.Double3;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Leander Knüttel
 * @author eatmyvenom
 * @version 02.01.2026
 */
public class BlueMapMarkerSet {
    public static class Marker {
        public String type;
        public String label;
        public Double3 position;
        public Double3[] shape = new Double3[0];
        public Double3[][] holes = new Double3[0][];
        public Color lineColor;
        public Color fillColor;
        public String icon = "";
    }
    public Map<String, Marker> markers = new HashMap<>();
    public String label;
}
