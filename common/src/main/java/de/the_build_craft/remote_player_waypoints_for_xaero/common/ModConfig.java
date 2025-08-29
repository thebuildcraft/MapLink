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

import de.the_build_craft.remote_player_waypoints_for_xaero.common.wrappers.Text;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Leander Knüttel
 * @version 29.08.2025
 */
@Config(name = "remote_player_waypoints_for_xaero")
#if MC_VER < MC_1_20_6
@Config.Gui.Background("minecraft:textures/block/acacia_planks.png")
@Config.Gui.CategoryBackground(
        category = "friends",
        background = "minecraft:textures/block/oak_planks.png"
)
@Config.Gui.CategoryBackground(
        category = "hud",
        background = "minecraft:textures/block/birch_planks.png"
)
@Config.Gui.CategoryBackground(
        category = "miniMap",
        background = "minecraft:textures/block/dark_oak_planks.png"
)
@Config.Gui.CategoryBackground(
        category = "worldMap",
        background = "minecraft:textures/block/spruce_planks.png"
)
#endif
public class ModConfig extends PartitioningSerializer.GlobalData {
    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.TransitiveObject
    public General general = new General();

    @ConfigEntry.Category("friends")
    @ConfigEntry.Gui.TransitiveObject
    public Friends friends = new Friends();

    @ConfigEntry.Category("hud")
    @ConfigEntry.Gui.TransitiveObject
    public HudModule hud = new HudModule();

    @ConfigEntry.Category("minimap")
    @ConfigEntry.Gui.TransitiveObject
    public MiniMapModule minimap = new MiniMapModule();

    @ConfigEntry.Category("worldmap")
    @ConfigEntry.Gui.TransitiveObject
    public WorldMapModule worldmap = new WorldMapModule();

    public ModConfig() {
    }

    @Config(name = "general")
    public static class General implements ConfigData {
        public boolean enabled = true;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 2000, max = 10000)
        public int updateDelay = 2000;

