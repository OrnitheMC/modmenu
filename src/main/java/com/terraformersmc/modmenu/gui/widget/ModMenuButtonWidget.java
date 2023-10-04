package com.terraformersmc.modmenu.gui.widget;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ModMenuButtonWidget extends ButtonWidget {
	public ModMenuButtonWidget(int id, int x, int y, int width, int height, Text text) {
		super(id, x, y, width, height, text.getFormattedContent());
	}

	@Override
	public void render(Minecraft minecraft, int mouseX, int mouseY, float delta) {
		super.render(minecraft, mouseX, mouseY, delta);
		if (ModMenuConfig.BUTTON_UPDATE_BADGE.getValue() && ModMenu.areModUpdatesAvailable()) {
			UpdateAvailableBadge.renderBadge(this.width + this.x - 16, this.height / 2 + this.y - 4);
		}
	}
}
