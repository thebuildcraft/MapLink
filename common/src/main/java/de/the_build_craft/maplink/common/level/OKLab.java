/*
 *    This file is part of the Map Link mod
 *    licensed under the GNU GPL v3 License.
 *
 *    Copyright (C) 2026  Leander Knüttel and contributors
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

package de.the_build_craft.maplink.common.level;

import de.the_build_craft.maplink.common.waypoints.Color;

/**
 * @author Leander Knüttel
 * @version 08.03.2026
 */
public class OKLab {
    //from https://bottosson.github.io/posts/oklab/
    public static float[] linearRgbToOkLab(float r, float g, float b) {
        float l = 0.4122214708f * r + 0.5363325363f * g + 0.0514459929f * b;
        float m = 0.2119034982f * r + 0.6806995451f * g + 0.1073969566f * b;
        float s = 0.0883024619f * r + 0.2817188376f * g + 0.6299787005f * b;

        float l_ = (float) Math.cbrt(l);
        float m_ = (float) Math.cbrt(m);
        float s_ = (float) Math.cbrt(s);

        return new float[]{
                0.2104542553f * l_ + 0.7936177850f * m_ - 0.0040720468f * s_,
                1.9779984951f * l_ - 2.4285922050f * m_ + 0.4505937099f * s_,
                0.0259040371f * l_ + 0.7827717662f * m_ - 0.8086757660f * s_,
        };
    }

    public static float[] sRgbToOkLab(Color color) {
        return linearRgbToOkLab(
                gammaToLinear(color.r / 255f),
                gammaToLinear(color.g / 255f),
                gammaToLinear(color.b / 255f));
    }

    //from https://bottosson.github.io/posts/colorwrong/
    private static float gammaToLinear(float value) {
        if (value >= 0.04045) return (float) Math.pow((value + 0.055) / 1.055, 2.4);
        return (float) (value / 12.92);
    }
}
