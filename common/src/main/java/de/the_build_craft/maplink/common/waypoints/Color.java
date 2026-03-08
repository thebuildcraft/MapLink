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

import net.minecraft.util.Mth;

import java.util.List;
import java.util.Objects;

/**
 * @author Leander Knüttel
 * @version 08.03.2026
 */
public class Color {
    public final int r;
    public final int g;
    public final int b;
    public final float a;

    public Color() {
        this(0, 0, 0, 0);
    }

    public Color(int r, int g, int b, float a) {
        this.r = MathUtils.clamp(r, 0, 255);
        this.g = MathUtils.clamp(g, 0, 255);
        this.b = MathUtils.clamp(b, 0, 255);
        this.a = MathUtils.clamp(a, 0, 1);
    }

    public Color(String hex, float a) {
        if (hex == null) hex = "000000";
        hex = hex.replace("#", "");
        r = Integer.parseInt(hex.substring(0, 2), 16);
        g = Integer.parseInt(hex.substring(2, 4), 16);
        b = Integer.parseInt(hex.substring(4, 6), 16);
        this.a = a;
    }

    public Color(int argb) {
        a = argbToA(argb) / 255f;
        r = argbToR(argb);
        g = argbToG(argb);
        b = argbToB(argb);
    }

    public static int argbToA(int argb) {
        return (argb >>> 24) & 255;
    }

    public static int argbToR(int argb) {
        return (argb >>> 16) & 255;
    }

    public static int argbToG(int argb) {
        return (argb >>> 8) & 255;
    }

    public static int argbToB(int argb) {
        return argb & 255;
    }

    public static int ABGRtoARGB(int abgr) {
        int a = argbToA(abgr);
        int b = argbToR(abgr);
        int g = argbToG(abgr);
        int r = argbToB(abgr);
        return a << 24 | r << 16 | g << 8 | b;
    }

    public int getAsRGBA() {
        return (r & 255) << 24 | (g & 255) << 16 | (b & 255) << 8 | ((int)(a * 255) & 255);
    }

    public int getAsARGB() {
        return (r & 255) << 16 | (g & 255) << 8 | (b & 255) | ((int)(a * 255) & 255) << 24;
    }

    public int getAsBGRA() {
        return (r & 255) << 8 | (g & 255) << 16 | (b & 255) << 24 | ((int)(a * 255) & 255);
    }

    public static Color combineColors(List<Color> colors, float alphaMul, float alphaMin, float alphaMax) {
        float alphaSum = 0;
        float currAlphaMax = 0;
        for (Color c : colors) {
            alphaSum += c.a;
            currAlphaMax = Math.max(currAlphaMax, c.a);
        }
        float r = 0;
        float g = 0;
        float b = 0;
        for (Color c : colors) {
            float factor = c.a / alphaSum;
            r += c.r * factor;
            g += c.g * factor;
            b += c.b * factor;
        }
        float a = Mth.clamp(currAlphaMax * alphaMul, alphaMin, alphaMax);
        return new Color(Math.round(r), Math.round(g), Math.round(b), a);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Color color = (Color) o;
        return r == color.r && g == color.g && b == color.b && Float.compare(a, color.a) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(r, g, b, a);
    }

    @Override
    public Color clone() {
        return new Color(r, g, b, a);
    }

    @Override
    public String toString() {
        return "Color{" +
                "r=" + r +
                ", g=" + g +
                ", b=" + b +
                ", a=" + a +
                '}';
    }
}
