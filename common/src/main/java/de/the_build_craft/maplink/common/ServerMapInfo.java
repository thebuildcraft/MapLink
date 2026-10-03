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

package de.the_build_craft.maplink.common;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import static de.the_build_craft.maplink.common.AbstractModInitializer.LOGGER;

/**
 * Web map info sent by the server on the "maplink:server" channel,
 * e.g. by the Map Link Velocity plugin when switching servers behind a proxy.
 * <p>
 * Payload: UTF-8 JSON {@code {"v":1,"server":"survival","type":"squaremap","link":"https://map.example.com"}}
 * <ul>
 *     <li>no "type": the server has no web map configured, use the client config as usual</li>
 *     <li>"type":"none": this server has no web map</li>
 * </ul>
 *
 * @author d0ddn
 * @version 03.10.2026
 */
public class ServerMapInfo {
    public static final String CHANNEL_NAMESPACE = "maplink";
    public static final String CHANNEL_PATH = "server";

    public final String server;
    /** null if no web map is configured on the server */
    public final @Nullable String type;
    public final String link;

    private static volatile ServerMapInfo current;

    private ServerMapInfo(String server, @Nullable String type, String link) {
        this.server = server;
        this.type = type;
        this.link = link;
    }

    public static @Nullable ServerMapInfo get() {
        return current;
    }

    public static void clear() {
        current = null;
    }

    public static void handle(String json) {
        try {
            JsonObject object = JsonParser.parseString(json).getAsJsonObject();
            String server = getString(object, "server");
            String type = getString(object, "type");
            String link = getString(object, "link");
            if (server.isEmpty()) {
                LOGGER.warn("Received web map info without a server name: " + json);
                return;
            }
            ServerMapInfo info = new ServerMapInfo(server, type.isEmpty() ? null : type.toLowerCase(Locale.ROOT), link);
            ServerMapInfo old = current;
            if (old == null || !old.server.equals(info.server) || !String.valueOf(old.type).equals(String.valueOf(info.type)) || !old.link.equals(info.link)) {
                LOGGER.info("Received web map info from the server: " + json);
            }
            current = info;
        } catch (Exception e) {
            LOGGER.error("Invalid web map info from the server: " + json, e);
        }
    }

    private static String getString(JsonObject object, String key) {
        return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsString().trim() : "";
    }

    /** true if the server said that it has no web map */
    public boolean hasNoMap() {
        return "none".equals(type);
    }

    /** the map type for the config, or null if there is no (known) web map */
    @Nullable
    public ModConfig.ServerEntry.MapType getMapType() {
        if (type == null || hasNoMap() || link.isEmpty()) return null;
        for (ModConfig.ServerEntry.MapType mapType : ModConfig.ServerEntry.MapType.values()) {
            if (mapType.name().toLowerCase(Locale.ROOT).equals(type)) return mapType;
        }
        LOGGER.warn("Unknown web map type from the server: " + type);
        return null;
    }
}
