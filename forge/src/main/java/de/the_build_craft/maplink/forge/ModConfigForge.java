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

package de.the_build_craft.maplink.forge;

import de.the_build_craft.maplink.common.AbstractModInitializer;
import de.the_build_craft.maplink.common.CommonModConfig;
import de.the_build_craft.maplink.common.ModConfig;
import de.the_build_craft.maplink.common.ModConfigGui;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
#if MC_VER < MC_1_17_1
import net.minecraftforge.fml.ExtensionPoint;
#elif MC_VER == MC_1_17_1
import net.minecraftforge.fmlclient.ConfigGuiHandler;
#elif MC_VER >= MC_1_18_2 && MC_VER < MC_1_19_2
import net.minecraftforge.client.ConfigGuiHandler;
#else
import net.minecraftforge.client.ConfigScreenHandler;
#endif

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;

import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Leander Knüttel
 * @version 19.09.2025
 */
public class ModConfigForge extends CommonModConfig {
    private static final String oldId = "remote_player_waypoints_for_xaero";

    @SuppressWarnings("removal")
    @Override
    protected ModConfig getConfig() {
        try {
            Path oldConfig = FMLPaths.CONFIGDIR.get().resolve(oldId);
            Path newConfig = FMLPaths.CONFIGDIR.get().resolve(AbstractModInitializer.MOD_ID);
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

        #if MC_VER < MC_1_17_1
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, screen) -> {
            return ModConfigGui.getConfigBuilder().setParentScreen(screen).build();
        });
        #elif MC_VER >= MC_1_17_1 && MC_VER < MC_1_19_2
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
                () -> new ConfigGuiHandler.ConfigGuiFactory((mc, screen) -> ModConfigGui.getConfigBuilder().setParentScreen(screen).build()));
        #else
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> ModConfigGui.getConfigBuilder().setParentScreen(parent).build()));
        #endif

        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }
}