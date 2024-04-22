package com.terraformersmc.modmenu;

import com.google.common.collect.ImmutableMap;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.api.UpdateChecker;
import com.terraformersmc.modmenu.gui.ModMenuOptionsScreen;
import com.terraformersmc.modmenu.mixin.AccessorMinecraft;

import com.terraformersmc.modmenu.util.mod.fabric.FabricLoaderUpdateChecker;
import com.terraformersmc.modmenu.util.mod.quilt.QuiltLoaderUpdateChecker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.options.OptionsScreen;

import java.util.Map;

public class ModMenuModMenuCompat implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ModMenuOptionsScreen::new;
	}

	@Override
	public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
		return ImmutableMap.of("minecraft", parent -> new OptionsScreen(parent, AccessorMinecraft.getInstance().options));
	}

	@Override
	public Map<String, UpdateChecker> getProvidedUpdateCheckers() {
		if (ModMenu.runningQuilt) {
			return ImmutableMap.of("quilt_loader", new QuiltLoaderUpdateChecker());
		} else {
			return ImmutableMap.of("fabricloader", new FabricLoaderUpdateChecker());
		}
	}
}
