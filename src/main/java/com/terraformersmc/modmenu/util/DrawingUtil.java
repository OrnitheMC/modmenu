package com.terraformersmc.modmenu.util;

import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.mixin.AccessorMinecraft;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.render.Window;
import net.minecraft.client.render.vertex.Tesselator;
import net.ornithemc.osl.text.api.TextComponent;

import java.util.List;
import java.util.Random;
import java.util.Stack;

import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class DrawingUtil extends GuiElement {
	private static final Minecraft CLIENT = AccessorMinecraft.getInstance();
	private static final DrawingUtil GUI = new DrawingUtil();
	private static final ScissorStack SCISSOR_STACK = new ScissorStack();

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

	public static void drawBadge(int x, int y, int tagWidth, TextComponent text, int outlineColor, int fillColor, int textColor) {
		GUI.fill(x + 1, y - 1, x + tagWidth, y, outlineColor);
		GUI.fill(x, y, x + 1, y + DrawingUtil.fontHeight, outlineColor);
		GUI.fill(x + 1, y + 1 + DrawingUtil.fontHeight - 1, x + tagWidth, y + DrawingUtil.fontHeight + 1, outlineColor);
		GUI.fill( x + tagWidth, y, x + tagWidth + 1, y + DrawingUtil.fontHeight, outlineColor);
		GUI.fill( x + 1, y, x + tagWidth, y + DrawingUtil.fontHeight, fillColor);
		String s = text.buildFormattedString();
		CLIENT.textRenderer.draw(s, (int) (x + 1 + (tagWidth - CLIENT.textRenderer.getWidth(s)) / (float) 2), y + 1, textColor);
	}

	public static void drawTexture(int x, int y, float u, float v, int width, int height, float scaleU, float scaleV) {
		float invertedScaleU = 1.0f / scaleU;
		float invertedScaleV = 1.0f / scaleV;
		Tesselator tesselator = Tesselator.INSTANCE;
		tesselator.begin();
		tesselator.vertex(x, y + height, 0.0, u * invertedScaleU, (v + (float) height) * invertedScaleV);
		tesselator.vertex(x + width, y + height, 0.0, (u + (float) width) * invertedScaleU, (v + (float) height) * invertedScaleV);
		tesselator.vertex(x + width, y, 0.0, (u + (float) width) * invertedScaleU, v * invertedScaleV);
		tesselator.vertex(x, y, 0.0, u * invertedScaleU, v * invertedScaleV);
		tesselator.end();
	}

	public static void pushScissorArea(int x0, int y0, int x1, int y1) {
		applyScissorArea(SCISSOR_STACK.push(new ScissorArea(x0, y0, x1 - x0, y1 - y0)));
	}

	public static void popScissorArea() {
		applyScissorArea(SCISSOR_STACK.pop());
	}

	private static void applyScissorArea(ScissorArea area) {
		if (area == null) {
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
		} else {
			Window window = new Window(CLIENT.options, CLIENT.width, CLIENT.height);

			int windowHeight = CLIENT.height;
			double windowScale = window.scale;

			int x = (int)(area.x * windowScale);
			int y = (int)(windowHeight - (area.y + area.height) * windowScale);
			int width = (int)(area.width * windowScale);
			int height = (int)(area.height * windowScale);

			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			GL11.glScissor(x, y, Math.max(0, width), Math.max(0, height));
		}
	}

	private static class ScissorStack {

		private final Stack<ScissorArea> areas = new Stack<>();

		public ScissorArea push(ScissorArea area) {
			if (!areas.isEmpty()) {
				area = areas.peek().intersection(area);
			}

			return areas.push(area);
		}

		public ScissorArea pop() {
			if (areas.isEmpty()) {
				throw new IllegalStateException("popping empty scissor stack");
			} else{
				areas.pop();
				return areas.isEmpty() ? null : areas.peek();
			}
		}
	}

	private static class ScissorArea {

		private static final ScissorArea EMPTY = new ScissorArea(0, 0, 0, 0);

		private final int x;
		private final int y;
		private final int width;
		private final int height;

		public ScissorArea(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		public ScissorArea intersection(ScissorArea o) {
			int x0 = Math.max(x, o.x);
			int y0 = Math.max(y, o.y);
			int x1 = Math.min(x + width, o.x + o.width);
			int y1 = Math.min(y + height, o.y + o.height);

			return (x0 == x1 || y0 == y1) ? EMPTY : new ScissorArea(x0, y0, x1 - x0, y1 - y0);
		}
	}
}
