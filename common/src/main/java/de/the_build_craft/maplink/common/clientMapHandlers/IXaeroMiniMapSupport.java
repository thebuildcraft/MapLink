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

package de.the_build_craft.maplink.common.clientMapHandlers;

import de.the_build_craft.maplink.common.waypoints.Position;
import de.the_build_craft.maplink.common.waypoints.WaypointState;
import net.minecraft.client.renderer.texture.DynamicTexture;
#if MC_VER >= MC_1_19_4
import org.joml.Matrix4f;
#else
import com.mojang.math.Matrix4f;
#endif

/**
 * @author Leander Knüttel
 * @version 15.02.2026
 */
public interface IXaeroMiniMapSupport extends IXaeroMapSupport {
    void addOrUpdateMiniMapWaypoint(Position position, WaypointState waypointState, boolean miniMap);
    void batchDrawCustomIcon(Matrix4f matrix, DynamicTexture dynamicTexture, float x, float y, int width, int height, float a);
    void drawAllCustomIcons();
}
