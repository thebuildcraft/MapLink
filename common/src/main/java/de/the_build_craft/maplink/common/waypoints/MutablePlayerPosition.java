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

package de.the_build_craft.maplink.common.waypoints;

import de.the_build_craft.maplink.common.clientMapHandlers.ClientMapHandler;
import net.minecraft.util.Mth;

import java.util.UUID;

import static de.the_build_craft.maplink.common.CommonModConfig.config;

/**
 * @author Leander Knüttel
 * @version 03.10.2025
 */
public class MutablePlayerPosition {
    private final String id;
    public final UUID uuid;
    private final MutableDouble3 prevPos;
    private final MutableDouble3 pos;
    private WaypointState waypointState;
    private long prevUpdate;
    private double interpolationTime = 100.0;

    public MutablePlayerPosition(PlayerPosition playerPosition, WaypointState waypointState) {
        this.id = playerPosition.id;
        #if MC_VER >= MC_1_21_9
        this.uuid = playerPosition.gameProfile.id();
        #else
        this.uuid = playerPosition.gameProfile.getId();
        #endif
        this.prevPos = new MutableDouble3(playerPosition.pos);
        this.pos = new MutableDouble3(playerPosition.pos);
        this.waypointState = waypointState;
        prevUpdate = System.currentTimeMillis();
    }

    public WaypointState getWaypointState() {
        if (waypointState.isOld) waypointState = ClientMapHandler.getWaypointState(id);
        return waypointState;
    }

    public void updateFrom(Double3 newPos) {
        prevPos.updateFrom(pos);
        pos.updateFrom(newPos);
        prevUpdate = System.currentTimeMillis();
        interpolationTime = Mth.clamp(config.general.interpolationTime, 1.0, 100.0);
    }

    //from https://easings.net/#easeInOutCubic
    double easeInOutCubic(double x) {
        return (x < 0.5) ? (4 * x * x * x) : (1 - Math.pow(-2 * x + 2, 3) / 2);
    }

    public double getLerpedX() {
        if (config.general.interpolationTime < 1) return pos.x;
        return Mth.lerp(easeInOutCubic(Mth.clamp((System.currentTimeMillis() - prevUpdate) / interpolationTime, 0, 1)), prevPos.x, pos.x);
    }

    public double getLerpedY() {
        if (config.general.interpolationTime < 1) return pos.y;
        return Mth.lerp(easeInOutCubic(Mth.clamp((System.currentTimeMillis() - prevUpdate) / interpolationTime, 0, 1)), prevPos.y, pos.y);
    }

    public double getLerpedZ() {
        if (config.general.interpolationTime < 1) return pos.z;
        return Mth.lerp(easeInOutCubic(Mth.clamp((System.currentTimeMillis() - prevUpdate) / interpolationTime, 0, 1)), prevPos.z, pos.z);
    }
}
