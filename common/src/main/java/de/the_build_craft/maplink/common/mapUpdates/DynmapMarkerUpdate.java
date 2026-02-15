/*
 *    This file is part of the Map Link mod
 *    licensed under the GNU GPL v3 License.
 *
 *    Copyright (C) 2024 - 2026  Leander Knüttel and contributors
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

import java.util.HashMap;
import java.util.Map;

/**
 * JSON object from dynmap API. Send in update requests
 *
 * @author Leander Knüttel
 * @author eatmyvenom
 * @version 25.08.2025
 */
public class DynmapMarkerUpdate {
    public static class Set {
        public static class Marker {
            public float x;
            public float y;
            public float z;
            public String label;
            public String icon = "";
        }
        public static class Area {
            public String fillcolor;
            public float fillopacity;
            public String color;
            public float opacity;
            public String label;
            public float[] x = new float[0];
            public float[] z = new float[0];
        }
        public static class Circle extends Marker {
            public String fillcolor;
            public float fillopacity;
            public String color;
            public float opacity;
            public float xr;
            public float zr;
        }

        public String label;
        public Map<String, Marker> markers = new HashMap<>();
        public Map<String, Area> areas = new HashMap<>();
        public Map<String, Circle> circles = new HashMap<>();
    }

    public Map<String, Set> sets = new HashMap<>();
}