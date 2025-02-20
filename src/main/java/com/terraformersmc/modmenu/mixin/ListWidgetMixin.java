package com.terraformersmc.modmenu.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.widget.ListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ListWidget.class)
public abstract class ListWidgetMixin {
	@Shadow
	protected abstract int getHeight();

	@WrapOperation(method = "render(IIF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ListWidget;getMaxScroll()I"))
	private int skipBlock(ListWidget instance, Operation<Integer> operation) {
		if (this.getHeight() == 0) {
			return 0;
		} else {
			return operation.call(instance);
		}
	}
}
