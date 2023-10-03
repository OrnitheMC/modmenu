package com.terraformersmc.modmenu.mixin;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.event.ModMenuEventHandler;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.ModMenuButtonWidget;
import com.terraformersmc.modmenu.gui.widget.UpdateCheckerTexturedButtonWidget;

import net.minecraft.client.gui.GuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.resource.Identifier;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen extends Screen {
	/** button id for menu.online button */
	private static final int ONLINE = 14;
	/** button id for modmenu.title button */
	private static final int MODS = 69;
	private static final Identifier FABRIC_ICON_BUTTON_LOCATION = new Identifier(ModMenu.MOD_ID, "textures/gui/mods_button.png");

	@Inject(at = @At(value = "TAIL"), method = "init")
	private void onInit(CallbackInfo ci) {
		final List<ButtonWidget> buttons = this.buttons;
		final List<GuiEventListener> children = this.children;
		if (ModMenuConfig.MODIFY_TITLE_SCREEN.getValue()) {
			int modsButtonIndex = -1;
			final int spacing = 24;
			int buttonsY = this.height / 4 + 48;
			for (int i = 0; i < buttons.size(); i++) {
				ButtonWidget button = buttons.get(i);
				if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.CLASSIC) {
					if (button.visible) {
						ModMenuEventHandler.shiftButtons(button, modsButtonIndex == -1, spacing);
						if (modsButtonIndex == -1) {
							buttonsY = button.y;
						}
					}
				}
				if (button.id == ONLINE) {
					if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.REPLACE_REALMS) {
						int j = children.indexOf(button);
						ButtonWidget modsButton = new ModMenuButtonWidget(MODS, button.x, button.y, button.getWidth(), ((AccessorButtonWidget) button).getHeight(), ModMenuApi.createModsButtonText(), this);
						buttons.set(i, modsButton);
						children.set(j, modsButton);
					} else {
						if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.SHRINK) {
							button.setWidth(98);
						}
						modsButtonIndex = i + 1;
						if (button.visible) {
							buttonsY = button.y;
						}
					}
				}

			}
			if (modsButtonIndex != -1) {
				if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.CLASSIC) {
					this.addButton(new ModMenuButtonWidget(MODS, this.width / 2 - 100, buttonsY + spacing, 200, 20, ModMenuApi.createModsButtonText(), this));
				} else if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.SHRINK) {
					this.addButton(new ModMenuButtonWidget(MODS, this.width / 2 + 2, buttonsY, 98, 20, ModMenuApi.createModsButtonText(), this));
				} else if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.ICON) {
					this.addButton(new UpdateCheckerTexturedButtonWidget(MODS, this.width / 2 + 104, buttonsY, 20, 20, 0, 0, 20, FABRIC_ICON_BUTTON_LOCATION, 32, 64) {
						@Override
						public void m_9319498(double d, double e) {
							MixinTitleScreen.this.minecraft.openScreen(new ModsScreen(MixinTitleScreen.this));
						}
					});
				}
			}
		}
	}

	@ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;init(Lnet/minecraft/client/Minecraft;II)V"), method = "init", index = 2)
	private int adjustRealmsHeight(int height) {
		if (ModMenuConfig.MODIFY_TITLE_SCREEN.getValue() && ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.CLASSIC) {
			return height - 51;
		} else if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.REPLACE_REALMS || ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.SHRINK) {
			return -99999;
		}
		return height;
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/TitleScreen;drawString(Lnet/minecraft/client/render/TextRenderer;Ljava/lang/String;III)V", ordinal = 0))
	private String onRender(String string) {
		if (ModMenuConfig.MODIFY_TITLE_SCREEN.getValue() && ModMenuConfig.MOD_COUNT_LOCATION.getValue().isOnTitleScreen()) {
			String count = ModMenu.getDisplayedModCount();
			String specificKey = "modmenu.mods." + count;
			String replacementKey = I18n.hasTranslation(specificKey) ? specificKey : "modmenu.mods.n";
			if (ModMenuConfig.EASTER_EGGS.getValue() && I18n.hasTranslation(specificKey + ".secret")) {
				replacementKey = specificKey + ".secret";
			}
			return string.replace(I18n.translate(I18n.translate("menu.modded")), I18n.translate(replacementKey, count));
		}
		return string;
	}
}
