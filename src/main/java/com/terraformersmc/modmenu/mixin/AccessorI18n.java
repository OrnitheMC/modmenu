package com.terraformersmc.modmenu.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.resource.language.Locale;

@Mixin(I18n.class)
public interface AccessorI18n {

	@Accessor("locale")
	public static Locale getTranslations() {
		throw new UnsupportedOperationException();
	}
}
