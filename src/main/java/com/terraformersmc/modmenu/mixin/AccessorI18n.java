package com.terraformersmc.modmenu.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.resource.language.TranslationStorage;

@Mixin(I18n.class)
public interface AccessorI18n {

	@Accessor("translations")
	public static TranslationStorage getTranslations() {
		throw new UnsupportedOperationException();
	}
}
