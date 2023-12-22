package com.terraformersmc.modmenu.util;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.mixin.AccessorMinecraft;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class DrawingUtil extends GuiElement {
	private static final Minecraft CLIENT = AccessorMinecraft.getInstance();
	private static final DrawingUtil GUI = new DrawingUtil();

	public static final int fontHeight = 8;

	public static void drawRandomVersionBackground(Mod mod, int x, int y, int width, int height) {
		int seed = mod.getName().hashCode() + mod.getVersion().hashCode();
		Random random = new Random(seed);
		int color = 0xFF000000 | MathUtil.toRgb(MathUtil.nextFloat(random, 0f, 1f), MathUtil.nextFloat(random, 0.7f, 0.8f), 0.9f);
		if (!ModMenuConfig.RANDOM_JAVA_COLORS.getValue()) {
			color = 0xFFDD5656;
		}
		GL11.glColor4f(1f, 1f, 1f, 1f);
		GUI.fill(x, y, x + width, y + height, color);
	}

	public static void drawWrappedString(String string, int x, int y, int wrapWidth, int lines, int color) {
		while (string != null && string.endsWith("\n")) {
			string = string.substring(0, string.length() - 1);
		}
		List<String> strings = CLIENT.textRenderer.split(string, wrapWidth);
		for (int i = 0; i < strings.size(); i++) {
			if (i >= lines) {
				break;
			}
			String renderable = strings.get(i);
			if (i == lines - 1 && strings.size() > lines) {
				renderable += "...";
			}
			int x1 = x;
			CLIENT.textRenderer.draw(renderable, x1, y + i * DrawingUtil.fontHeight, color);
		}
	}

	public static void drawBadge(int x, int y, int tagWidth, String text, int outlineColor, int fillColor, int textColor) {
		GUI.fill(x + 1, y - 1, x + tagWidth, y, outlineColor);
		GUI.fill(x, y, x + 1, y + DrawingUtil.fontHeight, outlineColor);
		GUI.fill(x + 1, y + 1 + DrawingUtil.fontHeight - 1, x + tagWidth, y + DrawingUtil.fontHeight + 1, outlineColor);
		GUI.fill( x + tagWidth, y, x + tagWidth + 1, y + DrawingUtil.fontHeight, outlineColor);
		GUI.fill( x + 1, y, x + tagWidth, y + DrawingUtil.fontHeight, fillColor);
		CLIENT.textRenderer.draw(text, (int) (x + 1 + (tagWidth - CLIENT.textRenderer.getWidth(text)) / (float) 2), y + 1, textColor);
	}

	public static void drawTexture(int x, int y, float u, float v, int width, int height, float scaleU, float scaleV) {
		float invertedScaleU = 1.0f / scaleU;
		float invertedScaleV = 1.0f / scaleV;
		BufferBuilder bufferBuilder = BufferBuilder.INSTANCE;
		bufferBuilder.start();
		bufferBuilder.vertex(x, y + height, 0.0, u * invertedScaleU, (v + (float) height) * invertedScaleV);
		bufferBuilder.vertex(x + width, y + height, 0.0, (u + (float) width) * invertedScaleU, (v + (float) height) * invertedScaleV);
		bufferBuilder.vertex(x + width, y, 0.0, (u + (float) width) * invertedScaleU, v * invertedScaleV);
		bufferBuilder.vertex(x, y, 0.0, u * invertedScaleU, v * invertedScaleV);
		bufferBuilder.end();
	}
}
