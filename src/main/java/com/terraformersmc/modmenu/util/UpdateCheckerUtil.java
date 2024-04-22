package com.terraformersmc.modmenu.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.api.UpdateChannel;
import com.terraformersmc.modmenu.api.UpdateChecker;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModrinthUpdateInfo;
import net.fabricmc.loader.api.FabricLoader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class UpdateCheckerUtil {
	public static final Logger LOGGER = LogManager.getLogger("Mod Menu/Update Checker");

	private static final HttpClient client = HttpClientBuilder.create().build();
	private static boolean modrinthApiV2Deprecated = false;

	private static boolean allowsUpdateChecks(Mod mod) {
		return mod.allowsUpdateChecks();
	}

	public static void checkForUpdates() {
		if (!ModMenuConfig.UPDATE_CHECKER.getValue()) {
			return;
		}

		LOGGER.info("Checking mod updates...");
		
		CompletableFuture.runAsync(UpdateCheckerUtil::checkForModrinthUpdates);
		checkForCustomUpdates();
	}

	public static void checkForCustomUpdates() {
		ModMenu.MODS.values().stream().filter(UpdateCheckerUtil::allowsUpdateChecks).forEach(mod -> {
			UpdateChecker updateChecker = mod.getUpdateChecker();
			if (updateChecker == null) {
				return;
			}
			UpdateCheckerThread.run(mod, () -> mod.setUpdateInfo(updateChecker.checkForUpdates()));
		});
	}

	public static void checkForModrinthUpdates() {
		if (modrinthApiV2Deprecated) {
			return;
		}

		Map<String, Set<Mod>> modHashes = new HashMap<>();
		new ArrayList<>(ModMenu.MODS.values()).stream().filter(UpdateCheckerUtil::allowsUpdateChecks).filter(mod -> mod.getUpdateChecker() == null).forEach(mod -> {
			String modId = mod.getId();

			try {
				String hash = mod.getSha512Hash();

				if (hash != null) {
					LOGGER.debug("Hash for {} is {}", modId, hash);
					modHashes.putIfAbsent(hash, new HashSet<>());
					modHashes.get(hash).add(mod);
				}
			} catch (IOException e) {
				LOGGER.error("Error getting mod hash for mod {}: ", modId, e);
			}
		});

		String environment = ModMenu.devEnvironment ? "/development" : "";
		String primaryLoader = ModMenu.runningQuilt ? "quilt" : "fabric";
		List<String> loaders = ModMenu.runningQuilt ? Arrays.asList("fabric", "quilt") : Arrays.asList("fabric");

		String mcVer = FabricLoader.getInstance().getModContainer("minecraft").get()
		.getMetadata().getVersion().getFriendlyString();
		String[] splitVersion = FabricLoader.getInstance().getModContainer(ModMenu.MOD_ID)
			.get().getMetadata().getVersion().getFriendlyString().split("\\+", 1); // Strip build metadata for privacy
		final String modMenuVersion = splitVersion.length > 1 ? splitVersion[1] : splitVersion[0];
		final String userAgent = String.format("%s/%s (%s/%s%s)", ModMenu.GITHUB_REF, modMenuVersion, mcVer, primaryLoader, environment);

		List<UpdateChannel> updateChannels;
		UpdateChannel preferredChannel = UpdateChannel.getUserPreference();

		if (preferredChannel == UpdateChannel.RELEASE) {
			updateChannels = Arrays.asList(UpdateChannel.RELEASE);
		} else if (preferredChannel == UpdateChannel.BETA) {
			updateChannels = Arrays.asList(UpdateChannel.BETA, UpdateChannel.RELEASE);
		} else {
			updateChannels = Arrays.asList(UpdateChannel.ALPHA, UpdateChannel.BETA, UpdateChannel.RELEASE);
		}

		String body = ModMenu.GSON_MINIFIED.toJson(new LatestVersionsFromHashesBody(modHashes.keySet(), loaders, mcVer, updateChannels));

		LOGGER.debug("User agent: " + userAgent);
		LOGGER.debug("Body: " + body);

		try {
			HttpUriRequest latestVersionsRequest = RequestBuilder.post()
				.setEntity(new StringEntity(body))
				.addHeader("User-Agent", userAgent)
				.addHeader("Content-Type", "application/json")
				.setUri(URI.create("https://api.modrinth.com/v2/version_files/update"))
				.build();

			HttpResponse latestVersionsResponse = client.execute(latestVersionsRequest);

			int status = latestVersionsResponse.getStatusLine().getStatusCode();
			LOGGER.debug("Status: " + status);
			if (status == 410) {
				modrinthApiV2Deprecated = true;
				LOGGER.warn("Modrinth API v2 is deprecated, unable to check for mod updates.");
			} else if (status == 200) {
				JsonObject responseObject = new JsonParser().parse(EntityUtils.toString(latestVersionsResponse.getEntity())).getAsJsonObject();
				LOGGER.debug(String.valueOf(responseObject));
				responseObject.entrySet().forEach(entry -> {
					String lookupHash = entry.getKey();
					JsonObject versionObj = entry.getValue().getAsJsonObject();
					String projectId = versionObj.get("project_id").getAsString();
					String versionType = versionObj.get("version_type").getAsString();
					String versionNumber = versionObj.get("version_number").getAsString();
					String versionId = versionObj.get("id").getAsString();
					List<JsonElement> files = new ArrayList<>();
					versionObj.get("files").getAsJsonArray().forEach(files::add);
					Optional<JsonElement> primaryFile = files.stream()
						.filter(file -> file.getAsJsonObject().get("primary").getAsBoolean()).findFirst();

					if (!primaryFile.isPresent()) {
						return;
					}

					UpdateChannel updateChannel = UpdateCheckerUtil.getUpdateChannel(versionType);
					String versionHash = primaryFile.get().getAsJsonObject().get("hashes").getAsJsonObject().get("sha512").getAsString();

					if (!Objects.equals(versionHash, lookupHash)) {
						// hashes different, there's an update.
						modHashes.get(lookupHash).forEach(mod -> {
							LOGGER.info("Update available for '{}@{}', (-> {})", mod.getId(), mod.getVersion(), versionNumber);
							mod.setUpdateInfo(new ModrinthUpdateInfo(projectId, versionId, versionNumber, updateChannel));
						});
					}
				});
			}
		} catch (IOException e) {
			LOGGER.error("Error checking for updates: ", e);
		}
	}

	private static UpdateChannel getUpdateChannel(String versionType) {
		try {
			return UpdateChannel.valueOf(versionType.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException | NullPointerException e) {
			return UpdateChannel.RELEASE;
		}
	}

	public static class LatestVersionsFromHashesBody {
		public Collection<String> hashes;
		public String algorithm = "sha512";
		public Collection<String> loaders;
		@SerializedName("game_versions")
		public Collection<String> gameVersions;
		@SerializedName("version_types")
		public Collection<String> versionTypes;

		public LatestVersionsFromHashesBody(Collection<String> hashes, Collection<String> loaders, String mcVersion, Collection<UpdateChannel> updateChannels) {
			this.hashes = hashes;
			this.loaders = loaders;
			this.gameVersions = new HashSet<>();
			this.gameVersions.add(mcVersion);
			
			this.versionTypes = new HashSet<>();

			for (UpdateChannel updateChannel : updateChannels) {
				this.versionTypes.add(updateChannel.toString().toLowerCase());
			}
		}
	}
}
