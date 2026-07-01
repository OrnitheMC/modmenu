package com.terraformersmc.modmenu.config.option;

import com.terraformersmc.modmenu.util.TranslationUtil;

import net.ornithemc.osl.text.api.TextComponent;
import net.ornithemc.osl.text.api.TextComponents;

public class BooleanConfigOption implements ConfigOption {
	private final String key;
	private final boolean defaultValue;
	private final TextComponent description;
	private final TextComponent enabledText;
	private final TextComponent disabledText;

	public BooleanConfigOption(String key, boolean defaultValue, String enabledKey, String disabledKey) {
		ConfigOptionStorage.setBoolean(key, defaultValue);
		String translationKey = TranslationUtil.translationKeyOf("option", key);
		this.key = key;
		this.defaultValue = defaultValue;
		this.description = TextComponents.translatable(translationKey);
		this.enabledText = TextComponents.translatable(translationKey + "." + enabledKey);
		this.disabledText = TextComponents.translatable(translationKey + "." + disabledKey);
	}

	public BooleanConfigOption(String key, boolean defaultValue) {
		this(key, defaultValue, "true", "false");
	}

	public String getKey() {
		return key;
	}

	public boolean getValue() {
		return ConfigOptionStorage.getBoolean(key);
	}

	public void setValue(boolean value) {
		ConfigOptionStorage.setBoolean(key, value);
	}

	public void toggleValue() {
		ConfigOptionStorage.toggleBoolean(key);
	}

	public boolean getDefaultValue() {
		return defaultValue;
	}

	@Override
	public String getValueLabel() {
		return TranslationUtil.translateOptionLabel(description, getValue() ? enabledText : disabledText);
	}

	@Override
	public void click() {
		toggleValue();
	}
}
