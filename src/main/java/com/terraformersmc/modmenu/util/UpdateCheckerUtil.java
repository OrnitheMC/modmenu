package com.terraformersmc.modmenu.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.api.UpdateChannel;
import com.terraformersmc.modmenu.api.UpdateChecker;
import com.terraformersmc.modmenu.api.UpdateInfo;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModrinthUpdateInfo;
import net.fabricmc.loader.api.FabricLoader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UpdateCheckerUtil {
	public static final Logger LOGGER = LogManager.getLogger("Mod Menu/Update Checker");

	private static boolean modrinthApiV2Deprecated = false;

	private static boolean allowsUpdateChecks(Mod mod) {
		return mod.allowsUpdateChecks();
	}

	public static void checkForUpdates() {
		if (!ModMenuConfig.UPDATE_CHECKER.getValue()) {
			return;
		}

		LOGGER.info("Checking mod updates...");
		CompletableFuture.runAsync(UpdateCheckerUtil::checkForUpdates0);
	}

	private static void checkForUpdates0() {
		ExecutorService executor = Executors.newCachedThreadPool(new UpdateCheckerThreadFactory());
		List<Mod> withoutUpdateChecker = new ArrayList<>();

		ModMenu.MODS.values().stream().filter(UpdateCheckerUtil::allowsUpdateChecks).forEach(mod -> {
			UpdateChecker updateChecker = mod.getUpdateChecker();

			if (updateChecker == null) {
				withoutUpdateChecker.add(mod); // Fall back to update checking via Modrinth
			} else {
				executor.submit(() -> {
					// We don't know which mod the thread is for yet in the thread factory
					Thread.currentThread().setName("ModMenu/Update Checker/" + mod.getName());

					UpdateInfo update = updateChecker.checkForUpdates();

					if (update == null) {
						return;
					}

					mod.setUpdateInfo(update);
					LOGGER.info("Update available for '{}@{}'", mod.getId(), mod.getVersion());
				});
			}
		});

		if (modrinthApiV2Deprecated) {
			return;
		}

		Map<String, Set<Mod>> modHashes = getModHashes(withoutUpdateChecker);

		Future<Map<String, Instant>> currentVersionsFuture = executor.submit(() -> getCurrentVersions(modHashes.keySet()));
		Future<Map<String, VersionUpdate>> updatedVersionsFuture = executor.submit(() -> getUpdatedVersions(modHashes.keySet()));

		Map<String, Instant> currentVersions = null;
		Map<String, VersionUpdate> updatedVersions = null;

		try {
			currentVersions = currentVersionsFuture.get();
			updatedVersions = updatedVersionsFuture.get();
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		if (currentVersions == null || updatedVersions == null) {
			return;
		}

		for (String hash : modHashes.keySet()) {
			Instant date = currentVersions.get(hash);
			VersionUpdate data = updatedVersions.get(hash);

			if (date == null || data == null) {
				continue;
			}

			// Current version is still the newest
			if (Objects.equals(hash, data.hash)) {
				continue;
			}

			// Current version is newer than what's
			// Available on our preferred update channel
			if (date.compareTo(data.releaseDate) >= 0) {
				continue;
			}

			for (Mod mod : modHashes.get(hash)) {
				mod.setUpdateInfo(data.asUpdateInfo());
				LOGGER.info("Update available for '{}@{}', (-> {})", mod.getId(), mod.getVersion(), data.versionNumber);
			}
		}
	}

	private static Map<String, Set<Mod>> getModHashes(Collection<Mod> mods) {
		Map<String, Set<Mod>> results = new HashMap<>();

		for (Mod mod : mods) {
			String modId = mod.getId();

			try {
				String hash = mod.getSha512Hash();

				if (hash != null) {
					LOGGER.debug("Hash for {} is {}", modId, hash);
					results.putIfAbsent(hash, new HashSet<>());
					results.get(hash).add(mod);
				}
			} catch (IOException e) {
				LOGGER.error("Error getting mod hash for mod {}: ", modId, e);
			}
		}

		return results;
	}

	/**
	 * @return a map of file hash to its release date on Modrinth.
	 */
	private static @Nullable Map<String, Instant> getCurrentVersions(Collection<String> modHashes) {
		String body = ModMenu.GSON_MINIFIED.toJson(new CurrentVersionsFromHashes(modHashes));

		try {
			RequestBuilder request = RequestBuilder.post()
				.setEntity(new StringEntity(body))
				.addHeader("Content-Type", "application/json")
				.setUri(URI.create("https://api.modrinth.com/v2/version_files"));

			HttpResponse response = HttpUtil.request(request);
			int status = response.getStatusLine().getStatusCode();

			if (status == 410) {
				modrinthApiV2Deprecated = true;
				LOGGER.warn("Modrinth API v2 is deprecated, unable to check for mod updates.");
			} else if (status == 200) {
				Map<String, Instant> results = new HashMap<>();
				JsonObject data = new JsonParser().parse(EntityUtils.toString(response.getEntity())).getAsJsonObject();

				data.entrySet().forEach((Map.Entry<String, JsonElement> entry) -> {
					Instant date;
					JsonObject version = entry.getValue().getAsJsonObject();

					try {
						date = Instant.parse(version.get("date_published").getAsString());
					} catch (DateTimeParseException e) {
						return;
					}

					results.put(entry.getKey(), date);
				});

				return results;
			}
		} catch (IOException e) {
			LOGGER.error("Error checking for versions: ", e);
		}

		return null;
	}

	public static class CurrentVersionsFromHashes {
		public Collection<String> hashes;
		public String algorithm = "sha512";

		public CurrentVersionsFromHashes(Collection<String> hashes) {
			this.hashes = hashes;
		}
	}

	private static UpdateChannel getUpdateChannel(String versionType) {
		try {
			return UpdateChannel.valueOf(versionType.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException | NullPointerException e) {
			return UpdateChannel.RELEASE;
		}
	}

	private static @Nullable Map<String, VersionUpdate> getUpdatedVersions(Collection<String> modHashes) {
		String mcVer = FabricLoader.getInstance().getModContainer("minecraft").get()
		.getMetadata().getVersion().getFriendlyString();
		List<String> loaders = ModMenu.runningQuilt ? Arrays.asList("fabric", "quilt") : Arrays.asList("fabric");

		List<UpdateChannel> updateChannels;
		UpdateChannel preferredChannel = UpdateChannel.getUserPreference();

		if (preferredChannel == UpdateChannel.RELEASE) {
			updateChannels = Arrays.asList(UpdateChannel.RELEASE);
		} else if (preferredChannel == UpdateChannel.BETA) {
			updateChannels = Arrays.asList(UpdateChannel.BETA, UpdateChannel.RELEASE);
		} else {
			updateChannels = Arrays.asList(UpdateChannel.ALPHA, UpdateChannel.BETA, UpdateChannel.RELEASE);
		}

		String body = ModMenu.GSON_MINIFIED.toJson(new LatestVersionsFromHashesBody(modHashes, loaders, mcVer, updateChannels));

		LOGGER.debug("Body: " + body);

		try {
			RequestBuilder latestVersionsRequest = RequestBuilder.post()
				.setEntity(new StringEntity(body))
				.addHeader("Content-Type", "application/json")
				.setUri(URI.create("https://api.modrinth.com/v2/version_files/update"));

			HttpResponse response = HttpUtil.request(latestVersionsRequest);

			int status = response.getStatusLine().getStatusCode();
			LOGGER.debug("Status: " + status);
			if (status == 410) {
				modrinthApiV2Deprecated = true;
				LOGGER.warn("Modrinth API v2 is deprecated, unable to check for mod updates.");
			} else if (status == 200) {
				Map<String, VersionUpdate> results = new HashMap<>();
				JsonObject responseObject = new JsonParser().parse(EntityUtils.toString(response.getEntity())).getAsJsonObject();
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

					Instant date;

					try {
						date = Instant.parse(versionObj.get("date_published").getAsString());
					} catch (DateTimeParseException e) {
						return;
					}

					UpdateChannel updateChannel = UpdateCheckerUtil.getUpdateChannel(versionType);
					String versionHash = primaryFile.get().getAsJsonObject().get("hashes").getAsJsonObject().get("sha512").getAsString();

					results.put(lookupHash, new VersionUpdate(projectId, versionId, versionNumber, date, updateChannel, versionHash));
				});

				return results;
			}
		} catch (IOException e) {
			LOGGER.error("Error checking for updates: ", e);
		}

		return null;
	}

	private static class VersionUpdate {
		String projectId;
		String versionId;
		String versionNumber;
		Instant releaseDate;
		UpdateChannel updateChannel;
		String hash;

		public VersionUpdate(String projectId, String versionId, String versionNumber, Instant releaseDate, UpdateChannel updateChannel, String has) {
			this.projectId = projectId;
			this.versionId = versionId;
			this.versionNumber = versionNumber;
			this.releaseDate = releaseDate;
			this.updateChannel = updateChannel;
			this.hash = has;
		}

		private UpdateInfo asUpdateInfo() {
			return new ModrinthUpdateInfo(this.projectId, this.versionId, this.versionNumber, this.updateChannel);
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
