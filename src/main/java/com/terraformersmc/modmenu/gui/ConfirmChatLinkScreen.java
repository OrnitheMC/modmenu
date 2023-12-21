package com.terraformersmc.modmenu.gui;

import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.locale.LanguageManager;

public abstract class ConfirmChatLinkScreen extends ConfirmScreen {

	private String warning;
	private String copy;

	public ConfirmChatLinkScreen(Screen parent, String string, int id) {
		super(parent, LanguageManager.getInstance().translate("chat.link.confirm"), LanguageManager.getInstance().translate("gui.yes"), LanguageManager.getInstance().translate("gui.no"), string, id);
		LanguageManager languageManager = LanguageManager.getInstance();
		this.copy = languageManager.translate("chat.copy");
		this.warning = languageManager.translate("chat.link.warning");
	}

	public void init() {
		super.init();
		this.buttons.add(new ButtonWidget(2, this.width / 3 - 83 + 105, this.height / 6 + 96, 100, 20, this.copy));
	}

	protected void buttonClicked(ButtonWidget button) {
		if (button.id == 2) {
			this.copy();
			super.buttonClicked((ButtonWidget) this.buttons.get(1));
		} else {
			super.buttonClicked(button);
		}
	}

	public abstract void copy();

	public void render(int mouseX, int mouseY, float tickDelta) {
		super.render(mouseX, mouseY, tickDelta);
		this.drawCenteredString(this.textRenderer, this.warning, this.width / 2, 110, 0xFFCCCC);
	}
}
