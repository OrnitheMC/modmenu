package com.terraformersmc.modmenu.util;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JsonUtil {
	private JsonUtil() {}

	public static Optional<String> getString(JsonObject parent, String field) {
		if (!parent.has(field)) {
			return Optional.empty();
		}

		JsonElement value = parent.get(field);

		if (!value.isJsonPrimitive() || !((JsonPrimitive)value).isString()) {
			return Optional.empty();
		}

		return Optional.of(value.getAsString());
	}

	public static Optional<Boolean> getBoolean(JsonObject parent, String field) {
		if (!parent.has(field)) {
			return Optional.empty();
		}

		JsonElement value = parent.get(field);

		if (!value.isJsonPrimitive() || !((JsonPrimitive)value).isBoolean()) {
			return Optional.empty();
		}

		return Optional.of(value.getAsBoolean());
	}
}
