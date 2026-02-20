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

package de.the_build_craft.maplink.common;

import de.the_build_craft.maplink.common.wrappers.Text;
import me.shedaniel.clothconfig2.api.*;
import me.shedaniel.clothconfig2.gui.entries.*;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
#if MC_VER >= MC_1_19_2
import net.minecraft.network.chat.contents.TranslatableContents;
#else
import net.minecraft.network.chat.TranslatableComponent;
#endif
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.the_build_craft.maplink.common.CommonModConfig.config;

/**
 * @author Leander Knüttel
 * @version 20.02.2026
 */
@SuppressWarnings({"UnstableApiUsage", "rawtypes"})
public class ModConfigGui {
    public static ConfigBuilder getConfigBuilder() {
        ModConfig defaultConfig = new ModConfig();
        ConfigBuilder builder = ConfigBuilder.create().setTitle(Text.translatable("maplink.title"));
        builder.setGlobalized(false);
        builder.setGlobalizedExpanded(false);
        builder.transparentBackground();
        builder.setSavingRunnable(CommonModConfig::saveConfig);
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("maplink.category.general"));
        BooleanListEntry mainToggle = entryBuilder.startBooleanToggle(Text.translatable("maplink.option.general.enabled"), config.general.enabled).setDefaultValue(true).setSaveConsumer(b -> config.general.enabled = b).build();
        general.addEntry(mainToggle);
        general.addEntry(new NestedListListEntry<ModConfig.ServerEntry, MultiElementListEntry<ModConfig.ServerEntry>>(
                Text.translatable("maplink.option.general.serverEntries"),
                config.general.serverEntries,
                config.general.serverEntries.isEmpty() || (config.general.serverEntries.size() == 1 && (config.general.serverEntries.get(0).ip.isEmpty() || config.general.serverEntries.get(0).link.isEmpty())),
                () -> Optional.of(new Component[]{Text.translatable("maplink.option.general.serverEntries.@Tooltip")}),
                value -> config.general.serverEntries = value,
                () -> Arrays.asList(new ModConfig.ServerEntry()),
                entryBuilder.getResetButtonKey(),
                true,
                false,
                (se, entryList) -> {
                    ModConfig.ServerEntry serverEntry;
                    if (se == null) {
                        serverEntry = new ModConfig.ServerEntry();
                        entryList.getValue().add(serverEntry);
                    } else {
                        serverEntry = se;
                    }
                    return new MultiElementListEntry<>(
                            (se == null || se.ip.isEmpty()) ? Text.translatable("maplink.option.ServerEntry") : Text.literal(se.ip + getServerLink(se.link)),
                            serverEntry,
                            Arrays.asList(
                                    entryBuilder.startEnumSelector(Text.translatable("maplink.option.ServerEntry.maptype"), ModConfig.ServerEntry.MapType.class, serverEntry.maptype).setDefaultValue(ModConfig.ServerEntry.MapType.Bluemap).setSaveConsumer(m -> serverEntry.maptype = m).build(),
                                    entryBuilder.startStrField(Text.translatable("maplink.option.ServerEntry.ip"), getServerIp(se)).setDefaultValue("").setSaveConsumer(s -> serverEntry.ip = s).build(),
                                    entryBuilder.startStrField(Text.translatable("maplink.option.ServerEntry.link"), serverEntry.link).setDefaultValue("").setSaveConsumer(s -> serverEntry.link = s).build(),

                                    entryBuilder.startSubCategory(Text.translatable("maplink.option.ServerEntry.markerVisibilityMode.@PrefixText"), Arrays.asList(
                                    entryBuilder.startEnumSelector(Text.translatable("maplink.option.ServerEntry.markerVisibilityMode"), ModConfig.ServerEntry.MarkerVisibilityMode.class, serverEntry.markerVisibilityMode).setDefaultValue(ModConfig.ServerEntry.MarkerVisibilityMode.Auto).setSaveConsumer(m -> serverEntry.markerVisibilityMode = m).setTooltip(Text.translatable("maplink.option.ServerEntry.markerVisibilityMode.@Tooltip")).build(),
                                    entryBuilder.startStrList(Text.translatable("maplink.option.ServerEntry.markerLayers"), serverEntry.markerLayers).setDefaultValue(new ArrayList<>()).setSaveConsumer(l -> serverEntry.markerLayers = l).setTooltip(Text.translatable("maplink.option.ServerEntry.markerLayers.@Tooltip")).build(),
                                    entryBuilder.startEnumSelector(Text.translatable("maplink.option.ServerEntry.individualMarkerMode"), ModConfig.ServerEntry.SimpleMarkerVisibilityMode.class, serverEntry.individualMarkerMode).setDefaultValue(ModConfig.ServerEntry.SimpleMarkerVisibilityMode.BlackList).setSaveConsumer(s -> serverEntry.individualMarkerMode = s).setTooltip(Text.translatable("maplink.option.ServerEntry.individualMarkerMode.@Tooltip")).build(),
                                    entryBuilder.startStrList(Text.translatable("maplink.option.ServerEntry.markers"), serverEntry.markers).setDefaultValue(new ArrayList<>()).setSaveConsumer(l -> serverEntry.markers = l).setTooltip(Text.translatable("maplink.option.ServerEntry.markers.@Tooltip")).build()
                                    )).setExpanded(false).build(),

                                    entryBuilder.startSubCategory(Text.translatable("maplink.option.ServerEntry.areaMarkerVisibilityMode.@PrefixText"), Arrays.asList(
                                    entryBuilder.startEnumSelector(Text.translatable("maplink.option.ServerEntry.areaMarkerVisibilityMode"), ModConfig.ServerEntry.MarkerVisibilityMode.class, serverEntry.areaMarkerVisibilityMode).setDefaultValue(ModConfig.ServerEntry.MarkerVisibilityMode.Auto).setSaveConsumer(m -> serverEntry.areaMarkerVisibilityMode = m).setTooltip(Text.translatable("maplink.option.ServerEntry.areaMarkerVisibilityMode.@Tooltip")).build(),
                                    entryBuilder.startStrList(Text.translatable("maplink.option.ServerEntry.areaMarkerLayers"), serverEntry.areaMarkerLayers).setDefaultValue(new ArrayList<>()).setSaveConsumer(l -> serverEntry.areaMarkerLayers = l).setTooltip(Text.translatable("maplink.option.ServerEntry.areaMarkerLayers.@Tooltip")).build(),
                                    entryBuilder.startEnumSelector(Text.translatable("maplink.option.ServerEntry.individualAreaMarkerMode"), ModConfig.ServerEntry.SimpleMarkerVisibilityMode.class, serverEntry.individualAreaMarkerMode).setDefaultValue(ModConfig.ServerEntry.SimpleMarkerVisibilityMode.BlackList).setSaveConsumer(s -> serverEntry.individualAreaMarkerMode = s).setTooltip(Text.translatable("maplink.option.ServerEntry.individualAreaMarkerMode.@Tooltip")).build(),
                                    entryBuilder.startStrList(Text.translatable("maplink.option.ServerEntry.areaMarkers"), serverEntry.areaMarkers).setDefaultValue(new ArrayList<>()).setSaveConsumer(l -> serverEntry.areaMarkers = l).setTooltip(Text.translatable("maplink.option.ServerEntry.areaMarkers.@Tooltip")).build()
                                    )).setExpanded(false).build(),

                                    entryBuilder.startSubCategory(Text.translatable("maplink.option.ServerEntry.iconMarkerVisibilityMode.@PrefixText"), Arrays.asList(
                                    entryBuilder.startEnumSelector(Text.translatable("maplink.option.ServerEntry.iconMarkerVisibilityMode"), ModConfig.ServerEntry.MarkerVisibilityMode.class, serverEntry.iconMarkerVisibilityMode).setDefaultValue(ModConfig.ServerEntry.MarkerVisibilityMode.Auto).setSaveConsumer(m -> serverEntry.iconMarkerVisibilityMode = m).setTooltip(Text.translatable("maplink.option.ServerEntry.iconMarkerVisibilityMode.@Tooltip")).build(),
                                    entryBuilder.startStrList(Text.translatable("maplink.option.ServerEntry.iconMarkerLayers"), serverEntry.iconMarkerLayers).setDefaultValue(new ArrayList<>()).setSaveConsumer(l -> serverEntry.iconMarkerLayers = l).setTooltip(Text.translatable("maplink.option.ServerEntry.iconMarkerLayers.@Tooltip")).build(),
                                    entryBuilder.startEnumSelector(Text.translatable("maplink.option.ServerEntry.individualIconMode"), ModConfig.ServerEntry.SimpleMarkerVisibilityMode.class, serverEntry.individualIconMode).setDefaultValue(ModConfig.ServerEntry.SimpleMarkerVisibilityMode.BlackList).setSaveConsumer(s -> serverEntry.individualIconMode = s).setTooltip(Text.translatable("maplink.option.ServerEntry.individualIconMode.@Tooltip")).build(),
                                    entryBuilder.startStrList(Text.translatable("maplink.option.ServerEntry.icons"), serverEntry.icons).setDefaultValue(new ArrayList<>()).setSaveConsumer(l -> serverEntry.icons = l).setTooltip(Text.translatable("maplink.option.ServerEntry.icons.@Tooltip")).build()
                                    )).setExpanded(false).build()
                            ),
                            shouldExpand(se));
                }));
        general.addEntry(entryBuilder.startIntField(Text.translatable("maplink.option.general.maxUpdateDelay"), config.general.maxUpdateDelay).setDefaultValue(defaultConfig.general.maxUpdateDelay).setMin(1000).setMax(4000).setSaveConsumer(i -> config.general.maxUpdateDelay = i).setTooltip(Text.translatable("maplink.option.general.maxUpdateDelay.@Tooltip")).build());
        general.addEntry(entryBuilder.startIntField(Text.translatable("maplink.option.general.defaultY"), config.general.defaultY).setDefaultValue(defaultConfig.general.defaultY).setMin(-100).setMax(1000).setSaveConsumer(i -> config.general.defaultY = i).setTooltip(Text.translatable("maplink.option.general.defaultY.@Tooltip")).build());
        general.addEntry(entryBuilder.startEnumSelector(Text.translatable("maplink.option.general.minimapWaypointsRenderBelow"), ModConfig.ConditionalActiveMode.class, config.general.minimapWaypointsRenderBelow).setDefaultValue(defaultConfig.general.minimapWaypointsRenderBelow).setSaveConsumer(c -> config.general.minimapWaypointsRenderBelow = c).setTooltip(Text.translatable("maplink.option.general.minimapWaypointsRenderBelow.@Tooltip")).build());

