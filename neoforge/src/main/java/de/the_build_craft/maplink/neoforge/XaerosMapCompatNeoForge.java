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

package de.the_build_craft.maplink.neoforge;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import de.the_build_craft.maplink.common.clientMapHandlers.XaeroIconRenderData;
import de.the_build_craft.maplink.common.clientMapHandlers.XaerosMapCompat;
import net.minecraft.client.renderer.texture.DynamicTexture;
#if MC_VER >= MC_1_19_4
import org.joml.Matrix4f;
#else
import com.mojang.math.Matrix4f;
#endif
import xaero.common.graphics.CustomRenderTypes;
import xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRenderer;
import xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;
import xaero.hud.minimap.BuiltInHudModules;

import java.util.List;
import java.util.Map;

/**
 * @author Leander Knüttel
 * @version 25.08.2025
 */
public class XaerosMapCompatNeoForge extends XaerosMapCompat {
    //partially from Earthcomputer/minimap-sync licensed under the MIT License
    //(but heavily modified)
    @Override
    public void drawAllCustomIcons() {
        if (textureToData.isEmpty()) return;
        MultiTextureRenderTypeRendererProvider multiTextureRenderTypeRenderers = BuiltInHudModules.MINIMAP.getCurrentSession().getMultiTextureRenderTypeRenderers();
        MultiTextureRenderTypeRenderer renderer = multiTextureRenderTypeRenderers.getRenderer(
                #if MC_VER >= MC_1_21_6
                MultiTextureRenderTypeRendererProvider::defaultTextureBind,
                CustomRenderTypes.GUI);
                #elif MC_VER >= MC_1_21_5
                MultiTextureRenderTypeRendererProvider::defaultTextureBind,
                CustomRenderTypes.GUI_NEAREST);
                #elif MC_VER >= MC_1_17_1
                textureId -> RenderSystem.setShaderTexture(0, textureId),
                MultiTextureRenderTypeRendererProvider::defaultTextureBind,
                CustomRenderTypes.GUI_NEAREST);
                #else
                RenderSystem::bindTexture,
                CustomRenderTypes.GUI_NEAREST);
                #endif
        for (Map.Entry<DynamicTexture, List<XaeroIconRenderData>> iconData : textureToData.entrySet()) {
            #if MC_VER >= MC_1_21_5
            BufferBuilder buffer = renderer.begin(iconData.getKey().getTexture());
            #else
            BufferBuilder buffer = renderer.begin(iconData.getKey().getId());
            #endif

            for (XaeroIconRenderData xaeroIconRenderData : iconData.getValue()) {
                Matrix4f pose = xaeroIconRenderData.pose;
                float x = xaeroIconRenderData.x;
                float y = xaeroIconRenderData.y;
                float width = xaeroIconRenderData.width;
                float height = xaeroIconRenderData.height;
                int a = (int) (xaeroIconRenderData.a * 255);

                #if MC_VER >= MC_1_21_1
                buffer.addVertex(pose, x, y, 0).setWhiteAlpha(a).setUv(0, 0);
                buffer.addVertex(pose, x, y + height, 0).setWhiteAlpha(a).setUv(0, 1);
                buffer.addVertex(pose, x + width, y + height, 0).setWhiteAlpha(a).setUv(1, 1);
                buffer.addVertex(pose, x + width, y, 0).setWhiteAlpha(a).setUv(1, 0);
                #elif MC_VER >= MC_1_17_1
                buffer.vertex(pose, x, y, 0).color(255, 255, 255, a).uv(0, 0).endVertex();
                buffer.vertex(pose, x, y + height, 0).color(255, 255, 255, a).uv(0, 1).endVertex();
                buffer.vertex(pose, x + width, y + height, 0).color(255, 255, 255, a).uv(1, 1).endVertex();
                buffer.vertex(pose, x + width, y, 0).color(255, 255, 255, a).uv(1, 0).endVertex();
                #else
                buffer.vertex(pose, x, y, 0).uv(0, 0).color(255, 255, 255, a).endVertex();
                buffer.vertex(pose, x, y + height, 0).uv(0, 1).color(255, 255, 255, a).endVertex();
                buffer.vertex(pose, x + width, y + height, 0).uv(1, 1).color(255, 255, 255, a).endVertex();
                buffer.vertex(pose, x + width, y, 0).uv(1, 0).color(255, 255, 255, a).endVertex();
                #endif
            }
        }
        multiTextureRenderTypeRenderers.draw(renderer);
        textureToData.clear();
    }
}
