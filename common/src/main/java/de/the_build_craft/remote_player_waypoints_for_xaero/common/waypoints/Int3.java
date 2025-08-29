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
 * @version 29.08.2025
 */
public class Int3 {
    public final int x;
    public final int y;
    public final int z;

    public Int3(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {
        return x + " " + y + " " + z;
    }

    public Float3 toFloat3() {
        return new Float3(x, y, z);
    }

    public Int3 toChunkCords() {
        return new Int3(x >> 4, y >> 4, z >> 4);
    }

    public Int3 add(Int3 int3) {
        return new Int3(x + int3.x, y + int3.y, z + int3.z);
    }
}
