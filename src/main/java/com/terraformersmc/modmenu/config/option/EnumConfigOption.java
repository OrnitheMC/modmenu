package com.terraformersmc.modmenu.config.option;

import com.terraformersmc.modmenu.util.TranslationUtil;

import net.ornithemc.osl.text.api.TextComponent;
import net.ornithemc.osl.text.api.TextComponents;

import java.util.Locale;

public class EnumConfigOption<E extends Enum<E>> implements ConfigOption {
	private final String key;
	private final Class<E> enumClass;
	private final E defaultValue;
	private final TextComponent description;
	private final TextComponent[] valueTexts;

	public EnumConfigOption(String key, E defaultValue) {
		ConfigOptionStorage.setEnum(key, defaultValue);
		String translationKey = TranslationUtil.translationKeyOf("option", key);
		this.key = key;
		this.enumClass = defaultValue.getDeclaringClass();
		this.defaultValue = defaultValue;
		this.description = TextComponents.translatable(translationKey);
		this.valueTexts = new TextComponent[this.enumClass.getEnumConstants().length];
		for (E value : this.enumClass.getEnumConstants()) {
			this.valueTexts[value.ordinal()] = TextComponents.translatable(translationKey + "." + value.name().toLowerCase(Locale.ROOT));
		}
	}

	public String getKey() {
		return key;
	}

	public E getValue() {
		return ConfigOptionStorage.getEnum(key, enumClass);
	}

	public void setValue(E value) {
		ConfigOptionStorage.setEnum(key, value);
	}

	public void cycleValue() {
		ConfigOptionStorage.cycleEnum(key, enumClass);
	}

	public void cycleValue(int amount) {
		ConfigOptionStorage.cycleEnum(key, enumClass, amount);
	}

	public E getDefaultValue() {
		return defaultValue;
	}

	@Override
	public String getValueLabel() {
		return TranslationUtil.translateOptionLabel(description, valueTexts[this.getValue().ordinal()]);
	}

	@Override
	public void click() {
		cycleValue();
	}
}
