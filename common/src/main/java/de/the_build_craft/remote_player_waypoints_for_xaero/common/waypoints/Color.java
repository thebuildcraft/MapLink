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
 * @version 23.07.2025
 */
public class Color {
    public int r;
    public int g;
    public int b;
    public float a;

    public Color() {}

    public Color(int r, int g, int b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Color(String hex, float a) {
        hex = hex.replace("#", "");
        r = Integer.parseInt(hex.substring(0, 2));
        g = Integer.parseInt(hex.substring(2, 4));
        b = Integer.parseInt(hex.substring(4, 6));
        this.a = a;
    }

    public int getAsRGBA() {
        return (r & 255) << 24 | (g & 255) << 16 | (b & 255) << 8 | ((int)(a * 255) & 255);
    }

    public int getAsARGB() {
        return (r & 255) << 16 | (g & 255) << 8 | (b & 255) | ((int)(a * 255) & 255) << 24;
    }

    public int getAsBGRA(float alphaMul, float alphaMin, float alphaMax) {
        return (r & 255) << 8 | (g & 255) << 16 | (b & 255) << 24 | ((int)(Math.clamp(a * alphaMul, alphaMin, alphaMax) * 255) & 255);
    }

    public int getAsBGRA() {
        return getAsBGRA(1, 0, 1);
    }

    public void combine(Color color) {
        r = (r + color.r) & 255;
        g = (g + color.g) & 255;
        b = (b + color.b) & 255;
        a = Math.max(a, color.a);
    }
}