        SubCategoryBuilder playerOptions = entryBuilder.startSubCategory(Text.translatable("maplink.option.general.enablePlayerWaypoints.@PrefixText"));
        BooleanListEntry playerToggle = entryBuilder.startBooleanToggle(Text.translatable("maplink.option.general.enablePlayerWaypoints"), config.general.enablePlayerWaypoints).setDefaultValue(defaultConfig.general.enablePlayerWaypoints).setSaveConsumer(b -> config.general.enablePlayerWaypoints = b).build();
        BooleanListEntry playerTrackerToggle = entryBuilder.startBooleanToggle(Text.translatable("maplink.option.general.showPlayerWaypointsAsTrackedPlayers"), config.general.showPlayerWaypointsAsTrackedPlayers).setDefaultValue(defaultConfig.general.showPlayerWaypointsAsTrackedPlayers).setSaveConsumer(b -> config.general.showPlayerWaypointsAsTrackedPlayers = b).build();
        BooleanListEntry playerIconToggle = entryBuilder.startBooleanToggle(Text.translatable("maplink.option.general.enablePlayerIconWaypoints"), config.general.enablePlayerIconWaypoints).setDefaultValue(defaultConfig.general.enablePlayerIconWaypoints).setSaveConsumer(b -> config.general.enablePlayerIconWaypoints = b).setRequirement(Requirement.isFalse(playerTrackerToggle)).build();
        playerOptions.addAll(Arrays.asList(
                playerToggle,
                playerTrackerToggle,
                entryBuilder.startIntSlider(Text.translatable("maplink.option.general.interpolationTime"), config.general.interpolationTime, 0, 100).setDefaultValue(defaultConfig.general.interpolationTime).setSaveConsumer(i -> config.general.interpolationTime = i).setRequirement(Requirement.isTrue(playerTrackerToggle)).build(),
                playerIconToggle,
                entryBuilder.startBooleanToggle(Text.translatable("maplink.option.general.useMcHeadsPlayerNameIcons"), config.general.useMcHeadsPlayerNameIcons).setDefaultValue(defaultConfig.general.useMcHeadsPlayerNameIcons).setSaveConsumer(b -> config.general.useMcHeadsPlayerNameIcons = b).setRequirement(Requirement.all(Requirement.isFalse(playerTrackerToggle), Requirement.isTrue(playerIconToggle))).build(),
                entryBuilder.startEnumSelector(Text.translatable("maplink.option.general.playerWaypointColor"), ModConfig.WaypointColor.class, config.general.playerWaypointColor).setDefaultValue(defaultConfig.general.playerWaypointColor).setSaveConsumer(w -> config.general.playerWaypointColor = w).setEnumNameProvider(e -> ((ModConfig.WaypointColor)e).toText()).setRequirement(Requirement.isFalse(playerTrackerToggle)).build()
        ));
        autoTooltip(playerOptions);
        autoRequirement(playerOptions, Requirement.isTrue(playerToggle), playerToggle);
        general.addEntry(playerOptions.build());

