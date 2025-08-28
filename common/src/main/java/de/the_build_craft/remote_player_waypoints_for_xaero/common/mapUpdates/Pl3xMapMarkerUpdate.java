/*
 *    This file is part of the Remote player waypoints for Xaero's Map mod
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

package de.the_build_craft.remote_player_waypoints_for_xaero.common.mapUpdates;

import de.the_build_craft.remote_player_waypoints_for_xaero.common.waypoints.Int3;

/**
 * @author Leander Knüttel
 * @version 28.08.2025
 */
public class Pl3xMapMarkerUpdate {
    public static class Data {
        public String key;
        //Icon
        public Int3 point;
        public String image = "";
        //Circle
        public Int3 center;
        public float radius;
        //Multipolygon
        public Polygon[] polygons = new Polygon[0];
        //Polygon
        public Polyline[] polylines = new Polyline[0];
        //Polyline
        public Int3[] points = new Int3[0];
        //Rectangle
        public Int3 point1;
        public Int3 point2;
    }

    public static class Polygon {
        public Polyline[] polylines = new Polyline[0];
    }

    public static class Polyline {
        public Int3[] points = new Int3[0];
    }

    public static class Options {
        public static class ToolTip {
            public String content;
        }
        public static class Stroke {

            public boolean enabled = true;
            public int color = -13399809;
        }
        public static class Fill {

            public boolean enabled;
            public int color = -13399809;
        }

        public Stroke stroke;
        public Fill fill;
        public ToolTip tooltip;
    }

    public String type;
    public Data data;
    public Options options;
}
