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

/**
 * @author Leander Knüttel
 * @version 30.08.2025
 */
public class Float3 {
    public final float x;
    public final float y;
    public final float z;

    public Float3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Float3(double x, double y, double z) {
        this.x = (float) x;
        this.y = (float) y;
        this.z = (float) z;
    }

    @Override
    public String toString() {
        return x + " " + y + " " + z;
    }

    public boolean roughlyEqual(Float3 float3) {
        final float e = 0.01f;
        return (Math.abs(x - float3.x) < e) && (Math.abs(y - float3.y) < e) && (Math.abs(z - float3.z) < e);
    }

    public Int3 roundToInt3() {
        return new Int3(Math.round(x), Math.round(y), Math.round(z));
    }

    public Int3 floorToInt3() {
        return new Int3((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    public Int3 ceilToInt3() {
        return new Int3((int) Math.ceil(x), (int) Math.ceil(y), (int) Math.ceil(z));
    }

    public Float3 add(Float3 float3) {
        return new Float3(x + float3.x, y + float3.y, z + float3.z);
    }
}
