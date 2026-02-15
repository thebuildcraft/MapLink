/*
 *    This file is part of the Map Link mod
 *    licensed under the GNU GPL v3 License.
 *
 *    Copyright (C) 2026  Leander Knüttel and contributors
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

import de.the_build_craft.maplink.common.waypoints.MutablePlayerPosition;
import xaero.hud.minimap.player.tracker.system.IRenderedPlayerTracker;

import java.util.Map;
import java.util.UUID;

/**
 * @author Leander Knüttel
 * @version 15.02.2026
 */
public class MiniMapPlayerTrackerSystem extends RemotePlayerTrackerSystem<MiniMapPlayerTrackerReader> implements IRenderedPlayerTracker<MutablePlayerPosition> {
    public MiniMapPlayerTrackerSystem(MiniMapPlayerTrackerReader reader, Map<UUID, MutablePlayerPosition> map) {
        super(reader, map);
    }
}
