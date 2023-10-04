package com.terraformersmc.modmenu.util;

import java.net.URI;
import java.net.URISyntaxException;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.mixin.InvokerScreen;

import net.minecraft.client.gui.screen.Screen;

public class ScreenUtil {

	public static void openLink(Screen screen, String link, String source) {
		try {
			((InvokerScreen) screen).invokeOpenLink(new URI(link));
		} catch (URISyntaxException e) {
			ModMenu.LOGGER.warn("failed to open link for " + source, e);
		}
	}
}
