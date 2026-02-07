package com.terraformersmc.modmenu.mixin;

import java.util.Map;
import net.minecraft.client.resource.language.Locale;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Locale.class)
public interface AccessorTranslationStorage {

	@Accessor("translations")
	Map<String, String> getTranslations();

}
