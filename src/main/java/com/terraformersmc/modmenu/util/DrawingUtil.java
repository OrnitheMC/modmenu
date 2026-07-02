package com.terraformersmc.modmenu.util;

import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.platform.Window;
import net.minecraft.util.math.MathHelper;
import net.ornithemc.osl.text.api.TextComponent;

import java.util.List;
import java.util.Random;
import java.util.Stack;

import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class DrawingUtil {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final ScissorStack SCISSOR_STACK = new ScissorStack();

	public static void drawRandomVersionBackground(Mod mod, int x, int y, int width, int height) {
		int seed = mod.getName().hashCode() + mod.getVersion().hashCode();
		Random random = new Random(seed);
		int color = 0xFF000000 | MathHelper.toRgb(MathHelper.nextFloat(random, 0f, 1f), MathHelper.nextFloat(random, 0.7f, 0.8f), 0.9f);
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
			if (CLIENT.textRenderer.isBidirectional()) {
				int width = CLIENT.textRenderer.getWidth(renderable);
				x1 += (float) (wrapWidth - width);
			}
			CLIENT.textRenderer.draw(renderable, x1, y + i * CLIENT.textRenderer.fontHeight, color);
		}
	}

	public static void drawBadge(int x, int y, int tagWidth, TextComponent text, int outlineColor, int fillColor, int textColor) {
		GuiElement.fill(x + 1, y - 1, x + tagWidth, y, outlineColor);
		GuiElement.fill(x, y, x + 1, y + CLIENT.textRenderer.fontHeight, outlineColor);
		GuiElement.fill(x + 1, y + 1 + CLIENT.textRenderer.fontHeight - 1, x + tagWidth, y + CLIENT.textRenderer.fontHeight + 1, outlineColor);
		GuiElement.fill( x + tagWidth, y, x + tagWidth + 1, y + CLIENT.textRenderer.fontHeight, outlineColor);
		GuiElement.fill( x + 1, y, x + tagWidth, y + CLIENT.textRenderer.fontHeight, fillColor);
		String s = text.buildFormattedString();
		CLIENT.textRenderer.draw(s, (int) (x + 1 + (tagWidth - CLIENT.textRenderer.getWidth(s)) / (float) 2), y + 1, textColor);
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
			Window window = CLIENT.window;

			int windowHeight = window.getHeight();
			double windowScale = window.getGuiScale();

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
