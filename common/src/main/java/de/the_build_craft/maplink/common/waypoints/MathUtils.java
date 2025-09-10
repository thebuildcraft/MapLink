/*
 *    This file is part of the Map Link mod
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

package de.the_build_craft.maplink.common.waypoints;

/**
 * @author Leander Knüttel
 * @version 25.07.2025
 */
public class MathUtils {
    private MathUtils(){}

    public static long combineIntsToLong(int a, int b) {
        return (((long) a) << 32) | (b & 0xFFFFFFFFL);
    }

    public static int getFirstIntFromLong(long l) {
        return (int) (l >> 32);
    }

    public static int getSecondIntFromLong(long l) {
        return (int) l;
    }

    public static long shiftRightIntsInLong(long l, int shift) {
        return combineIntsToLong(getFirstIntFromLong(l) >> shift, getSecondIntFromLong(l) >> shift);
    }

    public static long shiftLeftIntsInLong(long l, int shift) {
        return combineIntsToLong(getFirstIntFromLong(l) << shift, getSecondIntFromLong(l) << shift);
    }
}
