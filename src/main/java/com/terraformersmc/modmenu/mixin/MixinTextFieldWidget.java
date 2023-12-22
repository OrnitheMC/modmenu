package com.terraformersmc.modmenu.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.terraformersmc.modmenu.gui.widget.Controller;
import com.terraformersmc.modmenu.gui.widget.TextFieldAccess;

import net.minecraft.client.gui.widget.TextFieldWidget;

@Mixin(TextFieldWidget.class)
public class MixinTextFieldWidget implements TextFieldAccess {

	@Shadow
	private String text;
	@Unique
	private Controller controller;

	// captured value of TextFieldWidget#text at the start of a keyPressed call
	private String modmenu$prevText;

	@Inject(
		method = "keyPressed",
		at = @At(
			value = "HEAD"
		)
	)
	private void modmenu$beforeKeyPressed(CallbackInfo ci) {
		if (this.controller != null) {
			this.modmenu$prevText = this.text;
		}
	}

	@Inject(
		method = "keyPressed",
		at = @At(
			value = "TAIL"
		)
	)
	private void modmenu$afterKeyPressed(CallbackInfo ci) {
		if (this.controller != null) {
			if (this.modmenu$prevText != null && !this.text.equals(this.modmenu$prevText)) {
				this.controller.setValue(this.text);
			}
			this.modmenu$prevText = null;
		}
	}

	@Override
	public void setController(Controller controller) {
		this.controller = controller;
	}
}
