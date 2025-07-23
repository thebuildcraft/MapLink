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
import de.the_build_craft.remote_player_waypoints_for_xaero.common.wrappers.Text;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import xaero.map.WorldMapSession;
import xaero.map.highlight.ChunkHighlighter;
import xaero.map.world.MapDimension;

import java.util.List;

/**
 * @author Leander Knüttel
 * @version 23.07.2025
 */
public class AreaMarkerHighlighter extends ChunkHighlighter implements MapHighlightClearer {
    public AreaMarkerHighlighter() {
        super(true);
        XaeroClientMapHandler.mapHighlightClearer = this;
    }

    @Override
    public void clearHashCache() {
        MapDimension mapDim = WorldMapSession.getCurrentSession().getMapProcessor().getMapWorld()
                .getDimension(ResourceKey.create(Registries.DIMENSION, Minecraft.getInstance().level.dimension().location()));
        if (mapDim != null) mapDim.getHighlightHandler().clearCachedHashes();
    }

    @Override
    protected int[] getColors(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        long key = ((long) chunkX << 32) | chunkZ;
        if (!XaeroClientMapHandler.chunkHighlightMap.containsKey(key)) return null;
        ChunkHighlight chunkHighlight = XaeroClientMapHandler.chunkHighlightMap.get(key);
        return new int[]{//TODO check sides
                chunkHighlight.fillColor.getAsBGRA(.5f, 0f, .7f),
                chunkHighlight.lineColor.getAsBGRA(.5f, 0f, .7f),
                chunkHighlight.lineColor.getAsBGRA(.5f, 0f, .7f),
                chunkHighlight.lineColor.getAsBGRA(.5f, 0f, .7f),
                chunkHighlight.lineColor.getAsBGRA(.5f, 0f, .7f)};
    }

    @Override
    public Component getChunkHighlightSubtleTooltip(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        return Text.literal(XaeroClientMapHandler.chunkHighlightMap.get(((long) chunkX << 32) | chunkZ  & 0xFFFFFFFFL).setName);
    }

    @Override
    public Component getChunkHighlightBluntTooltip(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        return Text.literal(XaeroClientMapHandler.chunkHighlightMap.get(((long) chunkX << 32) | chunkZ  & 0xFFFFFFFFL).name);
    }

    @Override
    public int calculateRegionHash(ResourceKey<Level> dimension, int regionX, int regionZ) {
        return 1 + XaeroClientMapHandler.chunkHighlightHash;
    }

    @Override
    public boolean regionHasHighlights(ResourceKey<Level> dimension, int regionX, int regionZ) {
        return XaeroClientMapHandler.regionsWithChunkHighlights.contains(((long) regionX << 32) | regionZ & 0xFFFFFFFFL);
    }

    @Override
    public boolean chunkIsHighlit(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        return XaeroClientMapHandler.chunkHighlightMap.containsKey(((long) chunkX << 32) | chunkZ & 0xFFFFFFFFL);
    }

    @Override
    public void addMinimapBlockHighlightTooltips(List<Component> list, ResourceKey<Level> dimension, int blockX, int blockZ, int width) {
    }
}
