package com.terraformersmc.modmenu.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.terraformersmc.modmenu.util.ListWidgetHelper;

import net.minecraft.client.gui.widget.ListWidget;

@Mixin(ListWidget.class)
public class MixinListWidget implements ListWidgetHelper {

	@Inject(method = "capScrolling", at = @At("HEAD"))
	private void modmenu$capScrolling(CallbackInfo ci) {
		this.doCapScrolling();
	}

	@Override
	public void doCapScrolling() {
	}
}
