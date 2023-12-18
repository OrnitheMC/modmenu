package com.terraformersmc.modmenu.util;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class TextureUtil {

	public static BufferedImage readImage(InputStream is) throws IOException {
		try {
			BufferedImage bufferedImage = ImageIO.read(is);
			return bufferedImage;
		} finally {
			TextureUtil.closeQuietly(is);
		}
	}

	public static void closeQuietly(Closeable c) {
		try {
			if (c != null) {
				c.close();
			}
		} catch (IOException e) {
		}
	}
}
