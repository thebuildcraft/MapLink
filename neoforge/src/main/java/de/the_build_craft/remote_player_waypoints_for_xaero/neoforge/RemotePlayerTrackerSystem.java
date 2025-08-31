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

package de.the_build_craft.remote_player_waypoints_for_xaero.neoforge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.authlib.GameProfile;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.waypoints.MutablePlayerPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import xaero.common.HudMod;
import xaero.hud.minimap.player.tracker.system.IRenderedPlayerTracker;
import xaero.map.radar.tracker.system.IPlayerTrackerSystem;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * @author Leander Knüttel
 * @version 31.08.2025
 */
public class RemotePlayerTrackerSystem implements IRenderedPlayerTracker<MutablePlayerPosition>, IPlayerTrackerSystem<MutablePlayerPosition> {
    private final RemotePlayerTrackerReader reader;
    private final Map<UUID, MutablePlayerPosition> map;
    private static final DecimalFormat precision0 = new DecimalFormat("0");
    private static final DecimalFormat precision1 = new DecimalFormat("0.0");

    public RemotePlayerTrackerSystem(RemotePlayerTrackerReader reader, Map<UUID, MutablePlayerPosition> map) {
        this.reader = reader;
        this.map = map;
    }

    @Override
    public RemotePlayerTrackerReader getReader() {
        return reader;
    }

    @Override
    public Iterator<MutablePlayerPosition> getTrackedPlayerIterator() {
        return map.values().iterator();
    }

    public static String injectDistanceText(GameProfile instance, Operation<String> original, Vec3 pos) {
        if (Minecraft.getInstance().cameraEntity == null) return original.call(instance);
        #if MC_VER >= MC_1_17_1
        Vec3 cameraPos = Minecraft.getInstance().cameraEntity.getEyePosition();
        #else
        Vec3 cameraPos = Minecraft.getInstance().cameraEntity.getEyePosition(1);
        #endif
        double distance = cameraPos.distanceTo(pos);
        int autoConvertToKmThreshold = HudMod.INSTANCE.getSettings().autoConvertWaypointDistanceToKmThreshold;
        String distanceText = (autoConvertToKmThreshold != -1 && distance >= autoConvertToKmThreshold)
                ? precision1.format(distance / 1000) + "km"
                : precision0.format(distance) + "m";
        return distanceText + " | " + original.call(instance);
    }
}
