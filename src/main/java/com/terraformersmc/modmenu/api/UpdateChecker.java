package com.terraformersmc.modmenu.api;

public interface UpdateChecker {

	/**
	 * Gets called when ModMenu is checking for updates.
	 * This is done in a separate thread, so this call can/should be blocking.
	 *
	 * @return The update info
	 */
	UpdateInfo checkForUpdates();

}
