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

#if MC_VER >= MC_1_21_1
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
#if MC_VER >= MC_1_21_11
import net.minecraft.resources.Identifier;
#else
import net.minecraft.resources.ResourceLocation;
#endif

import java.nio.charset.StandardCharsets;

/**
 * The raw "maplink:server" payload, see {@link ServerMapInfo}
 *
 * @author d0ddn
 * @version 03.10.2026
 */
public record ServerMapInfoPayload(String json) implements CustomPacketPayload {
    public static final Type<ServerMapInfoPayload> TYPE = new Type<>(
            #if MC_VER >= MC_1_21_11
            Identifier.fromNamespaceAndPath(ServerMapInfo.CHANNEL_NAMESPACE, ServerMapInfo.CHANNEL_PATH));
            #else
            ResourceLocation.fromNamespaceAndPath(ServerMapInfo.CHANNEL_NAMESPACE, ServerMapInfo.CHANNEL_PATH));
            #endif
    public static final StreamCodec<FriendlyByteBuf, ServerMapInfoPayload> CODEC =
            CustomPacketPayload.codec(ServerMapInfoPayload::write, ServerMapInfoPayload::read);

    private static ServerMapInfoPayload read(FriendlyByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return new ServerMapInfoPayload(new String(bytes, StandardCharsets.UTF_8));
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeBytes(json.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
#endif
