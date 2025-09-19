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

package de.the_build_craft.maplink.forge;

import de.the_build_craft.maplink.common.AbstractModInitializer;
import de.the_build_craft.maplink.common.LoaderType;
import de.the_build_craft.maplink.forge.wrappers.ForgeModChecker;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * main entry point on Forge
 *
 * @author James Seibel
 * @author Leander Knüttel
 * @version 19.09.2025
 */
@Mod(AbstractModInitializer.MOD_ID)
public class ForgeMain extends AbstractModInitializer
{
	@SuppressWarnings("removal")
    public ForgeMain()
	{
		loaderType = LoaderType.Forge;
        new ModConfigForge();
		new XaerosMapCompatForge();

		// Register the mod initializer (Actual event registration is done in the different proxies)
		FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLClientSetupEvent e) -> this.onInitializeClient());
		FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLDedicatedServerSetupEvent e) -> this.onInitializeServer());
	}

	@Override
	public void onInitializeClient(){
		super.onInitializeClient();

		//Forge Client init here
	}

	@Override
	public void onInitializeServer(){
		super.onInitializeServer();

		//Forge Server init here
	}
	
	@Override
	protected void createInitialBindings() {
		new ForgeModChecker();

		//Forge static Instances here
	}
	
	@Override
	protected IEventProxy createClientProxy() { return new ForgeClientProxy(); }
	
	@Override
	protected IEventProxy createServerProxy(boolean isDedicated) { return new ForgeServerProxy(isDedicated); }
	
	@Override
	protected void initializeModCompat()
	{
	}
}
