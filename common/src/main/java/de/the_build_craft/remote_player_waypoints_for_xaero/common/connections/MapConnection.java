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

package de.the_build_craft.remote_player_waypoints_for_xaero.common.connections;

import de.the_build_craft.remote_player_waypoints_for_xaero.common.ModConfig;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.waypoints.PlayerPosition;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.AbstractModInitializer;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.wrappers.Text;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.wrappers.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

import static de.the_build_craft.remote_player_waypoints_for_xaero.common.CommonModConfig.*;

/**
 * @author Leander Knüttel
 * @author eatmyvenom
 * @version 25.08.2025
 */
public abstract class MapConnection {
    public URL queryURL;
    public final Minecraft mc;
    public String currentDimension = "";
    public String onlineMapConfigLink;
    long lastUpdateTimeMs;
    public boolean foundPlayer;
    public boolean partOfLiveAtlas;

    public MapConnection() {
        this.mc = Minecraft.getInstance();
        lastUpdateTimeMs = System.currentTimeMillis();
    }

    public void setCurrentDimension(String currentDimension) {
        this.currentDimension = currentDimension;
    }

    @NotNull
    protected String getBaseURL(ModConfig.ServerEntry serverEntry, boolean useHttps) {
        String baseURL = serverEntry.link;
        if (!baseURL.startsWith(useHttps ? "https://" : "http://")){
            baseURL = (useHttps ? "https://" : "http://") + baseURL;
        }

        int i = baseURL.indexOf("?");
        if (i != -1){
            baseURL = baseURL.substring(0, i - 1);
        }

        i = baseURL.indexOf("#");
        if (i != -1){
            baseURL = baseURL.substring(0, i - 1);
        }

        if (baseURL.endsWith("index.html")){
            baseURL = baseURL.substring(0, baseURL.length() - 10);
        }

        if (baseURL.endsWith("/")){
            baseURL = baseURL.substring(0, baseURL.length() - 1);
        }
        return baseURL.replace(" ", "%20");
    }

    public abstract HashMap<String, PlayerPosition> getPlayerPositions() throws IOException;

    public HashMap<String, PlayerPosition> HandlePlayerPositions(PlayerPosition[] playerPositions){
        HashMap<String, PlayerPosition> newPlayerPositions = new HashMap<>();
        if (mc.player == null) {
            return newPlayerPositions;
        }
        String clientName = mc.player.getName().getString();
        foundPlayer = false;
        if (!AbstractModInitializer.overwriteCurrentDimension) {
            currentDimension = "";
            for (PlayerPosition p : playerPositions){
                if (Objects.equals(p.name, clientName)) {
                    currentDimension = p.world;
                    foundPlayer = true;
                    break;
                }
            }
        }

        if (config.general.debugMode && config.general.chatLogInDebugMode) {
            Utils.sendToClientChat("---");
        }
        for (PlayerPosition p : playerPositions) {
            UpdateAfkInfo(p);

            if (config.general.debugMode || (Objects.equals(p.world, currentDimension) && !Objects.equals(p.name, clientName))) {
                newPlayerPositions.put(p.name, p);
            }
        }
        lastUpdateTimeMs = System.currentTimeMillis();

        return newPlayerPositions;
    }

    public void UpdateAfkInfo(PlayerPosition playerPosition){
        if (AbstractModInitializer.lastPlayerDataDic.containsKey(playerPosition.name)) {
            if (AbstractModInitializer.lastPlayerDataDic.get(playerPosition.name).CompareCords(playerPosition)) {
                if (AbstractModInitializer.AfkTimeDic.containsKey(playerPosition.name)) {
                    AbstractModInitializer.AfkTimeDic.put(playerPosition.name, AbstractModInitializer.AfkTimeDic.get(playerPosition.name) + (System.currentTimeMillis() - lastUpdateTimeMs));
                } else {
                    AbstractModInitializer.AfkTimeDic.put(playerPosition.name, (System.currentTimeMillis() - lastUpdateTimeMs));
                }
                if (config.general.debugMode && config.general.chatLogInDebugMode) {
                    Utils.sendToClientChat(playerPosition.name + "  afk_time: " + AbstractModInitializer.AfkTimeDic.get(playerPosition.name) / 1000);
                }
                if (AbstractModInitializer.AfkTimeDic.get(playerPosition.name) / 1000 >= config.general.timeUntilAfk) {
                    AbstractModInitializer.AfkDic.put(playerPosition.name, true);
                }
            } else {
                AbstractModInitializer.AfkTimeDic.put(playerPosition.name, 0L);
                AbstractModInitializer.AfkDic.put(playerPosition.name, false);
            }
        }
        AbstractModInitializer.lastPlayerDataDic.put(playerPosition.name, playerPosition);
    }

    public abstract void getWaypointPositions() throws IOException;

    public void OpenOnlineMapConfig(){
        #if MC_VER < MC_1_21_5
        Utils.sendToClientChat(Text.literal(onlineMapConfigLink).withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, onlineMapConfigLink))));
        #else
        Utils.sendToClientChat(Text.literal(onlineMapConfigLink).withStyle(Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(URI.create(onlineMapConfigLink)))));
        #endif
    }

    public abstract HashSet<String> getMarkerLayers();
}