        SubCategoryBuilder afkOptions = entryBuilder.startSubCategory(Text.translatable("maplink.option.general.showAfkInTabList.@PrefixText"));
        BooleanListEntry afkToggle = entryBuilder.startBooleanToggle(Text.translatable("maplink.option.general.showAfkInTabList"), config.general.showAfkInTabList).setDefaultValue(defaultConfig.general.showAfkInTabList).setSaveConsumer(b -> config.general.showAfkInTabList = b).build();
        BooleanListEntry afkTimeToggle = entryBuilder.startBooleanToggle(Text.translatable("maplink.option.general.showAfkTimeInTabList"), config.general.showAfkTimeInTabList).setDefaultValue(defaultConfig.general.showAfkTimeInTabList).setSaveConsumer(b -> config.general.showAfkTimeInTabList = b).build();
        afkOptions.addAll(Arrays.asList(
                afkToggle,
                entryBuilder.startIntField(Text.translatable("maplink.option.general.timeUntilAfk"), config.general.timeUntilAfk).setDefaultValue(defaultConfig.general.timeUntilAfk).setSaveConsumer(i -> config.general.timeUntilAfk = i).build(),
                afkTimeToggle,
                entryBuilder.startBooleanToggle(Text.translatable("maplink.option.general.hideAfkMinutes"), config.general.hideAfkMinutes).setDefaultValue(defaultConfig.general.hideAfkMinutes).setSaveConsumer(b -> config.general.hideAfkMinutes = b).setRequirement(Requirement.isTrue(afkTimeToggle)).build(),
                entryBuilder.startColorField(Text.translatable("maplink.option.general.AfkColor"), config.general.AfkColor).setDefaultValue(defaultConfig.general.AfkColor).setSaveConsumer(i -> config.general.AfkColor = i).build(),
                entryBuilder.startColorField(Text.translatable("maplink.option.general.unknownAfkStateColor"), config.general.unknownAfkStateColor).setDefaultValue(defaultConfig.general.unknownAfkStateColor).setSaveConsumer(i -> config.general.unknownAfkStateColor = i).build()
        ));
        autoTooltip(afkOptions);
        autoRequirement(afkOptions, Requirement.isTrue(afkToggle), afkToggle);
        general.addEntry(afkOptions.build());

        SubCategoryBuilder markerOptions = entryBuilder.startSubCategory(Text.translatable("maplink.option.general.enableMarkerWaypoints.@PrefixText"));
        BooleanListEntry markerToggle = entryBuilder.startBooleanToggle(Text.translatable("maplink.option.general.enableMarkerWaypoints"), config.general.enableMarkerWaypoints).setDefaultValue(defaultConfig.general.enableMarkerWaypoints).setSaveConsumer(b -> config.general.enableMarkerWaypoints = b).build();
        BooleanListEntry markerIconToggle = entryBuilder.startBooleanToggle(Text.translatable("maplink.option.general.enableMarkerIcons"), config.general.enableMarkerIcons).setDefaultValue(defaultConfig.general.enableMarkerIcons).setSaveConsumer(b -> config.general.enableMarkerIcons = b).build();
        markerOptions.addAll(Arrays.asList(
                markerToggle,
                markerIconToggle,
                entryBuilder.startBooleanToggle(Text.translatable("maplink.option.general.showDefaultMarkerIcons"), config.general.showDefaultMarkerIcons).setDefaultValue(defaultConfig.general.showDefaultMarkerIcons).setSaveConsumer(b -> config.general.showDefaultMarkerIcons = b).setRequirement(Requirement.isTrue(markerIconToggle)).build(),
                entryBuilder.startEnumSelector(Text.translatable("maplink.option.general.markerWaypointColor"), ModConfig.WaypointColor.class, config.general.markerWaypointColor).setDefaultValue(defaultConfig.general.markerWaypointColor).setSaveConsumer(w -> config.general.markerWaypointColor = w).setEnumNameProvider(e -> ((ModConfig.WaypointColor)e).toText()).build()
        ));
        autoTooltip(markerOptions);
        autoRequirement(markerOptions, Requirement.isTrue(markerToggle), markerToggle);
        general.addEntry(markerOptions.build());

        SubCategoryBuilder areaOptions = entryBuilder.startSubCategory(Text.translatable("maplink.option.general.enableAreaMarkerOverlay.@PrefixText"));
        BooleanListEntry areaToggle = entryBuilder.startBooleanToggle(Text.translatable("maplink.option.general.enableAreaMarkerOverlay"), config.general.enableAreaMarkerOverlay).setDefaultValue(defaultConfig.general.enableAreaMarkerOverlay).setSaveConsumer(b -> config.general.enableAreaMarkerOverlay = b).build();
        areaOptions.addAll(Arrays.asList(
                areaToggle,
                entryBuilder.startIntSlider(Text.translatable("maplink.option.general.areaFillAlphaMul"), config.general.areaFillAlphaMul, 0, 200).setDefaultValue(defaultConfig.general.areaFillAlphaMul).setSaveConsumer(i -> config.general.areaFillAlphaMul = i).build(),
                entryBuilder.startIntSlider(Text.translatable("maplink.option.general.areaFillAlphaMin"), config.general.areaFillAlphaMin, 0, 100).setDefaultValue(defaultConfig.general.areaFillAlphaMin).setSaveConsumer(i -> config.general.areaFillAlphaMin = i).build(),
                entryBuilder.startIntSlider(Text.translatable("maplink.option.general.areaFillAlphaMax"), config.general.areaFillAlphaMax, 0, 100).setDefaultValue(defaultConfig.general.areaFillAlphaMax).setSaveConsumer(i -> config.general.areaFillAlphaMax = i).build(),
                entryBuilder.startIntSlider(Text.translatable("maplink.option.general.areaLineAlphaMul"), config.general.areaLineAlphaMul, 0, 200).setDefaultValue(defaultConfig.general.areaLineAlphaMul).setSaveConsumer(i -> config.general.areaLineAlphaMul = i).build(),
                entryBuilder.startIntSlider(Text.translatable("maplink.option.general.areaLineAlphaMin"), config.general.areaLineAlphaMin, 0, 100).setDefaultValue(defaultConfig.general.areaLineAlphaMin).setSaveConsumer(i -> config.general.areaLineAlphaMin = i).build(),
                entryBuilder.startIntSlider(Text.translatable("maplink.option.general.areaLineAlphaMax"), config.general.areaLineAlphaMax, 0, 100).setDefaultValue(defaultConfig.general.areaLineAlphaMax).setSaveConsumer(i -> config.general.areaLineAlphaMax = i).build(),
                entryBuilder.startIntSlider(Text.translatable("maplink.option.general.blocksPerChunkThreshold"), config.general.blocksPerChunkThreshold, 1, 256).setDefaultValue(defaultConfig.general.blocksPerChunkThreshold).setSaveConsumer(i -> config.general.blocksPerChunkThreshold = i).build(),
                entryBuilder.startIntField(Text.translatable("maplink.option.general.maxChunkArea"), config.general.maxChunkArea).setDefaultValue(defaultConfig.general.maxChunkArea).setSaveConsumer(i -> config.general.maxChunkArea = i).setMin(1).build(),
                entryBuilder.startBooleanToggle(Text.translatable("maplink.option.general.excludeOPAC"), config.general.excludeOPAC).setDefaultValue(defaultConfig.general.excludeOPAC).setSaveConsumer(b -> config.general.excludeOPAC = b).build()
        ));
        autoTooltip(areaOptions);
        autoRequirement(areaOptions, Requirement.isTrue(areaToggle), areaToggle);
        general.addEntry(areaOptions.build());

