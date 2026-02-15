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
public class Double3 {
    public static final Double3 ZERO = new Double3(0, 0, 0);

    public final double x;
    public final double y;
    public final double z;

    public Double3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {
        return x + " " + y + " " + z;
    }

    public boolean roughlyEqual(Double3 double3) {
        final double e = 0.01;
        return (Math.abs(x - double3.x) < e) && (Math.abs(y - double3.y) < e) && (Math.abs(z - double3.z) < e);
    }

    public Int3 roundToInt3() {
        return new Int3((int) Math.round(x), (int) Math.round(y), (int) Math.round(z));
    }

    public Int3 floorToInt3() {
        return new Int3((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    public Int3 ceilToInt3() {
        return new Int3((int) Math.ceil(x), (int) Math.ceil(y), (int) Math.ceil(z));
    }

    public Double3 add(Double3 double3) {
        return new Double3(x + double3.x, y + double3.y, z + double3.z);
    }

    public Double3 sub(Double3 double3) {
        return new Double3(x - double3.x, y - double3.y, z - double3.z);
    }

    public double dot(Double3 double3) {
        return x * double3.x + y * double3.y + z * double3.z;
    }

    public double len() {
        return Math.sqrt(x * x + y * y + z * z);
    }
}
