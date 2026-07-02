package com.terraformersmc.modmenu.gui.widget;

import com.terraformersmc.modmenu.util.DrawingUtil;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.TextRenderer;

import net.ornithemc.osl.lifecycle.api.client.MinecraftInstance;

public class ScrollingButtonWidget extends ButtonWidget {

	private static final int EDGE = 3;

	private final TextRenderer textRenderer;

	public ScrollingButtonWidget(int id, int x, int y, int width, int height, String message) {
		super(id, x, y, width, height, message);

		this.textRenderer = MinecraftInstance.get().textRenderer;
	}

	public void render(int mouseX, int mouseY, float tickDelta) {
		String label = this.message;
		this.message = "";

		super.render(mouseX, mouseY, tickDelta);

		this.message = label;

		if (this.visible) {
			int buttonWidth = this.width;
			int availableWidth = buttonWidth - 2 * EDGE;
			int messageWidth = this.textRenderer.getWidth(this.message);

			if (messageWidth > availableWidth) {
				int x = this.x + buttonWidth - (buttonWidth + availableWidth) / 2;
				int y = this.y + (this.height - 8) / 2;

				double time = System.nanoTime() / 1000000000.0D;
				double progress = 0.5D + Math.sin(time) / 2;
				int maxScroll = messageWidth - availableWidth;
				int scroll = (int) (maxScroll * progress);

				DrawingUtil.pushScissorArea(x, y, x + availableWidth, y + this.textRenderer.fontHeight);
				this.textRenderer.drawWithShadow(this.message, x - scroll, y, 0xFFFFFFFF);
				DrawingUtil.popScissorArea();
			} else {
				int x = this.x + buttonWidth - (buttonWidth + messageWidth) / 2;
				int y = this.y + (this.height - 8) / 2;

				this.textRenderer.drawWithShadow(this.message, x, y, 0xFFFFFFFF);
			}
		}
	}
}
