package com.terraformersmc.modmenu.mixin;

import java.util.Properties;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.locale.LanguageManager;

@Mixin(LanguageManager.class)
public interface AccessorLanguageManager {

	@Accessor("translations")
	public static Properties getTranslations() {
		throw new UnsupportedOperationException();
	}
}
