package com.terraformersmc.modmenu.util.mod.fabric;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.terraformersmc.modmenu.api.UpdateChannel;
import com.terraformersmc.modmenu.api.UpdateChecker;
import com.terraformersmc.modmenu.api.UpdateInfo;
import com.terraformersmc.modmenu.util.HttpUtil;
import com.terraformersmc.modmenu.util.JsonUtil;
import com.terraformersmc.modmenu.util.OptionalUtil;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.resource.language.I18n;

public class FabricLoaderUpdateChecker implements UpdateChecker {
	public static final Logger LOGGER = LogManager.getLogger("Mod Menu/Fabric Update Checker");
	private static final URI LOADER_VERSIONS = URI.create("https://meta.fabricmc.net/v2/versions/loader");

	@Override
	public UpdateInfo checkForUpdates() {
		UpdateInfo result = null;

		try {
			result = checkForUpdates0();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			LOGGER.error("Failed Fabric Loader update check!", e);
		}

		return result;
	}

	private static UpdateInfo checkForUpdates0() throws IOException, InterruptedException {
		UpdateChannel preferredChannel = UpdateChannel.getUserPreference();

		RequestBuilder request = RequestBuilder.get().setUri(LOADER_VERSIONS);
		HttpResponse response = HttpUtil.request(request);

		int status = response.getStatusLine().getStatusCode();

		if (status != 200) {
			LOGGER.warn("Fabric Meta responded with a non-200 status: {}!", status);
			return null;
		}

		Header[] contentType = response.getHeaders("Content-Type");

		if (contentType.length == 0 || !contentType[0].getValue().contains("application/json")) {
			LOGGER.warn("Fabric Meta responded with a non-json content type, aborting loader update check!");
			return null;
		}

		JsonElement data = new JsonParser().parse(EntityUtils.toString(response.getEntity()));

		if (!data.isJsonArray()) {
			LOGGER.warn("Received invalid data from Fabric Meta, aborting loader update check!");
			return null;
		}

		SemanticVersion match = null;
		boolean stableVersion = true;

		for (JsonElement child : data.getAsJsonArray()) {
			if (!child.isJsonObject()) {
				continue;
			}

			JsonObject object = child.getAsJsonObject();
			Optional<String> version = JsonUtil.getString(object, "version");

			if (!version.isPresent()) {
				continue;
			}

			SemanticVersion parsed;

			try {
				parsed = SemanticVersion.parse(version.get());
			} catch (VersionParsingException e) {
				continue;
			}

			// Why aren't betas just marked as beta in the version string ...
			boolean stable = OptionalUtil.isPresentAndTrue(JsonUtil.getBoolean(object, "stable"));

			if (preferredChannel == UpdateChannel.RELEASE && !stable) {
				continue;
			}

			if (match == null || isNewer(parsed, match)) {
				match = parsed;
				stableVersion = stable;
			}
		}

		Version current = getCurrentVersion();

		if (match == null || !isNewer(match, current)) {
			LOGGER.debug("Fabric Loader is up to date.");
			return null;
		}

		LOGGER.debug("Fabric Loader has a matching update available!");
		return new FabricLoaderUpdateInfo(match.getFriendlyString(), stableVersion);
	}

	private static boolean isNewer(Version self, Version other) {
		return self.compareTo(other) > 0;
	}

	private static Version getCurrentVersion() {
		return FabricLoader.getInstance().getModContainer("fabricloader").get().getMetadata().getVersion();
	}

	private static class FabricLoaderUpdateInfo implements UpdateInfo {
		private final String version;
		private final boolean isStable;

		private FabricLoaderUpdateInfo(String version, boolean isStable) {
			this.version = version;
			this.isStable = isStable;
		}

		@Override
		public boolean isUpdateAvailable() {
			return true;
		}

		@Override
		public @Nullable String getUpdateMessage() {
			return I18n.translate("modmenu.install_version", this.version);
		}

		@Override
		public String getDownloadLink() {
			return "https://fabricmc.net/use/installer";
		}

		@Override
		public UpdateChannel getUpdateChannel() {
			return this.isStable ? UpdateChannel.RELEASE : UpdateChannel.BETA;
		}
	}
}