        SubCategoryBuilder miscOptions = entryBuilder.startSubCategory(Text.translatable("maplink.option.general.ignoredServers.@PrefixText"));
        miscOptions.addAll(Arrays.asList(
                entryBuilder.startStrList(Text.translatable("maplink.option.general.ignoredServers"), config.general.ignoredServers).setDefaultValue(new ArrayList<>()).setSaveConsumer(l -> config.general.ignoredServers = l).build(),
                entryBuilder.startBooleanToggle(Text.translatable("maplink.option.general.ignoreMarkerMessage"), config.general.ignoreMarkerMessage).setDefaultValue(defaultConfig.general.ignoreMarkerMessage).setSaveConsumer(b -> config.general.ignoreMarkerMessage = b).build(),
                entryBuilder.startBooleanToggle(Text.translatable("maplink.option.general.hideAllChatErrors"), config.general.hideAllChatErrors).setDefaultValue(defaultConfig.general.hideAllChatErrors).setSaveConsumer(b -> config.general.hideAllChatErrors = b).build()
        ));
        autoTooltip(miscOptions);
        general.addEntry(miscOptions.build());

        SubCategoryBuilder devOptions = entryBuilder.startSubCategory(Text.translatable("maplink.option.general.debugMode.@PrefixText"));
        devOptions.addAll(Arrays.asList(
                entryBuilder.startBooleanToggle(Text.translatable("maplink.option.general.debugMode"), config.general.debugMode).setDefaultValue(defaultConfig.general.debugMode).setSaveConsumer(b -> config.general.debugMode = b).build(),
                entryBuilder.startBooleanToggle(Text.translatable("maplink.option.general.chatLogInDebugMode"), config.general.chatLogInDebugMode).setDefaultValue(defaultConfig.general.debugMode).setSaveConsumer(b -> config.general.chatLogInDebugMode = b).build()
        ));
        autoTooltip(devOptions);
        general.addEntry(devOptions.build());


        ConfigCategory friends = builder.getOrCreateCategory(Text.translatable("maplink.category.friends"));
        List<AbstractConfigListEntry> friendOptions = Arrays.asList(
                entryBuilder.startStrList(Text.translatable("maplink.option.friends.friendList"), config.friends.friendList).setDefaultValue(new ArrayList<>()).setSaveConsumer(l -> config.friends.friendList = l).build(),
                entryBuilder.startBooleanToggle(Text.translatable("maplink.option.friends.onlyShowFriendsWaypoints"), config.friends.onlyShowFriendsWaypoints).setDefaultValue(defaultConfig.friends.onlyShowFriendsWaypoints).setSaveConsumer(b -> config.friends.onlyShowFriendsWaypoints = b).build(),
                entryBuilder.startBooleanToggle(Text.translatable("maplink.option.friends.onlyShowFriendsIconWaypoints"), config.friends.onlyShowFriendsIconWaypoints).setDefaultValue(defaultConfig.friends.onlyShowFriendsIconWaypoints).setSaveConsumer(b -> config.friends.onlyShowFriendsIconWaypoints = b).setRequirement(Requirement.isFalse(playerTrackerToggle)).build(),
                entryBuilder.startBooleanToggle(Text.translatable("maplink.option.friends.alwaysShowFriendsWaypoints"), config.friends.alwaysShowFriendsWaypoints).setDefaultValue(defaultConfig.friends.alwaysShowFriendsWaypoints).setSaveConsumer(b -> config.friends.alwaysShowFriendsWaypoints = b).build(),
                entryBuilder.startBooleanToggle(Text.translatable("maplink.option.friends.alwaysShowFriendsIconWaypoints"), config.friends.alwaysShowFriendsIconWaypoints).setDefaultValue(defaultConfig.friends.alwaysShowFriendsIconWaypoints).setSaveConsumer(b -> config.friends.alwaysShowFriendsIconWaypoints = b).setRequirement(Requirement.isFalse(playerTrackerToggle)).build(),
                entryBuilder.startBooleanToggle(Text.translatable("maplink.option.friends.overwriteFriendWaypointColor"), config.friends.overwriteFriendWaypointColor).setDefaultValue(defaultConfig.friends.overwriteFriendWaypointColor).setSaveConsumer(b -> config.friends.overwriteFriendWaypointColor = b).setRequirement(Requirement.isFalse(playerTrackerToggle)).build(),
                entryBuilder.startEnumSelector(Text.translatable("maplink.option.friends.friendWaypointColor"), ModConfig.WaypointColor.class, config.friends.friendWaypointColor).setDefaultValue(defaultConfig.friends.friendWaypointColor).setSaveConsumer(w -> config.friends.friendWaypointColor = w).setEnumNameProvider(e -> ((ModConfig.WaypointColor)e).toText()).setRequirement(Requirement.isFalse(playerTrackerToggle)).build()
        );
        autoTooltip(friendOptions);
        for (AbstractConfigListEntry entry : friendOptions) friends.addEntry(entry);


        ConfigCategory hud = builder.getOrCreateCategory(Text.translatable("maplink.category.hud"));
        List<AbstractConfigListEntry> hudOptions = Arrays.asList(
                entryBuilder.startEnumSelector(Text.translatable("maplink.option.hud.showPlayerWaypoints"), ModConfig.ConditionalActiveMode.class, config.hud.showPlayerWaypoints).setDefaultValue(defaultConfig.hud.showPlayerWaypoints).setSaveConsumer(c -> config.hud.showPlayerWaypoints = c).build(),
                entryBuilder.startEnumSelector(Text.translatable("maplink.option.hud.showMarkerWaypoints"), ModConfig.ConditionalActiveMode.class, config.hud.showMarkerWaypoints).setDefaultValue(defaultConfig.hud.showMarkerWaypoints).setSaveConsumer(c -> config.hud.showMarkerWaypoints = c).build(),
                entryBuilder.startBooleanToggle(Text.translatable("maplink.option.hud.hidePlayersInRange"), config.hud.hidePlayersInRange).setDefaultValue(defaultConfig.hud.hidePlayersInRange).setSaveConsumer(b -> config.hud.hidePlayersInRange = b).build(),
                entryBuilder.startBooleanToggle(Text.translatable("maplink.option.hud.hidePlayersVisible"), config.hud.hidePlayersVisible).setDefaultValue(defaultConfig.hud.hidePlayersVisible).setSaveConsumer(b -> config.hud.hidePlayersVisible = b).build(),
                entryBuilder.startBooleanToggle(Text.translatable("maplink.option.hud.showTrackerDistance"), config.hud.showTrackerDistance).setDefaultValue(defaultConfig.hud.showTrackerDistance).setSaveConsumer(b -> config.hud.showTrackerDistance = b).setRequirement(Requirement.isTrue(playerTrackerToggle)).build()
        );
        autoTooltip(hudOptions);
        for (AbstractConfigListEntry entry : hudOptions) hud.addEntry(entry);

