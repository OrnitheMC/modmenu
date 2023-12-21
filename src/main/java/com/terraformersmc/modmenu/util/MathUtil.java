package com.terraformersmc.modmenu.util;

import java.util.Random;

import org.lwjgl.Sys;

import net.minecraft.util.math.MathHelper;

public class MathUtil {

	public static float nextFloat(Random random, float min, float max) {
		if (min >= max) {
			return min;
		}
		return random.nextFloat() * (max - min) + min;
	}

	public static int toRgb(float hue, float saturation, float value) {
		float blue;
		float green;
		float red;
		int i = (int) (hue * 6.0f) % 6;
		float f1 = hue * 6.0f - (float) i;
		float f2 = value * (1.0f - saturation);
		float f3 = value * (1.0f - f1 * saturation);
		float f4 = value * (1.0f - (1.0f - f1) * saturation);
		switch (i) {
		case 0: {
			red = value;
			green = f4;
			blue = f2;
			break;
		}
		case 1: {
			red = f3;
			green = value;
			blue = f2;
			break;
		}
		case 2: {
			red = f2;
			green = value;
			blue = f4;
			break;
		}
		case 3: {
			red = f2;
			green = f3;
			blue = value;
			break;
		}
		case 4: {
			red = f4;
			green = f2;
			blue = value;
			break;
		}
		case 5: {
			red = value;
			green = f2;
			blue = f3;
			break;
		}
		default: {
			throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + hue + ", " + saturation + ", " + value);
		}
		}
		int r = MathHelper.clamp((int) (red * 255.0f), 0, 255);
		int g = MathHelper.clamp((int) (green * 255.0f), 0, 255);
		int b = MathHelper.clamp((int) (blue * 255.0f), 0, 255);
		return r << 16 | g << 8 | b;
	}

	public static long getTime() {
		return Sys.getTime() * 1000L / Sys.getTimerResolution();
	}
}
