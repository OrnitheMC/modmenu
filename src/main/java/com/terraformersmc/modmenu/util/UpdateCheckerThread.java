package com.terraformersmc.modmenu.util;

import com.terraformersmc.modmenu.util.mod.Mod;

public class UpdateCheckerThread extends Thread {

	protected UpdateCheckerThread(Mod mod, Runnable runnable) {
		super(runnable);
		setDaemon(true);
		setName(String.format("Update Checker/%s", mod.getName()));
	}

	public static void run(Mod mod, Runnable runnable) {
		new UpdateCheckerThread(mod, runnable).start();
	}

}
