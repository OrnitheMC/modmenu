package com.terraformersmc.modmenu.gui.widget;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ModMenuButtonWidget extends ButtonWidget {
	private final Screen screen;

	public ModMenuButtonWidget(int id, int x, int y, int width, int height, Text text, Screen screen) {
		super(id, x, y, width, height, text.getFormattedString());
		this.screen = screen;
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		super.render(mouseX, mouseY, delta);
		if (ModMenuConfig.BUTTON_UPDATE_BADGE.getValue() && ModMenu.areModUpdatesAvailable()) {
			UpdateAvailableBadge.renderBadge(this.width + this.x - 16, this.height / 2 + this.y - 4);
		}
	}

	@Override
	public void m_9319498(double d, double e) {
		Minecraft.getInstance().openScreen(new ModsScreen(this.screen));
	}
}
