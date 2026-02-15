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

package de.the_build_craft.maplink.mixins.common.mods.xaerominimap;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import de.the_build_craft.maplink.common.clientMapHandlers.XaeroMiniMapSupport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import xaero.hud.minimap.element.render.MinimapElementRenderLocation;
import xaero.hud.minimap.waypoint.WaypointCollector;
import xaero.hud.minimap.waypoint.render.AbstractWaypointRenderContext;
import xaero.hud.minimap.waypoint.render.AbstractWaypointRenderProvider;
import xaero.hud.minimap.waypoint.render.WaypointMapRenderContext;
import xaero.hud.minimap.waypoint.render.world.WaypointWorldRenderContext;

import java.util.List;

import static de.the_build_craft.maplink.common.CommonModConfig.config;

/**
 * @author Leander Knüttel
 * @version 15.02.2026
 */
@Pseudo
@Mixin(AbstractWaypointRenderProvider.class)
public abstract class AbstractWaypointRenderProviderMixin {
    @WrapOperation(method = "begin*", at = @At(value = "INVOKE", target = "Lxaero/hud/minimap/waypoint/WaypointCollector;collect(Ljava/util/List;)V"))
    private void begin(WaypointCollector instance, List<Object> destination, Operation<Void> original, MinimapElementRenderLocation location, AbstractWaypointRenderContext context) {
        if (context instanceof WaypointWorldRenderContext) {
            if (config.hud.showPlayerWaypoints.isActive()) destination.addAll(XaeroMiniMapSupport.idToHudPlayer.values());
            if (config.hud.showMarkerWaypoints.isActive()) destination.addAll(XaeroMiniMapSupport.idToHudMarker.values());
        } else if (context instanceof WaypointMapRenderContext) {
            if (config.minimap.showPlayerWaypoints.isActive()) destination.addAll(XaeroMiniMapSupport.idToMiniMapPlayer.values());
            if (config.minimap.showMarkerWaypoints.isActive()) destination.addAll(XaeroMiniMapSupport.idToMiniMapMarker.values());
        }
        original.call(instance, destination);
    }
}