        @ConfigEntry.Gui.Tooltip
        public List<ServerEntry> serverEntries = new ArrayList<>();

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = -100, max = 400)
        public int defaultY = 64;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ConditionalActiveMode minimapWaypointsRenderBelow = ConditionalActiveMode.NEVER;

        //Player options
        @ConfigEntry.Gui.PrefixText
        public boolean enablePlayerWaypoints = true;

        public boolean enablePlayerIconWaypoints = true;

        @ConfigEntry.Gui.Tooltip()
        public boolean useMcHeadsPlayerNameIcons = false;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public WaypointColor playerWaypointColor = WaypointColor.Black;

        @ConfigEntry.Gui.Tooltip()
        public boolean enablePlayerTrackerSystem = false;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100000)
        public int maxPlayerTrackerHudAndMinimapDistance = 100000;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100000)
        public int maxPlayerTrackerWorldmapDistance = 100000;

        //AFK options
        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip()
        public boolean showAfkInTabList = true;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 60, max = 600)
        public int timeUntilAfk = 120;

        @ConfigEntry.Gui.Tooltip()
        public boolean showAfkTimeInTabList = true;

        public boolean hideAfkMinutes = false;

        @ConfigEntry.ColorPicker
        public int AfkColor = 0xFF5500;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.ColorPicker
        public int unknownAfkStateColor = 0x606060;

        //Marker options
        @ConfigEntry.Gui.PrefixText
        public boolean enableMarkerWaypoints = true;

        public boolean enableMarkerIcons = true;

        @ConfigEntry.Gui.Tooltip
        public boolean showDefaultMarkerIcons = true;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public WaypointColor markerWaypointColor = WaypointColor.Gray;

        //Area Marker options
        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip
        public boolean enableAreaMarkerOverlay = true;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 200)
        public int areaFillAlphaMul = 100;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int areaFillAlphaMin = 0;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int areaFillAlphaMax = 42;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 200)
        public int areaLineAlphaMul = 100;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int areaLineAlphaMin = 0;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int areaLineAlphaMax = 100;

        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.BoundedDiscrete(min = 1, max = 256)
        public int blocksPerChunkThreshold = 128;

        //auto handled options
        @ConfigEntry.Gui.PrefixText
        public List<String> ignoredServers = new ArrayList<>();

        public boolean ignoreMarkerMessage = false;

        //dev options
        @ConfigEntry.Gui.PrefixText
        public boolean debugMode = false;

        public boolean chatLogInDebugMode = false;

        @ConfigEntry.Gui.Excluded
        public boolean ignoreCertificatesUseAtYourOwnRisk = false;

        public General() {
        }
    }

    @Config(name = "friends")
    public static class Friends implements ConfigData {
        @ConfigEntry.Gui.Tooltip()
        public List<String> friendList = new ArrayList<>();

        public boolean onlyShowFriendsWaypoints = false;

        public boolean onlyShowFriendsIconWaypoints = false;

        public boolean onlyShowFriendsPlayerTracker = false;

        public boolean alwaysShowFriendsWaypoints = true;

        public boolean alwaysShowFriendsIconWaypoints = true;

        public boolean alwaysShowFriendsPlayerTracker = true;

        public boolean overwriteFriendWaypointColor = false;

        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public WaypointColor friendWaypointColor = WaypointColor.Black;

        public Friends() {
        }
    }

    @Config(name = "hud")
    public static class HudModule implements ConfigData {
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ConditionalActiveMode showPlayerWaypoints = ConditionalActiveMode.ALWAYS;

        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ConditionalActiveMode showMarkerWaypoints = ConditionalActiveMode.ALWAYS;

        public boolean hidePlayersInRange = false;

        public boolean hidePlayersVisible = true;

        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 10, max = 200)
        public int playerTextScale = 100;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 10, max = 200)
        public int playerIconScale = 100;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 10, max = 200)
        public int markerTextScale = 100;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 10, max = 200)
        public int markerIconScale = 100;

        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 50)
        public int minPlayerDistance = 10;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 100, max = 100000)
        public int maxPlayerDistance = 100000;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1000)
        public int maxPlayerWaypoints = 40;

        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 50)
        public int minPlayerIconDistance = 0;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 100, max = 100000)
        public int maxPlayerIconDistance = 100000;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1000)
        public int maxPlayerIconWaypoints = 40;

        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 50)
        public int minMarkerDistance = 0;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 100, max = 100000)
        public int maxMarkerDistance = 100000;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1000)
        public int maxMarkerWaypoints = 40;

        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 50)
        public int minMarkerIconDistance = 0;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 100, max = 100000)
        public int maxMarkerIconDistance = 100000;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1000)
        public int maxMarkerIconWaypoints = 40;

        public HudModule() {
        }
    }

    @Config(name = "minimap")
    public static class MiniMapModule implements ConfigData {
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ConditionalActiveMode showPlayerWaypoints = ConditionalActiveMode.ALWAYS;

        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ConditionalActiveMode showMarkerWaypoints = ConditionalActiveMode.ALWAYS;

        public boolean hidePlayersInRange = false;

        @ConfigEntry.Gui.Tooltip()
        public boolean outOfBoundsPlayerWaypoints = true;

        @ConfigEntry.Gui.Tooltip()
        public boolean outOfBoundsMarkerWaypoints = true;

        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 10, max = 200)
        public int playerTextScale = 100;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 10, max = 200)
        public int playerIconScale = 100;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 10, max = 200)
        public int markerTextScale = 100;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 10, max = 200)
        public int markerIconScale = 100;

        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 50)
        public int minPlayerDistance = 0;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 100, max = 100000)
        public int maxPlayerDistance = 100000;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1000)
        public int maxPlayerWaypoints = 40;

        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 50)
        public int minPlayerIconDistance = 0;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 100, max = 100000)
        public int maxPlayerIconDistance = 100000;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1000)
        public int maxPlayerIconWaypoints = 40;

        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 50)
        public int minMarkerDistance = 0;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 100, max = 100000)
        public int maxMarkerDistance = 100000;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1000)
        public int maxMarkerWaypoints = 40;

        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 50)
        public int minMarkerIconDistance = 0;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 100, max = 100000)
        public int maxMarkerIconDistance = 100000;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1000)
        public int maxMarkerIconWaypoints = 40;

        public MiniMapModule() {
        }
    }

    @Config(name = "worldmap")
    public static class WorldMapModule implements ConfigData {
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ConditionalActiveMode showPlayerWaypoints = ConditionalActiveMode.ALWAYS;

        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ConditionalActiveMode showMarkerWaypoints = ConditionalActiveMode.ALWAYS;

        public boolean hidePlayersInRange = false;

        public boolean waypointIconBackground = false;

        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 10, max = 200)
        public int playerTextScale = 100;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 10, max = 200)
        public int playerIconScale = 100;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 10, max = 200)
        public int markerTextScale = 100;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 10, max = 200)
        public int markerIconScale = 100;

        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 50)
        public int minPlayerDistance = 0;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 100, max = 1000000)
        public int maxPlayerDistance = 1000000;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1000)
        public int maxPlayerWaypoints = 1000;

        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 50)
        public int minPlayerIconDistance = 0;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 100, max = 1000000)
        public int maxPlayerIconDistance = 1000000;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1000)
        public int maxPlayerIconWaypoints = 1000;

        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 50)
        public int minMarkerDistance = 0;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 100, max = 1000000)
        public int maxMarkerDistance = 1000000;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 10000)
        public int maxMarkerWaypoints = 10000;

        @ConfigEntry.Gui.PrefixText
        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 50)
        public int minMarkerIconDistance = 0;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 100, max = 1000000)
        public int maxMarkerIconDistance = 1000000;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.BoundedDiscrete(min = 0, max = 10000)
        public int maxMarkerIconWaypoints = 10000;

        public WorldMapModule() {
        }
    }

    public static class ServerEntry {
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public MapType maptype;

        public String ip;

        public String link;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public MarkerVisibilityMode markerVisibilityMode;

        @ConfigEntry.Gui.Tooltip()
        public List<String> markerLayers;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public MarkerVisibilityMode areaMarkerVisibilityMode;

        @ConfigEntry.Gui.Tooltip()
        public List<String> areaMarkerLayers;

        @ConfigEntry.Gui.Tooltip()
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public MarkerVisibilityMode iconMarkerVisibilityMode;

        @ConfigEntry.Gui.Tooltip()
        public List<String> iconMarkerLayers;

        public ServerEntry() {
            this("",
                    "",
                    MapType.Dynmap,
                    MarkerVisibilityMode.Auto,
                    new ArrayList<>(),
                    MarkerVisibilityMode.Auto,
                    new ArrayList<>(),
                    MarkerVisibilityMode.Auto,
                    new ArrayList<>());
        }

        public ServerEntry(String ip,
                           String link,
                           MapType maptype,
                           MarkerVisibilityMode markerVisibilityMode,
                           List<String> markerLayers,
                           MarkerVisibilityMode areaMarkerVisibilityMode,
                           List<String> areaMarkerLayers,
                           MarkerVisibilityMode iconMarkerVisibilityMode,
                           List<String> iconMarkerLayers) {
            this.ip = ip;
            this.link = link;
            this.maptype = maptype;
            this.markerVisibilityMode = markerVisibilityMode;
            this.markerLayers = markerLayers;
            this.areaMarkerVisibilityMode = areaMarkerVisibilityMode;
            this.areaMarkerLayers = areaMarkerLayers;
            this.iconMarkerVisibilityMode = iconMarkerVisibilityMode;
            this.iconMarkerLayers = iconMarkerLayers;
        }

        public void setMarkerLayers(List<String> layers) {
            if (markerVisibilityMode == MarkerVisibilityMode.Auto) {
                markerLayers = layers;
                markerVisibilityMode = MarkerVisibilityMode.All;
            }
            if (areaMarkerVisibilityMode == MarkerVisibilityMode.Auto) {
                areaMarkerLayers = layers;
                areaMarkerVisibilityMode = MarkerVisibilityMode.All;
            }
            if (iconMarkerVisibilityMode == MarkerVisibilityMode.Auto) {
                iconMarkerLayers = layers;
                iconMarkerVisibilityMode = MarkerVisibilityMode.All;
            }
            CommonModConfig.saveConfig();
        }

        public boolean needsMarkerLayerUpdate() {
            return markerVisibilityMode == MarkerVisibilityMode.Auto
                    || areaMarkerVisibilityMode == MarkerVisibilityMode.Auto
                    || iconMarkerVisibilityMode == MarkerVisibilityMode.Auto;
        }

        public enum MapType {
            Dynmap,
            Squaremap,
            Bluemap,
            Pl3xMap,
            LiveAtlas;

            MapType() {
            }
        }

        public enum MarkerVisibilityMode {
            Auto,
            All,
            None,
            BlackList,
            WhiteList;

            MarkerVisibilityMode() {
            }
        }

        public boolean includeMarkerLayer(String layer) {
            return includeLayer(markerVisibilityMode, markerLayers, layer);
        }

        public boolean includeAreaMarkerLayer(String layer) {
            return includeLayer(areaMarkerVisibilityMode, areaMarkerLayers, layer);
        }

        public boolean includeIconMarkerLayer(String layer) {
            return includeLayer(iconMarkerVisibilityMode, iconMarkerLayers, layer);
        }

        private boolean includeLayer(MarkerVisibilityMode markerVisibilityMode, List<String> layers, String layer) {
            switch (markerVisibilityMode) {
                case Auto:
                case All:
                    return true;
                case None:
                    return false;
                case BlackList:
                    return !layers.contains(layer);
                case WhiteList:
                    return layers.contains(layer);
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    public enum WaypointColor {
        Black,
        DarkBlue,
        DarkGreen,
        DarkAqua,
        DarkRed,
        DarkPurple,
        Gold,
        Gray,
        DarkGray,
        Blue,
        Green,
        Aqua,
        Red,
        LightPurple,
        Yellow,
        White;

        WaypointColor(){
        }

        @Override
        public String toString() {
            return Text.translatable("remote_player_waypoints_for_xaero.WaypointColor." + this.name()).getString();
        }
    }

    public enum ConditionalActiveMode {
        NEVER,
        ALWAYS,
        WHEN_PLAYER_LIST_SHOWN,
        WHEN_PLAYER_LIST_HIDDEN;

        ConditionalActiveMode(){
        }

        public boolean isActive() {
            boolean playerListShown = Minecraft.getInstance().options.keyPlayerList.isDown();

            switch (this) {
                case NEVER: return false;
                case ALWAYS: return true;
                case WHEN_PLAYER_LIST_SHOWN: return playerListShown;
                case WHEN_PLAYER_LIST_HIDDEN: return !playerListShown;
            }
            return false;
        }

        @Override
        public String toString() {
            return Text.translatable("remote_player_waypoints_for_xaero.ConditionalActiveMode." + this.name()).getString();
        }
    }
}