package com.terraformersmc.modmenu.gui.widget;


import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.resource.Identifier;

public class TexturedButtonWidget extends ButtonWidget {
	protected final Identifier texture;
	protected final int u;
	protected final int v;
	protected final int vOff;
	protected final int textureWidth;
	protected final int textureHeight;

	public TexturedButtonWidget(int id, int x, int y, int width, int height, int u, int v, int hoveredVOffset, Identifier texture, int textureWidth, int textureHeight) {
		super(id, x, y, width, height, "");
		this.texture = texture;
		this.u = u;
		this.v = v;
		this.vOff = hoveredVOffset;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		if (this.visible) {
			Minecraft.getInstance().getTextureManager().bind(this.texture);
			GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
			boolean hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			int u = this.u;
			int v = this.v;
			if (hovered) {
				v += this.vOff;
			}
			GuiElement.drawTexture(this.x, this.y, u, v, this.width, this.height, this.textureWidth, this.textureHeight);
		}
	}
}
