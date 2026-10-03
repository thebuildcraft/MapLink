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

package de.the_build_craft.maplink.common.clientMapHandlers;

import de.the_build_craft.maplink.common.waypoints.Position;
import de.the_build_craft.maplink.common.wrappers.Text;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

import java.util.*;
import java.util.regex.Matcher;

/**
 * Turns marker names with style tokens (see {@link Position#STYLE_TOKEN_PATTERN}) into styled Components.
 * Styles accumulate until "reset" and are reset at every new line.
 *
 * @author d0ddn
 * @version 03.10.2026
 */
public class StyledText {
    private static final String CODES = "0123456789abcdefklmnor";
    private static final ChatFormatting[] BY_CODE = {
            ChatFormatting.BLACK, ChatFormatting.DARK_BLUE, ChatFormatting.DARK_GREEN, ChatFormatting.DARK_AQUA,
            ChatFormatting.DARK_RED, ChatFormatting.DARK_PURPLE, ChatFormatting.GOLD, ChatFormatting.GRAY,
            ChatFormatting.DARK_GRAY, ChatFormatting.BLUE, ChatFormatting.GREEN, ChatFormatting.AQUA,
            ChatFormatting.RED, ChatFormatting.LIGHT_PURPLE, ChatFormatting.YELLOW, ChatFormatting.WHITE,
            ChatFormatting.OBFUSCATED, ChatFormatting.BOLD, ChatFormatting.STRIKETHROUGH,
            ChatFormatting.UNDERLINE, ChatFormatting.ITALIC, ChatFormatting.RESET};
    private static final Set<ChatFormatting> FORMATS = EnumSet.of(ChatFormatting.OBFUSCATED, ChatFormatting.BOLD,
            ChatFormatting.STRIKETHROUGH, ChatFormatting.UNDERLINE, ChatFormatting.ITALIC);

    public static MutableComponent toComponent(Collection<String> names) {
        MutableComponent result = Text.literal("");
        boolean first = true;
        for (String name : names) {
            if (!first) result.append(Text.literal(Position.LINE_SEPARATOR));
            result.append(toComponent(name));
            first = false;
        }
        return result;
    }

    public static MutableComponent toComponent(String name) {
        MutableComponent result = Text.literal("");
        String[] lines = name.split(Position.LINE_SEPARATOR, -1);
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) result.append(Text.literal(Position.LINE_SEPARATOR));
            appendLine(result, lines[i]);
        }
        return result;
    }

    private static void appendLine(MutableComponent result, String line) {
        Set<ChatFormatting> formats = EnumSet.noneOf(ChatFormatting.class);
        ChatFormatting color = null;
        Integer rgb = null;

        Matcher matcher = Position.STYLE_TOKEN_PATTERN.matcher(line);
        int last = 0;
        while (true) {
            boolean found = matcher.find();
            String text = line.substring(last, found ? matcher.start() : line.length());
            if (!text.isEmpty()) {
                MutableComponent part = Text.literal(text).withStyle(formats.toArray(new ChatFormatting[0]));
                if (color != null) part = part.withStyle(color);
                if (rgb != null) {
                    int finalRgb = rgb;
                    part = part.withStyle(style -> style.withColor(TextColor.fromRgb(finalRgb)));
                }
                result.append(part);
            }
            if (!found) break;
            last = matcher.end();

            for (String token : matcher.group(1).split(",")) {
                if (token.isEmpty()) continue;
                if (token.startsWith("#")) {
                    try {
                        rgb = Integer.parseInt(token.substring(1), 16);
                        color = null;
                    } catch (NumberFormatException ignored) {
                    }
                    continue;
                }
                List<ChatFormatting> formattings = parseToken(token);
                for (ChatFormatting formatting : formattings) {
                    if (formatting == ChatFormatting.RESET) {
                        formats.clear();
                        color = null;
                        rgb = null;
                    } else if (FORMATS.contains(formatting)) {
                        formats.add(formatting);
                    } else {
                        color = formatting;
                        rgb = null;
                    }
                }
            }
        }
    }

    /** a name like "bold" / "dark_red" or a sequence of Minecraft formatting codes like "l6" */
    private static List<ChatFormatting> parseToken(String token) {
        try {
            return Collections.singletonList(ChatFormatting.valueOf(alias(token).toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ignored) {
        }
        List<ChatFormatting> result = new ArrayList<>();
        for (char c : token.toCharArray()) {
            int index = CODES.indexOf(c);
            if (index < 0) return Collections.emptyList();
            result.add(BY_CODE[index]);
        }
        return result;
    }

    private static String alias(String token) {
        switch (token) {
            case "underlined": return "underline";
            case "strike": return "strikethrough";
            case "obf": return "obfuscated";
            default: return token;
        }
    }
}
