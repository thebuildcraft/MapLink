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

package de.the_build_craft.remote_player_waypoints_for_xaero.mixins.neoforge.mods.xaeroworldmap;

import de.the_build_craft.remote_player_waypoints_for_xaero.common.AbstractModInitializer;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.clientMapHandlers.XaeroClientMapHandler;
import de.the_build_craft.remote_player_waypoints_for_xaero.neoforge.RemotePlayerTrackerReader;
import de.the_build_craft.remote_player_waypoints_for_xaero.neoforge.RemotePlayerTrackerSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.map.WorldMap;
import xaero.map.mods.SupportMods;

/**
 * @author Leander Knüttel
 * @version 31.08.2025
 */
@Pseudo
@Mixin(SupportMods.class)
public class SupportModMixin {
    @Inject(method =  "load", at = @At("TAIL"))
    private static void load(CallbackInfo ci) {
        try {
            WorldMap.playerTrackerSystemManager.register(AbstractModInitializer.MOD_ID, new RemotePlayerTrackerSystem(
                    new RemotePlayerTrackerReader(), XaeroClientMapHandler.worldmapPlayerTrackerPositions));
        } catch (Exception ignored) {}
    }
}
