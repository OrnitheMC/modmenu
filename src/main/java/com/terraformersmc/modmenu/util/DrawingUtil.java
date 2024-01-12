package com.terraformersmc.modmenu.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.text.*;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Random;

@Environment(EnvType.CLIENT)
public class DrawingUtil {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	public static void drawRandomVersionBackground(Mod mod, int x, int y, int width, int height) {
		int seed = mod.getName().hashCode() + mod.getVersion().hashCode();
		Random random = new Random(seed);
		int color = 0xFF000000 | MathUtil.toRgb(MathHelper.nextFloat(random, 0f, 1f), MathHelper.nextFloat(random, 0.7f, 0.8f), 0.9f);
		if (!ModMenuConfig.RANDOM_JAVA_COLORS.getValue()) {
			color = 0xFFDD5656;
		}
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		GuiElement.fill(x, y, x + width, y + height, color);
	}

	public static void drawWrappedString(String string, int x, int y, int wrapWidth, int lines, int color) {
		while (string != null && string.endsWith("\n")) {
			string = string.substring(0, string.length() - 1);
		}
		List<String> strings = CLIENT.textRenderer.wrapLines(string, wrapWidth);
		for (int i = 0; i < strings.size(); i++) {
			if (i >= lines) {
				break;
			}
			String renderable = strings.get(i);
			if (i == lines - 1 && strings.size() > lines) {
				renderable += "...";
			}
			int x1 = x;
			if (CLIENT.textRenderer.isRightToLeft()) {
				int width = CLIENT.textRenderer.getStringWidth(renderable);
				x1 += (float) (wrapWidth - width);
			}
			CLIENT.textRenderer.drawWithoutShadow(renderable, x1, y + i * CLIENT.textRenderer.fontHeight, color);
		}
	}

	public static void drawBadge(int x, int y, int tagWidth, Text text, int outlineColor, int fillColor, int textColor) {
		GuiElement.fill(x + 1, y - 1, x + tagWidth, y, outlineColor);
		GuiElement.fill(x, y, x + 1, y + CLIENT.textRenderer.fontHeight, outlineColor);
		GuiElement.fill(x + 1, y + 1 + CLIENT.textRenderer.fontHeight - 1, x + tagWidth, y + CLIENT.textRenderer.fontHeight + 1, outlineColor);
		GuiElement.fill( x + tagWidth, y, x + tagWidth + 1, y + CLIENT.textRenderer.fontHeight, outlineColor);
		GuiElement.fill( x + 1, y, x + tagWidth, y + CLIENT.textRenderer.fontHeight, fillColor);
		String s = text.getFormattedContent();
		CLIENT.textRenderer.drawWithoutShadow(s, (int) (x + 1 + (tagWidth - CLIENT.textRenderer.getStringWidth(s)) / (float) 2), y + 1, textColor);
	}
}