        SubCategoryBuilder waypointHudScaleOptions = entryBuilder.startSubCategory(Text.translatable("maplink.option.hud.playerTextScale.@PrefixText"));
        waypointHudScaleOptions.addAll(Arrays.asList(
                entryBuilder.startIntSlider(Text.translatable("maplink.option.hud.playerTextScale"), config.hud.playerTextScale, 10, 200).setDefaultValue(defaultConfig.hud.playerTextScale).setSaveConsumer(i -> config.hud.playerTextScale = i).setRequirement(Requirement.isFalse(playerTrackerToggle)).build(),
                entryBuilder.startIntSlider(Text.translatable("maplink.option.hud.playerIconScale"), config.hud.playerIconScale, 10, 200).setDefaultValue(defaultConfig.hud.playerIconScale).setSaveConsumer(i -> config.hud.playerIconScale = i).setRequirement(Requirement.isFalse(playerTrackerToggle)).build(),
                entryBuilder.startIntSlider(Text.translatable("maplink.option.hud.markerTextScale"), config.hud.markerTextScale, 10, 200).setDefaultValue(defaultConfig.hud.markerTextScale).setSaveConsumer(i -> config.hud.markerTextScale = i).build(),
                entryBuilder.startIntSlider(Text.translatable("maplink.option.hud.markerIconScale"), config.hud.markerIconScale, 10, 200).setDefaultValue(defaultConfig.hud.markerIconScale).setSaveConsumer(i -> config.hud.markerIconScale = i).build()
        ));
        autoTooltip(waypointHudScaleOptions);
        hud.addEntry(waypointHudScaleOptions.build());

        SubCategoryBuilder hudPlayerOptions = entryBuilder.startSubCategory(Text.translatable("maplink.option.hud.minVisiblePlayerDistance.@PrefixText"));
        hudPlayerOptions.addAll(Arrays.asList(
                entryBuilder.startIntField(Text.translatable("maplink.option.hud.minVisiblePlayerDistance"), config.hud.minVisiblePlayerDistance).setDefaultValue(defaultConfig.hud.minVisiblePlayerDistance).setSaveConsumer(i -> config.hud.minVisiblePlayerDistance = i).setMin(0).build(),
                entryBuilder.startIntField(Text.translatable("maplink.option.hud.minNotVisiblePlayerDistance"), config.hud.minNotVisiblePlayerDistance).setDefaultValue(defaultConfig.hud.minNotVisiblePlayerDistance).setSaveConsumer(i -> config.hud.minNotVisiblePlayerDistance = i).setMin(0).build(),
                entryBuilder.startIntField(Text.translatable("maplink.option.hud.maxPlayerDistance"), config.hud.maxPlayerDistance).setDefaultValue(defaultConfig.hud.maxPlayerDistance).setSaveConsumer(i -> config.hud.maxPlayerDistance = i).setMin(0).build(),
                entryBuilder.startIntField(Text.translatable("maplink.option.hud.maxPlayerWaypoints"), config.hud.maxPlayerWaypoints).setDefaultValue(defaultConfig.hud.maxPlayerWaypoints).setSaveConsumer(i -> config.hud.maxPlayerWaypoints = i).setMin(0).build()
        ));
        autoTooltip(hudPlayerOptions);
        hud.addEntry(hudPlayerOptions.build());

        SubCategoryBuilder hudPlayerIconOptions = entryBuilder.startSubCategory(Text.translatable("maplink.option.hud.minPlayerIconDistance.@PrefixText"));
        hudPlayerIconOptions.addAll(Arrays.asList(
                entryBuilder.startIntField(Text.translatable("maplink.option.hud.minPlayerIconDistance"), config.hud.minPlayerIconDistance).setDefaultValue(defaultConfig.hud.minPlayerIconDistance).setSaveConsumer(i -> config.hud.minPlayerIconDistance = i).setMin(0).build(),
                entryBuilder.startIntField(Text.translatable("maplink.option.hud.maxPlayerIconDistance"), config.hud.maxPlayerIconDistance).setDefaultValue(defaultConfig.hud.maxPlayerIconDistance).setSaveConsumer(i -> config.hud.maxPlayerIconDistance = i).setMin(0).build(),
                entryBuilder.startIntField(Text.translatable("maplink.option.hud.maxPlayerIconWaypoints"), config.hud.maxPlayerIconWaypoints).setDefaultValue(defaultConfig.hud.maxPlayerIconWaypoints).setSaveConsumer(i -> config.hud.maxPlayerIconWaypoints = i).setMin(0).build()
        ));
        autoTooltip(hudPlayerIconOptions);
        hud.addEntry(hudPlayerIconOptions.setRequirement(Requirement.isFalse(playerTrackerToggle)).build());

        SubCategoryBuilder hudMarkerOptions = entryBuilder.startSubCategory(Text.translatable("maplink.option.hud.minMarkerDistance.@PrefixText"));
        hudMarkerOptions.addAll(Arrays.asList(
                entryBuilder.startIntField(Text.translatable("maplink.option.hud.minMarkerDistance"), config.hud.minMarkerDistance).setDefaultValue(defaultConfig.hud.minMarkerDistance).setSaveConsumer(i -> config.hud.minMarkerDistance = i).setMin(0).build(),
                entryBuilder.startIntField(Text.translatable("maplink.option.hud.maxMarkerDistance"), config.hud.maxMarkerDistance).setDefaultValue(defaultConfig.hud.maxMarkerDistance).setSaveConsumer(i -> config.hud.maxMarkerDistance = i).setMin(0).build(),
                entryBuilder.startIntField(Text.translatable("maplink.option.hud.maxMarkerWaypoints"), config.hud.maxMarkerWaypoints).setDefaultValue(defaultConfig.hud.maxMarkerWaypoints).setSaveConsumer(i -> config.hud.maxMarkerWaypoints = i).setMin(0).build()
        ));
        autoTooltip(hudMarkerOptions);
        hud.addEntry(hudMarkerOptions.build());

        SubCategoryBuilder hudMarkerIconOptions = entryBuilder.startSubCategory(Text.translatable("maplink.option.hud.minMarkerIconDistance.@PrefixText"));
        hudMarkerIconOptions.addAll(Arrays.asList(
                entryBuilder.startIntField(Text.translatable("maplink.option.hud.minMarkerIconDistance"), config.hud.minMarkerIconDistance).setDefaultValue(defaultConfig.hud.minMarkerIconDistance).setSaveConsumer(i -> config.hud.minMarkerIconDistance = i).setMin(0).build(),
                entryBuilder.startIntField(Text.translatable("maplink.option.hud.maxMarkerIconDistance"), config.hud.maxMarkerIconDistance).setDefaultValue(defaultConfig.hud.maxMarkerIconDistance).setSaveConsumer(i -> config.hud.maxMarkerIconDistance = i).setMin(0).build(),
                entryBuilder.startIntField(Text.translatable("maplink.option.hud.maxMarkerIconWaypoints"), config.hud.maxMarkerIconWaypoints).setDefaultValue(defaultConfig.hud.maxMarkerIconWaypoints).setSaveConsumer(i -> config.hud.maxMarkerIconWaypoints = i).setMin(0).build()
        ));
        autoTooltip(hudMarkerIconOptions);
        hud.addEntry(hudMarkerIconOptions.build());


