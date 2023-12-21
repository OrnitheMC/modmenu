package com.terraformersmc.modmenu.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public interface AccessorMinecraft {

	@Accessor("INSTANCE")
	public static Minecraft getInstance() {
		throw new UnsupportedOperationException();
	}
}
