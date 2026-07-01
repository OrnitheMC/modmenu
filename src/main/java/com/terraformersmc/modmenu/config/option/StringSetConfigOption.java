package com.terraformersmc.modmenu.config.option;

import net.ornithemc.osl.text.api.TextComponent;
import net.ornithemc.osl.text.api.TextComponents;

import com.terraformersmc.modmenu.util.TranslationUtil;

import java.util.Set;

public class StringSetConfigOption {
	private final String key;
	private final Set<String> defaultValue;
	private final TextComponent description;

	public StringSetConfigOption(String key, Set<String> defaultValue) {
		super();
		ConfigOptionStorage.setStringSet(key, defaultValue);
		String translationKey = TranslationUtil.translationKeyOf("option", key);
		this.key = key;
		this.defaultValue = defaultValue;
		this.description = TextComponents.translatable(translationKey);
	}

	public String getKey() {
		return key;
	}

	public Set<String> getValue() {
		return ConfigOptionStorage.getStringSet(key);
	}

	public void setValue(Set<String> value) {
		ConfigOptionStorage.setStringSet(key, value);
	}

	public TextComponent getMessage() {
		return description;
	}

	public Set<String> getDefaultValue() {
		return defaultValue;
	}
}
