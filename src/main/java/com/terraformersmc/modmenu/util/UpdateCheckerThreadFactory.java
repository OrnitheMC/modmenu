package com.terraformersmc.modmenu.util;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class UpdateCheckerThreadFactory implements ThreadFactory {
	static final AtomicInteger COUNT = new AtomicInteger(-1);

	@Override
	public Thread newThread(@NotNull Runnable runnable) {
		int index = COUNT.incrementAndGet();
		return new Thread(runnable, "ModMenu/Update Checker/" + index);
	}
}
