/*
 *    This file is part of the Remote player waypoints for Xaero's Map mod
 *    licensed under the GNU GPL v3 License.
 *    (some parts of this file are originally from "RemotePlayers" by ewpratten)
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

package de.the_build_craft.remote_player_waypoints_for_xaero.common.waypoints;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.AbstractClientPlayer;

/**
 * A player's auth profile and position
 *
 * @author ewpratten
 * @author Leander Knüttel
 * @version 29.08.2025
 */
public class PlayerPosition extends Position {
    public GameProfile gameProfile;
    public final String world;

    public PlayerPosition(String name, int x, int y, int z, String world) {
        super(name, x, y, z, name, "players");
        this.world = world;
    }

    public PlayerPosition(String name, float x, float y, float z, String world) {
        super(name, x, y, z, name, "players");
        this.world = world;
    }

    public PlayerPosition(AbstractClientPlayer player) {
        super(player.getGameProfile().getName(), (float) player.getX(), (float) player.getY(), (float) player.getZ(), player.getGameProfile().getName(), "players");
        this.world = "";
        this.gameProfile = player.getGameProfile();
    }
}