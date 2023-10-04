package com.terraformersmc.modmenu.mixin;

import java.net.URI;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.screen.Screen;

@Mixin(Screen.class)
public interface InvokerScreen {

	@Invoker("openLink")
	void invokeOpenLink(URI link);

}
