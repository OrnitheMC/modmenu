package com.terraformersmc.modmenu.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.GLX;

import com.terraformersmc.modmenu.util.GlUtil;

@Mixin(GLX.class)
public class MixinGLX {

	@Inject(method = "init", at = @At("HEAD"))
	private static void modmenu$initGlx(CallbackInfo ci) {
		GlUtil.init();
	}
}
