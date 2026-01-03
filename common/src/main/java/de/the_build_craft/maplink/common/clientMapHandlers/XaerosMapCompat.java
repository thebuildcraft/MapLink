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

package de.the_build_craft.maplink.common.clientMapHandlers;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.texture.DynamicTexture;
#if MC_VER != MC_1_19_4 && MC_VER != MC_1_20_2 && MC_VER != MC_1_20_6
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.map.common.config.option.WorldMapProfiledConfigOptions;
#endif
import xaero.map.WorldMap;
import xaero.common.HudMod;

#if MC_VER >= MC_1_21_11
import xaero.map.graphics.CustomRenderTypes;
import xaero.map.graphics.renderer.multitexture.MultiTextureRenderTypeRenderer;
import xaero.map.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;
#endif

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
 * @version 03.01.2026
 */
public abstract class XaerosMapCompat {
    public static XaerosMapCompat Instance;

    public static int xaeroAutoConvertToKmThreshold;
    public static boolean xaeroWaypointBackground;

    #if MC_VER >= MC_1_21_11
    public final MultiTextureRenderTypeRendererProvider multiTextureRenderTypeRendererProvider;
    public MultiTextureRenderTypeRenderer GUI_NEAREST_Renderer;
    #endif

    public XaerosMapCompat() {
        Instance = this;
        #if MC_VER >= MC_1_21_11
        multiTextureRenderTypeRendererProvider = new MultiTextureRenderTypeRendererProvider(2);
        #endif
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

    public static void cacheXaeroSettings() {
        #if MC_VER != MC_1_19_4 && MC_VER != MC_1_20_2 && MC_VER != MC_1_20_6
        xaeroAutoConvertToKmThreshold = HudMod.INSTANCE.getHudConfigs().getClientConfigManager().getCurrentProfile().get(MinimapProfiledConfigOptions.WAYPOINT_CONVERT_DISTANCE_TO_KM_AT);
        xaeroWaypointBackground = WorldMap.INSTANCE.getConfigs().getClientConfigManager().getCurrentProfile().get(WorldMapProfiledConfigOptions.WAYPOINT_BACKGROUNDS);
        #else
        xaeroAutoConvertToKmThreshold = HudMod.INSTANCE.getSettings().autoConvertWaypointDistanceToKmThreshold;
        xaeroWaypointBackground = WorldMap.settings.waypointBackgrounds;
        #endif
    }

    #if MC_VER >= MC_1_21_11
    public void createGuiNearestRenderer() {
        GUI_NEAREST_Renderer = multiTextureRenderTypeRendererProvider.getRenderer(MultiTextureRenderTypeRendererProvider::defaultTextureBind, CustomRenderTypes.GUI_NEAREST);
    }

    public void drawGuiNearestRenderer() {
        XaerosMapCompat.Instance.multiTextureRenderTypeRendererProvider.draw(GUI_NEAREST_Renderer);
    }
    #endif
}
