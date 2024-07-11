package com.terraformersmc.modmenu.util;

import net.fabricmc.loader.api.FabricLoader;

import java.util.Arrays;
import java.util.List;

public final class VersionUtil {
	private static final List<String> PREFIXES = Arrays.asList("version", "ver", "v");

	private VersionUtil() {}

	public static String stripPrefix(String version) {
		version = version.trim();

		for (String prefix : PREFIXES) {
			if (version.startsWith(prefix)) {
				return version.substring(prefix.length());
			}
		}

		return version;
	}

	public static String getPrefixedVersion(String version) {
		return "v" + stripPrefix(version);
	}

	public static String removeBuildMetadata(String version) {
		return version.split("\\+")[0];
	}

	/**
	 * @return a Modrinth API-compatible Minecraft version string.
	 */
	public static String getModrinthCompatibleMcVersion() {
		String version = FabricLoader.getInstance().getModContainer("minecraft").get()
			.getMetadata().getVersion().getFriendlyString();

		if (version.startsWith("1.0.0-alpha.")) {
			// Turn 1.0.0-alpha.2.3 into a1.2.3
			return "a1." + version.substring(12);
		} else if (version.startsWith("1.0.0-beta.")) {
			// Turn 1.0.0-beta.7.3 into b1.7.3
			return "b1." + version.substring(11);
		} else if (version.equals("1.0.0")) {
			return "1.0"; // 1.0.0 is the only release with an extra following zero for some reason ...
		}

		return version;
	}
}
