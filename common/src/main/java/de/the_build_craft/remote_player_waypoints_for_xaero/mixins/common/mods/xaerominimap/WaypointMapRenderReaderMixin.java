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

package de.the_build_craft.remote_player_waypoints_for_xaero.mixins.common.mods.xaerominimap;

import de.the_build_craft.remote_player_waypoints_for_xaero.common.waypoints.TempWaypoint;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.waypoints.WaypointState;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.waypoint.render.WaypointMapRenderContext;
import xaero.hud.minimap.waypoint.render.WaypointMapRenderReader;

/**
 * @author Leander Knüttel
 * @version 25.08.2025
 */
@Pseudo
@Mixin(value = WaypointMapRenderReader.class, remap = false)
public class WaypointMapRenderReaderMixin {
    //partially from Earthcomputer/minimap-sync licensed under the MIT License
    @Inject(method = "getRenderBoxRight(Lxaero/common/minimap/waypoints/Waypoint;Lxaero/hud/minimap/waypoint/render/WaypointMapRenderContext;F)I", at = @At("HEAD"), cancellable = true)
    private void modifyRenderBoxForCustomIcon(Waypoint element, WaypointMapRenderContext context, float partialTicks, CallbackInfoReturnable<Integer> cir) {
        if (element instanceof TempWaypoint) {
            WaypointState waypointState = ((TempWaypoint) element).getWaypointState();
            if (waypointState.renderIconOnMiniMap) {
                cir.setReturnValue(5);
            } else {
                int width = Minecraft.getInstance().font.width(waypointState.abbreviation) / 2;
                cir.setReturnValue(5 + (width > 4 ? width - 4 : 0));
            }
        }
    }
}