/*
 *    This file is part of the Map Link mod
 *    licensed under the GNU GPL v3 License.
 *    (some parts of this file are originally from "RemotePlayers" by ewpratten)
 *
 *    Copyright (C) 2024 - 2026  Leander Knüttel and contributors
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A marker's name and position
 *
 * @author Leander Knüttel
 * @version 03.10.2026
 */
public class Position {
    /** Hidden style markup like {@code <!--ml:bold,gold-->}, invisible in the browser */
    private static final Pattern styleCommentPattern = Pattern.compile("(?is)<!--\\s*ml:(.*?)-->");
    private static final Pattern commentPattern = Pattern.compile("(?s)<!--.*?-->");
    /** Internal representation of the style markup inside {@link #name}, parsed by StyledText */
    public static final Pattern STYLE_TOKEN_PATTERN = Pattern.compile("§\\{([^}]*)}");
    private static final Pattern lineBreakTagPattern = Pattern.compile("(?i)<br\\s*/?>|</?(?:div|p|li|tr|h[1-6])\\b(?:\"[^\"]*\"|'[^']*'|[^'\">])*>");
    private static final Pattern tagPattern = Pattern.compile("<(?:\"[^\"]*\"|'[^']*'|[^'\">])*>");
    private static final Pattern lineSplitPattern = Pattern.compile("\\s*\\R\\s*");
    /** Xaero's TextSplitter only breaks lines on a "\n" that is surrounded by spaces */
    public static final String LINE_SEPARATOR = " \n ";
    public final String name;
    public final Double3 pos;
    public final String id;
    public final MarkerLayer layer;

    public Position(String name, int x, int y, int z, String id, MarkerLayer layer) {
        this(name, false, x, y, z, id, layer);
    }

    public Position(String name, double x, double y, double z, String id, MarkerLayer layer) {
        this(name, false, x, y, z, id, layer);
    }

    protected Position(String name, boolean multiline, int x, int y, int z, String id, MarkerLayer layer) {
        this.name = getDisplayName(name, multiline);
        this.pos = new Double3(x + 0.5f, y, z + 0.5f);
        this.id = ClientMapHandler.waypointPrefix +id;
        this.layer = layer;
    }

    protected Position(String name, boolean multiline, double x, double y, double z, String id, MarkerLayer layer) {
        this.name = getDisplayName(name, multiline);
        this.pos = new Double3(x, y,z);
        this.id = ClientMapHandler.waypointPrefix + id;
        this.layer = layer;
    }

    static String getDisplayName(String name){
        return getDisplayName(name, false);
    }

    static String getDisplayName(String name, boolean multiline){
        Matcher styleMatcher = styleCommentPattern.matcher(name);
        StringBuffer styled = new StringBuffer();
        while (styleMatcher.find()) {
            String tokens = multiline ? "§{" + styleMatcher.group(1).trim().toLowerCase(Locale.ROOT).replaceAll("[\\s;,]+", ",") + "}" : "";
            styleMatcher.appendReplacement(styled, Matcher.quoteReplacement(tokens));
        }
        styleMatcher.appendTail(styled);
        String text = commentPattern.matcher(styled.toString()).replaceAll("");
        text = lineBreakTagPattern.matcher(text).replaceAll("\n");
        text = tagPattern.matcher(text).replaceAll("");
        text = org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4(text);
        List<String> lines = new ArrayList<>();
        String pendingTokens = "";
        for (String line : lineSplitPattern.split(text.trim())) {
            line = pendingTokens + line;
            // style tokens without text (e.g. in front of a <div>) apply to the next line
            if (STYLE_TOKEN_PATTERN.matcher(line).replaceAll("").trim().isEmpty()) {
                pendingTokens = line;
                continue;
            }
            pendingTokens = "";
            lines.add(line);
        }
        return String.join(multiline ? LINE_SEPARATOR : " ", lines);
    }
}
