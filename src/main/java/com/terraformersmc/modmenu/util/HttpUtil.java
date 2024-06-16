package com.terraformersmc.modmenu.util;

import java.io.IOException;
import java.util.Optional;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.terraformersmc.modmenu.ModMenu;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class HttpUtil {
	private static final String USER_AGENT = buildUserAgent();
	private static final CloseableHttpClient HTTP_CLIENT = HttpClientBuilder.create().build();

	private HttpUtil() {}

	public static HttpResponse request(RequestBuilder builder) throws IOException {
		builder.setHeader("User-Agent", USER_AGENT);
		return HTTP_CLIENT.execute(builder.build());
    }

	private static String buildUserAgent() {
		String env = ModMenu.devEnvironment ? "/development" : "";
		String loader = ModMenu.runningQuilt ? "quilt" : "fabric";

		String modMenuVersion = getModMenuVersion(ModMenu.MOD_ID);
		String minecraftVersion = getModMenuVersion("minecraft");

		// -> TerraformersMC/ModMenu/9.1.0 (1.20.3/quilt/development)
		return String.format("%s/%s (%s/%s%s)", ModMenu.GITHUB_REF, modMenuVersion, minecraftVersion, loader, env);
	}

	private static String getModMenuVersion(String modId) {
		Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(modId);

		if (!container.isPresent()) {
			throw new RuntimeException("Unable to find Modmenu's own mod container!");
		}

		return VersionUtil.removeBuildMetadata(container.get().getMetadata().getVersion().getFriendlyString());
	}
}
