package com.terraformersmc.modmenu.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.terraformersmc.modmenu.util.GlUtil;
import net.minecraft.client.render.platform.GLX;

@Mixin(GLX.class)
public class MixinGLX {

	@Inject(method = "init", at = @At("HEAD"))
	private static void modmenu$initGlx(CallbackInfo ci) {
		GlUtil.init();
	}
}