        ConfigCategory minimap = builder.getOrCreateCategory(Text.translatable("maplink.category.minimap"));
        List<AbstractConfigListEntry> minimapOptions = Arrays.asList(
                entryBuilder.startEnumSelector(Text.translatable("maplink.option.minimap.showPlayerWaypoints"), ModConfig.ConditionalActiveMode.class, config.minimap.showPlayerWaypoints).setDefaultValue(defaultConfig.minimap.showPlayerWaypoints).setSaveConsumer(c -> config.minimap.showPlayerWaypoints = c).build(),
                entryBuilder.startEnumSelector(Text.translatable("maplink.option.minimap.showMarkerWaypoints"), ModConfig.ConditionalActiveMode.class, config.minimap.showMarkerWaypoints).setDefaultValue(defaultConfig.minimap.showMarkerWaypoints).setSaveConsumer(c -> config.minimap.showMarkerWaypoints = c).build(),
                entryBuilder.startBooleanToggle(Text.translatable("maplink.option.minimap.hidePlayersInRange"), config.minimap.hidePlayersInRange).setDefaultValue(defaultConfig.minimap.hidePlayersInRange).setSaveConsumer(b -> config.minimap.hidePlayersInRange = b).build(),
                entryBuilder.startBooleanToggle(Text.translatable("maplink.option.minimap.outOfBoundsPlayerWaypoints"), config.minimap.outOfBoundsPlayerWaypoints).setDefaultValue(defaultConfig.minimap.outOfBoundsPlayerWaypoints).setSaveConsumer(b -> config.minimap.outOfBoundsPlayerWaypoints = b).build(),
                entryBuilder.startBooleanToggle(Text.translatable("maplink.option.minimap.outOfBoundsMarkerWaypoints"), config.minimap.outOfBoundsMarkerWaypoints).setDefaultValue(defaultConfig.minimap.outOfBoundsMarkerWaypoints).setSaveConsumer(b -> config.minimap.outOfBoundsMarkerWaypoints = b).build()
        );
        autoTooltip(minimapOptions);
        for (AbstractConfigListEntry entry : minimapOptions) minimap.addEntry(entry);

        SubCategoryBuilder waypointMinimapScaleOptions = entryBuilder.startSubCategory(Text.translatable("maplink.option.minimap.playerTextScale.@PrefixText"));
        waypointMinimapScaleOptions.addAll(Arrays.asList(
                entryBuilder.startIntSlider(Text.translatable("maplink.option.minimap.playerTextScale"), config.minimap.playerTextScale, 10, 200).setDefaultValue(defaultConfig.minimap.playerTextScale).setSaveConsumer(i -> config.minimap.playerTextScale = i).setRequirement(Requirement.isFalse(playerTrackerToggle)).build(),
                entryBuilder.startIntSlider(Text.translatable("maplink.option.minimap.playerIconScale"), config.minimap.playerIconScale, 10, 200).setDefaultValue(defaultConfig.minimap.playerIconScale).setSaveConsumer(i -> config.minimap.playerIconScale = i).setRequirement(Requirement.isFalse(playerTrackerToggle)).build(),
                entryBuilder.startIntSlider(Text.translatable("maplink.option.minimap.markerTextScale"), config.minimap.markerTextScale, 10, 200).setDefaultValue(defaultConfig.minimap.markerTextScale).setSaveConsumer(i -> config.minimap.markerTextScale = i).build(),
                entryBuilder.startIntSlider(Text.translatable("maplink.option.minimap.markerIconScale"), config.minimap.markerIconScale, 10, 200).setDefaultValue(defaultConfig.minimap.markerIconScale).setSaveConsumer(i -> config.minimap.markerIconScale = i).build()
        ));
        autoTooltip(waypointMinimapScaleOptions);
        minimap.addEntry(waypointMinimapScaleOptions.build());

        SubCategoryBuilder minimapPlayerOptions = entryBuilder.startSubCategory(Text.translatable("maplink.option.minimap.minPlayerDistance.@PrefixText"));
        minimapPlayerOptions.addAll(Arrays.asList(
                entryBuilder.startIntField(Text.translatable("maplink.option.minimap.minPlayerDistance"), config.minimap.minPlayerDistance).setDefaultValue(defaultConfig.minimap.minPlayerDistance).setSaveConsumer(i -> config.minimap.minPlayerDistance = i).setMin(0).build(),
                entryBuilder.startIntField(Text.translatable("maplink.option.minimap.maxPlayerDistance"), config.minimap.maxPlayerDistance).setDefaultValue(defaultConfig.minimap.maxPlayerDistance).setSaveConsumer(i -> config.minimap.maxPlayerDistance = i).setMin(0).build(),
                entryBuilder.startIntField(Text.translatable("maplink.option.minimap.maxPlayerWaypoints"), config.minimap.maxPlayerWaypoints).setDefaultValue(defaultConfig.minimap.maxPlayerWaypoints).setSaveConsumer(i -> config.minimap.maxPlayerWaypoints = i).setMin(0).build()
        ));
        autoTooltip(minimapPlayerOptions);
        minimap.addEntry(minimapPlayerOptions.build());

        SubCategoryBuilder minimapPlayerIconOptions = entryBuilder.startSubCategory(Text.translatable("maplink.option.minimap.minPlayerIconDistance.@PrefixText"));
        minimapPlayerIconOptions.addAll(Arrays.asList(
                entryBuilder.startIntField(Text.translatable("maplink.option.minimap.minPlayerIconDistance"), config.minimap.minPlayerIconDistance).setDefaultValue(defaultConfig.minimap.minPlayerIconDistance).setSaveConsumer(i -> config.minimap.minPlayerIconDistance = i).setMin(0).build(),
                entryBuilder.startIntField(Text.translatable("maplink.option.minimap.maxPlayerIconDistance"), config.minimap.maxPlayerIconDistance).setDefaultValue(defaultConfig.minimap.maxPlayerIconDistance).setSaveConsumer(i -> config.minimap.maxPlayerIconDistance = i).setMin(0).build(),
                entryBuilder.startIntField(Text.translatable("maplink.option.minimap.maxPlayerIconWaypoints"), config.minimap.maxPlayerIconWaypoints).setDefaultValue(defaultConfig.minimap.maxPlayerIconWaypoints).setSaveConsumer(i -> config.minimap.maxPlayerIconWaypoints = i).setMin(0).build()
        ));
        autoTooltip(minimapPlayerIconOptions);
        minimap.addEntry(minimapPlayerIconOptions.setRequirement(Requirement.isFalse(playerTrackerToggle)).build());

