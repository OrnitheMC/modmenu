package com.terraformersmc.modmenu.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.widget.ButtonWidget;

@Mixin(ButtonWidget.class)
public interface AccessorButtonWidget {

	@Accessor("height")
	int getHeight();

	@Accessor("width")
	void setWidth(int width);

}
