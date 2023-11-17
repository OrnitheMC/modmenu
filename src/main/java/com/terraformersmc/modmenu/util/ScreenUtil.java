package com.terraformersmc.modmenu.util;

import java.net.URI;
import java.net.URISyntaxException;

import com.terraformersmc.modmenu.ModMenu;

import net.minecraft.client.gui.screen.Screen;

public class ScreenUtil {

	public static void openLink(Screen screen, String link, String source) {
		try {
			openLink(new URI(link));
		} catch (URISyntaxException e) {
			ModMenu.LOGGER.warn("failed to open link for " + source, e);
		}
	}

	private static void openLink(URI uri) {
        try {
            Class<?> clazz = Class.forName("java.awt.Desktop");
            Object object = clazz.getMethod("getDesktop").invoke(null);
            clazz.getMethod("browse", URI.class).invoke(object, uri);
        } catch (Throwable throwable) {
            ModMenu.LOGGER.error("Couldn't open link", throwable);
        }
    }
}
