/*
 *    This file is part of the Map Link mod
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

package de.the_build_craft.maplink.common.clientMapHandlers.playerTracker;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.authlib.GameProfile;
import de.the_build_craft.maplink.common.AbstractModInitializer;
import de.the_build_craft.maplink.common.clientMapHandlers.XaeroClientMapHandler;
import de.the_build_craft.maplink.common.waypoints.MutablePlayerPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * @author Leander Knüttel
 * @version 15.02.2026
 */
public class RemotePlayerTrackerSystem<P extends RemotePlayerTrackerReader> {
    private final P reader;
    private final Map<UUID, MutablePlayerPosition> map;
    private static final DecimalFormat precision0 = new DecimalFormat("0");
    private static final DecimalFormat precision1 = new DecimalFormat("0.0");

    public RemotePlayerTrackerSystem(P reader, Map<UUID, MutablePlayerPosition> map) {
        this.reader = reader;
        this.map = map;
    }

    public P getReader() {
        return reader;
    }

    public Iterator<MutablePlayerPosition> getTrackedPlayerIterator() {
        return map.values().iterator();
    }

    public static String injectDistanceText(GameProfile instance, Operation<String> original, Vec3 pos) {
        #if MC_VER >= MC_1_21_9
        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        #else
        Entity cameraEntity = Minecraft.getInstance().cameraEntity;
        #endif
        if (cameraEntity == null) return original.call(instance);
        #if MC_VER >= MC_1_17_1
        Vec3 cameraPos = cameraEntity.getEyePosition();
        #else
        Vec3 cameraPos = cameraEntity.getEyePosition(1);
        #endif
        double distance = cameraPos.distanceTo(pos);

        int xaeroAutoConvertToKmThreshold = AbstractModInitializer.xaeroMiniMapInstalled ? XaeroClientMapHandler.xaeroMiniMapSupport.getXaeroAutoConvertToKmThreshold() : XaeroClientMapHandler.xaeroWorldMapSupport.getXaeroAutoConvertToKmThreshold();

        String distanceText = (xaeroAutoConvertToKmThreshold != -1 && distance >= xaeroAutoConvertToKmThreshold)
                ? precision1.format(distance / 1000) + "km"
                : precision0.format(distance) + "m";
        return distanceText + " | " + original.call(instance);
    }
}
