/*
 *    This file is part of the Map Link mod
 *    licensed under the GNU GPL v3 License.
 *    (some parts of this file are originally from the Distant Horizons mod by James Seibel)
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

package de.the_build_craft.maplink.fabric;

import com.mojang.brigadier.CommandDispatcher;
import de.the_build_craft.maplink.common.AbstractModInitializer;
import de.the_build_craft.maplink.common.MainThreadTaskQueue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
#if MC_VER > MC_1_18_2
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
#else
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
#endif
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.commands.CommandSourceStack;
import org.apache.logging.log4j.Logger;

/**
 * This handles all events sent to the client
 * 
 * @author coolGi
 * @author Ran
 * @author Leander Knüttel
 * @version 23.10.2025
 */
@Environment(EnvType.CLIENT)
public class FabricClientProxy implements AbstractModInitializer.IEventProxy
{
	private static final Logger LOGGER = AbstractModInitializer.LOGGER;

	public void registerEvents()
	{
		LOGGER.info("Registering Fabric Client Events");

		#if MC_VER > MC_1_18_2
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> FabricMain.registerClientCommands((CommandDispatcher<CommandSourceStack>) (CommandDispatcher<?>) dispatcher));
		#else
		FabricMain.registerClientCommands((CommandDispatcher<CommandSourceStack>) (CommandDispatcher<?>) ClientCommandManager.DISPATCHER);
		#endif

		ClientTickEvents.END_CLIENT_TICK.register(t -> MainThreadTaskQueue.executeQueuedTasks());

		ClientPlayConnectionEvents.DISCONNECT.register((c, m) -> AbstractModInitializer.slowUpdateTask.Reset());
		ClientLoginConnectionEvents.DISCONNECT.register((c, m) -> AbstractModInitializer.slowUpdateTask.Reset());

		//register Fabric Client Events here
	}
}
