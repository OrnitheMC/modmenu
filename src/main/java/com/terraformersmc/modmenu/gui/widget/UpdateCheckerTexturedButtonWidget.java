package com.terraformersmc.modmenu.gui.widget;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;

import net.minecraft.resource.Identifier;

public class UpdateCheckerTexturedButtonWidget extends TexturedButtonWidget {
	public UpdateCheckerTexturedButtonWidget(int id, int x, int y, int width, int height, int u, int v, int hoveredVOffset, Identifier texture, int textureWidth, int textureHeight) {
		super(id, x, y, width, height, u, v, hoveredVOffset, texture, textureWidth, textureHeight);
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		super.render(mouseX, mouseY, delta);
		if (this.visible && ModMenuConfig.BUTTON_UPDATE_BADGE.getValue() && ModMenu.areModUpdatesAvailable()) {
			UpdateAvailableBadge.renderBadge(this.x + this.width - 5, this.y - 3);
		}
	}
}