        SubCategoryBuilder minimapMarkerOptions = entryBuilder.startSubCategory(Text.translatable("maplink.option.minimap.minMarkerDistance.@PrefixText"));
        minimapMarkerOptions.addAll(Arrays.asList(
                entryBuilder.startIntField(Text.translatable("maplink.option.minimap.minMarkerDistance"), config.minimap.minMarkerDistance).setDefaultValue(defaultConfig.minimap.minMarkerDistance).setSaveConsumer(i -> config.minimap.minMarkerDistance = i).setMin(0).build(),
                entryBuilder.startIntField(Text.translatable("maplink.option.minimap.maxMarkerDistance"), config.minimap.maxMarkerDistance).setDefaultValue(defaultConfig.minimap.maxMarkerDistance).setSaveConsumer(i -> config.minimap.maxMarkerDistance = i).setMin(0).build(),
                entryBuilder.startIntField(Text.translatable("maplink.option.minimap.maxMarkerWaypoints"), config.minimap.maxMarkerWaypoints).setDefaultValue(defaultConfig.minimap.maxMarkerWaypoints).setSaveConsumer(i -> config.minimap.maxMarkerWaypoints = i).setMin(0).build()
        ));
        autoTooltip(minimapMarkerOptions);
        minimap.addEntry(minimapMarkerOptions.build());

        SubCategoryBuilder minimapMarkerIconOptions = entryBuilder.startSubCategory(Text.translatable("maplink.option.minimap.minMarkerIconDistance.@PrefixText"));
        minimapMarkerIconOptions.addAll(Arrays.asList(
                entryBuilder.startIntField(Text.translatable("maplink.option.minimap.minMarkerIconDistance"), config.minimap.minMarkerIconDistance).setDefaultValue(defaultConfig.minimap.minMarkerIconDistance).setSaveConsumer(i -> config.minimap.minMarkerIconDistance = i).setMin(0).build(),
                entryBuilder.startIntField(Text.translatable("maplink.option.minimap.maxMarkerIconDistance"), config.minimap.maxMarkerIconDistance).setDefaultValue(defaultConfig.minimap.maxMarkerIconDistance).setSaveConsumer(i -> config.minimap.maxMarkerIconDistance = i).setMin(0).build(),
                entryBuilder.startIntField(Text.translatable("maplink.option.minimap.maxMarkerIconWaypoints"), config.minimap.maxMarkerIconWaypoints).setDefaultValue(defaultConfig.minimap.maxMarkerIconWaypoints).setSaveConsumer(i -> config.minimap.maxMarkerIconWaypoints = i).setMin(0).build()
        ));
        autoTooltip(minimapMarkerIconOptions);
        minimap.addEntry(minimapMarkerIconOptions.build());


        ConfigCategory worldmap = builder.getOrCreateCategory(Text.translatable("maplink.category.worldmap"));
        List<AbstractConfigListEntry> worldmapOptions = Arrays.asList(
                entryBuilder.startEnumSelector(Text.translatable("maplink.option.worldmap.showPlayerWaypoints"), ModConfig.ConditionalActiveMode.class, config.worldmap.showPlayerWaypoints).setDefaultValue(defaultConfig.worldmap.showPlayerWaypoints).setSaveConsumer(c -> config.worldmap.showPlayerWaypoints = c).build(),
                entryBuilder.startEnumSelector(Text.translatable("maplink.option.worldmap.showMarkerWaypoints"), ModConfig.ConditionalActiveMode.class, config.worldmap.showMarkerWaypoints).setDefaultValue(defaultConfig.worldmap.showMarkerWaypoints).setSaveConsumer(c -> config.worldmap.showMarkerWaypoints = c).build(),
                entryBuilder.startBooleanToggle(Text.translatable("maplink.option.worldmap.hidePlayersInRange"), config.worldmap.hidePlayersInRange).setDefaultValue(defaultConfig.worldmap.hidePlayersInRange).setSaveConsumer(b -> config.worldmap.hidePlayersInRange = b).build(),
                entryBuilder.startBooleanToggle(Text.translatable("maplink.option.worldmap.waypointIconBackground"), config.worldmap.waypointIconBackground).setDefaultValue(defaultConfig.worldmap.waypointIconBackground).setSaveConsumer(b -> config.worldmap.waypointIconBackground = b).build(),
                entryBuilder.startBooleanToggle(Text.translatable("maplink.option.worldmap.showTrackerDistance"), config.worldmap.showTrackerDistance).setDefaultValue(defaultConfig.worldmap.showTrackerDistance).setSaveConsumer(b -> config.worldmap.showTrackerDistance = b).setRequirement(Requirement.isTrue(playerTrackerToggle)).build()
        );
        autoTooltip(worldmapOptions);
        for (AbstractConfigListEntry entry : worldmapOptions) worldmap.addEntry(entry);

        SubCategoryBuilder waypointWorldmapScaleOptions = entryBuilder.startSubCategory(Text.translatable("maplink.option.worldmap.playerTextScale.@PrefixText"));
        waypointWorldmapScaleOptions.addAll(Arrays.asList(
                entryBuilder.startIntSlider(Text.translatable("maplink.option.worldmap.playerTextScale"), config.worldmap.playerTextScale, 10, 200).setDefaultValue(defaultConfig.worldmap.playerTextScale).setSaveConsumer(i -> config.worldmap.playerTextScale = i).setRequirement(Requirement.isFalse(playerTrackerToggle)).build(),
                entryBuilder.startIntSlider(Text.translatable("maplink.option.worldmap.playerIconScale"), config.worldmap.playerIconScale, 10, 200).setDefaultValue(defaultConfig.worldmap.playerIconScale).setSaveConsumer(i -> config.worldmap.playerIconScale = i).setRequirement(Requirement.isFalse(playerTrackerToggle)).build(),
                entryBuilder.startIntSlider(Text.translatable("maplink.option.worldmap.markerTextScale"), config.worldmap.markerTextScale, 10, 200).setDefaultValue(defaultConfig.worldmap.markerTextScale).setSaveConsumer(i -> config.worldmap.markerTextScale = i).build(),
                entryBuilder.startIntSlider(Text.translatable("maplink.option.worldmap.markerIconScale"), config.worldmap.markerIconScale, 10, 200).setDefaultValue(defaultConfig.worldmap.markerIconScale).setSaveConsumer(i -> config.worldmap.markerIconScale = i).build()
        ));
        autoTooltip(waypointWorldmapScaleOptions);
        worldmap.addEntry(waypointWorldmapScaleOptions.build());

        SubCategoryBuilder worldmapPlayerOptions = entryBuilder.startSubCategory(Text.translatable("maplink.option.worldmap.minPlayerDistance.@PrefixText"));
        worldmapPlayerOptions.addAll(Arrays.asList(
                entryBuilder.startIntField(Text.translatable("maplink.option.worldmap.minPlayerDistance"), config.worldmap.minPlayerDistance).setDefaultValue(defaultConfig.worldmap.minPlayerDistance).setSaveConsumer(i -> config.worldmap.minPlayerDistance = i).setMin(0).build(),
                entryBuilder.startIntField(Text.translatable("maplink.option.worldmap.maxPlayerDistance"), config.worldmap.maxPlayerDistance).setDefaultValue(defaultConfig.worldmap.maxPlayerDistance).setSaveConsumer(i -> config.worldmap.maxPlayerDistance = i).setMin(0).build(),
                entryBuilder.startIntField(Text.translatable("maplink.option.worldmap.maxPlayerWaypoints"), config.worldmap.maxPlayerWaypoints).setDefaultValue(defaultConfig.worldmap.maxPlayerWaypoints).setSaveConsumer(i -> config.worldmap.maxPlayerWaypoints = i).setMin(0).build()
        ));
        autoTooltip(worldmapPlayerOptions);
        worldmap.addEntry(worldmapPlayerOptions.build());

