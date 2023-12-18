package com.terraformersmc.modmenu.util.mod.fabric;

import net.fabricmc.loader.api.ModContainer;

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

	private final Map<Path, BufferedImage> modIconCache = new HashMap<>();

	public BufferedImage createIcon(ModContainer iconSource, String iconPath) {
		try {
			Path path = iconSource.getPath(iconPath);
			BufferedImage cachedIcon = getCachedModIcon(path);
			if (cachedIcon != null) {
				return cachedIcon;
			}
			cachedIcon = getCachedModIcon(path);
			if (cachedIcon != null) {
				return cachedIcon;
			}
			try (InputStream inputStream = Files.newInputStream(path)) {
				BufferedImage image = TextureUtil.readImage(inputStream);
				if (image.getWidth() != image.getHeight()) {
					throw new IllegalStateException("must be a square icon");
				}
				cacheModIcon(path, image);
				return image;
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

	BufferedImage getCachedModIcon(Path path) {
		return modIconCache.get(path);
	}

	void cacheModIcon(Path path, BufferedImage tex) {
		modIconCache.put(path, tex);
	}
}
