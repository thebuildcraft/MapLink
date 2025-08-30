/*
 *    This file is part of the Remote player waypoints for Xaero's Map mod
 *    licensed under the GNU GPL v3 License.
 *
 *    Copyright (C) 2024 - 2025  Leander Knüttel and contributors
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

package de.the_build_craft.remote_player_waypoints_for_xaero.mixins.common.client;

import de.the_build_craft.remote_player_waypoints_for_xaero.common.AbstractModInitializer;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.Style;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.wrappers.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static de.the_build_craft.remote_player_waypoints_for_xaero.common.CommonModConfig.*;

/**
 * @author Leander Knüttel
 * @author MeerBiene
 * @version 30.08.2025
 */
@Mixin(PlayerTabOverlay.class)
public class PlayerListHudMixin {

    @Unique
    private static String remote_player_waypoints_for_xaero$getFormatedDuration(String playerName) {
        long durationInMs = System.currentTimeMillis() - AbstractModInitializer.lastPlayerActivityTimeMap.get(playerName);
        int durationInMin = (int)(durationInMs / 60_000);
        int hours = (int) Math.floor(durationInMin / 60.0);
        int minutes = durationInMin % 60;

        if (hours == 0) {
            return minutes + " min";
        }
        if (config.general.hideAfkMinutes) {
            return hours + " h";
        }
        return hours + " h  " + minutes + " min";
    }

    @Inject(method = "getNameForDisplay", at = @At("RETURN"), cancellable = true)
    private void injected(PlayerInfo entry, CallbackInfoReturnable<Component> cir) {
        if (!(config.general.enabled
                && AbstractModInitializer.connected
                && config.general.showAfkInTabList)) {
            return;
        }

        String playerName = entry.getProfile().getName();
        MutableComponent newText = cir.getReturnValue().copy();

        if (AbstractModInitializer.AfkMap.containsKey(playerName)) {
            if (AbstractModInitializer.AfkMap.get(playerName)) {
                if (config.general.showAfkTimeInTabList) {
                    cir.setReturnValue(newText.append(Text.literal("  [AFK: "
                                    + (AbstractModInitializer.playerOverAfkTimeMap.get(playerName) ? "> " : "")
                                    + remote_player_waypoints_for_xaero$getFormatedDuration(playerName) + "]")
                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(config.general.AfkColor)))));
                }
                else {
                    cir.setReturnValue(newText.append(Text.literal("  [AFK]")
                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(config.general.AfkColor)))));
                }
            }
        } else {
            cir.setReturnValue(newText.append(Text.literal("  [???]")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(config.general.unknownAfkStateColor)))));
        }
    }
}
