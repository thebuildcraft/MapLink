/*
 *    This file is part of the Map Link mod
 *    licensed under the GNU GPL v3 License.
 *
 *    Copyright (C) 2025 - 2026  Leander Knüttel and contributors
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

/**
 * @author Leander Knüttel
 * @version 05.09.2025
 */
public class MutableDouble3 {
    public double x;
    public double y;
    public double z;

    public MutableDouble3() {
    }

    public MutableDouble3(Double3 double3) {
        this.x = double3.x;
        this.y = double3.y;
        this.z = double3.z;
    }

    public void updateFrom(Double3 double3) {
        this.x = double3.x;
        this.y = double3.y;
        this.z = double3.z;
    }

    public void updateFrom(MutableDouble3 mutableDouble3) {
        this.x = mutableDouble3.x;
        this.y = mutableDouble3.y;
        this.z = mutableDouble3.z;
    }

    public Double3 toDouble3() {
        return new Double3(x, y, z);
    }
}
