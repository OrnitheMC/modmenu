package com.terraformersmc.modmenu.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;

public class TextureUtil {

	public static BufferedImage readImage(InputStream is) throws IOException {
		try {
			BufferedImage bufferedImage = ImageIO.read(is);
			return bufferedImage;
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
}
