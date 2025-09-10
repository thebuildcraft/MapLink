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

package de.the_build_craft.maplink.mixins.common.mods.xaeroworldmap;

import de.the_build_craft.maplink.common.clientMapHandlers.XaeroClientMapHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.element.render.ElementRenderLocation;
import xaero.map.mods.gui.WaypointRenderContext;
import xaero.map.mods.gui.WaypointRenderProvider;
import xaero.map.mods.gui.Waypoint;

import java.util.Collections;
import java.util.Iterator;

import static de.the_build_craft.maplink.common.CommonModConfig.config;

/**
 * @author Leander Knüttel
 * @version 07.09.2025
 */
@Pseudo
@Mixin(WaypointRenderProvider.class)
public class WaypointRenderProviderMixin {
    @Unique
    private Iterator<Waypoint> maplink$playerIterator;

    @Unique
    private Iterator<Waypoint> maplink$markerIterator;

    @Unique
    private boolean maplink$originalHadNext;

    @Inject(method = "begin*", at = @At("HEAD"))
    private void begin(ElementRenderLocation location, WaypointRenderContext context, CallbackInfo ci) {
        maplink$playerIterator = config.worldmap.showPlayerWaypoints.isActive() ?
                XaeroClientMapHandler.idToWorldMapPlayer.values().iterator() : Collections.emptyIterator();
        maplink$markerIterator = config.worldmap.showMarkerWaypoints.isActive() ?
                XaeroClientMapHandler.idToWorldMapMarker.values().iterator() : Collections.emptyIterator();
    }

    @Inject(method = "hasNext*", at = @At("RETURN"), cancellable = true)
    private void hasNext(ElementRenderLocation location, WaypointRenderContext context, CallbackInfoReturnable<Boolean> cir) {
        maplink$originalHadNext = cir.getReturnValue();
        cir.setReturnValue(maplink$originalHadNext || maplink$playerIterator.hasNext() || maplink$markerIterator.hasNext());
    }

    @Inject(method = "getNext*", at = @At("HEAD"), cancellable = true)
    private void getNext(ElementRenderLocation location, WaypointRenderContext context, CallbackInfoReturnable<Waypoint> cir) {
        if (maplink$originalHadNext) {
            maplink$originalHadNext = false;
            return;
        }
        if (maplink$playerIterator.hasNext()) {
            cir.setReturnValue(maplink$playerIterator.next());
            return;
        }
        if (maplink$markerIterator.hasNext()) {
            cir.setReturnValue(maplink$markerIterator.next());
        }
    }

    @Inject(method = "end*", at = @At("HEAD"))
    private void end(ElementRenderLocation location, WaypointRenderContext context, CallbackInfo ci) {
        maplink$originalHadNext = false;
        maplink$playerIterator = null;
        maplink$markerIterator = null;
    }
}
