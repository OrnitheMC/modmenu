package com.terraformersmc.modmenu.gui.widget;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;

import net.minecraft.client.Minecraft;

public class UpdateCheckerTexturedButtonWidget extends TexturedButtonWidget {
	public UpdateCheckerTexturedButtonWidget(int id, int x, int y, int width, int height, int u, int v, int hoveredVOffset, String texture, int textureWidth, int textureHeight) {
		super(id, x, y, width, height, u, v, hoveredVOffset, texture, textureWidth, textureHeight);
	}

	@Override
	public void render(Minecraft minecraft, int mouseX, int mouseY) {
		super.render(minecraft, mouseX, mouseY);
		if (this.visible && ModMenuConfig.BUTTON_UPDATE_BADGE.getValue() && ModMenu.areModUpdatesAvailable()) {
			UpdateAvailableBadge.renderBadge(this.x + this.width - 5, this.y - 3);
		}
	}
}
