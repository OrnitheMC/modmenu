package com.terraformersmc.modmenu.gui.widget;

import org.lwjgl.opengl.GL11;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.util.DrawingUtil;

import net.minecraft.client.Minecraft;

public class UpdateAvailableBadge {
	private static final String UPDATE_ICON = "/assets/" + ModMenu.MOD_ID + "/textures/gui/update_icon.png";

	public static void renderBadge(int x, int y) {
		GL11.glColor4f(1f, 1f, 1f, 1f);
		int animOffset = 0;
		if ((Minecraft.getTime() / 800L & 1L) == 1L) {
			animOffset = 8;
		}
		Minecraft.getInstance().textureManager.bind(UPDATE_ICON);
		DrawingUtil.drawTexture(x, y, 0f, animOffset, 8, 8, 8, 16);
	}
}
