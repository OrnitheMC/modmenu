package com.terraformersmc.modmenu.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.lwjgl.Sys;

import com.terraformersmc.modmenu.ModMenu;

import net.minecraft.util.Utils;

public class OsUtil {

	/**
	 * impl taken from GLX in 1.9+
	 */
	public static void openFolder(File folder) {
		String path = folder.getAbsolutePath();
		if (Utils.getOS() == Utils.OS.MACOS) {
			try {
				ModMenu.LOGGER.info(path);
				Runtime.getRuntime().exec(new String[] { "/usr/bin/open", path });
				return;
			} catch (IOException e) {
				ModMenu.LOGGER.error("Couldn't open file", e);
			}
		} else if (Utils.getOS() == Utils.OS.WINDOWS) {
			String command = String.format("cmd.exe /C start \"Open file\" \"%s\"", path);
			try {
				Runtime.getRuntime().exec(command);
				return;
			} catch (IOException e) {
				ModMenu.LOGGER.error("Couldn't open file", (Throwable) e);
			}
		}
		boolean useSys = false;
		try {
			Class<?> desktopClass = Class.forName("java.awt.Desktop");
			Object desktop = desktopClass.getMethod("getDesktop").invoke(null);
			desktopClass.getMethod("browse", URI.class).invoke(desktop, folder.toURI());
		} catch (Throwable t) {
			ModMenu.LOGGER.error("Couldn't open link", t);
			useSys = true;
		}
		if (useSys) {
			ModMenu.LOGGER.info("Opening via system class!");
			Sys.openURL("file://" + path);
		}
	}
}
