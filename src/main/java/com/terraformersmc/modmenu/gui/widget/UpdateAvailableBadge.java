package com.terraformersmc.modmenu.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.resource.Identifier;
import net.minecraft.util.Utils;

public class UpdateAvailableBadge {
	private static final Identifier UPDATE_ICON = new Identifier("realms", "textures/gui/realms/trial_icon.png");

	public static void renderBadge(int x, int y) {
		GlStateManager.color4f(1f, 1f, 1f, 1f);
		int animOffset = 0;
		if ((Utils.getTimeMillis() / 800L & 1L) == 1L) {
			animOffset = 8;
		}
		Minecraft.getInstance().getTextureManager().bind(UPDATE_ICON);
		GuiElement.drawTexture(x, y, 0f, animOffset, 8, 8, 8, 16);
	}
}
