package com.terraformersmc.modmenu.util.mod.fabric;

import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.render.texture.DynamicTexture;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;

import com.terraformersmc.modmenu.util.TextureUtil;

import org.apache.logging.log4j.LogManager;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class FabricIconHandler {
	private static final Logger LOGGER = LogManager.getLogger("Mod Menu | FabricIconHandler");

	private final Map<Path, DynamicTexture> modIconCache = new HashMap<>();

	public DynamicTexture createIcon(ModContainer iconSource, String iconPath) {
		try {
			Path path = iconSource.getPath(iconPath);
			DynamicTexture cachedIcon = getCachedModIcon(path);
			if (cachedIcon != null) {
				return cachedIcon;
			}
			cachedIcon = getCachedModIcon(path);
			if (cachedIcon != null) {
				return cachedIcon;
			}
			try (InputStream inputStream = Files.newInputStream(path)) {
				BufferedImage image = TextureUtil.readImage(inputStream);
				Validate.validState(image.getWidth() == image.getHeight(), "Must be square icon");
				DynamicTexture tex = new DynamicTexture(image);
				cacheModIcon(path, tex);
				return tex;
			}

		} catch (IllegalStateException e) {
			if (e.getMessage().equals("Must be square icon")) {
				LOGGER.error("Mod icon must be a square for icon source {}: {}", iconSource.getMetadata().getId(), iconPath, e);
			}

			return null;
		} catch (Throwable t) {
			if (!iconPath.equals("assets/" + iconSource.getMetadata().getId() + "/icon.png")) {
				LOGGER.error("Invalid mod icon for icon source {}: {}", iconSource.getMetadata().getId(), iconPath, t);
			}
			return null;
		}
	}

	DynamicTexture getCachedModIcon(Path path) {
		return modIconCache.get(path);
	}

	void cacheModIcon(Path path, DynamicTexture tex) {
		modIconCache.put(path, tex);
	}
}
