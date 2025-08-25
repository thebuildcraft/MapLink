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

package de.the_build_craft.remote_player_waypoints_for_xaero.common.clientMapHandlers;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.texture.DynamicTexture;
#if MC_VER >= MC_1_19_4
import org.joml.Matrix4f;
#else
import com.mojang.math.Matrix4f;
#endif

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Leander Knüttel
 * @version 25.08.2025
 */
public abstract class XaerosMapCompat {
    public static XaerosMapCompat Instance;

    public XaerosMapCompat() {
        Instance = this;
    }

    protected static final Map<DynamicTexture, List<XaeroIconRenderData>> textureToData = new Object2ObjectOpenHashMap<>();

    public static void batchDrawCustomIcon(Matrix4f matrix, DynamicTexture dynamicTexture, float x, float y, int width, int height, float a) {
        if (dynamicTexture == null) return;
        if (!textureToData.containsKey(dynamicTexture)) textureToData.put(dynamicTexture, new ArrayList<>());
        try {
            #if MC_VER >= MC_1_19_4
            textureToData.get(dynamicTexture).add(new XaeroIconRenderData((Matrix4f) matrix.clone(), x, y, width, height, a));
            #else
            textureToData.get(dynamicTexture).add(new XaeroIconRenderData(new Matrix4f(matrix), x, y, width, height, a));
            #endif
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public abstract void drawAllCustomIcons();
}
