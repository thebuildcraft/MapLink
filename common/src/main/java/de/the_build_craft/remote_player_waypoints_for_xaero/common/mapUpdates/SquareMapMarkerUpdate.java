/*
 *    This file is part of the Remote player waypoints for Xaero's Map mod
 *    licensed under the GNU GPL v3 License.
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

package de.the_build_craft.remote_player_waypoints_for_xaero.common.mapUpdates;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.the_build_craft.remote_player_waypoints_for_xaero.common.waypoints.Int3;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Leander Knüttel
 * @version 25.08.2025
 */
public class SquareMapMarkerUpdate {
    public static class Marker{
        public Int3 point;
        public String tooltip;
        public String type;
        @JsonAdapter(PointsAdapterFactory.class)
        public Int3[][][] points = new Int3[0][][];
        public String fillColor;
        public String color;
        public float opacity;
        public String icon = "";
    }

    public String name;
    public String id;
    public Marker[] markers = new Marker[0];

    public static class PointsAdapterFactory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            return (TypeAdapter<T>) new PointsAdapter(gson);
        }
    }

    public static class PointsAdapter extends TypeAdapter<Int3[][][]> {
        private static final Type array2dType = new TypeToken<Int3[][]>() {}.getType();
        private final Gson gson;

        public PointsAdapter(Gson gson) {
            this.gson = gson;
        }

        @Override
        public void write(JsonWriter jsonWriter, Int3[][][] int3s) {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public Int3[][][] read(JsonReader jsonReader) throws IOException {
            jsonReader.beginArray();
            Int3[][][] result;
            switch (jsonReader.peek()) {
                case BEGIN_ARRAY:
                    result = read3dArray(jsonReader);
                    break;
                case BEGIN_OBJECT:
                    result = new Int3[][][]{{readArray(jsonReader)}};
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            jsonReader.endArray();
            return result;
        }

        Int3[] readArray(JsonReader jsonReader) throws IOException {
            List<Int3> list = new ArrayList<>();
            while (jsonReader.hasNext()) {
                list.add(gson.fromJson(jsonReader, Int3.class));
            }
            Int3[] arr = new Int3[list.size()];
            for (int i = 0; i < list.size(); i++) {
                arr[i] = list.get(i);
            }
            return arr;
        }

        Int3[][][] read3dArray(JsonReader jsonReader) throws IOException {
            List<Int3[][]> list = new ArrayList<>();
            while (jsonReader.hasNext()) {
                list.add(gson.fromJson(jsonReader, array2dType));
            }
            Int3[][][] arr = new Int3[list.size()][][];
            for (int i = 0; i < list.size(); i++) {
                arr[i] = list.get(i);
            }
            return arr;
        }
    }
}
