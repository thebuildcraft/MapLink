/*
 *    This file is part of the Map Link mod
 *    licensed under the GNU GPL v3 License.
 *    (some parts of this file are originally from the Distant Horizons mod by James Seibel)
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

package de.the_build_craft.maplink.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import de.the_build_craft.maplink.common.AbstractModInitializer;
import de.the_build_craft.maplink.common.MainThreadTaskQueue;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
#if MC_VER >= MC_1_20_6
import net.neoforged.neoforge.client.event.ClientTickEvent;
#else
import net.neoforged.neoforge.event.TickEvent.ClientTickEvent;
#endif
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.Logger;

/**
 * This handles all events sent to the client
 *
 * @author James Seibel
 * @author Leander Knüttel
 * @version 28.10.2025
 */
public class NeoforgeClientProxy implements AbstractModInitializer.IEventProxy
{
	private static final Logger LOGGER = AbstractModInitializer.LOGGER;

	@Override
	public void registerEvents()
	{
		LOGGER.info("Registering NeoForge Client Events");

		NeoForge.EVENT_BUS.register(this);

		//OR register NeoForge Client Events here
	}

	@SubscribeEvent
	@SuppressWarnings("unchecked")
	public void registerClientCommands(RegisterClientCommandsEvent event) {
		NeoforgeMain.registerClientCommands((CommandDispatcher<CommandSourceStack>) (CommandDispatcher<?>) event.getDispatcher());
	}

	@SubscribeEvent
	#if MC_VER >= MC_1_20_6
	public void onClientTick(ClientTickEvent.Post event) {
	#else
	public void onClientTick(ClientTickEvent event) {
	#endif
		MainThreadTaskQueue.executeQueuedTasks();
	}

	@SubscribeEvent
	public void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		AbstractModInitializer.slowUpdateTask.Reset();
	}

}
