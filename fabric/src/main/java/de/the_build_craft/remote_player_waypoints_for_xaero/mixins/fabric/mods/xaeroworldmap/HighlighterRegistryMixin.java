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

package de.the_build_craft.remote_player_waypoints_for_xaero.mixins.fabric.mods.xaeroworldmap;

import de.the_build_craft.remote_player_waypoints_for_xaero.fabric.AreaMarkerHighlighter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.highlight.AbstractHighlighter;
import xaero.map.highlight.HighlighterRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Leander Knüttel
 * @version 25.08.2025
 */
@Pseudo
@Mixin(HighlighterRegistry.class)
public class HighlighterRegistryMixin {
    @Unique
    private static final AreaMarkerHighlighter remote_player_waypoints_for_xaero$areaMarkerHighlighter = new AreaMarkerHighlighter();

    @Shadow
    private List<AbstractHighlighter> highlighters;

    @Inject(method = "getHighlighters", at = @At("RETURN"), cancellable = true, remap = false)
    private void injected(CallbackInfoReturnable<List<AbstractHighlighter>> cir) {
        if (highlighters.contains(remote_player_waypoints_for_xaero$areaMarkerHighlighter)) return;
        List<AbstractHighlighter> newList = new ArrayList<>(highlighters);
        newList.add(remote_player_waypoints_for_xaero$areaMarkerHighlighter);
        highlighters = newList;
        cir.setReturnValue(newList);
    }
}
