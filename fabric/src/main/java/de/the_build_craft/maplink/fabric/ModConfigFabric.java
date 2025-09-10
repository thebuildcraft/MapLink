/*
 *    This file is part of the Map Link mod
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

package de.the_build_craft.maplink.fabric;

import de.the_build_craft.maplink.common.AbstractModInitializer;
import de.the_build_craft.maplink.common.CommonModConfig;
import de.the_build_craft.maplink.common.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.FileUtils;

import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Leander Knüttel
 * @version 09.09.2025
 */
public class ModConfigFabric extends CommonModConfig {
    private static final String oldId = "remote_player_waypoints_for_xaero";

    @Override
    protected ModConfig getConfig() {
        try {
            Path oldConfig = FabricLoader.getInstance().getConfigDir().resolve(oldId);
            Path newConfig = FabricLoader.getInstance().getConfigDir().resolve(AbstractModInitializer.MOD_ID);
            if (Files.exists(oldConfig) && !Files.exists(newConfig)) {
                #if MC_VER > MC_1_16_5
                FileUtils.copyDirectory(oldConfig.toFile(), newConfig.toFile(), null, true, new CopyOption[0]);
                #else
                FileUtils.copyDirectory(oldConfig.toFile(), newConfig.toFile(), true);
                #endif
            }
        } catch (Throwable t) {
            AbstractModInitializer.LOGGER.error("Failed to migrate config file", t);
        }

        AutoConfig.register(ModConfig.class, PartitioningSerializer.wrap(JanksonConfigSerializer::new));
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }
}
