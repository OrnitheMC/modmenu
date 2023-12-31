package com.terraformersmc.modmenu.mixin;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.ModMenuButtonWidget;
import com.terraformersmc.modmenu.gui.widget.UpdateCheckerTexturedButtonWidget;

import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.resource.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class MixinGameMenu extends Screen {
	/** button id for gui.advancements button */
	private static final int ADVANCEMENTS = 5;
	/** button id for gui.stats button */
	private static final int STATS = 6;
	/** button id for menu.shareToLan button */
	private static final int SHARE_TO_LAN = 7;
	/** button id for modmenu.title button */
	private static final int MODS = 69;
	private static final Identifier FABRIC_ICON_BUTTON_LOCATION = new Identifier(ModMenu.MOD_ID, "textures/gui/mods_button.png");
	@Inject(method = "init", at = @At(value = "TAIL"))
	private void onInit(CallbackInfo ci) {
		if (ModMenuConfig.MODIFY_GAME_MENU.getValue()) {
			final ModMenuConfig.GameMenuButtonStyle style = ModMenuConfig.GAME_MENU_BUTTON_STYLE.getValue();
			final int spacing = 4;
			int modsButtonIndex = -1;
			int modsButtonX = -1;
			int modsButtonY = -1;
			int modsButtonWidth = -1;
			int modsButtonHeight = 20;
			for (int i = 0; i < this.buttons.size(); i++) {
				final ButtonWidget button = this.buttons.get(i);
				if (style == ModMenuConfig.GameMenuButtonStyle.BELOW_ADVANCEMENTS && button.id == ADVANCEMENTS) {
					modsButtonX = button.x;
					modsButtonWidth = button.getWidth();
				}
				if (style == ModMenuConfig.GameMenuButtonStyle.BELOW_STATISTICS && button.id == STATS) {
					modsButtonX = button.x;
					modsButtonWidth = button.getWidth();
				}
				if (style == ModMenuConfig.GameMenuButtonStyle.BELOW_ADVANCEMENTS_AND_STATISTICS && button.id == ADVANCEMENTS) {
					modsButtonX = button.x;
					modsButtonWidth = 2 * button.getWidth() + spacing;
				}
				if (style == ModMenuConfig.GameMenuButtonStyle.ICON && button.id == STATS) {
					modsButtonX = button.x + button.getWidth() + spacing;
					modsButtonWidth = modsButtonHeight;
				}
				if (button.id == SHARE_TO_LAN) {
					modsButtonIndex = i + 1;
					if (style == ModMenuConfig.GameMenuButtonStyle.ICON) {
						modsButtonY = button.y;
					} else {
						modsButtonY = button.y - spacing - modsButtonHeight;
					}
				}
			}
			if (modsButtonIndex != -1) {
				if (style == ModMenuConfig.GameMenuButtonStyle.ICON) {
					this.addButton(new UpdateCheckerTexturedButtonWidget(MODS, modsButtonX, modsButtonY, modsButtonWidth, modsButtonHeight, 0, 0, 20, FABRIC_ICON_BUTTON_LOCATION, 32, 64) {
						@Override
						public void m_9319498(double d, double e) {
							MixinGameMenu.this.minecraft.openScreen(new ModsScreen(MixinGameMenu.this));
						}
					});
				} else {
					this.addButton(new ModMenuButtonWidget(MODS, modsButtonX, modsButtonY, modsButtonWidth, modsButtonHeight, ModMenuApi.createModsButtonText(), this));
				}
			}
		}
	}
}