        SubCategoryBuilder worldmapPlayerIconOptions = entryBuilder.startSubCategory(Text.translatable("maplink.option.worldmap.minPlayerIconDistance.@PrefixText"));
        worldmapPlayerIconOptions.addAll(Arrays.asList(
                entryBuilder.startIntField(Text.translatable("maplink.option.worldmap.minPlayerIconDistance"), config.worldmap.minPlayerIconDistance).setDefaultValue(defaultConfig.worldmap.minPlayerIconDistance).setSaveConsumer(i -> config.worldmap.minPlayerIconDistance = i).setMin(0).build(),
                entryBuilder.startIntField(Text.translatable("maplink.option.worldmap.maxPlayerIconDistance"), config.worldmap.maxPlayerIconDistance).setDefaultValue(defaultConfig.worldmap.maxPlayerIconDistance).setSaveConsumer(i -> config.worldmap.maxPlayerIconDistance = i).setMin(0).build(),
                entryBuilder.startIntField(Text.translatable("maplink.option.worldmap.maxPlayerIconWaypoints"), config.worldmap.maxPlayerIconWaypoints).setDefaultValue(defaultConfig.worldmap.maxPlayerIconWaypoints).setSaveConsumer(i -> config.worldmap.maxPlayerIconWaypoints = i).setMin(0).build()
        ));
        autoTooltip(worldmapPlayerIconOptions);
        worldmap.addEntry(worldmapPlayerIconOptions.setRequirement(Requirement.isFalse(playerTrackerToggle)).build());

        SubCategoryBuilder worldmapMarkerOptions = entryBuilder.startSubCategory(Text.translatable("maplink.option.worldmap.minMarkerDistance.@PrefixText"));
        worldmapMarkerOptions.addAll(Arrays.asList(
                entryBuilder.startIntField(Text.translatable("maplink.option.worldmap.minMarkerDistance"), config.worldmap.minMarkerDistance).setDefaultValue(defaultConfig.worldmap.minMarkerDistance).setSaveConsumer(i -> config.worldmap.minMarkerDistance = i).setMin(0).build(),
                entryBuilder.startIntField(Text.translatable("maplink.option.worldmap.maxMarkerDistance"), config.worldmap.maxMarkerDistance).setDefaultValue(defaultConfig.worldmap.maxMarkerDistance).setSaveConsumer(i -> config.worldmap.maxMarkerDistance = i).setMin(0).build(),
                entryBuilder.startIntField(Text.translatable("maplink.option.worldmap.maxMarkerWaypoints"), config.worldmap.maxMarkerWaypoints).setDefaultValue(defaultConfig.worldmap.maxMarkerWaypoints).setSaveConsumer(i -> config.worldmap.maxMarkerWaypoints = i).setMin(0).build()
        ));
        autoTooltip(worldmapMarkerOptions);
        worldmap.addEntry(worldmapMarkerOptions.build());

        SubCategoryBuilder worldmapMarkerIconOptions = entryBuilder.startSubCategory(Text.translatable("maplink.option.worldmap.minMarkerIconDistance.@PrefixText"));
        worldmapMarkerIconOptions.addAll(Arrays.asList(
                entryBuilder.startIntField(Text.translatable("maplink.option.worldmap.minMarkerIconDistance"), config.worldmap.minMarkerIconDistance).setDefaultValue(defaultConfig.worldmap.minMarkerIconDistance).setSaveConsumer(i -> config.worldmap.minMarkerIconDistance = i).setMin(0).build(),
                entryBuilder.startIntField(Text.translatable("maplink.option.worldmap.maxMarkerIconDistance"), config.worldmap.maxMarkerIconDistance).setDefaultValue(defaultConfig.worldmap.maxMarkerIconDistance).setSaveConsumer(i -> config.worldmap.maxMarkerIconDistance = i).setMin(0).build(),
                entryBuilder.startIntField(Text.translatable("maplink.option.worldmap.maxMarkerIconWaypoints"), config.worldmap.maxMarkerIconWaypoints).setDefaultValue(defaultConfig.worldmap.maxMarkerIconWaypoints).setSaveConsumer(i -> config.worldmap.maxMarkerIconWaypoints = i).setMin(0).build()
        ));
        autoTooltip(worldmapMarkerIconOptions);
        worldmap.addEntry(worldmapMarkerIconOptions.build());

        return builder;
    }

    private static boolean shouldExpand(@Nullable ModConfig.ServerEntry serverEntry) {
        return true;

        // Cloth Config doesn't allow manual expanding...

        /*if (serverEntry == null) return true;
        if (serverEntry.ip.isEmpty() || serverEntry.link.isEmpty()) return true;
        ServerData serverData = Minecraft.getInstance().getCurrentServer();
        if (serverData == null) return true;
        String serverIP = serverData.ip.toLowerCase(Locale.ROOT);
        return Objects.equals(serverEntry.ip, serverIP);*/
    }

    private static String getServerIp(@Nullable ModConfig.ServerEntry serverEntry) {
        if (serverEntry != null) return serverEntry.ip;
        ServerData serverData = Minecraft.getInstance().getCurrentServer();
        if (serverData == null) return "";
        return serverData.ip;
    }

    private static String getServerLink(String link) {
        Matcher matcher = Pattern.compile("(https?://)?[^/?#]+(/(?!index\\.html)[^/?#]+)*").matcher(link);
        if (!matcher.find()) return "";
        return " : " + matcher.group();
    }

    private static void autoTooltip(List<AbstractConfigListEntry> entries) {
        for (AbstractConfigListEntry<?> entry : entries) {
            if (entry instanceof TooltipListEntry<?>) {
                #if MC_VER >= MC_1_19_2
                String tooltipKey = ((TranslatableContents) entry.getFieldName().getContents()).getKey() + ".@Tooltip";
                #else
                String tooltipKey = ((TranslatableComponent) entry.getFieldName()).getKey() + ".@Tooltip";
                #endif
                Component tooltip = Text.translatable(tooltipKey);
                if (!tooltipKey.equals(tooltip.getString())) ((TooltipListEntry<?>) entry).setTooltipSupplier(() -> Optional.of(new Component[]{tooltip}));
            }
        }
    }

    private static void autoRequirement(List<AbstractConfigListEntry> entries, Requirement requirement, AbstractConfigListEntry<?> ... excluded) {
        List<AbstractConfigListEntry<?>> excludedList = Arrays.asList(excluded);
        for (AbstractConfigListEntry<?> entry : entries) {
            if (excludedList.contains(entry)) continue;
            if (entry.getRequirement() == null) {
                entry.setRequirement(requirement);
            } else {
                entry.setRequirement(Requirement.all(entry.getRequirement(), requirement));
            }
        }
    }
}
