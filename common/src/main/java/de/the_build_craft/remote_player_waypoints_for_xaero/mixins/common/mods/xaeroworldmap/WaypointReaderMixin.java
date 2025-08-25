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

package de.the_build_craft.remote_player_waypoints_for_xaero.mixins.common.mods.xaeroworldmap;

import de.the_build_craft.remote_player_waypoints_for_xaero.common.waypoints.CustomWorldMapWaypoint;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.waypoints.WaypointState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.element.render.ElementRenderLocation;
import xaero.map.mods.gui.Waypoint;
import xaero.map.mods.gui.WaypointReader;
import xaero.map.mods.gui.WaypointRenderContext;

import static de.the_build_craft.remote_player_waypoints_for_xaero.common.CommonModConfig.*;

/**
 * @author Leander Knüttel
 * @version 25.08.2025
 */
@Pseudo
@Mixin(WaypointReader.class)
public class WaypointReaderMixin {
    @Inject(method = "getInteractionBoxTop(Lxaero/map/mods/gui/Waypoint;Lxaero/map/mods/gui/WaypointRenderContext;F)I",
            at = @At("RETURN"), cancellable = true)
    private void top(Waypoint element, WaypointRenderContext context, float partialTicks, CallbackInfoReturnable<Integer> cir) {
        if (element instanceof CustomWorldMapWaypoint && ((CustomWorldMapWaypoint) element).getWaypointState().renderIconOnWorldMap) {
            if (!config.worldmap.waypointIconBackground) cir.setReturnValue(-12);
        }
    }

    @Inject(method = "getInteractionBoxBottom(Lxaero/map/mods/gui/Waypoint;Lxaero/map/mods/gui/WaypointRenderContext;F)I",
            at = @At("RETURN"), cancellable = true)
    private void bottom(Waypoint element, WaypointRenderContext context, float partialTicks, CallbackInfoReturnable<Integer> cir) {
        if (element instanceof CustomWorldMapWaypoint && ((CustomWorldMapWaypoint) element).getWaypointState().renderIconOnWorldMap) {
            if (!config.worldmap.waypointIconBackground) cir.setReturnValue(12);
        }
    }

    @Inject(method = "isRightClickValid(Lxaero/map/mods/gui/Waypoint;)Z",
            at = @At("RETURN"), cancellable = true)
    private void disableMenu(Waypoint element, CallbackInfoReturnable<Boolean> cir) {
        if (element instanceof CustomWorldMapWaypoint) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getBoxScale(Lxaero/map/element/render/ElementRenderLocation;Lxaero/map/mods/gui/Waypoint;Lxaero/map/mods/gui/WaypointRenderContext;)F",
            at = @At("RETURN"), cancellable = true)
    private void customScale(ElementRenderLocation location, Waypoint element, WaypointRenderContext context, CallbackInfoReturnable<Float> cir){
        if (element instanceof CustomWorldMapWaypoint) {
            WaypointState waypointState = ((CustomWorldMapWaypoint) element).getWaypointState();
            if (waypointState.renderIconOnWorldMap) {
                cir.setReturnValue(cir.getReturnValueF() *
                        (waypointState.isPlayer ? config.worldmap.playerIconScale : config.worldmap.markerIconScale) / 100f);
            } else {
                cir.setReturnValue(cir.getReturnValueF() *
                        (waypointState.isPlayer ? config.worldmap.playerTextScale : config.worldmap.markerTextScale) / 100f);
            }
        }
    }
}
