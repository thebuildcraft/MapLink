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

package de.the_build_craft.remote_player_waypoints_for_xaero.fabric;

import de.the_build_craft.remote_player_waypoints_for_xaero.common.clientMapHandlers.MapHighlightClearer;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.clientMapHandlers.XaeroClientMapHandler;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.waypoints.ChunkHighlight;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.waypoints.MathUtils;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.wrappers.Text;
import net.minecraft.client.Minecraft;
#if MC_VER >= MC_1_19_4
import net.minecraft.core.registries.Registries;
#else
import net.minecraft.core.Registry;
#endif
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import xaero.map.WorldMapSession;
import xaero.map.highlight.ChunkHighlighter;
import xaero.map.world.MapDimension;

import java.util.List;

import static de.the_build_craft.remote_player_waypoints_for_xaero.common.CommonModConfig.*;

/**
 * @author Leander Knüttel
 * @version 25.08.2025
 */
public class AreaMarkerHighlighter extends ChunkHighlighter implements MapHighlightClearer {
    public AreaMarkerHighlighter() {
        super(true);
        XaeroClientMapHandler.mapHighlightClearer = this;
    }

    @Override
    public void clearHashCache() {
        if (Minecraft.getInstance().level == null) return;
        MapDimension mapDim = WorldMapSession.getCurrentSession().getMapProcessor().getMapWorld()
                #if MC_VER >= MC_1_19_4
                .getDimension(ResourceKey.create(Registries.DIMENSION, Minecraft.getInstance().level.dimension().location()));
                #else
                .getDimension(ResourceKey.create(Registry.DIMENSION_REGISTRY, Minecraft.getInstance().level.dimension().location()));
                #endif
        if (mapDim != null) mapDim.getHighlightHandler().clearCachedHashes();
    }

    @Override
    protected int[] getColors(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        long key = MathUtils.combineIntsToLong(chunkX, chunkZ);
        if (!XaeroClientMapHandler.chunkHighlightMap.containsKey(key)) return null;
        ChunkHighlight chunkHighlight = XaeroClientMapHandler.chunkHighlightMap.get(key);
        ChunkHighlight top = XaeroClientMapHandler.chunkHighlightMap
                .getOrDefault(MathUtils.combineIntsToLong(chunkX, chunkZ - 1), ChunkHighlight.NullHighlight);
        ChunkHighlight bottom = XaeroClientMapHandler.chunkHighlightMap
                .getOrDefault(MathUtils.combineIntsToLong(chunkX, chunkZ + 1), ChunkHighlight.NullHighlight);
        ChunkHighlight left = XaeroClientMapHandler.chunkHighlightMap
                .getOrDefault(MathUtils.combineIntsToLong(chunkX - 1, chunkZ), ChunkHighlight.NullHighlight);
        ChunkHighlight right = XaeroClientMapHandler.chunkHighlightMap
                .getOrDefault(MathUtils.combineIntsToLong(chunkX + 1, chunkZ), ChunkHighlight.NullHighlight);
        int fillColor = chunkHighlight.fillColor.changeAlphaToNew(config.general.areaFillAlphaMul / 100f,
                config.general.areaFillAlphaMin / 100f, config.general.areaFillAlphaMax / 100f).getAsBGRA();
        int lineColor = chunkHighlight.lineColor.changeAlphaToNew(config.general.areaLineAlphaMul / 100f,
                config.general.areaLineAlphaMin / 100f, config.general.areaLineAlphaMax / 100f).getAsBGRA();
        return new int[]{
                fillColor,
                top.name.equals(chunkHighlight.name) ? fillColor : lineColor,
                right.name.equals(chunkHighlight.name) ? fillColor : lineColor,
                bottom.name.equals(chunkHighlight.name) ? fillColor : lineColor,
                left.name.equals(chunkHighlight.name) ? fillColor : lineColor
        };
    }

    @Override
    public Component getChunkHighlightSubtleTooltip(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        return Text.literal(XaeroClientMapHandler.chunkHighlightMap.get(MathUtils.combineIntsToLong(chunkX, chunkZ)).setName);
    }

    @Override
    public Component getChunkHighlightBluntTooltip(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        return Text.literal(XaeroClientMapHandler.chunkHighlightMap.get(MathUtils.combineIntsToLong(chunkX, chunkZ)).name);
    }

    @Override
    public int calculateRegionHash(ResourceKey<Level> dimension, int regionX, int regionZ) {
        return 1 + XaeroClientMapHandler.chunkHighlightHash;
    }

    @Override
    public boolean regionHasHighlights(ResourceKey<Level> dimension, int regionX, int regionZ) {
        return XaeroClientMapHandler.regionsWithChunkHighlights.contains(MathUtils.combineIntsToLong(regionX, regionZ));
    }

    @Override
    public boolean chunkIsHighlit(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        return XaeroClientMapHandler.chunkHighlightMap.containsKey(MathUtils.combineIntsToLong(chunkX, chunkZ));
    }

    @Override
    public void addMinimapBlockHighlightTooltips(List<Component> list, ResourceKey<Level> dimension, int blockX, int blockZ, int width) {
    }
}
