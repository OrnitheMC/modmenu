package com.terraformersmc.modmenu.gui;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.config.ModMenuConfigManager;
import com.terraformersmc.modmenu.gui.widget.ConfigOptionListWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.resource.language.I18n;

public class ModMenuOptionsScreen extends Screen {

	private static final int DONE = 0;

	private Screen previous;
	private String title;
	private ConfigOptionListWidget list;
	private int mouseX;
	private int mouseY;

	public ModMenuOptionsScreen(Screen previous) {
		this.previous = previous;
		this.title = I18n.translate("modmenu.options");
	}

	@Override
	public void init() {
		this.list = new ConfigOptionListWidget(this.minecraft, this.width, this.height, 32, this.height - 32, 25, ModMenuConfig.asOptions());
		this.buttons.add(new ButtonWidget(DONE, this.width / 2 - 100, this.height - 27, 200, 20, I18n.translate("gui.done")));
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.renderBackground();
		this.list.render(mouseX, mouseY, delta);
		this.drawCenteredString(this.textRenderer, this.title, this.width / 2, 5, 0xffffff);
		super.render(mouseX, mouseY, delta);
	}

	@Override
	public void handleMouse() {
		super.handleMouse();
		if (this.list.isMouseInList(mouseX, mouseY)) {
	//		this.list.handleMouse();
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button) {
		this.list.mouseClicked(mouseX, mouseY, button);
		super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void buttonClicked(ButtonWidget button) {
		switch (button.id) {
		case DONE:
			ModMenu.checkForUpdates();
			ModMenuConfigManager.save();
			ModMenuOptionsScreen.this.minecraft.openScreen(ModMenuOptionsScreen.this.previous);
			break;
		}
	}

	@Override
	public void removed() {
		ModMenuConfigManager.save();
	}
}
