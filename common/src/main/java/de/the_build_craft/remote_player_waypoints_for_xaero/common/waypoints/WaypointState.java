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

package de.the_build_craft.remote_player_waypoints_for_xaero.common.waypoints;

import de.the_build_craft.remote_player_waypoints_for_xaero.common.clientMapHandlers.ClientMapHandler;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.clientMapHandlers.XaeroClientMapHandler;
import net.minecraft.client.renderer.texture.DynamicTexture;
import xaero.map.icon.XaeroIcon;

/**
 * @author Leander Knüttel
 * @version 25.08.2025
 */
public class WaypointState {
    public boolean renderOnHud = true;
    public boolean renderIconOnHud = true;
    public boolean renderOnMiniMap = true;
    public boolean renderIconOnMiniMap = true;
    public boolean renderOnWorldMap = true;
    public boolean renderIconOnWorldMap = true;

    public final boolean isTemp;
    public boolean isOld;

    public final String abbreviation;
    public final boolean hasIcon;
    public final boolean isPlayer;

    private final String iconLink;
    private DynamicTexture dynamicTexture;
    private XaeroIcon xaeroIcon;

    public WaypointState(String name, String iconLink, boolean isPlayer, boolean isTemp) {
        this.iconLink = iconLink;
        this.hasIcon = iconLink != null;
        this.abbreviation = getAbbreviation(name);
        this.isPlayer = isPlayer;
        this.isTemp = isTemp;
    }

    public DynamicTexture getDynamicTexture() {
        if (dynamicTexture != null) return dynamicTexture;
        dynamicTexture = ClientMapHandler.getDynamicTexture(iconLink);
        return dynamicTexture;
    }

    public XaeroIcon getXaeroIcon() {
        if (xaeroIcon != null) return xaeroIcon;
        xaeroIcon = XaeroClientMapHandler.getXaeroIcon(iconLink);
        return xaeroIcon;
    }

    private String getAbbreviation(String name){
        StringBuilder abbreviation = new StringBuilder();
        String[] words = name.split("[ _\\-,:;.()\\[\\]{}/\\\\|]");
        int count = 0;
        for (String word : words) {
            if (word.isEmpty()) continue;
            abbreviation.append(word.substring(0, 1).toUpperCase());
            count++;
            if (count >= 3) break;
        }
        return abbreviation.toString();
    }
}
