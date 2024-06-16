package com.terraformersmc.modmenu.gui.widget.entries;

import java.awt.image.BufferedImage;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.gui.widget.UpdateAvailableBadge;
import com.terraformersmc.modmenu.mixin.AccessorMinecraft;
import com.terraformersmc.modmenu.util.DrawingUtil;
import com.terraformersmc.modmenu.util.MathUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadgeRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.resource.language.I18n;

public class ModListEntry extends GuiElement implements EntryListWidget.Entry {
	public static final String UNKNOWN_ICON = "/gui/unknown_pack.png";
	private static final String MOD_CONFIGURATION_ICON = "/assets/modmenu/textures/gui/mod_configuration.png";
//	private static final String ERROR_ICON = new Identifier("minecraft", "textures/gui/world_selection.png");
	private static final String ERROR_ICON = "/gui/trap.png";

	protected final Minecraft client;
	public final Mod mod;
	protected final ModListWidget list;
	protected Integer iconGlId;
	protected static final int FULL_ICON_SIZE = 32;
	protected static final int COMPACT_ICON_SIZE = 19;
	protected long sinceLastClick;

	public ModListEntry(Mod mod, ModListWidget list) {
		this.mod = mod;
		this.list = list;
		this.client = AccessorMinecraft.getInstance();
	}

	@Override
	public void render(int index, int x, int y, int rowWidth, int rowHeight, BufferBuilder bufferBuilder, int mouseX, int mouseY, boolean hovered) {
		x += getXOffset();
		rowWidth -= getXOffset();
		int iconSize = ModMenuConfig.COMPACT_LIST.getValue() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
		String modId = mod.getId();
		if ("java".equals(modId)) {
			DrawingUtil.drawRandomVersionBackground(mod, x, y, iconSize, iconSize);
		}
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_BLEND);
		this.bindIconTexture();
		DrawingUtil.drawTexture(x, y, 0.0F, 0.0F, iconSize, iconSize, iconSize, iconSize);
		GL11.glDisable(GL11.GL_BLEND);
		String name = mod.getTranslatedName();
		String trimmedName = name;
		int maxNameWidth = rowWidth - iconSize - 3;
		TextRenderer font = this.client.textRenderer;
		if (font.getWidth(name) > maxNameWidth) {
			String ellipsis = "...";
			trimmedName = font.trim(name, maxNameWidth - font.getWidth(ellipsis)) + ellipsis;
		}
		font.draw(trimmedName, x + iconSize + 3, y + 1, 0xFFFFFF);
		int updateBadgeXOffset = 0;
		if (ModMenuConfig.UPDATE_CHECKER.getValue() && !ModMenuConfig.DISABLE_UPDATE_CHECKER.getValue().contains(modId) && (mod.hasUpdate() || mod.getChildHasUpdate())) {
			UpdateAvailableBadge.renderBadge(x + iconSize + 3 + font.getWidth(name) + 2, y);
			updateBadgeXOffset = 11;
		}
		if (!ModMenuConfig.HIDE_BADGES.getValue()) {
			new ModBadgeRenderer(x + iconSize + 3 + font.getWidth(name) + 2 + updateBadgeXOffset, y, x + rowWidth, mod, list.getParent()).draw(mouseX, mouseY);
		}
		if (!ModMenuConfig.COMPACT_LIST.getValue()) {
			String summary = mod.getSummary();
			DrawingUtil.drawWrappedString(summary, (x + iconSize + 3 + 4), (y + DrawingUtil.fontHeight + 2), rowWidth - iconSize - 7, 2, 0x808080);
		} else {
			DrawingUtil.drawWrappedString(mod.getPrefixedVersion(), (x + iconSize + 3), (y + DrawingUtil.fontHeight + 2), rowWidth - iconSize - 7, 2, 0x808080);
		}

		if (!(this instanceof ParentEntry) && ModMenuConfig.QUICK_CONFIGURE.getValue() && (this.list.getParent().getModHasConfigScreen().get(modId) || this.list.getParent().modScreenErrors.containsKey(modId))) {
			final int textureSize = ModMenuConfig.COMPACT_LIST.getValue() ? (int) (256 / (FULL_ICON_SIZE / (double) COMPACT_ICON_SIZE)) : 256;
			if (/*this.client.options.touchscreen ||*/ hovered) {
				this.fill(x, y, x + iconSize, y + iconSize, -1601138544);
				boolean hoveringIcon = mouseX - x < iconSize;
				int v = hoveringIcon ? iconSize : 0;
				if (this.list.getParent().modScreenErrors.containsKey(modId)) {
					this.client.textureManager.bind(this.client.textureManager.load(ERROR_ICON));
					DrawingUtil.drawTexture(x, y, 96.0F, (float) v, iconSize, iconSize, textureSize, textureSize);
					if (hoveringIcon) {
						Throwable e = this.list.getParent().modScreenErrors.get(modId);
						this.list.getParent().setTooltip(this.client.textRenderer.split(I18n.translate("modmenu.configure.error", modId, modId) + "\n\n" + /*Formatting.RED +*/ e.toString(), 175));
					}
				} else {
					this.client.textureManager.bind(this.client.textureManager.load(MOD_CONFIGURATION_ICON));
					DrawingUtil.drawTexture(x, y, 0.0F, (float) v, iconSize, iconSize, textureSize, textureSize);
				}
			}
		}
	}

	@Override
	public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int entryMouseX, int entryMouseY) {
		list.select(this);
		if (ModMenuConfig.QUICK_CONFIGURE.getValue() && this.list.getParent().getModHasConfigScreen().get(this.mod.getId())) {
			int iconSize = ModMenuConfig.COMPACT_LIST.getValue() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
			if (mouseX - list.getRowLeft() <= iconSize) {
				this.openConfig();
			} else if (MathUtil.getTime() - this.sinceLastClick < 250) {
				this.openConfig();
			}
		}
		this.sinceLastClick = MathUtil.getTime();
		return true;
	}

	@Override
	public void mouseReleased(int index, int mouseX, int mouseY, int button, int entryMouseX, int entryMouseY) {
	}

	public boolean keyPressed(char chr, int key) {
		return false;
	}

	public void openConfig() {
		client.openScreen(ModMenu.getConfigScreen(mod.getId(), list.getParent()));
	}

	public Mod getMod() {
		return mod;
	}

	public void bindIconTexture() {
		if (this.iconGlId == null) {
			BufferedImage icon = mod.getIcon(list.getFabricIconHandler(), 64 * this.client.options.guiScale);
			if (icon != null) {
				this.iconGlId = this.client.textureManager.bind(icon);
			} else {
				this.iconGlId = this.client.textureManager.load(UNKNOWN_ICON);
			}
		}
		this.client.textureManager.bind(this.iconGlId);
	}

	public int getXOffset() {
		return 0;
	}
}
