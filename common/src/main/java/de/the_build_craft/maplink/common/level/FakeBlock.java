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

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author Leander Knüttel
 * @version 08.03.2026
 */
public class FakeBlock {
    public final BlockState bottom;
    public final BlockState[] top;
    public final ResourceKey<Biome> biome;
    public final int additionalHeight;

    public FakeBlock(BlockState bottom, BlockState[] top, ResourceKey<Biome> biome) {
        this.bottom = bottom;
        this.top = top;
        this.biome = biome;

        int tempHeight = 0;
        for (int i = 0; i < top.length; i++) {
            if (top[i] != null) {
                tempHeight = top.length - i;
                break;
            }
        }
        additionalHeight = tempHeight;
    }
}
