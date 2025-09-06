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

package de.the_build_craft.remote_player_waypoints_for_xaero.common;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

import java.util.Locale;
import java.util.Objects;

/**
 * @author Leander Knüttel
 * @version 06.09.2025
 */
public abstract class CommonModConfig {
    public static ModConfig config;

    public CommonModConfig() {
        config = getConfig();
    }

    protected abstract ModConfig getConfig();

    public static void saveConfig() {
        AutoConfig.getConfigHolder(ModConfig.class).save();
    }

    public static void setIgnoreMarkerMessage(boolean on) {
        config.general.ignoreMarkerMessage = on;
        saveConfig();
    }

    public static int getPlayerWaypointColor(String playerName) {
        if (config.friends.overwriteFriendWaypointColor && config.friends.friendList.contains(playerName)){
            return config.friends.friendWaypointColor.ordinal();
        }
        else {
            return config.general.playerWaypointColor.ordinal();
        }
    }

    public static ModConfig.ServerEntry getCurrentServerEntry() {
        ServerData serverData = Minecraft.getInstance().getCurrentServer();
        if (serverData == null) return null;
        String serverIP = serverData.ip.toLowerCase(Locale.ROOT);
        for (ModConfig.ServerEntry server : config.general.serverEntries){
            if (Objects.equals(serverIP, server.ip.toLowerCase(Locale.ROOT))){
                return server;
            }
        }
        return null;
    }

    public static int getWaypointLayerOrder() {
        return config.general.minimapWaypointsRenderBelow.isActive() ? -1 : 100;
    }
}
