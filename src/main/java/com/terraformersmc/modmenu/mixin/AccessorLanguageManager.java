package com.terraformersmc.modmenu.mixin;

import java.util.Properties;
import net.minecraft.locale.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Language.class)
public interface AccessorLanguageManager {

	@Accessor("translations")
	Properties getTranslations();

}
